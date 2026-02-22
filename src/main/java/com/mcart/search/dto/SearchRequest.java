package com.mcart.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for search operations from the e-commerce UI.
 * Contains the search term and optional filters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    @NotBlank(message = "Search term is required")
    private String searchTerm;

    private SearchFilters filters;

    @PositiveOrZero(message = "Page must be 0 or greater")
    private int page = 0;

    @Positive(message = "Size must be positive")
    private int size = 20;

    private String sortBy = "relevance";

    private String sortOrder = "desc";

    public SearchRequest(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public SearchRequest(String searchTerm, SearchFilters filters) {
        this.searchTerm = searchTerm;
        this.filters = filters;
    }
}
