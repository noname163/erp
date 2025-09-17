package com.dat.erp.entities;

import java.time.LocalDateTime;

import com.dat.erp.customannotation.encriptedcolumn.Encrypted;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "user")
@Entity
@Table(name = "user_identifications")
public class UserIdentification {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @ManyToOne
    @JoinColumn(name = "user_code", nullable = false)
    private UserInformation user;

    @Column(name = "id_type", nullable = false)
    private String idType;

    @Column(name = "id_number", nullable = false)
    @Encrypted(mode = Encrypted.Mode.ENCRYPT)
    private String idNumber;

    @Encrypted(mode = Encrypted.Mode.ENCRYPT)
    private String issuedDate;
    @Encrypted(mode = Encrypted.Mode.ENCRYPT)
    private String expiryDate;

    @Column(name = "issued_by")
    @Encrypted(mode = Encrypted.Mode.ENCRYPT)
    private String issuedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
