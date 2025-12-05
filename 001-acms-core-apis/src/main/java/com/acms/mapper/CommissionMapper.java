package com.acms.mapper;

import com.acms.dto.CommissionDTO;
import com.acms.dto.CommissionDTO.AgentSummaryDTO;
import com.acms.dto.CommissionDTO.PolicySummaryDTO;
import com.acms.model.Agent;
import com.acms.model.Commission;
import com.acms.model.Policy;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommissionMapper {

    CommissionMapper INSTANCE = Mappers.getMapper(CommissionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "policy", ignore = true) // Will be set separately
    @Mapping(target = "agent", ignore = true) // Will be set separately
    Commission toEntity(CommissionDTO dto);

    @Mapping(target = "policy", expression = "java(mapToPolicySummary(entity.getPolicy()))")
    @Mapping(target = "agent", expression = "java(mapToAgentSummary(entity.getAgent()))")
    CommissionDTO toDto(Commission entity);

    List<CommissionDTO> toDtoList(List<Commission> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "policy", ignore = true) // Prevent updating policy
    @Mapping(target = "agent", ignore = true) // Prevent updating agent
    void updateCommissionFromDto(CommissionDTO dto, @MappingTarget Commission entity);

    @AfterMapping
    default void setAuditFields(CommissionDTO dto, @MappingTarget Commission entity) {
        // Set default status if not provided
        if (dto.getStatus() == null) {
            entity.setStatus(Commission.CommissionStatus.PENDING);
        }
        
        // Set default commission type if not provided
        if (dto.getCommissionType() == null) {
            entity.setCommissionType(Commission.CommissionType.PERCENTAGE);
        }
        
        // Set calculation date if not provided
        if (dto.getCalculationDate() == null) {
            entity.setCalculationDate(java.time.LocalDate.now());
        }
        
        // Set isActive based on status
        entity.setActive(dto.getStatus() != Commission.CommissionStatus.CANCELLED && 
                       dto.getStatus() != Commission.CommissionStatus.FORFEITED);
    }

    @Named("mapToPolicySummary")
    default PolicySummaryDTO mapToPolicySummary(Policy policy) {
        if (policy == null) {
            return null;
        }
        
        return PolicySummaryDTO.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .policyType(policy.getPolicyType())
                .premium(policy.getPremium())
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
    @Mapping(target = "policy", ignore = true)
    @Mapping(target = "agent", ignore = true)
    void updateCommissionFromDtoWithoutReferences(CommissionDTO dto, @MappingTarget Commission entity);
}
