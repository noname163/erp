package com.dat.erp.entities;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PayrollResultDetailTest {

    @Test
    void constructorAssignsUniqueDetailCode() {
        PayrollResultDetail first = new PayrollResultDetail();
        PayrollResultDetail second = new PayrollResultDetail();

        assertNotNull(first.getCode());
        assertTrue(first.getCode().startsWith("PRD"));
        assertNotEquals(first.getCode(), second.getCode());
    }
}
