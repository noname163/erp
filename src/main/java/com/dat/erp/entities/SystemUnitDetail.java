package com.dat.erp.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@ToString(exclude = { "from", "to" })
@Entity
@Table(name = "system_unit_detail")
public class SystemUnitDetail extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_unit_code_from", referencedColumnName = "code", nullable = false)
    private SystemUnit from;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_unit_code_to", referencedColumnName = "code", nullable = false)
    private SystemUnit to;

    @Column(name = "exchange_quantity")
    private BigDecimal exchangeQuantity;
}

