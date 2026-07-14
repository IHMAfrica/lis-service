package moh.gov.zm.lis.notify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.common.PagedResponse;
import moh.gov.zm.lis.exception.ValidationException;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import moh.gov.zm.lis.notify.service.NotificationDispatcher;
import moh.gov.zm.lis.notify.service.NotificationService;
import moh.gov.zm.lis.notify.sse.NotificationSseHub;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Notifications", description = "User notifications with live SSE delivery")
@RestController
@RequestMapping("/api/v1/lis-service/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationSseHub sseHub;
    private final NotificationService notificationService;
    private final NotificationDispatcher dispatcher;

    @Operation(summary = "Live notification stream (Server-Sent Events) for the current user")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> stream(@RequestHeader("X-User-Id") UUID userId) {
        return sseHub.stream(userId);
    }

    @Operation(summary = "List the current user's notifications (most recent first)")
    @GetMapping
    public Mono<PagedResponse<NotificationDTO.NotificationResponse>> list(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.list(userId, unreadOnly, page, size);
    }

    @Operation(summary = "Unread notification count for the current user")
    @GetMapping("/unread-count")
    public Mono<NotificationDTO.UnreadCountResponse> unreadCount(@RequestHeader("X-User-Id") UUID userId) {
        return notificationService.unreadCount(userId);
    }

    @Operation(summary = "Mark a notification as read")
    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> markRead(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id) {
        return notificationService.markRead(userId, id);
    }

    @Operation(summary = "Mark all of the current user's notifications as read")
    @PostMapping("/read-all")
    public Mono<NotificationDTO.MarkReadResult> markAllRead(@RequestHeader("X-User-Id") UUID userId) {
        return notificationService.markAllRead(userId);
    }

    @Operation(summary = "Delete (hide) a notification for the current user")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id) {
        return notificationService.delete(userId, id);
    }

    @Operation(summary = "Create and dispatch a notification",
            description = "Targets a facility's active users (facilityId) or a single user (userId). "
                    + "Internal/test surface for the plumbing; lab-event handlers will call the dispatcher directly.")
    @PostMapping("/dispatch")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<NotificationDTO.NotificationResponse> dispatch(@Valid @RequestBody NotificationDTO.DispatchRequest request) {
        if (request.getFacilityId() != null) {
            return dispatcher.dispatchToFacility(request.getFacilityId(), request);
        }
        if (request.getUserId() != null) {
            return dispatcher.dispatchToUser(request.getUserId(), request);
        }
        return Mono.error(new ValidationException("Either facilityId or userId is required",
                Map.of("target", "facilityId or userId is required")));
    }
}
