package zm.gov.moh.lisservice.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.ErrorResponse;
import zm.gov.moh.lisservice.constant.ListResponse;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.lab.dto.OrderStatusDTO;
import zm.gov.moh.lisservice.lab.service.OrderStatusService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping(value = "api/v1/lis-service/order-statuses", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Order Status", description = "Lab order status API")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @Operation(summary = "Get all order statuses (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved order statuses",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public Mono<ResponseEntity<PagedResponse<OrderStatusDTO.OrderStatusResponse>>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return orderStatusService.findAll(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get an order status by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved order status",
                    content = @Content(schema = @Schema(implementation = OrderStatusDTO.OrderStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order status not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrderStatusDTO.OrderStatusResponse>> findById(@PathVariable UUID id) {
        return orderStatusService.findById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Get order statuses by order ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved order statuses for the given order ID",
                    content = @Content(schema = @Schema(implementation = ListResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/order/{orderId}")
    public Mono<ResponseEntity<ListResponse<OrderStatusDTO.OrderStatusResponse>>> findByOrderId(
            @PathVariable String orderId
    ) {
        return orderStatusService.findByOrderId(orderId).map(ResponseEntity::ok);
    }
}
