package com.dat.erp.entities;

import com.dat.erp.converters.EncryptFieldConverter;

import jakarta.persistence.Column;
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
@ToString(exclude = "user")
@Entity
@Table(name = "user_identifications")
public class UserIdentification extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_code", nullable = false)
    private UserInformation user;

    @Column(name = "id_type", nullable = false)
    private String idType;

    @Column(name = "id_number", nullable = false)
    @Convert(converter = EncryptFieldConverter.class)
    private String idNumber;

    @Convert(converter = EncryptFieldConverter.class)
    private String issuedDate;
    @Convert(converter = EncryptFieldConverter.class)
    private String expiryDate;

    @Column(name = "issued_by")
    @Convert(converter = EncryptFieldConverter.class)
    private String issuedBy;
}
