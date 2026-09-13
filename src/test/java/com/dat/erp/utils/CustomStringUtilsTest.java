package com.dat.erp.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.dat.erp.entities.Account;
import com.dat.erp.entities.Role;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.ForbiddenException;
import com.dat.erp.systemconfigs.CustomUserDetails;

class CustomStringUtilsTest {

    @Test
    void normalizeUrl_handlesNullAndEmpty() {
        assertThat(CustomStringUtils.normalizeUrl(null)).isNull();
        assertThat(CustomStringUtils.normalizeUrl("")).isEqualTo("");
    }

    @Test
    void normalizeUrl_addsLeadingSlashAndKeepsTwoSegments() {
        assertThat(CustomStringUtils.normalizeUrl("api/auth/login")).isEqualTo("/api/auth");
        assertThat(CustomStringUtils.normalizeUrl("/api")).isEqualTo("/api");
        assertThat(CustomStringUtils.normalizeUrl("/api/auth/login")).isEqualTo("/api/auth");
    }

    @Test
    void resolveScopedEmployeeCode_nonEmployeeReturnsNormalizedRequestedCode() {
        CustomUserDetails currentUser = new CustomUserDetails(new Account(), null);

        assertThat(CustomStringUtils.resolveScopedEmployeeCode(currentUser, " EMP001 "))
                .isEqualTo("EMP001");
        assertThat(CustomStringUtils.resolveScopedEmployeeCode(currentUser, " "))
                .isNull();
    }

    @Test
    void resolveScopedEmployeeCode_employeeUsesOwnCodeWhenFilterMissing() {
        CustomUserDetails currentUser = employeeUser(" EMP001 ");

        assertThat(CustomStringUtils.resolveScopedEmployeeCode(currentUser, null))
                .isEqualTo("EMP001");
    }

    @Test
    void resolveScopedEmployeeCode_employeeCannotRequestAnotherEmployee() {
        CustomUserDetails currentUser = employeeUser("EMP001");

        assertThatThrownBy(() -> CustomStringUtils.resolveScopedEmployeeCode(currentUser, "EMP002"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("AUTH_403_001: Forbidden");
    }

    @Test
    void resolveScopedEmployeeCode_employeeWithoutProfileCodeIsForbidden() {
        CustomUserDetails currentUser = employeeUser(" ");

        assertThatThrownBy(() -> CustomStringUtils.resolveScopedEmployeeCode(currentUser, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("AUTH_403_001: Forbidden");
    }

    private static CustomUserDetails employeeUser(String profileCode) {
        Role role = new Role();
        role.setName("EMPLOYEE");

        Account account = new Account();
        account.setRole(role);

        UserProfile profile = new UserProfile();
        com.dat.erp.testutils.EntityTestData.setCode(profile, profileCode);

        return new CustomUserDetails(account, profile);
    }
}
