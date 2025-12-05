package com.acms.repository;

import com.acms.model.Agent;
import com.acms.model.Commission;
import com.acms.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends BaseRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentReference(String paymentReference);
    
    boolean existsByPaymentReference(String paymentReference);
    
    Optional<Payment> findByCommissionId(Long commissionId);
    
    @Query("SELECT p FROM Payment p WHERE p.commission.id = :commissionId")
    Page<Payment> findByCommissionId(@Param("commissionId") Long commissionId, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.agent.id = :agentId")
    Page<Payment> findByAgentId(@Param("agentId") Long agentId, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    Page<Payment> findByStatus(@Param("status") Payment.PaymentStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :method")
    Page<Payment> findByPaymentMethod(@Param("method") Payment.PaymentMethod method, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    Page<Payment> findByPaymentDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.processedDate BETWEEN :startDate AND :endDate")
    Page<Payment> findByProcessedDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.paymentDate <= :date")
    List<Payment> findPendingPaymentsForProcessing(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'PROCESSING' AND p.paymentDate <= :date")
    List<Payment> findStuckProcessingPayments(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.paymentDate <= :date")
    List<Payment> findFailedPaymentsForRetry(@Param("date") LocalDate date);
    
    @Query("SELECT SUM(p.paymentAmount) FROM Payment p WHERE p.agent.id = :agentId AND p.status = 'COMPLETED'")
    BigDecimal sumCompletedPaymentsByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT SUM(p.paymentAmount) FROM Payment p WHERE p.agent.id = :agentId AND p.status = 'PENDING'")
    BigDecimal sumPendingPaymentsByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.agent.id = :agentId AND p.status = :status")
    Long countPaymentsByAgentAndStatus(@Param("agentId") Long agentId, @Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.commission = :commission")
    Page<Payment> findByCommission(@Param("commission") Commission commission, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.agent = :agent")
    Page<Payment> findByAgent(@Param("agent") Agent agent, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE " +
           "LOWER(p.paymentReference) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.transactionId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.notes) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Payment> search(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.active = :isActive")
    List<Payment> findAllByActive(@Param("isActive") boolean isActive);
    
    @Query("SELECT p FROM Payment p WHERE p.agent.id = :agentId AND p.paymentDate BETWEEN :startDate AND :endDate")
    Page<Payment> findByAgentAndDateRange(
            @Param("agentId") Long agentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionId(@Param("transactionId") String transactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.bankName = :bankName")
    Page<Payment> findByBankName(@Param("bankName") String bankName, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate >= :date")
    List<Payment> findRecentCompletedPayments(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate < :date AND p.status IN ('PENDING', 'PROCESSING')")
    List<Payment> findOverduePayments(@Param("date") LocalDate date);
}
