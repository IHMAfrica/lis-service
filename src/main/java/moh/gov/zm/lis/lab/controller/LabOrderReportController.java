package moh.gov.zm.lis.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.lab.service.LabOrderReportService;
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

@Tag(name = "Reports", description = "Downloadable lab-workflow reports")
@RestController
@RequestMapping("/api/v1/lis-service/reports")
@RequiredArgsConstructor
public class LabOrderReportController {
    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final LabOrderReportService reportService;

    @Operation(summary = "Download the lab-order turn-around-time report (.xlsx)",
            description = "One row per lab order for the facility, joined to its acknowledgement and current "
                    + "result. Tat is the order-to-result turn-around time (days + hours). Optionally bounded "
                    + "by an inclusive [from, to] date window on when the order was placed.")
    @GetMapping(value = "/lab-orders", produces = XLSX_MEDIA_TYPE)
    public Mono<ResponseEntity<byte[]>> labOrderReport(
            @Parameter(description = "Facility MFL code to scope the report to", required = true, example = "101010")
            @RequestParam String mflCode,
            @Parameter(description = "Start of the window (inclusive, ISO date). Omit for no lower bound.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End of the window (inclusive, ISO date). Omit for no upper bound.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        String filename = "lab-orders-" + mflCode + ".xlsx";
        return reportService.generate(mflCode, from, to)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE))
                        .body(bytes));
    }
}
