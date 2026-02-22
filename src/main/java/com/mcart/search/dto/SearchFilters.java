package com.mcart.search.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Filters to be applied to the search query.
 * Supports common e-commerce filter criteria.
 */
@Data
@NoArgsConstructor
public class SearchFilters {

    private List<String> categories;
    private List<String> brands;
    private Double minPrice;
    private Double maxPrice;
    private List<String> attributes;  // e.g., ["color:red", "size:M"]
    private Boolean inStock;
    private Double minRating;

    public boolean hasFilters() {
        return (categories != null && !categories.isEmpty())
                || (brands != null && !brands.isEmpty())
                || minPrice != null
                || maxPrice != null
                || (attributes != null && !attributes.isEmpty())
                || inStock != null
                || minRating != null;
    }
}
