package com.dat.erp.entities;

import java.util.List;

import com.dat.erp.constants.CommonStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
@Entity
@Table(name = "department", uniqueConstraints = {
        @UniqueConstraint(name = "uk_department_code_company_code", columnNames = { "code", "company_code" })
})
@ToString(exclude = { "userProfiles" })
public class Department extends BaseAuditableEntity {
    private String name;

    private String description;

    private CommonStatus status;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserProfile> userProfiles;

}
