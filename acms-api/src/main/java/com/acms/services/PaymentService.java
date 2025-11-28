package com.acms.services;

import com.acms.dto.PaymentDTO;
import com.acms.exception.ResourceNotFoundException;
import com.acms.models.Agent;
import com.acms.models.Commission;
import com.acms.models.Payment;
import com.acms.repositories.AgentRepository;
import com.acms.repositories.CommissionRepository;
import com.acms.repositories.PaymentRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentRepository paymentRepository;
    private final CommissionRepository commissionRepository;
    private final AgentRepository agentRepository;
    private final ModelMapper modelMapper;
    
    @Autowired
    public PaymentService(PaymentRepository paymentRepository, CommissionRepository commissionRepository,
                         AgentRepository agentRepository, ModelMapper modelMapper) {
        this.paymentRepository = paymentRepository;
        this.commissionRepository = commissionRepository;
        this.agentRepository = agentRepository;
        this.modelMapper = modelMapper;
    }
    
    @Cacheable(value = "payments", key = "#id")
    public PaymentDTO getPaymentById(Long id) {
        logger.info("Fetching payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return convertToDto(payment);
    }
    
    public Page<PaymentDTO> getPaymentsByAgent(Long agentId, Pageable pageable) {
        logger.info("Fetching payments for agent: {}", agentId);
        return paymentRepository.findByAgentId(agentId, pageable)
                .map(this::convertToDto);
    }
    
    public Page<PaymentDTO> getPaymentsByCommission(Long commissionId, Pageable pageable) {
        logger.info("Fetching payments for commission: {}", commissionId);
        return paymentRepository.findByCommissionId(commissionId, pageable)
                .map(this::convertToDto);
    }
    
    @CacheEvict(value = "payments", allEntries = true)
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        logger.info("Creating new payment for commission: {}", paymentDTO.getCommissionId());
        
        Commission commission = commissionRepository.findById(paymentDTO.getCommissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + paymentDTO.getCommissionId()));
        
        Agent agent = agentRepository.findById(paymentDTO.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + paymentDTO.getAgentId()));
        
        Payment payment = convertToEntity(paymentDTO);
        payment.setCommission(commission);
        payment.setAgent(agent);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Created payment with id: {}", savedPayment.getId());
        
        return convertToDto(savedPayment);
    }
    
    @CacheEvict(value = "payments", key = "#id")
    public PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO) {
        logger.info("Updating payment with id: {}", id);
        
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        modelMapper.map(paymentDTO, existingPayment);
        existingPayment.setUpdatedAt(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.save(existingPayment);
        logger.info("Updated payment with id: {}", updatedPayment.getId());
        
        return convertToDto(updatedPayment);
    }
    
    @CacheEvict(value = "payments", key = "#id")
    public void deletePayment(Long id) {
        logger.info("Deleting payment with id: {}", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        
        payment.setDeleted(true);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        
        logger.info("Soft deleted payment with id: {}", id);
    }
    
    private PaymentDTO convertToDto(Payment payment) {
        return modelMapper.map(payment, PaymentDTO.class);
    }
    
    private Payment convertToEntity(PaymentDTO paymentDTO) {
        return modelMapper.map(paymentDTO, Payment.class);
    }
}
