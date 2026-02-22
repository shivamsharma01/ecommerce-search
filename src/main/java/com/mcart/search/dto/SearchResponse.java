package com.mcart.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for search operations.
 * Contains the search results and pagination metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalHits / size) : 0;
    }
}
