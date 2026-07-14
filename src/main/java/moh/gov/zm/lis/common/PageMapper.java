package moh.gov.zm.lis.common;

import java.util.List;
import java.util.function.Function;

/**
 * Builds a {@link PagedResponse} from already-mapped content and a total count.
 * Centralises the page-metadata arithmetic so every paged endpoint reports it
 * consistently.
 */
public final class PageMapper {
    private PageMapper() {
    }

    /**
     * Paginate an already-filtered, in-memory list (e.g. a cached snapshot),
     * mapping the requested slice to the response type.
     */
    public static <E, R> PagedResponse<R> ofList(List<E> filtered, int page, int size, Function<E, R> mapper) {
        int safeSize = Math.max(size, 0);
        int from = Math.min((long) page * safeSize > Integer.MAX_VALUE ? filtered.size() : page * safeSize, filtered.size());
        int to = Math.min(from + safeSize, filtered.size());
        List<R> content = filtered.subList(Math.max(from, 0), Math.max(to, 0)).stream().map(mapper).toList();
        return of(content, filtered.size(), page, safeSize);
    }

    public static <T> PagedResponse<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        return PagedResponse.<T>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .size(size)
                .hasNext(page + 1 < totalPages)
                .hasPrevious(page > 0)
                .build();
    }
}
