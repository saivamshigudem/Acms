package com.acms.mapper;

import com.acms.dto.AgentDTO;
import com.acms.model.Agent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-04T12:46:36+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class AgentMapperImpl implements AgentMapper {

    @Override
    public Agent toEntity(AgentDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Agent.AgentBuilder agent = Agent.builder();

        agent.agentCode( dto.getAgentCode() );
        agent.firstName( dto.getFirstName() );
        agent.middleName( dto.getMiddleName() );
        agent.lastName( dto.getLastName() );
        agent.email( dto.getEmail() );
        agent.phone( dto.getPhone() );
        agent.status( dto.getStatus() );
        agent.dateOfBirth( dto.getDateOfBirth() );
        agent.hireDate( dto.getHireDate() );
        agent.terminationDate( dto.getTerminationDate() );
        agent.address( dto.getAddress() );
        agent.city( dto.getCity() );
        agent.state( dto.getState() );
        agent.postalCode( dto.getPostalCode() );
        agent.country( dto.getCountry() );
        agent.notes( dto.getNotes() );

        return agent.build();
    }

    @Override
    public AgentDTO toDto(Agent entity) {
        if ( entity == null ) {
            return null;
        }

        AgentDTO.AgentDTOBuilder agentDTO = AgentDTO.builder();

        agentDTO.id( entity.getId() );
        agentDTO.agentCode( entity.getAgentCode() );
        agentDTO.firstName( entity.getFirstName() );
        agentDTO.middleName( entity.getMiddleName() );
        agentDTO.lastName( entity.getLastName() );
        agentDTO.email( entity.getEmail() );
        agentDTO.phone( entity.getPhone() );
        agentDTO.status( entity.getStatus() );
        agentDTO.dateOfBirth( entity.getDateOfBirth() );
        agentDTO.hireDate( entity.getHireDate() );
        agentDTO.terminationDate( entity.getTerminationDate() );
        agentDTO.address( entity.getAddress() );
        agentDTO.city( entity.getCity() );
        agentDTO.state( entity.getState() );
        agentDTO.postalCode( entity.getPostalCode() );
        agentDTO.country( entity.getCountry() );
        agentDTO.notes( entity.getNotes() );
        agentDTO.version( entity.getVersion() );

        return agentDTO.build();
    }

    @Override
    public List<AgentDTO> toDtoList(List<Agent> entities) {
        if ( entities == null ) {
            return null;
        }

        List<AgentDTO> list = new ArrayList<AgentDTO>( entities.size() );
        for ( Agent agent : entities ) {
            list.add( toDto( agent ) );
        }

        return list;
    }

    @Override
    public void updateAgentFromDto(AgentDTO dto, Agent entity) {
        if ( dto == null ) {
            return;
        }

        entity.setActive( dto.isActive() );
        if ( dto.getVersion() != null ) {
            entity.setVersion( dto.getVersion() );
        }
        if ( dto.getFirstName() != null ) {
            entity.setFirstName( dto.getFirstName() );
        }
        if ( dto.getMiddleName() != null ) {
            entity.setMiddleName( dto.getMiddleName() );
        }
        if ( dto.getLastName() != null ) {
            entity.setLastName( dto.getLastName() );
        }
        if ( dto.getEmail() != null ) {
            entity.setEmail( dto.getEmail() );
        }
        if ( dto.getPhone() != null ) {
            entity.setPhone( dto.getPhone() );
        }
        if ( dto.getStatus() != null ) {
            entity.setStatus( dto.getStatus() );
        }
        if ( dto.getDateOfBirth() != null ) {
            entity.setDateOfBirth( dto.getDateOfBirth() );
        }
        if ( dto.getHireDate() != null ) {
            entity.setHireDate( dto.getHireDate() );
        }
        if ( dto.getTerminationDate() != null ) {
            entity.setTerminationDate( dto.getTerminationDate() );
        }
        if ( dto.getAddress() != null ) {
            entity.setAddress( dto.getAddress() );
        }
        if ( dto.getCity() != null ) {
            entity.setCity( dto.getCity() );
        }
        if ( dto.getState() != null ) {
            entity.setState( dto.getState() );
        }
        if ( dto.getPostalCode() != null ) {
            entity.setPostalCode( dto.getPostalCode() );
        }
        if ( dto.getCountry() != null ) {
            entity.setCountry( dto.getCountry() );
        }
        if ( dto.getNotes() != null ) {
            entity.setNotes( dto.getNotes() );
        }

        setAuditFields( dto, entity );
    }
}
