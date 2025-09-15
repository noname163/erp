package com.dat.erp.customannotation.encriptedcolumn;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Encrypted {
    Mode mode() default Mode.ENCRYPT;

    enum Mode {
        ENCRYPT, // For email, address, etc. (decryptable)
        HASH // For password (not decryptable)
    }
}
