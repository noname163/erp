package com.dat.erp.entities;

import java.time.LocalDate;

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.constants.ShiftType;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
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
@Table(name = "employee_shifts", uniqueConstraints = @UniqueConstraint(columnNames = { "shift_date", "shift_type",
        "quantity" }))
@ToString(exclude = { "company" })
@NamedEntityGraph(name = "WorkSchedule.full", attributeNodes = {
        @NamedAttributeNode("company")
})
public class WorkSchedule extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate shiftDate;

    private Integer quantity;

    private ShiftType shiftType;

    private CommonStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_code", referencedColumnName = "code", nullable = false)
    private Company company;

}
