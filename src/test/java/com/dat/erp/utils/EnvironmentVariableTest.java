package com.dat.erp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EnvironmentVariableTest {

    @Test
    void getWhitelistAsList_splitsByComma() {
        EnvironmentVariable env = new EnvironmentVariable();
        env.setWhitelist("/a,/b,/c");

        assertThat(env.getWhitelistAsList()).containsExactly("/a", "/b", "/c");
    }
}

