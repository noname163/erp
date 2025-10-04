package com.dat.erp.repositories.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.dat.erp.entities.EmployeeInformation;
import jakarta.persistence.criteria.JoinType;

public class EmployeeSpecifications {

    public static Specification<EmployeeInformation> hasDepartmentCode(String departmentCode) {
        return (root, query, cb) -> {
            if (departmentCode == null || departmentCode.isBlank())
                return null;
            return cb.equal(root.get("department").get("code"), departmentCode);
        };
    }

    public static Specification<EmployeeInformation> hasCompanyCode(String companyCode) {
        return (root, query, cb) -> {
            if (companyCode == null || companyCode.isBlank())
                return null;
            return cb.equal(root.get("company").get("code"), companyCode);
        };
    }

    public static Specification<EmployeeInformation> hasManagerCode(String managerCode) {
        return (root, query, cb) -> {
            if (managerCode == null || managerCode.isBlank())
                return null;
            return cb.equal(root.get("managerCode"), managerCode);
        };
    }

    public static Specification<EmployeeInformation> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank())
                return null;
            String likePattern = "%" + keyword.toLowerCase() + "%";

            // join with user to access firstName and lastName
            var userJoin = root.join("user", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("email")), likePattern),
                    cb.like(cb.lower(root.get("nickname")), likePattern),
                    cb.like(cb.lower(userJoin.get("firstName")), likePattern),
                    cb.like(cb.lower(userJoin.get("lastName")), likePattern));
        };
    }

    public static Specification<EmployeeInformation> build(String departmentCode, String companyCode,
            String managerCode, String keyword) {
        return Specification
                .where(hasDepartmentCode(departmentCode))
                .and(hasCompanyCode(companyCode))
                .and(hasManagerCode(managerCode))
                .and(search(keyword));
    }
}
