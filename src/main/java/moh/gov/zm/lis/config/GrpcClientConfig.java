package moh.gov.zm.lis.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zm.gov.moh.zmscpromessagereceiver.grpc.MessageReceiverServiceGrpc;

@Configuration
@RequiredArgsConstructor
public class GrpcClientConfig {
    private final ConfigProperties configProperties;

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel messageReceiverChannel() {
        return ManagedChannelBuilder
                .forAddress(configProperties.getMessageReceiverHost(), configProperties.getMessageReceiverPort())
                .usePlaintext()
                .build();
    }

    @Bean
    public MessageReceiverServiceGrpc.MessageReceiverServiceStub messageReceiverStub(ManagedChannel messageReceiverChannel) {
        return MessageReceiverServiceGrpc.newStub(messageReceiverChannel);
    }
}
