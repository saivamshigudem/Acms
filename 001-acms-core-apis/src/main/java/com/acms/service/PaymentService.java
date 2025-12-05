package com.acms.service;

import com.acms.dto.PaymentDTO;
import com.acms.exception.ResourceNotFoundException;
import com.acms.mapper.PaymentMapper;
import com.acms.model.Agent;
import com.acms.model.Commission;
import com.acms.model.Payment;
import com.acms.repository.AgentRepository;
import com.acms.repository.CommissionRepository;
import com.acms.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.acms.config.CacheConfig.PAYMENT_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AgentRepository agentRepository;
    private final CommissionRepository commissionRepository;
    private final PaymentMapper paymentMapper;

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable)
                .map(paymentMapper::toDto);
    }

    @Cacheable(value = PAYMENT_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        log.info("Fetching payment with id: {}", id);
        return paymentRepository.findById(id)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByReference(String paymentReference) {
        log.info("Fetching payment with reference: {}", paymentReference);
        return paymentRepository.findByPaymentReference(paymentReference)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + paymentReference));
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByCommissionId(Long commissionId, Pageable pageable) {
        log.info("Fetching payments for commission with id: {}", commissionId);
        return paymentRepository.findByCommissionId(commissionId, pageable)
                .map(paymentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByAgentId(Long agentId, Pageable pageable) {
        log.info("Fetching payments for agent with id: {}", agentId);
        return paymentRepository.findByAgentId(agentId, pageable)
                .map(paymentMapper::toDto);
    }

    @Transactional
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        log.info("Creating new payment for commission: {}, agent: {}", 
                paymentDTO.getCommissionId(), paymentDTO.getAgentId());

        // Validate commission and agent exist
        Commission commission = commissionRepository.findById(paymentDTO.getCommissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + paymentDTO.getCommissionId()));
        
        Agent agent = agentRepository.findById(paymentDTO.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + paymentDTO.getAgentId()));

        // Check if payment already exists for this commission
        if (paymentRepository.findByCommissionId(paymentDTO.getCommissionId()).isPresent()) {
            throw new IllegalArgumentException("Payment already exists for commission " + paymentDTO.getCommissionId());
        }

        // Validate commission is in PAID status
        if (commission.getStatus() != Commission.CommissionStatus.PAID) {
            throw new IllegalArgumentException("Commission must be in PAID status to create payment");
        }

        // Generate payment reference if not provided
        if (paymentDTO.getPaymentReference() == null || paymentDTO.getPaymentReference().isEmpty()) {
            paymentDTO.setPaymentReference(generatePaymentReference());
        } else {
            // Check if payment reference already exists
            if (paymentRepository.existsByPaymentReference(paymentDTO.getPaymentReference())) {
                throw new IllegalArgumentException("Payment reference already exists: " + paymentDTO.getPaymentReference());
            }
        }

        // Validate payment amount matches commission amount
        if (paymentDTO.getPaymentAmount().compareTo(commission.getCommissionAmount()) != 0) {
            throw new IllegalArgumentException("Payment amount must match commission amount");
        }

        Payment payment = paymentMapper.toEntity(paymentDTO);
        payment.setCommission(commission);
        payment.setAgent(agent);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Created payment with id: {}", savedPayment.getId());
        return paymentMapper.toDto(savedPayment);
    }

    @CachePut(value = PAYMENT_CACHE, key = "#id")
    @Transactional
    public PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO) {
        log.info("Updating payment with id: {}", id);
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        // Prevent updating commission and agent references
        if (paymentDTO.getCommissionId() != null && 
            !existingPayment.getCommission().getId().equals(paymentDTO.getCommissionId())) {
            throw new IllegalArgumentException("Commission reference cannot be changed");
        }

        if (paymentDTO.getAgentId() != null && 
            !existingPayment.getAgent().getId().equals(paymentDTO.getAgentId())) {
            throw new IllegalArgumentException("Agent reference cannot be changed");
        }

        // Prevent updating payment reference
        if (paymentDTO.getPaymentReference() != null && 
            !existingPayment.getPaymentReference().equals(paymentDTO.getPaymentReference())) {
            throw new IllegalArgumentException("Payment reference cannot be changed");
        }

        paymentMapper.updatePaymentFromDto(paymentDTO, existingPayment);
        
        // Validate status transitions
        validatePaymentStatusTransition(existingPayment.getStatus(), paymentDTO.getStatus());
        
        Payment updatedPayment = paymentRepository.save(existingPayment);
        log.info("Updated payment with id: {}", id);
        return paymentMapper.toDto(updatedPayment);
    }

    @CacheEvict(value = PAYMENT_CACHE, key = "#id")
    @Transactional
    public void deletePayment(Long id) {
        log.info("Deleting payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        payment.setActive(false);
        paymentRepository.save(payment);
        log.info("Soft deleted payment with id: {}", id);
    }

    @CacheEvict(value = PAYMENT_CACHE, key = "#id")
    @Transactional
    public PaymentDTO updatePaymentStatus(Long id, Payment.PaymentStatus status) {
        log.info("Updating status to {} for payment with id: {}", status, id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        validatePaymentStatusTransition(payment.getStatus(), status);
        
        // Handle status-specific logic
        if (status == Payment.PaymentStatus.COMPLETED) {
            payment.setProcessedDate(LocalDate.now());
            // Generate transaction ID if not already set
            if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
                payment.setTransactionId(generateTransactionId());
            }
        } else if (status == Payment.PaymentStatus.CANCELLED) {
            payment.setProcessedDate(LocalDate.now());
        }
        
        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Updated status to {} for payment with id: {}", status, id);
        return paymentMapper.toDto(updatedPayment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable) {
        log.info("Fetching payments with status: {}", status);
        return paymentRepository.findByStatus(status, pageable)
                .map(paymentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Fetching payments with payment date between {} and {}", startDate, endDate);
        return paymentRepository.findByPaymentDateBetween(startDate, endDate, pageable)
                .map(paymentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByProcessedDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Fetching payments with processed date between {} and {}", startDate, endDate);
        return paymentRepository.findByProcessedDateBetween(startDate, endDate, pageable)
                .map(paymentMapper::toDto);
    }

    @Transactional
    public void processPendingPayments() {
        log.info("Processing pending payments");
        List<Payment> pendingPayments = paymentRepository.findPendingPaymentsForProcessing(LocalDate.now());
        
        for (Payment payment : pendingPayments) {
            // Business logic for processing payments
            if (shouldProcessPayment(payment)) {
                payment.setStatus(Payment.PaymentStatus.PROCESSING);
                paymentRepository.save(payment);
                log.info("Started processing payment: {}", payment.getId());
            }
        }
    }

    @Transactional
    public void processStuckPayments() {
        log.info("Processing stuck payments");
        List<Payment> stuckPayments = paymentRepository.findStuckProcessingPayments(LocalDate.now().minusDays(1));
        
        for (Payment payment : stuckPayments) {
            // Business logic for handling stuck payments
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setNotes("Payment processing timeout - marked as failed");
            paymentRepository.save(payment);
            log.info("Marked stuck payment as failed: {}", payment.getId());
        }
    }

    @Transactional
    public void retryFailedPayments() {
        log.info("Retrying failed payments");
        List<Payment> failedPayments = paymentRepository.findFailedPaymentsForRetry(LocalDate.now().minusDays(7));
        
        for (Payment payment : failedPayments) {
            // Business logic for retrying failed payments
            if (shouldRetryPayment(payment)) {
                payment.setStatus(Payment.PaymentStatus.PENDING);
                payment.setNotes("Retrying failed payment");
                paymentRepository.save(payment);
                log.info("Retrying failed payment: {}", payment.getId());
            }
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCompletedPaymentsByAgent(Long agentId) {
        BigDecimal total = paymentRepository.sumCompletedPaymentsByAgent(agentId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingPaymentsByAgent(Long agentId) {
        BigDecimal total = paymentRepository.sumPendingPaymentsByAgent(agentId);
        return total != null ? total : BigDecimal.ZERO;
    }

    private void validatePaymentStatusTransition(Payment.PaymentStatus currentStatus, 
            Payment.PaymentStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != Payment.PaymentStatus.PROCESSING && 
                    newStatus != Payment.PaymentStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case PROCESSING:
                if (newStatus != Payment.PaymentStatus.COMPLETED && 
                    newStatus != Payment.PaymentStatus.FAILED && 
                    newStatus != Payment.PaymentStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case COMPLETED:
                if (newStatus != Payment.PaymentStatus.REVERSED && 
                    newStatus != Payment.PaymentStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case FAILED:
                if (newStatus != Payment.PaymentStatus.PENDING && 
                    newStatus != Payment.PaymentStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case CANCELLED:
            case REVERSED:
                throw new IllegalArgumentException("Cannot change status from " + currentStatus);
        }
    }

    private boolean shouldProcessPayment(Payment payment) {
        // Business logic for determining if a payment should be processed
        // For example: check bank details, payment amount limits, etc.
        return payment.getBankAccount() != null && 
               payment.getBankName() != null &&
               payment.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean shouldRetryPayment(Payment payment) {
        // Business logic for determining if a failed payment should be retried
        // For example: limit retry attempts, check failure reason, etc.
        return payment.getPaymentAmount().compareTo(new BigDecimal("10000.00")) < 0; // Retry small amounts
    }

    private String generatePaymentReference() {
        return "PAY-" + LocalDate.now().getYear() + "-" + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
