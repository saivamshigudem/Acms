package com.acms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base abstract class for entities which will hold audit fields.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean active = true;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Called before persisting a new entity.
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        // Default createdBy/updatedBy will be set by AuditingEntityListener
        // You can override it in the service layer if needed
    }

    /**
     * Called before updating an entity.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        // updatedBy will be set by AuditingEntityListener
    }

    /**
     * Soft deletes the entity by marking it as inactive.
     * Override this method if you need custom soft delete logic.
     */
    public void softDelete() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
}
