package moh.gov.zm.lis.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.lab.service.ShippingListService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Tag(name = "Shipping list", description = "Facility lab-order shipping list (Zebra/MoH .xlsx)")
@RestController
@RequestMapping("/api/v1/lis-service/shipping-list")
@RequiredArgsConstructor
public class ShippingListController {
    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ShippingListService shippingListService;

    @Operation(summary = "Download the shipping list (.xlsx) for a facility and lab",
            description = "One list per lab: orders the facility shipped to the given lab, filtered by when the "
                    + "order was received. The period defaults to the last day when from/to are omitted; both "
                    + "bounds are inclusive dates.")
    @GetMapping(produces = XLSX_MEDIA_TYPE)
    public Mono<ResponseEntity<byte[]>> shippingList(
            @Parameter(description = "Sending facility MFL code (the 'From')", required = true, example = "504010")
            @RequestParam String mflCode,
            @Parameter(description = "Receiving lab code (the 'Refer To')", required = true, example = "LUS01")
            @RequestParam String labCode,
            @Parameter(description = "Start of the window (inclusive, ISO date). Defaults to `to`.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End of the window (inclusive, ISO date). Defaults to today (UTC).")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        String filename = "shipping-list-" + mflCode + "-" + labCode + ".xlsx";
        return shippingListService.generate(mflCode, labCode, from, to)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE))
                        .body(bytes));
    }
}
