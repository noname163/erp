package com.dat.erp.customannotation.encriptedcolumn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Encrypted {
    Mode mode() default Mode.ENCRYPT;

    enum Mode {
        ENCRYPT, // For email, address, etc. (decryptable)
        HASH // For password (not decryptable)
    }
}
