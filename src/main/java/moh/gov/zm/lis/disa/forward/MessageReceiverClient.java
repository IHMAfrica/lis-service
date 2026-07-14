package moh.gov.zm.lis.disa.forward;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import zm.gov.moh.zmscpromessagereceiver.grpc.MessageReceiverServiceGrpc;
import zm.gov.moh.zmscpromessagereceiver.grpc.MessageRequest;
import zm.gov.moh.zmscpromessagereceiver.grpc.MessageResponse;

/**
 * Sends a single message over the bidirectional {@code ReceiveMessages} stream and
 * completes once the downstream acknowledges it. A fresh stream is used per send —
 * simple and self-contained; lab-result volume does not warrant a shared stream.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageReceiverClient {
    private final MessageReceiverServiceGrpc.MessageReceiverServiceStub stub;

    public Mono<Void> send(MessageRequest request) {
        return Mono.create(sink -> {
            StreamObserver<MessageRequest> requestStream = stub.receiveMessages(new StreamObserver<>() {
                private volatile boolean settled = false;

                @Override
                public void onNext(MessageResponse response) {
                    settled = true;
                    if (response.getSuccess()) {
                        sink.success();
                    } else {
                        sink.error(new IllegalStateException(
                                "Downstream rejected message '" + response.getMessageId() + "': " + response.getError()));
                    }
                }

                @Override
                public void onError(Throwable t) {
                    sink.error(t);
                }

                @Override
                public void onCompleted() {
                    if (!settled) {
                        // stream closed without a response for our message
                        sink.error(new IllegalStateException("Downstream closed the stream without acknowledging the message"));
                    }
                }
            });

            try {
                requestStream.onNext(request);
                requestStream.onCompleted();
            } catch (RuntimeException e) {
                sink.error(e);
            }
        });
    }
}
