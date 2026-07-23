package com.dat.erp.entities;

import com.dat.erp.converters.EncryptFieldConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Builder
@ToString
public class Company extends BaseAuditableEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String industry;

    @Column(length = 100)
    @Convert(converter = EncryptFieldConverter.class)
    private String taxNumber;

    @Column(name = "secret_key")
    private String secretKey;

    @Column(name = "tax")
    @Convert(converter = EncryptFieldConverter.class)
    private String tax;

    @Column(name = "email")
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;
}
