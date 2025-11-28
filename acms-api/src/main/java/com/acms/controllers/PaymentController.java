package com.acms.controllers;

import com.acms.dto.PaymentDTO;
import com.acms.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;
    
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get payment by ID", description = "Retrieves a payment by their ID")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        logger.info("GET /api/payments/{}", id);
        PaymentDTO paymentDTO = paymentService.getPaymentById(id);
        return ResponseEntity.ok(paymentDTO);
    }
    
    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get payments by agent", description = "Retrieves all payments for a specific agent")
    public ResponseEntity<Page<PaymentDTO>> getPaymentsByAgent(
            @PathVariable Long agentId,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.info("GET /api/payments/agent/{}", agentId);
        Page<PaymentDTO> payments = paymentService.getPaymentsByAgent(agentId, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/commission/{commissionId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get payments by commission", description = "Retrieves all payments for a specific commission")
    public ResponseEntity<Page<PaymentDTO>> getPaymentsByCommission(
            @PathVariable Long commissionId,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.info("GET /api/payments/commission/{}", commissionId);
        Page<PaymentDTO> payments = paymentService.getPaymentsByCommission(commissionId, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new payment", description = "Creates a new payment with the provided details")
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        logger.info("POST /api/payments - Creating new payment");
        PaymentDTO createdPayment = paymentService.createPayment(paymentDTO);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPayment.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(createdPayment);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Update a payment", description = "Updates an existing payment's details")
    public ResponseEntity<PaymentDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTO paymentDTO) {
        logger.info("PUT /api/payments/{} - Updating payment", id);
        PaymentDTO updatedPayment = paymentService.updatePayment(id, paymentDTO);
        return ResponseEntity.ok(updatedPayment);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a payment", description = "Soft deletes a payment by ID")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        logger.info("DELETE /api/payments/{} - Deleting payment", id);
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
