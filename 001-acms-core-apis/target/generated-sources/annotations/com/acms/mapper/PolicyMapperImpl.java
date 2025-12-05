package com.acms.mapper;

import com.acms.dto.PolicyDTO;
import com.acms.model.Policy;
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
public class PolicyMapperImpl implements PolicyMapper {

    @Override
    public Policy toEntity(PolicyDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Policy.PolicyBuilder policy = Policy.builder();

        policy.policyNumber( dto.getPolicyNumber() );
        policy.policyType( dto.getPolicyType() );
        policy.status( dto.getStatus() );
        policy.groupName( dto.getGroupName() );
        policy.groupNumber( dto.getGroupNumber() );
        policy.effectiveDate( dto.getEffectiveDate() );
        policy.expirationDate( dto.getExpirationDate() );
        policy.premium( dto.getPremium() );
        policy.coverageAmount( dto.getCoverageAmount() );
        policy.deductibleAmount( dto.getDeductibleAmount() );
        policy.description( dto.getDescription() );
        policy.renewalDate( dto.getRenewalDate() );
        policy.cancellationDate( dto.getCancellationDate() );
        policy.cancellationReason( dto.getCancellationReason() );

        return policy.build();
    }

    @Override
    public PolicyDTO toDto(Policy entity) {
        if ( entity == null ) {
            return null;
        }

        PolicyDTO.PolicyDTOBuilder policyDTO = PolicyDTO.builder();

        policyDTO.id( entity.getId() );
        policyDTO.policyNumber( entity.getPolicyNumber() );
        policyDTO.policyType( entity.getPolicyType() );
        policyDTO.status( entity.getStatus() );
        policyDTO.groupName( entity.getGroupName() );
        policyDTO.groupNumber( entity.getGroupNumber() );
        policyDTO.effectiveDate( entity.getEffectiveDate() );
        policyDTO.expirationDate( entity.getExpirationDate() );
        policyDTO.premium( entity.getPremium() );
        policyDTO.coverageAmount( entity.getCoverageAmount() );
        policyDTO.deductibleAmount( entity.getDeductibleAmount() );
        policyDTO.description( entity.getDescription() );
        policyDTO.renewalDate( entity.getRenewalDate() );
        policyDTO.cancellationDate( entity.getCancellationDate() );
        policyDTO.cancellationReason( entity.getCancellationReason() );
        policyDTO.version( entity.getVersion() );

        policyDTO.agent( mapToAgentSummary(entity.getAgent()) );

        return policyDTO.build();
    }

    @Override
    public List<PolicyDTO> toDtoList(List<Policy> entities) {
        if ( entities == null ) {
            return null;
        }

        List<PolicyDTO> list = new ArrayList<PolicyDTO>( entities.size() );
        for ( Policy policy : entities ) {
            list.add( toDto( policy ) );
        }

        return list;
    }

    @Override
    public void updatePolicyFromDto(PolicyDTO dto, Policy entity) {
        if ( dto == null ) {
            return;
        }

        entity.setActive( dto.isActive() );
        if ( dto.getVersion() != null ) {
            entity.setVersion( dto.getVersion() );
        }
        if ( dto.getPolicyType() != null ) {
            entity.setPolicyType( dto.getPolicyType() );
        }
        if ( dto.getStatus() != null ) {
            entity.setStatus( dto.getStatus() );
        }
        if ( dto.getGroupName() != null ) {
            entity.setGroupName( dto.getGroupName() );
        }
        if ( dto.getGroupNumber() != null ) {
            entity.setGroupNumber( dto.getGroupNumber() );
        }
        if ( dto.getEffectiveDate() != null ) {
            entity.setEffectiveDate( dto.getEffectiveDate() );
        }
        if ( dto.getExpirationDate() != null ) {
            entity.setExpirationDate( dto.getExpirationDate() );
        }
        if ( dto.getPremium() != null ) {
            entity.setPremium( dto.getPremium() );
        }
        if ( dto.getCoverageAmount() != null ) {
            entity.setCoverageAmount( dto.getCoverageAmount() );
        }
        if ( dto.getDeductibleAmount() != null ) {
            entity.setDeductibleAmount( dto.getDeductibleAmount() );
        }
        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getRenewalDate() != null ) {
            entity.setRenewalDate( dto.getRenewalDate() );
        }
        if ( dto.getCancellationDate() != null ) {
            entity.setCancellationDate( dto.getCancellationDate() );
        }
        if ( dto.getCancellationReason() != null ) {
            entity.setCancellationReason( dto.getCancellationReason() );
        }

        setAuditFields( dto, entity );
    }

    @Override
    public void updatePolicyFromDtoWithoutAgent(PolicyDTO dto, Policy entity) {
        if ( dto == null ) {
            return;
        }

        entity.setActive( dto.isActive() );
        if ( dto.getVersion() != null ) {
            entity.setVersion( dto.getVersion() );
        }
        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getPolicyNumber() != null ) {
            entity.setPolicyNumber( dto.getPolicyNumber() );
        }
        if ( dto.getPolicyType() != null ) {
            entity.setPolicyType( dto.getPolicyType() );
        }
        if ( dto.getStatus() != null ) {
            entity.setStatus( dto.getStatus() );
        }
        if ( dto.getGroupName() != null ) {
            entity.setGroupName( dto.getGroupName() );
        }
        if ( dto.getGroupNumber() != null ) {
            entity.setGroupNumber( dto.getGroupNumber() );
        }
        if ( dto.getEffectiveDate() != null ) {
            entity.setEffectiveDate( dto.getEffectiveDate() );
        }
        if ( dto.getExpirationDate() != null ) {
            entity.setExpirationDate( dto.getExpirationDate() );
        }
        if ( dto.getPremium() != null ) {
            entity.setPremium( dto.getPremium() );
        }
        if ( dto.getCoverageAmount() != null ) {
            entity.setCoverageAmount( dto.getCoverageAmount() );
        }
        if ( dto.getDeductibleAmount() != null ) {
            entity.setDeductibleAmount( dto.getDeductibleAmount() );
        }
        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getRenewalDate() != null ) {
            entity.setRenewalDate( dto.getRenewalDate() );
        }
        if ( dto.getCancellationDate() != null ) {
            entity.setCancellationDate( dto.getCancellationDate() );
        }
        if ( dto.getCancellationReason() != null ) {
            entity.setCancellationReason( dto.getCancellationReason() );
        }

        setAuditFields( dto, entity );
    }
}
