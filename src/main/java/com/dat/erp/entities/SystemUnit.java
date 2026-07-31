package com.dat.erp.entities;

import java.util.List;

import com.dat.erp.constants.SystemUnitType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(exclude = { "fromDetails", "toDetails" })
@Entity
@Table(name = "system_unit", uniqueConstraints = {
        @UniqueConstraint(name = "uk_system_unit_code_company_code", columnNames = { "code", "company_code" })
})
public class SystemUnit extends BaseAuditableEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private SystemUnitType type;

    @OneToMany(mappedBy = "from", fetch = FetchType.LAZY)
    private List<SystemUnitDetail> fromDetails;

    @OneToMany(mappedBy = "to", fetch = FetchType.LAZY)
    private List<SystemUnitDetail> toDetails;
}

