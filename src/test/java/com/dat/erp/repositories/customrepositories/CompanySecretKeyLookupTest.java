package com.dat.erp.repositories.customrepositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.dat.erp.entities.Company;
import com.dat.erp.testutils.EntityTestData;

@DataJpaTest
class CompanySecretKeyLookupTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void resolvesKeyByCompanyIdentityInsteadOfTenantMetadata() {
        persistCompany("CMP-1", "SYSTEM", "key-one", false);
        persistCompany("CMP-2", "CMP-1", "key-two", false);

        assertEquals("key-one", accountRepository.getCurrentCompanySecretKeyByCompanyCode("CMP-1").orElseThrow());
        assertEquals("key-two", accountRepository.getCurrentCompanySecretKeyByCompanyCode("CMP-2").orElseThrow());
    }

    @Test
    void doesNotReturnKeyForDeletedOrMissingCompany() {
        persistCompany("CMP-DELETED", "SYSTEM", "deleted-key", true);

        assertTrue(accountRepository.getCurrentCompanySecretKeyByCompanyCode("CMP-DELETED").isEmpty());
        assertTrue(accountRepository.getCurrentCompanySecretKeyByCompanyCode("CMP-MISSING").isEmpty());
    }

    private void persistCompany(String code, String tenantCode, String secretKey, boolean deleted) {
        Company company = new Company();
        EntityTestData.setCode(company, code);
        EntityTestData.setCompanyCode(company, tenantCode);
        company.setName(code);
        company.setSecretKey(secretKey);
        if (deleted) {
            company.markDeleted();
        }
        entityManager.persistAndFlush(company);
        entityManager.clear();
    }
}
