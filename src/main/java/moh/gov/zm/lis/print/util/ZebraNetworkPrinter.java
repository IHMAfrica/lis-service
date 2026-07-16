package moh.gov.zm.lis.print.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.config.PrinterProperties;
import moh.gov.zm.lis.exception.PrinterException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Streams ZPL to a network-attached Zebra printer over its raw-print port (9100).
 * Selected when {@code lis.print.transport=network}; the default transport is USB
 * (see {@link ZebraUsbPrinter}). The blocking socket I/O runs on the bounded-elastic
 * scheduler.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "lis.print.transport", havingValue = "network")
@RequiredArgsConstructor
public class ZebraNetworkPrinter implements LabelPrinter {
    private final PrinterProperties props;

    @Override
    public String description() {
        return "network " + props.getHost() + ":" + props.getPort();
    }

    @Override
    public Mono<Void> print(String zpl) {
        if (!props.hasPrinter()) {
            return Mono.error(new PrinterException(
                    "No Zebra printer configured — set lis.print.zebra.host (env ZEBRA_PRINTER_HOST)"));
        }
        return Mono.fromCallable(() -> {
                    send(zpl);
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(ok -> log.info("Sent {} bytes of ZPL to Zebra printer {}:{}",
                        zpl.length(), props.getHost(), props.getPort()))
                .onErrorMap(ex -> new PrinterException("Failed to print to Zebra printer at "
                        + props.getHost() + ":" + props.getPort() + " — " + ex.getMessage()))
                .then();
    }

    private void send(String zpl) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(props.getHost(), props.getPort()), props.getConnectTimeoutMs());
            OutputStream out = socket.getOutputStream();
            out.write(zpl.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }
}
