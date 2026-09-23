package com.dat.erp.entities;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.dat.erp.contexts.TenantContext;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@FilterDef(name = "auditableFilter", parameters = {
        @ParamDef(name = "createdByList", type = String.class),
        @ParamDef(name = "updatedByList", type = String.class)
})
@Filter(name = "auditableFilter", condition = " (created_by in (:createdByList) OR updated_by in (:updatedByList)) ")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "code", nullable = false)
    @EqualsAndHashCode.Include
    private String code;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "company_code", nullable = false)
    @EqualsAndHashCode.Include
    private String companyCode;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public void markDeleted() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }

    @PrePersist
    protected void initializeTenant() {
        if (companyCode == null || companyCode.isBlank()) {
            companyCode = TenantContext.requireCompanyCode();
        }
    }

    protected final void assignCode(String code) {
        if (this.code != null && !this.code.isBlank()) {
            throw new IllegalStateException(
                "Entity code has already been assigned"
            );
        }

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                "Entity code must not be blank"
            );
        }

        this.code = code;
    }

    public final void initializeCode(String code) {
        assignCode(code);
    }

    public final void assignCompanyCode(String companyCode) {
        if (this.companyCode != null && !this.companyCode.isBlank()) {
            throw new IllegalStateException("Entity company code has already been assigned");
        }
        if (companyCode == null || companyCode.isBlank()) {
            throw new IllegalArgumentException("Entity company code must not be blank");
        }
        this.companyCode = companyCode;
    }

    protected BaseAuditableEntity(
            String code,
            String companyCode) {
        this.code = Objects.requireNonNull(code);
        this.companyCode = Objects.requireNonNull(companyCode);
    }

}
