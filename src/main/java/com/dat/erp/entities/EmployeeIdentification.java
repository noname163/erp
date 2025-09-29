package com.dat.erp.entities;

import com.dat.erp.converters.EncryptFieldConverter;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@ToString(exclude = "employee")
@Entity
@Table(name = "employee_identifications")
public class EmployeeIdentification extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false)
    private EmployeeInformation employee;

    private String idType;
    @Convert(converter = EncryptFieldConverter.class)
    private String idNumber;
    @Convert(converter = EncryptFieldConverter.class)
    private String issuedDate;
    @Convert(converter = EncryptFieldConverter.class)
    private String expiryDate;
    private String issuedBy;

}
