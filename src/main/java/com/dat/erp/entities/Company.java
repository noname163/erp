package com.dat.erp.entities;

import com.dat.erp.converters.EncryptFieldConverter;
import com.dat.erp.data.ContactInfoData;
import com.dat.erp.data.NamedResourceData;
import com.dat.erp.utils.ErrorUtils;

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

    public Company(
            NamedResourceData namedResource,
            ContactInfoData contactInfo,
            String industry,
            String taxNumber,
            String tax,
            String secretKey) {

        namedResource = ErrorUtils.requireNonNull(namedResource, "Company name data is required");
        contactInfo = ErrorUtils.requireNonNull(contactInfo, "Company contact data is required");

        this.name = namedResource.getName();
        this.industry = industry == null ? null : ErrorUtils.requireNotBlank(industry, "Industry must not be blank");
        this.email = contactInfo.getEmail();
        this.phoneNumber = contactInfo.getPhoneNumber();
        this.address = contactInfo.getAddress();
        this.taxNumber = taxNumber == null ? null : ErrorUtils.requireNotBlank(taxNumber, "Tax number must not be blank");
        this.tax = tax == null ? null : ErrorUtils.requireNotBlank(tax, "Tax must not be blank");
        this.secretKey = secretKey == null ? null : ErrorUtils.requireNotBlank(secretKey, "Secret key must not be blank");
    }

    public static Company create(
            NamedResourceData namedResource,
            ContactInfoData contactInfo,
            String industry,
            String taxNumber,
            String tax,
            String secretKey) {

        return new Company(namedResource, contactInfo, industry, taxNumber, tax, secretKey);
    }
}
