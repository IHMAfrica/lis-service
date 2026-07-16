package moh.gov.zm.lis.print.util;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.DiscoveredUsbPrinter;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.config.PrinterProperties;
import moh.gov.zm.lis.exception.PrinterException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * Prints ZPL to a USB-attached Zebra printer (the primary connection for the ZD421)
 * via the Zebra Link-OS SDK. It discovers the connected Zebra USB printers, opens a
 * {@link Connection}, writes the ZPL and closes. Selected by default
 * ({@code lis.print.transport=usb}); set {@code lis.print.transport=network} to use
 * {@link ZebraNetworkPrinter} instead.
 *
 * <p>USB discovery uses usb4java's native library, so the host must run a platform
 * for which a {@code libusb4java} native is bundled (Linux x86/x86_64/arm) and have
 * access to the USB device. The blocking SDK calls run on the bounded-elastic
 * scheduler.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "lis.print.transport", havingValue = "usb", matchIfMissing = true)
@RequiredArgsConstructor
public class ZebraUsbPrinter implements LabelPrinter {
    private final PrinterProperties props;

    @Override
    public String description() {
        String serial = props.getUsbSerialNumber();
        return "USB" + (serial != null && !serial.isBlank() ? " (serial " + serial + ")" : "");
    }

    @Override
    public Mono<Void> print(String zpl) {
        return Mono.fromCallable(() -> {
                    sendOverUsb(zpl);
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic())
                // keep our own PrinterExceptions verbatim; wrap SDK/native failures
                .onErrorMap(ex -> !(ex instanceof PrinterException),
                        ex -> new PrinterException("USB print failed — " + ex.getMessage()))
                .then();
    }

    private void sendOverUsb(String zpl) throws ConnectionException {
        DiscoveredUsbPrinter[] printers = UsbDiscoverer.getZebraUsbPrinters();
        if (printers.length == 0) {
            throw new PrinterException("No Zebra USB printer detected");
        }
        DiscoveredUsbPrinter target = select(printers);
        Connection connection = target.getConnection();
        try {
            connection.open();
            connection.write(zpl.getBytes(StandardCharsets.UTF_8));
            log.info("Sent {} bytes of ZPL to Zebra USB printer [{}]", zpl.length(), target.address);
        } finally {
            try {
                connection.close();
            } catch (ConnectionException e) {
                log.warn("Failed to close USB connection to {}: {}", target.address, e.getMessage());
            }
        }
    }

    /** The printer matching the configured serial number, or the first discovered if none is set. */
    private DiscoveredUsbPrinter select(DiscoveredUsbPrinter[] printers) {
        String serial = props.getUsbSerialNumber();
        if (serial == null || serial.isBlank()) {
            return printers[0];
        }
        return Arrays.stream(printers)
                .filter(p -> matchesSerial(p, serial))
                .findFirst()
                .orElseThrow(() -> new PrinterException("No Zebra USB printer with serial '" + serial + "' found"));
    }

    private boolean matchesSerial(DiscoveredUsbPrinter printer, String serial) {
        if (serial.equalsIgnoreCase(printer.address)) {
            return true;
        }
        Map<String, String> disco = printer.getDiscoveryDataMap();
        return disco != null && disco.values().stream().anyMatch(serial::equalsIgnoreCase);
    }
}
