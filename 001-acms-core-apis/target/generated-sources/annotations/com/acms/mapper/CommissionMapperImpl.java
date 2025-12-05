package com.acms.mapper;

import com.acms.dto.CommissionDTO;
import com.acms.model.Commission;
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
public class CommissionMapperImpl implements CommissionMapper {

    @Override
    public Commission toEntity(CommissionDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Commission.CommissionBuilder commission = Commission.builder();

        commission.commissionRate( dto.getCommissionRate() );
        commission.commissionAmount( dto.getCommissionAmount() );
        commission.premiumAmount( dto.getPremiumAmount() );
        commission.commissionType( dto.getCommissionType() );
        commission.calculationDate( dto.getCalculationDate() );
        commission.effectiveDate( dto.getEffectiveDate() );
        commission.expiryDate( dto.getExpiryDate() );
        commission.status( dto.getStatus() );
        commission.paymentDate( dto.getPaymentDate() );
        commission.paymentReference( dto.getPaymentReference() );
        commission.notes( dto.getNotes() );

        return commission.build();
    }

    @Override
    public CommissionDTO toDto(Commission entity) {
        if ( entity == null ) {
            return null;
        }

        CommissionDTO.CommissionDTOBuilder commissionDTO = CommissionDTO.builder();

        commissionDTO.id( entity.getId() );
        commissionDTO.commissionRate( entity.getCommissionRate() );
        commissionDTO.commissionAmount( entity.getCommissionAmount() );
        commissionDTO.premiumAmount( entity.getPremiumAmount() );
        commissionDTO.commissionType( entity.getCommissionType() );
        commissionDTO.calculationDate( entity.getCalculationDate() );
        commissionDTO.effectiveDate( entity.getEffectiveDate() );
        commissionDTO.expiryDate( entity.getExpiryDate() );
        commissionDTO.status( entity.getStatus() );
        commissionDTO.paymentDate( entity.getPaymentDate() );
        commissionDTO.paymentReference( entity.getPaymentReference() );
        commissionDTO.notes( entity.getNotes() );
        commissionDTO.version( entity.getVersion() );

        commissionDTO.policy( mapToPolicySummary(entity.getPolicy()) );
        commissionDTO.agent( mapToAgentSummary(entity.getAgent()) );

        return commissionDTO.build();
    }

    @Override
    public List<CommissionDTO> toDtoList(List<Commission> entities) {
        if ( entities == null ) {
            return null;
        }

        List<CommissionDTO> list = new ArrayList<CommissionDTO>( entities.size() );
        for ( Commission commission : entities ) {
            list.add( toDto( commission ) );
        }

        return list;
    }

    @Override
    public void updateCommissionFromDto(CommissionDTO dto, Commission entity) {
        if ( dto == null ) {
            return;
        }

        entity.setActive( dto.isActive() );
        if ( dto.getVersion() != null ) {
            entity.setVersion( dto.getVersion() );
        }
        if ( dto.getCommissionRate() != null ) {
            entity.setCommissionRate( dto.getCommissionRate() );
        }
        if ( dto.getCommissionAmount() != null ) {
            entity.setCommissionAmount( dto.getCommissionAmount() );
        }
        if ( dto.getPremiumAmount() != null ) {
            entity.setPremiumAmount( dto.getPremiumAmount() );
        }
        if ( dto.getCommissionType() != null ) {
            entity.setCommissionType( dto.getCommissionType() );
        }
        if ( dto.getCalculationDate() != null ) {
            entity.setCalculationDate( dto.getCalculationDate() );
        }
        if ( dto.getEffectiveDate() != null ) {
            entity.setEffectiveDate( dto.getEffectiveDate() );
        }
        if ( dto.getExpiryDate() != null ) {
            entity.setExpiryDate( dto.getExpiryDate() );
        }
        if ( dto.getStatus() != null ) {
            entity.setStatus( dto.getStatus() );
        }
        if ( dto.getPaymentDate() != null ) {
            entity.setPaymentDate( dto.getPaymentDate() );
        }
        if ( dto.getPaymentReference() != null ) {
            entity.setPaymentReference( dto.getPaymentReference() );
        }
        if ( dto.getNotes() != null ) {
            entity.setNotes( dto.getNotes() );
        }

        setAuditFields( dto, entity );
    }

    @Override
    public void updateCommissionFromDtoWithoutReferences(CommissionDTO dto, Commission entity) {
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
        if ( dto.getCommissionRate() != null ) {
            entity.setCommissionRate( dto.getCommissionRate() );
        }
        if ( dto.getCommissionAmount() != null ) {
            entity.setCommissionAmount( dto.getCommissionAmount() );
        }
        if ( dto.getPremiumAmount() != null ) {
            entity.setPremiumAmount( dto.getPremiumAmount() );
        }
        if ( dto.getCommissionType() != null ) {
            entity.setCommissionType( dto.getCommissionType() );
        }
        if ( dto.getCalculationDate() != null ) {
            entity.setCalculationDate( dto.getCalculationDate() );
        }
        if ( dto.getEffectiveDate() != null ) {
            entity.setEffectiveDate( dto.getEffectiveDate() );
        }
        if ( dto.getExpiryDate() != null ) {
            entity.setExpiryDate( dto.getExpiryDate() );
        }
        if ( dto.getStatus() != null ) {
            entity.setStatus( dto.getStatus() );
        }
        if ( dto.getPaymentDate() != null ) {
            entity.setPaymentDate( dto.getPaymentDate() );
        }
        if ( dto.getPaymentReference() != null ) {
            entity.setPaymentReference( dto.getPaymentReference() );
        }
        if ( dto.getNotes() != null ) {
            entity.setNotes( dto.getNotes() );
        }

        setAuditFields( dto, entity );
    }
}
