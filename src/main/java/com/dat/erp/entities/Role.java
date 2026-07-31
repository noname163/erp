package com.dat.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "role", uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_code_company_code", columnNames = { "code", "company_code" })
})
public class Role extends BaseAuditableEntity {
    @Column(unique = true, nullable = false)
    private String name;

    private String type;

    private String description;

    private Boolean isPublic;
}
