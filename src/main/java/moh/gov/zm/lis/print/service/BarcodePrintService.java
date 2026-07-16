package moh.gov.zm.lis.print.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.exception.ResourceNotFoundException;
import moh.gov.zm.lis.lab.entity.OrderStatus;
import moh.gov.zm.lis.lab.repository.OrderStatusRepository;
import moh.gov.zm.lis.print.util.LabelPrinter;
import moh.gov.zm.lis.config.PrinterProperties;
import moh.gov.zm.lis.print.util.ZplLabelEncoder;
import moh.gov.zm.lis.print.dto.BarcodePrintDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Transcodes a lab order's id into a Code 128 barcode label and prints it to the
 * Zebra ZD421 across the configured number of strips. The order is looked up in
 * {@code lab.order_status} first, so a barcode is only produced for a real order.
 */
@Service
@RequiredArgsConstructor
public class BarcodePrintService {
    private final OrderStatusRepository orderStatusRepository;
    private final ZplLabelEncoder encoder;
    private final LabelPrinter printer;
    private final PrinterProperties props;

    /** The ZPL that would be sent for this order — for preview or client-side printing. */
    public Mono<String> zplForOrder(String orderId, Integer strips) {
        return requireOrder(orderId).map(order -> encoder.encode(order.getOrderId(), strips(strips)));
    }

    /** Encode and send the order's barcode to the Zebra printer. */
    public Mono<BarcodePrintDTO.PrintResult> print(String orderId, Integer strips) {
        int count = strips(strips);
        return requireOrder(orderId).flatMap(order -> {
            String zpl = encoder.encode(order.getOrderId(), count);
            return printer.print(zpl).thenReturn(BarcodePrintDTO.PrintResult.builder()
                    .orderId(order.getOrderId())
                    .strips(count)
                    .printer(printer.description())
                    .status("PRINTED")
                    .build());
        });
    }

    private int strips(Integer requested) {
        return requested != null && requested > 0 ? requested : props.getStrips();
    }

    private Mono<OrderStatus> requireOrder(String orderId) {
        return orderStatusRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("LabOrder", orderId)));
    }
}
