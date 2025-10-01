package com.dat.erp.entities;

import com.dat.erp.constants.ApiType;
import com.dat.erp.constants.CommonEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "system_api", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "endpoint", "systemType" })
})
public class SystemApi extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String endpoint;

    private String description;

    private ApiType type;

    private CommonEnum systemType;

}
