package com.mcart.search.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    @NotBlank(message = "Search term is required")
    private String searchTerm;

    @Valid
    private SearchFilters filters;

    @PositiveOrZero(message = "Page must be 0 or greater")
    private int page = 0;

    @Positive(message = "Size must be positive")
    @Max(value = 100, message = "Size must not exceed 100")
    private int size = 20;

    @Pattern(regexp = "(?i)relevance|price|rating|name", message = "sortBy must be one of: relevance, price, rating, name")
    private String sortBy = "relevance";

    @Pattern(regexp = "(?i)asc|desc", message = "sortOrder must be asc or desc")
    private String sortOrder = "desc";

    public SearchRequest(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public SearchRequest(String searchTerm, SearchFilters filters) {
        this.searchTerm = searchTerm;
        this.filters = filters;
    }
}
