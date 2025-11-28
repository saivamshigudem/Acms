package com.acms.repositories;

import com.acms.models.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends BaseRepository<Payment, Long> {
    
    Page<Payment> findByAgentId(Long agentId, Pageable pageable);
    
    Page<Payment> findByCommissionId(Long commissionId, Pageable pageable);
    
    @Query("SELECT SUM(p.paymentAmount) FROM Payment p WHERE p.agent.id = :agentId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaymentsByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT p FROM Payment p WHERE p.agent.id = :agentId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsByAgentAndDateRange(
            @Param("agentId") Long agentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    Page<Payment> findByStatus(@Param("status") String status, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate < :date AND p.status = 'PENDING'")
    List<Payment> findOverduePayments(@Param("date") LocalDateTime date);
}
