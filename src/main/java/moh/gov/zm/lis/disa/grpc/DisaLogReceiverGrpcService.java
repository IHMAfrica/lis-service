package moh.gov.zm.lis.disa.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.disa.service.DisaLabOrderService;
import moh.gov.zm.lis.disa.service.DisaLogPayload;
import org.springframework.grpc.server.service.GrpcService;
import zm.gov.moh.zmscpromessagesender.grpc.MessageServiceGrpc;
import zm.gov.moh.zmscpromessagesender.grpc.SendMessageRequest;
import zm.gov.moh.zmscpromessagesender.grpc.SendMessageResponse;

import java.util.UUID;

/**
 * gRPC entry point for inbound lab orders. Only the DISA target system is handled:
 * the JSON payload is parsed to a {@link DisaLogPayload}, resolved and published
 * as HL7 via {@link DisaLabOrderService}. Processing is non-blocking — the
 * response is completed from the reactive pipeline's terminal signal.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class DisaLogReceiverGrpcService extends MessageServiceGrpc.MessageServiceImplBase {
    private static final String DISA = "disa";

    private final DisaLabOrderService disaLabOrderService;
    private final ObjectMapper objectMapper;

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        String targetSystem = request.getTargetSystem();
        UUID correlationId = parseCorrelationId(request.getCorrelationId());

        try {
            if (!DISA.equalsIgnoreCase(targetSystem.trim())) {
                String reason = "Unsupported target system: '" + targetSystem + "' (only 'disa' is handled)";
                log.warn("[{}] {}", correlationId, reason);
                respond(responseObserver, false, "REJECTED", reason);
                return;
            }

            DisaLogPayload payload = objectMapper.readValue(request.getPayload(), DisaLogPayload.class);
            validate(payload);

            disaLabOrderService.ingest(payload, correlationId).subscribe(
                    saved -> {
                        log.info("[{}] Published HL7 OML^O21 for order '{}' → lab '{}'",
                                correlationId, saved.getOrderId(), saved.getLabCode());
                        respond(responseObserver, true, "OK", null);
                    },
                    error -> {
                        log.error("[{}] Failed to process lab order: {}", correlationId, error.getMessage(), error);
                        respond(responseObserver, false, "ERROR", error.getMessage());
                    });

        } catch (Exception e) {
            log.error("[{}] Rejected malformed message: {}", correlationId, e.getMessage(), e);
            respond(responseObserver, false, "ERROR", e.getMessage());
        }
    }

    private void validate(DisaLogPayload payload) {
        if (payload.getHmisCode() == null || payload.getHmisCode().isBlank()) {
            throw new IllegalArgumentException("Payload is missing HMISCode");
        }
        if (payload.getInvestigationTestName() == null || payload.getInvestigationTestName().isBlank()) {
            throw new IllegalArgumentException("Payload is missing InvestigationTestName");
        }
        if (payload.getOrderNumber() == null || payload.getOrderNumber().isBlank()) {
            throw new IllegalArgumentException("Payload is missing OrderNumber");
        }
    }

    private UUID parseCorrelationId(String raw) {
        if (raw != null && !raw.isBlank()) {
            try {
                return UUID.fromString(raw.trim());
            } catch (IllegalArgumentException ignored) {
                log.warn("correlation_id '{}' is not a UUID; generating one", raw);
            }
        }
        return UUID.randomUUID();
    }

    private void respond(StreamObserver<SendMessageResponse> observer, boolean acknowledged, String status, String error) {
        SendMessageResponse.Builder builder = SendMessageResponse.newBuilder()
                .setAcknowledged(acknowledged)
                .setStatus(status);
        if (error != null) {
            builder.setErrorMessage(error);
        }
        observer.onNext(builder.build());
        observer.onCompleted();
    }
}
