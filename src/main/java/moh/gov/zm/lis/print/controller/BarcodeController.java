package moh.gov.zm.lis.print.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.print.dto.BarcodePrintDTO;
import moh.gov.zm.lis.print.service.BarcodePrintService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Lab order barcodes", description = "Barcode labels for lab orders (Zebra ZD421)")
@RestController
@RequestMapping("/api/v1/lis-service/lab-orders")
@RequiredArgsConstructor
public class BarcodeController {
    private final BarcodePrintService service;

    @Operation(summary = "Get the ZPL for a lab order's barcode label(s)",
            description = "Returns the raw ZPL II that encodes the order id as a Code 128 barcode across the "
                    + "requested number of strips. Useful for preview or for clients that print via their own driver.")
    @GetMapping(value = "/{orderId}/barcode.zpl", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<ResponseEntity<String>> zpl(
            @PathVariable String orderId,
            @Parameter(description = "Number of label strips (defaults to the configured value, normally 3)")
            @RequestParam(required = false) Integer strips) {
        return service.zplForOrder(orderId, strips)
                .map(zpl -> ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(zpl));
    }

    @Operation(summary = "Print a lab order's barcode to the Zebra ZD421",
            description = "Encodes the order id as a Code 128 barcode and sends it to the configured Zebra printer, "
                    + "one barcode per strip (default 3).")
    @PostMapping("/{orderId}/barcode/print")
    public Mono<BarcodePrintDTO.PrintResult> print(
            @PathVariable String orderId,
            @Parameter(description = "Number of label strips to print (default 3)")
            @RequestParam(required = false) Integer strips) {
        return service.print(orderId, strips);
    }
}
