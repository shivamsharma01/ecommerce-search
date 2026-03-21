package com.mcart.search.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for search operations.
 * Contains the search results and pagination metadata.
 */
@Data
@NoArgsConstructor
public class SearchResponse {

    private List<ProductSearchResult> results;
    private long totalHits;
    private int page;
    private int size;
    private int totalPages;

    public SearchResponse(List<ProductSearchResult> results, long totalHits, int page, int size) {
        this.results = results;
        this.totalHits = totalHits;
        this.page = page;
        this.size = size;
        this.totalPages = computeTotalPages(totalHits, size);
    }

    /**
     * Ceiling of {@code totalHits / size} without floating-point precision loss; caps at {@link Integer#MAX_VALUE}.
     */
    public static int computeTotalPages(long totalHits, int size) {
        if (size <= 0 || totalHits <= 0) {
            return 0;
        }
        long pages = (totalHits - 1L) / size + 1L;
        return pages > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) pages;
    }
}
