package com.dat.erp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dat.erp.dto.response.PagedResponse;

class PageableUtilsTest {

    @Test
    void create_defaultsPageAndSize_andCapsSize() {
        Pageable pageable = PageableUtils.create(-1, 1000, null, null);

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().isSorted()).isFalse();
    }

    @Test
    void mapPage_mapsContentAndKeepsMetadata() {
        Page<Integer> page = new PageImpl<>(List.of(1, 2, 3));

        PagedResponse<String> mapped = PageableUtils.mapPage(page, n -> "N" + n, "OK");

        assertThat(mapped.getData()).containsExactly("N1", "N2", "N3");
    }
}

