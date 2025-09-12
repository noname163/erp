package com.dat.erp.customannotation.searchable.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Searchable {
    String column() default ""; // Column/field name

    String path() default ""; // Nested path like "employees.name"

    Condition condition() default Condition.EQUALS;

    Group group() default Group.AND;

    enum Condition {
        EQUALS, LIKE, GREATER_THAN, LESS_THAN
    }

    enum Group {
        AND, OR
    }
}
