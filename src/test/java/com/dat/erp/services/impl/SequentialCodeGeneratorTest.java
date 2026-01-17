package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import com.dat.erp.entities.CodeSequence;
import com.dat.erp.repositories.customrepositories.CodeSequenceRepository;

class SequentialCodeGeneratorTest {

    @Mock
    private CodeSequenceRepository codeSequenceRepository;

    private SequentialCodeGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new SequentialCodeGenerator(codeSequenceRepository);
        when(codeSequenceRepository.save(any(CodeSequence.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void nextCodes_countMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> generator.nextCodes("CMP-", 0));
        assertThrows(IllegalArgumentException.class, () -> generator.nextCodes("CMP-", -1));
    }

    @Test
    void nextCode_firstTime_createsSequenceAndReturnsPaddedCode() {
        when(codeSequenceRepository.findByPrefix("CMP-")).thenReturn(Optional.empty());

        String code = generator.nextCode("CMP-");

        assertEquals("CMP-000001", code);
        verify(codeSequenceRepository, times(2)).save(any(CodeSequence.class));
    }

    @Test
    void nextCodes_allocatesRangeAndReturnsSequentialCodes() {
        CodeSequence seq = new CodeSequence("CMP-", 5L);
        when(codeSequenceRepository.findByPrefix("CMP-")).thenReturn(Optional.of(seq));

        List<String> codes = generator.nextCodes("CMP-", 3);

        assertEquals(List.of("CMP-000006", "CMP-000007", "CMP-000008"), codes);
        assertEquals(8L, seq.getLastNumber());
        verify(codeSequenceRepository).save(seq);
    }

    @Test
    void nextCode_raceOnInsert_recoversByReReading() {
        CodeSequence seq = new CodeSequence("CMP-", 10L);
        when(codeSequenceRepository.findByPrefix("CMP-")).thenReturn(Optional.empty(), Optional.of(seq));
        when(codeSequenceRepository.save(Mockito.argThat(
                s -> s != null && "CMP-".equals(s.getPrefix()) && s.getLastNumber() == 0L)))
                        .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(codeSequenceRepository.save(Mockito.argThat(
                s -> s != null && "CMP-".equals(s.getPrefix()) && s.getLastNumber() != 0L)))
                        .thenAnswer(inv -> inv.getArgument(0));

        String code = generator.nextCode("CMP-");

        assertEquals("CMP-000011", code);
        assertEquals(11L, seq.getLastNumber());
    }
}
