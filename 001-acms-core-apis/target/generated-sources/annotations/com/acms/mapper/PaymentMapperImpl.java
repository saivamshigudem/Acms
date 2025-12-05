package com.acms.mapper;

import com.acms.dto.PaymentDTO;
import com.acms.model.Payment;
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
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public Payment toEntity(PaymentDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Payment.PaymentBuilder payment = Payment.builder();

        payment.paymentAmount( dto.getPaymentAmount() );
        payment.paymentReference( dto.getPaymentReference() );
        payment.paymentMethod( dto.getPaymentMethod() );
        payment.status( dto.getStatus() );
        payment.paymentDate( dto.getPaymentDate() );
        payment.processedDate( dto.getProcessedDate() );
        payment.bankAccount( dto.getBankAccount() );
        payment.bankName( dto.getBankName() );
        payment.transactionId( dto.getTransactionId() );
        payment.notes( dto.getNotes() );

        return payment.build();
    }

    @Override
    public PaymentDTO toDto(Payment entity) {
        if ( entity == null ) {
            return null;
        }

        PaymentDTO.PaymentDTOBuilder paymentDTO = PaymentDTO.builder();

        paymentDTO.id( entity.getId() );
        paymentDTO.paymentAmount( entity.getPaymentAmount() );
        paymentDTO.paymentReference( entity.getPaymentReference() );
        paymentDTO.paymentMethod( entity.getPaymentMethod() );
        paymentDTO.status( entity.getStatus() );
        paymentDTO.paymentDate( entity.getPaymentDate() );
        paymentDTO.processedDate( entity.getProcessedDate() );
        paymentDTO.bankAccount( entity.getBankAccount() );
        paymentDTO.bankName( entity.getBankName() );
        paymentDTO.transactionId( entity.getTransactionId() );
        paymentDTO.notes( entity.getNotes() );
        paymentDTO.version( entity.getVersion() );

        paymentDTO.commission( mapToCommissionSummary(entity.getCommission()) );
        paymentDTO.agent( mapToAgentSummary(entity.getAgent()) );

        return paymentDTO.build();
    }

    @Override
    public List<PaymentDTO> toDtoList(List<Payment> entities) {
        if ( entities == null ) {
            return null;
        }

        List<PaymentDTO> list = new ArrayList<PaymentDTO>( entities.size() );
        for ( Payment payment : entities ) {
            list.add( toDto( payment ) );
        }

        return list;
    }

    @Override
    public void updatePaymentFromDto(PaymentDTO dto, Payment entity) {
        if ( dto == null ) {
            return;
        }

        entity.setActive( dto.isActive() );
        if ( dto.getVersion() != null ) {
            entity.setVersion( dto.getVersion() );
        }
        if ( dto.getPaymentAmount() != null ) {
            entity.setPaymentAmount( dto.getPaymentAmount() );
        }
        if ( dto.getPaymentReference() != null ) {
            entity.setPaymentReference( dto.getPaymentReference() );
        }
        if ( dto.getPaymentMethod() != null ) {
            entity.setPaymentMethod( dto.getPaymentMethod() );
        }
        if ( dto.getStatus() != null ) {
            entity.setStatus( dto.getStatus() );
        }
        if ( dto.getPaymentDate() != null ) {
            entity.setPaymentDate( dto.getPaymentDate() );
        }
        if ( dto.getProcessedDate() != null ) {
            entity.setProcessedDate( dto.getProcessedDate() );
        }
        if ( dto.getBankAccount() != null ) {
            entity.setBankAccount( dto.getBankAccount() );
        }
        if ( dto.getBankName() != null ) {
            entity.setBankName( dto.getBankName() );
        }
        if ( dto.getTransactionId() != null ) {
            entity.setTransactionId( dto.getTransactionId() );
        }
        if ( dto.getNotes() != null ) {
            entity.setNotes( dto.getNotes() );
        }

        setAuditFields( dto, entity );
    }

    @Override
    public void updatePaymentFromDtoWithoutReferences(PaymentDTO dto, Payment entity) {
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
        if ( dto.getPaymentAmount() != null ) {
            entity.setPaymentAmount( dto.getPaymentAmount() );
        }
        if ( dto.getPaymentReference() != null ) {
            entity.setPaymentReference( dto.getPaymentReference() );
        }
        if ( dto.getPaymentMethod() != null ) {
            entity.setPaymentMethod( dto.getPaymentMethod() );
        }
        if ( dto.getStatus() != null ) {
            entity.setStatus( dto.getStatus() );
        }
        if ( dto.getPaymentDate() != null ) {
            entity.setPaymentDate( dto.getPaymentDate() );
        }
        if ( dto.getProcessedDate() != null ) {
            entity.setProcessedDate( dto.getProcessedDate() );
        }
        if ( dto.getBankAccount() != null ) {
            entity.setBankAccount( dto.getBankAccount() );
        }
        if ( dto.getBankName() != null ) {
            entity.setBankName( dto.getBankName() );
        }
        if ( dto.getTransactionId() != null ) {
            entity.setTransactionId( dto.getTransactionId() );
        }
        if ( dto.getNotes() != null ) {
            entity.setNotes( dto.getNotes() );
        }

        setAuditFields( dto, entity );
    }
}
