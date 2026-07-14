package moh.gov.zm.lis.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.lab.dto.LabStatisticsDTO;
import moh.gov.zm.lis.lab.service.LabStatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Tag(name = "Statistics", description = "Aggregate lab-workflow statistics")
@RestController
@RequestMapping("/api/v1/lis-service/statistics")
@RequiredArgsConstructor
public class LabStatisticsController {
    private final LabStatisticsService statisticsService;

    @Operation(summary = "Lab-workflow statistics for a facility over a time period",
            description = "Counts orders sent, orders acknowledged, and results received (total / valid / "
                    + "unsolicited) for one facility. The period defaults to the last 30 days when `from`/`to` "
                    + "are omitted; both bounds are inclusive dates.")
    @GetMapping
    public Mono<LabStatisticsDTO.StatisticsResponse> facilityStatistics(
            @Parameter(description = "Facility MFL code to scope the statistics to", required = true, example = "101010")
            @RequestParam String mflCode,
            @Parameter(description = "Start of the window (inclusive, ISO date). Defaults to 29 days before `to`.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End of the window (inclusive, ISO date). Defaults to today (UTC).")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return statisticsService.forFacility(mflCode, from, to);
    }
}
