package com.dat.erp.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.dat.erp.constants.PayrollRunStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@ToString(exclude = {"results"})
@Entity
@Table(name = "payroll_run")
public class PayrollRun extends BaseAuditableEntity {

    @Column(name = "period")
    private String period;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PayrollRunStatus status;

    @Column(name = "run_at")
    private LocalDateTime runAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "payrollRun", fetch = FetchType.LAZY)
    private List<PayrollResult> results;
}

