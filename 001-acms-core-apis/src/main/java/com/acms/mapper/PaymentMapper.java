package com.acms.mapper;

import com.acms.dto.PaymentDTO;
import com.acms.dto.PaymentDTO.AgentSummaryDTO;
import com.acms.dto.PaymentDTO.CommissionSummaryDTO;
import com.acms.model.Agent;
import com.acms.model.Commission;
import com.acms.model.Payment;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commission", ignore = true) // Will be set separately
    @Mapping(target = "agent", ignore = true) // Will be set separately
    Payment toEntity(PaymentDTO dto);

    @Mapping(target = "commission", expression = "java(mapToCommissionSummary(entity.getCommission()))")
    @Mapping(target = "agent", expression = "java(mapToAgentSummary(entity.getAgent()))")
    PaymentDTO toDto(Payment entity);

    List<PaymentDTO> toDtoList(List<Payment> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commission", ignore = true) // Prevent updating commission
    @Mapping(target = "agent", ignore = true) // Prevent updating agent
    void updatePaymentFromDto(PaymentDTO dto, @MappingTarget Payment entity);

    @AfterMapping
    default void setAuditFields(PaymentDTO dto, @MappingTarget Payment entity) {
        // Set default status if not provided
        if (dto.getStatus() == null) {
            entity.setStatus(Payment.PaymentStatus.PENDING);
        }
        
        // Set default payment method if not provided
        if (dto.getPaymentMethod() == null) {
            entity.setPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER);
        }
        
        // Set payment date if not provided
        if (dto.getPaymentDate() == null) {
            entity.setPaymentDate(java.time.LocalDate.now());
        }
        
        // Set isActive based on status
        entity.setActive(dto.getStatus() != Payment.PaymentStatus.CANCELLED && 
                       dto.getStatus() != Payment.PaymentStatus.REVERSED);
    }

    @Named("mapToCommissionSummary")
    default CommissionSummaryDTO mapToCommissionSummary(Commission commission) {
        if (commission == null) {
            return null;
        }
        
        return CommissionSummaryDTO.builder()
                .id(commission.getId())
                .commissionAmount(commission.getCommissionAmount())
                .status(commission.getStatus().toString())
                .policyNumber(commission.getPolicy() != null ? commission.getPolicy().getPolicyNumber() : null)
                .build();
    }

    @Named("mapToAgentSummary")
    default AgentSummaryDTO mapToAgentSummary(Agent agent) {
        if (agent == null) {
            return null;
        }
        
        return AgentSummaryDTO.builder()
                .id(agent.getId())
                .agentCode(agent.getAgentCode())
                .fullName(String.format("%s %s", agent.getFirstName(), agent.getLastName()))
                .email(agent.getEmail())
                .build();
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "commission", ignore = true)
    @Mapping(target = "agent", ignore = true)
    void updatePaymentFromDtoWithoutReferences(PaymentDTO dto, @MappingTarget Payment entity);
}
