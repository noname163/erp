package com.dat.erp.entities;

import com.dat.erp.utils.UuidV7;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "email")
@ToString()
public class Email extends BaseAuditableEntity {
    public Email() {
        assignCode("EML-" + UuidV7.generate());
    }

    private String subject;
    private String emailFrom;
    private String emailTo;
    private String fullName;
    private String gender;
    private String htmlFilePath;
    @Column(name = "is_sent")
    private boolean sent;
    private Integer retryTime;
    private String errorMessage;
    private boolean needRetry;
}
