package com.acms.mapper;

import com.acms.dto.AgentDTO;
import com.acms.model.Agent;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AgentMapper {

    AgentMapper INSTANCE = Mappers.getMapper(AgentMapper.class);

    @Mapping(target = "id", ignore = true)
    Agent toEntity(AgentDTO dto);

    AgentDTO toDto(Agent entity);

    List<AgentDTO> toDtoList(List<Agent> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agentCode", ignore = true) // Prevent updating agent code
    void updateAgentFromDto(AgentDTO dto, @MappingTarget Agent entity);

    @AfterMapping
    default void setAuditFields(AgentDTO dto, @MappingTarget Agent entity) {
        // Set default status if not provided
        if (dto.getStatus() == null) {
            entity.setStatus(Agent.AgentStatus.ACTIVE);
        }
        
        // Set isActive based on status
        entity.setActive(dto.getStatus() == Agent.AgentStatus.ACTIVE);
    }
}
