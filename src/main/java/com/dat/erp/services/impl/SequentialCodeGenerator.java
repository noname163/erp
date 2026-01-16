package com.dat.erp.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.entities.CodeSequence;
import com.dat.erp.repositories.customrepositories.CodeSequenceRepository;
import com.dat.erp.services.CodeGenerator;

@Service
public class SequentialCodeGenerator implements CodeGenerator {

    private static final int MIN_DIGITS = 6;

    private final CodeSequenceRepository codeSequenceRepository;

    public SequentialCodeGenerator(CodeSequenceRepository codeSequenceRepository) {
        this.codeSequenceRepository = codeSequenceRepository;
    }

    @Transactional
    @Override
    public String nextCode(String prefix) {
        return nextCodes(prefix, 1).get(0);
    }

    @Transactional
    @Override
    public List<String> nextCodes(String prefix, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
        long startInclusive = allocateNextRangeStart(prefix, count);
        List<String> codes = new ArrayList<>(count);
        for (long number = startInclusive; number < startInclusive + count; number++) {
            codes.add(prefix + String.format("%0" + MIN_DIGITS + "d", number));
        }
        return codes;
    }

    private long allocateNextRangeStart(String prefix, int count) {
        CodeSequence seq = codeSequenceRepository.findByPrefix(prefix).orElse(null);
        if (seq == null) {
            try {
                seq = codeSequenceRepository.save(new CodeSequence(prefix, 0L));
            } catch (DataIntegrityViolationException ex) {
                seq = codeSequenceRepository.findByPrefix(prefix).orElseThrow();
            }
        }

        long last = Optional.ofNullable(seq.getLastNumber()).orElse(0L);
        long start = last + 1L;
        seq.setLastNumber(last + count);
        codeSequenceRepository.save(seq);
        return start;
    }
}
