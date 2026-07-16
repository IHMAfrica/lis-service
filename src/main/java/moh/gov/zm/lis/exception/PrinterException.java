package moh.gov.zm.lis.exception;

/**
 * A label/barcode print operation failed — e.g. the Zebra printer is unreachable
 * or rejected the job. Mapped to HTTP 502 (the printer is a downstream device).
 */
public class PrinterException extends BaseException {
    public PrinterException(String message) {
        super(message, "PRINTER_ERROR", 502);
    }
}
