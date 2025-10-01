package com.dat.erp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
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
@ToString(exclude = { "employee", "workSchedule" })
@Entity
@Table(name = "employee_shift")
@NamedEntityGraph(name = "EmployeeHasWorkSchedule.full", attributeNodes = {
        @NamedAttributeNode("employee"),
        @NamedAttributeNode("workSchedule")
})
public class EmployeeHasWorkSchedule extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false)
    private EmployeeInformation employee;

    @ManyToOne
    @JoinColumn(name = "schedule_code", referencedColumnName = "code", nullable = false)
    private WorkSchedule workSchedule;
}
