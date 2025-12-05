package com.acms.controller;

import com.acms.dto.PaymentDTO;
import com.acms.dto.ApiResponse;
import com.acms.model.Payment;
import com.acms.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing commission payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Get all payments", description = "Retrieve a paginated list of all payments")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getAllPayments(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentDTO> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieve a payment by its unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment found",
                    content = @Content(schema = @Schema(implementation = PaymentDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment not found")
    })
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(
            @Parameter(description = "ID of the payment to be retrieved") @PathVariable Long id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/reference/{paymentReference}")
    @Operation(summary = "Get payment by reference", description = "Retrieve a payment by its reference number")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByReference(
            @Parameter(description = "Payment reference") @PathVariable String paymentReference) {
        PaymentDTO payment = paymentService.getPaymentByReference(paymentReference);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/commission/{commissionId}")
    @Operation(summary = "Get payments by commission", description = "Retrieve payments for a specific commission")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getPaymentsByCommissionId(
            @Parameter(description = "Commission ID") @PathVariable Long commissionId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentDTO> payments = paymentService.getPaymentsByCommissionId(commissionId, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/agent/{agentId}")
    @Operation(summary = "Get payments by agent", description = "Retrieve payments for a specific agent")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getPaymentsByAgentId(
            @Parameter(description = "Agent ID") @PathVariable Long agentId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentDTO> payments = paymentService.getPaymentsByAgentId(agentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping
    @Operation(summary = "Create a new payment", description = "Register a new payment record")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Payment created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment details to be created",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentDTO.class)))
            @Valid @RequestBody PaymentDTO paymentDTO) {
        PaymentDTO createdPayment = paymentService.createPayment(paymentDTO);
        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + createdPayment.getId()))
                .body(ApiResponse.withStatus(HttpStatus.CREATED, "Payment created successfully", createdPayment));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a payment", description = "Update an existing payment's information")
    public ResponseEntity<ApiResponse<PaymentDTO>> updatePayment(
            @Parameter(description = "ID of the payment to be updated") @PathVariable Long id,
            @Valid @RequestBody PaymentDTO paymentDTO) {
        PaymentDTO updatedPayment = paymentService.updatePayment(id, paymentDTO);
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Payment updated successfully", updatedPayment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a payment", description = "Soft delete a payment by its ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayment(
            @Parameter(description = "ID of the payment to be deleted") @PathVariable Long id) {
        paymentService.deletePayment(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update payment status", description = "Update the status of an existing payment")
    public ResponseEntity<ApiResponse<PaymentDTO>> updatePaymentStatus(
            @Parameter(description = "ID of the payment") @PathVariable Long id,
            @RequestParam Payment.PaymentStatus status) {
        PaymentDTO updatedPayment = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Payment status updated successfully", updatedPayment));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status", description = "Retrieve payments filtered by status")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable Payment.PaymentStatus status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentDTO> payments = paymentService.getPaymentsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get payments by payment date range", description = "Retrieve payments within a payment date range")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getPaymentsByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentDTO> payments = paymentService.getPaymentsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/processed-date-range")
    @Operation(summary = "Get payments by processed date range", description = "Retrieve payments within a processed date range")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getPaymentsByProcessedDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentDTO> payments = paymentService.getPaymentsByProcessedDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/agent/{agentId}/summary")
    @Operation(summary = "Get agent payment summary", description = "Get total completed and pending payments for an agent")
    public ResponseEntity<ApiResponse<AgentPaymentSummary>> getAgentPaymentSummary(
            @Parameter(description = "Agent ID") @PathVariable Long agentId) {
        
        BigDecimal totalCompleted = paymentService.getTotalCompletedPaymentsByAgent(agentId);
        BigDecimal totalPending = paymentService.getTotalPendingPaymentsByAgent(agentId);
        
        AgentPaymentSummary summary = AgentPaymentSummary.builder()
                .agentId(agentId)
                .totalCompletedPayments(totalCompleted)
                .totalPendingPayments(totalPending)
                .totalPayments(totalCompleted.add(totalPending))
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/process-pending")
    @Operation(summary = "Process pending payments", description = "Process pending payments for payment")
    public ResponseEntity<ApiResponse<Void>> processPendingPayments() {
        paymentService.processPendingPayments();
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Pending payments processed successfully", null));
    }

    @PostMapping("/process-stuck")
    @Operation(summary = "Process stuck payments", description = "Process stuck payments in processing status")
    public ResponseEntity<ApiResponse<Void>> processStuckPayments() {
        paymentService.processStuckPayments();
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Stuck payments processed successfully", null));
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "Retry failed payments", description = "Retry failed payments")
    public ResponseEntity<ApiResponse<Void>> retryFailedPayments() {
        paymentService.retryFailedPayments();
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Failed payments retried successfully", null));
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(description = "Agent payment summary")
    public static class AgentPaymentSummary {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Agent ID", example = "1")
        private Long agentId;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "Total completed payments", example = "5000.00")
        private BigDecimal totalCompletedPayments;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "Total pending payments", example = "1500.00")
        private BigDecimal totalPendingPayments;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "Total payments (completed + pending)", example = "6500.00")
        private BigDecimal totalPayments;
    }
}
