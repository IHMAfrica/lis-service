package moh.gov.zm.lis.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Zebra label-printer settings and the geometry of a barcode strip. Constructor
 * injection keeps the fields final and lets the class be built directly in tests.
 * The ZD421 is a ZPL printer, so labels are described in printer dots (203 dpi ≈
 * 8 dots/mm by default).
 */
@Getter
@Configuration
public class PrinterProperties {
    /** Printer host/IP; blank means no default printer is configured. */
    private final String host;
    /** Raw-ZPL port; Zebra printers listen on 9100. */
    private final int port;
    private final int connectTimeoutMs;

    /** Number of label strips printed per order by default. */
    private final int strips;
    private final int printWidthDots;
    private final int labelLengthDots;
    private final int barcodeModuleWidth;
    private final int barcodeHeightDots;

    /** Which transport to use: {@code usb} (default, Zebra Link-OS SDK) or {@code network}. */
    private final String transport;
    /** Optional serial number to pick a specific printer when several are on USB; blank = first found. */
    private final String usbSerialNumber;

    public PrinterProperties(
            @Value("${lis.print.zebra.host:}") String host,
            @Value("${lis.print.zebra.port:9100}") int port,
            @Value("${lis.print.zebra.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${lis.print.label.strips:3}") int strips,
            @Value("${lis.print.label.print-width-dots:406}") int printWidthDots,
            @Value("${lis.print.label.length-dots:203}") int labelLengthDots,
            @Value("${lis.print.label.barcode-module-width:2}") int barcodeModuleWidth,
            @Value("${lis.print.label.barcode-height-dots:80}") int barcodeHeightDots,
            @Value("${lis.print.transport:usb}") String transport,
            @Value("${lis.print.zebra.usb-serial:}") String usbSerialNumber) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMs = connectTimeoutMs;
        this.strips = strips;
        this.printWidthDots = printWidthDots;
        this.labelLengthDots = labelLengthDots;
        this.barcodeModuleWidth = barcodeModuleWidth;
        this.barcodeHeightDots = barcodeHeightDots;
        this.transport = transport;
        this.usbSerialNumber = usbSerialNumber;
    }

    public boolean hasPrinter() {
        return host != null && !host.isBlank();
    }
}
