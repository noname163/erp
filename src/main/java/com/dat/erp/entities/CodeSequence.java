package com.dat.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "code_sequence", uniqueConstraints = {
        @UniqueConstraint(name = "uk_code_sequence_prefix", columnNames = { "prefix" })
})
public class CodeSequence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prefix", nullable = false, length = 50)
    private String prefix;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber;

    public CodeSequence(String prefix, long lastNumber) {
        this.prefix = prefix;
        this.lastNumber = lastNumber;
    }
}

