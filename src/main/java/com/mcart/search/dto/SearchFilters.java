package com.mcart.search.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Filters to be applied to the search query.
 * Supports common e-commerce filter criteria.
 */
@Data
@NoArgsConstructor
public class SearchFilters {

    private static final Pattern ATTRIBUTE_KEY = Pattern.compile("[a-zA-Z0-9_-]{1,64}");

    private List<String> categories;
    private List<String> brands;

    @DecimalMin(value = "0", message = "minPrice must be at least 0")
    private Double minPrice;

    @DecimalMin(value = "0", message = "maxPrice must be at least 0")
    private Double maxPrice;
    private List<String> attributes;  // e.g., ["color:red", "size:M"]
    private Boolean inStock;

    @DecimalMin(value = "0", message = "minRating must be at least 0")
    @DecimalMax(value = "5", message = "minRating must be at most 5")
    private Double minRating;

    @AssertTrue(message = "minPrice must be less than or equal to maxPrice when both are set")
    private boolean isPriceRangeConsistent() {
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice <= maxPrice;
    }

    @AssertTrue(message = "Each attribute must be 'key:value' with a non-empty value; key: letters, digits, underscore, hyphen (max 64 chars)")
    private boolean isAttributesWellFormed() {
        if (attributes == null || attributes.isEmpty()) {
            return true;
        }
        for (String attr : attributes) {
            if (attr == null || attr.isBlank()) {
                return false;
            }
            String[] parts = attr.split(":", 2);
            if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                return false;
            }
            if (!ATTRIBUTE_KEY.matcher(parts[0].trim()).matches()) {
                return false;
            }
        }
        return true;
    }

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
