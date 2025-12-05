package com.acms.mapper;

import com.acms.dto.PolicyDTO;
import com.acms.dto.PolicyDTO.AgentSummaryDTO;
import com.acms.model.Agent;
import com.acms.model.Policy;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PolicyMapper {

    PolicyMapper INSTANCE = Mappers.getMapper(PolicyMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agent", ignore = true) // Will be set separately
    Policy toEntity(PolicyDTO dto);

    @Mapping(target = "agent", expression = "java(mapToAgentSummary(entity.getAgent()))")
    PolicyDTO toDto(Policy entity);

    List<PolicyDTO> toDtoList(List<Policy> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agent", ignore = true) // Prevent updating agent
    @Mapping(target = "policyNumber", ignore = true) // Prevent updating policy number
    void updatePolicyFromDto(PolicyDTO dto, @MappingTarget Policy entity);

    @AfterMapping
    default void setAuditFields(PolicyDTO dto, @MappingTarget Policy entity) {
        // Set default status if not provided
        if (dto.getStatus() == null) {
            entity.setStatus(Policy.PolicyStatus.ACTIVE);
        }
        
        // Set isActive based on status
        entity.setActive(dto.getStatus() == Policy.PolicyStatus.ACTIVE);
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
    @Mapping(target = "agent", ignore = true)
    void updatePolicyFromDtoWithoutAgent(PolicyDTO dto, @MappingTarget Policy entity);
}
