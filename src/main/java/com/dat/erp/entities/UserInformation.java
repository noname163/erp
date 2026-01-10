package com.dat.erp.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.dat.erp.converters.EncryptFieldConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
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
@ToString(exclude = { "identifications", "addresses", "accounts" })
@Entity
@Table(name = "user_informations")
@NamedEntityGraph(name = "UserInformation.full", attributeNodes = {
        @NamedAttributeNode("identifications"),
        @NamedAttributeNode("addresses"),
        @NamedAttributeNode("accounts")
})
public class UserInformation extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    @Convert(converter = EncryptFieldConverter.class)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @Convert(converter = EncryptFieldConverter.class)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    @Convert(converter = EncryptFieldConverter.class)
    private String dateOfBirth;

    @Column(name = "email", nullable = false)
    @Convert(converter = EncryptFieldConverter.class)
    private String email;

    private String gender;

    private String nationality;

    @Column(name = "phone_number")
    @Convert(converter = EncryptFieldConverter.class)
    private String phoneNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserIdentification> identifications;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Address> addresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BankAccount> accounts;
}
