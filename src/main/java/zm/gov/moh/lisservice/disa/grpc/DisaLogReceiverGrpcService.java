package zm.gov.moh.lisservice.disa.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import zm.gov.moh.zmscpromessagesender.grpc.MessageServiceGrpc;
import zm.gov.moh.zmscpromessagesender.grpc.SendMessageRequest;
import zm.gov.moh.zmscpromessagesender.grpc.SendMessageResponse;
import zm.gov.moh.lisservice.disa.service.DisaLabOrderPublisher;
import zm.gov.moh.lisservice.disa.service.DisaLogPayload;
import zm.gov.moh.lisservice.disa.service.DisaLogResolutionService;
import zm.gov.moh.lisservice.disa.service.Oml021Builder;
import zm.gov.moh.lisservice.disa.service.ResolvedDisaLog;
import zm.gov.moh.lisservice.lab.entity.OrderStatus;
import zm.gov.moh.lisservice.lab.repository.LabTypeRepository;
import zm.gov.moh.lisservice.lab.repository.OrderStatusRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class DisaLogReceiverGrpcService extends MessageServiceGrpc.MessageServiceImplBase {

    private final Oml021Builder oml021Builder;
    private final DisaLabOrderPublisher publisher;
    private final DisaLogResolutionService resolutionService;
    private final OrderStatusRepository orderStatusRepository;
    private final LabTypeRepository labTypeRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void sendMessage(SendMessageRequest request,
                            StreamObserver<SendMessageResponse> responseObserver) {
        String correlationId = request.getCorrelationId();
        String targetSystem = request.getTargetSystem();

        try {
            Boolean typeExists = labTypeRepository.existsByNameIgnoreCase(targetSystem).block();
            if (!Boolean.TRUE.equals(typeExists)) {
                String reason = "Rejected: message_type '" + targetSystem + "' is not a known lab_type and is not allowed";
                log.warn("[{}] {}", correlationId, reason);
                responseObserver.onNext(SendMessageResponse.newBuilder()
                        .setAcknowledged(false)
                        .setStatus("REJECTED")
                        .setErrorMessage(reason)
                        .build());
                responseObserver.onCompleted();
                return;
            }

            DisaLogPayload payload = objectMapper.readValue(request.getPayload(), DisaLogPayload.class);

            if (payload.getHmisCode() == null || payload.getHmisCode().isBlank()) {
                throw new IllegalArgumentException("Payload is missing HMISCode");
            }
            if (payload.getInvestigationTestName() == null || payload.getInvestigationTestName().isBlank()) {
                throw new IllegalArgumentException("Payload is missing InvestigationTestName");
            }

            // Resolve lab code, LOINC, and MFL code from DB
            ResolvedDisaLog resolved = resolutionService.resolve(payload).block();
            if (resolved == null) {
                throw new IllegalStateException("Resolution returned null for correlationId: " + correlationId);
            }

            // Build and publish HL7 OML^O21
            String hl7 = oml021Builder.encode(oml021Builder.build(resolved));
            publisher.publish(hl7).block();

            // Kafka confirmed delivery – persist order_status record
            saveOrderStatus(resolved);

            log.info("[{}] Successfully published HL7 OML^O21 for order '{}' → lab '{}'",
                    correlationId, payload.getOrderNumber(), resolved.labCode());

            responseObserver.onNext(SendMessageResponse.newBuilder()
                    .setAcknowledged(true)
                    .setStatus("OK")
                    .build());

        } catch (Exception e) {
            log.error("[{}] Failed to process message: {}", correlationId, e.getMessage(), e);
            responseObserver.onNext(SendMessageResponse.newBuilder()
                    .setAcknowledged(false)
                    .setStatus("ERROR")
                    .setErrorMessage(e.getMessage())
                    .build());
        }

        responseObserver.onCompleted();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void saveOrderStatus(ResolvedDisaLog resolved) {
        DisaLogPayload payload = resolved.message();

        LocalDate orderDate;
        LocalTime orderTime;

        LocalDateTime collectionDt = payload.getInvestigationSampleCollectionDate();
        if (collectionDt != null) {
            orderDate = collectionDt.toLocalDate();
            orderTime = collectionDt.toLocalTime();
        } else {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            orderDate = now.toLocalDate();
            orderTime = now.toLocalTime();
        }

        OrderStatus status = OrderStatus.builder()
                .id(UUID.randomUUID())
                .orderId(payload.getOrderNumber())
                .orderDate(orderDate)
                .orderTime(orderTime)
                .mflCode(resolved.mflCode())
                .labCode(resolved.labCode())
                .build();

        orderStatusRepository.save(status).block();
        log.debug("Saved order_status for order '{}' → lab '{}'", payload.getOrderNumber(), resolved.labCode());
    }
}
