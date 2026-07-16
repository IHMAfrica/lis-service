package moh.gov.zm.lis.print.util;

import reactor.core.publisher.Mono;

/**
 * Sends a ready-to-print ZPL document to a label printer. Implementations are the
 * transport (network / SDK / USB); the ZPL itself is produced by
 * {@link ZplLabelEncoder}.
 */
public interface LabelPrinter {
    Mono<Void> print(String zpl);

    /** Human-readable description of the print target, for logs and print responses. */
    String description();
}
