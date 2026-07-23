package com.dat.erp.services;

import java.util.List;

public interface CodeGenerator {
    String nextCode(String prefix);

    List<String> nextCodes(String prefix, int count);
}
