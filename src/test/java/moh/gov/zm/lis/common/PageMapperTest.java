package moh.gov.zm.lis.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageMapperTest {

    @Test
    void of_computesPageMetadata_middlePage() {
        PagedResponse<String> page = PageMapper.of(List.of("a", "b"), 25, 1, 10);

        assertThat(page.getContent()).containsExactly("a", "b");
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getCurrentPage()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(10);
        assertThat(page.getHasNext()).isTrue();
        assertThat(page.getHasPrevious()).isTrue();
    }

    @Test
    void of_firstPage_hasNoPrevious() {
        PagedResponse<String> page = PageMapper.of(List.of("a"), 5, 0, 10);

        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getHasPrevious()).isFalse();
        assertThat(page.getHasNext()).isFalse();
    }

    @Test
    void of_lastPage_hasNoNext() {
        PagedResponse<String> page = PageMapper.of(List.of("x"), 21, 2, 10);

        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getHasNext()).isFalse();
        assertThat(page.getHasPrevious()).isTrue();
    }

    @Test
    void of_emptyResult() {
        PagedResponse<String> page = PageMapper.of(List.of(), 0, 0, 20);

        assertThat(page.getTotalPages()).isZero();
        assertThat(page.getHasNext()).isFalse();
        assertThat(page.getHasPrevious()).isFalse();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void ofList_slicesAndMapsRequestedPage() {
        List<Integer> all = List.of(1, 2, 3, 4, 5, 6, 7);

        PagedResponse<String> page = PageMapper.ofList(all, 1, 3, i -> "n" + i);

        assertThat(page.getContent()).containsExactly("n4", "n5", "n6");
        assertThat(page.getTotalElements()).isEqualTo(7);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getCurrentPage()).isEqualTo(1);
        assertThat(page.getHasNext()).isTrue();
        assertThat(page.getHasPrevious()).isTrue();
    }

    @Test
    void ofList_pageBeyondEnd_returnsEmptyContent() {
        List<Integer> all = List.of(1, 2, 3);

        PagedResponse<String> page = PageMapper.ofList(all, 5, 10, i -> "n" + i);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void ofList_lastPartialPage() {
        List<Integer> all = List.of(1, 2, 3, 4, 5);

        PagedResponse<String> page = PageMapper.ofList(all, 2, 2, i -> "n" + i);

        assertThat(page.getContent()).containsExactly("n5");
        assertThat(page.getHasNext()).isFalse();
    }
}
