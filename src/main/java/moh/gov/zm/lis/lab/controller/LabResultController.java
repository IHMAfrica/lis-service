package moh.gov.zm.lis.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.lab.dto.LabResultDTO;
import moh.gov.zm.lis.lab.service.LabResultReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Lab results", description = "Review of unsolicited lab results (accept / reject)")
@RestController
@RequestMapping("/api/v1/lis-service/lab-results")
@RequiredArgsConstructor
public class LabResultController {
    private final LabResultReviewService reviewService;

    @Operation(summary = "List unsolicited lab results awaiting review at the current user's facilities")
    @GetMapping("/pending-review")
    public Mono<PagedResponse<LabResultDTO.LabResultResponse>> pendingReview(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return reviewService.pendingReview(userId, page, size);
    }

    @Operation(summary = "Get a lab result (with observations)")
    @GetMapping("/{id}")
    public Mono<LabResultDTO.LabResultResponse> getById(@PathVariable UUID id) {
        return reviewService.findById(id);
    }

    @Operation(summary = "Accept an unsolicited result — forwards it downstream and notifies the facility")
    @PostMapping("/{id}/accept")
    public Mono<LabResultDTO.LabResultResponse> accept(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id) {
        return reviewService.accept(id, userId);
    }

    @Operation(summary = "Reject an unsolicited result — not forwarded; notifies the facility")
    @PostMapping("/{id}/reject")
    public Mono<LabResultDTO.LabResultResponse> reject(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) LabResultDTO.ReviewDecisionRequest request) {
        return reviewService.reject(id, userId, request != null ? request.getNote() : null);
    }
}
