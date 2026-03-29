package com.mcart.search.service;

import com.mcart.search.dto.SearchFilters;
import com.mcart.search.dto.SearchRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.stereotype.Component;

/**
 * Builds Elasticsearch {@link Criteria} and {@link Sort} for product search from a {@link SearchRequest}.
 */
@Component
public class ProductSearchCriteriaBuilder {

    /**
     * The UI sends {@code *} when the user leaves the search box empty so {@link com.mcart.search.dto.SearchRequest}
     * stays {@code @NotBlank}. A text {@code match} on {@code *} does not behave like SQL wildcard and matches
     * nothing, which breaks filter-only searches (e.g. price range only).
     */
    public Criteria buildCriteria(SearchRequest request) {
        String term = request.getSearchTerm() != null ? request.getSearchTerm().trim() : "";

        Criteria searchCriteria;
        if (isMatchAllSearchTerm(term)) {
            searchCriteria = Criteria.where("name").exists();
        } else {
            searchCriteria = Criteria.where("name").matches(term)
                    .or(Criteria.where("description").matches(term));
        }

        SearchFilters filters = request.getFilters();
        if (filters != null && filters.hasFilters()) {
            Criteria filterCriteria = applyFilters(filters);
            if (filterCriteria != null) {
                searchCriteria = searchCriteria.and(filterCriteria);
            }
        }

        return searchCriteria;
    }

    /**
     * Maps API sort fields to index fields. {@code name} uses the {@code name.sort} keyword subfield.
     */
    public Sort buildSort(SearchRequest request) {
        String by = request.getSortBy() != null ? request.getSortBy().trim().toLowerCase() : "relevance";
        Sort.Direction direction = "asc".equalsIgnoreCase(trimToEmpty(request.getSortOrder()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return switch (by) {
            case "relevance" -> Sort.by(direction, "_score");
            case "price" -> Sort.by(direction, "price");
            case "rating" -> Sort.by(direction, "rating");
            case "name" -> Sort.by(direction, "name.sort");
            default -> throw new IllegalStateException("Unsupported sortBy (validation should prevent this): " + by);
        };
    }

    private static String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private Criteria applyFilters(SearchFilters filters) {
        Criteria combined = null;

        if (filters.getCategories() != null && !filters.getCategories().isEmpty()) {
            Criteria cat = Criteria.where("categories").in(filters.getCategories());
            combined = combined == null ? cat : combined.and(cat);
        }

        if (filters.getBrands() != null && !filters.getBrands().isEmpty()) {
            Criteria brand = Criteria.where("brand").in(filters.getBrands());
            combined = combined == null ? brand : combined.and(brand);
        }

        if (filters.getMinPrice() != null || filters.getMaxPrice() != null) {
            Criteria price;
            if (filters.getMinPrice() != null && filters.getMaxPrice() != null) {
                price = Criteria.where("price").between(filters.getMinPrice(), filters.getMaxPrice());
            } else if (filters.getMinPrice() != null) {
                price = Criteria.where("price").greaterThanEqual(filters.getMinPrice());
            } else {
                price = Criteria.where("price").lessThanEqual(filters.getMaxPrice());
            }
            combined = combined == null ? price : combined.and(price);
        }

        if (filters.getInStock() != null) {
            Criteria stock = Criteria.where("inStock").is(filters.getInStock());
            combined = combined == null ? stock : combined.and(stock);
        }

        if (filters.getMinRating() != null) {
            Criteria rating = Criteria.where("rating").greaterThanEqual(filters.getMinRating());
            combined = combined == null ? rating : combined.and(rating);
        }

        if (filters.getAttributes() != null && !filters.getAttributes().isEmpty()) {
            for (String attr : filters.getAttributes()) {
                String[] parts = attr.split(":", 2);
                if (parts.length == 2) {
                    String key = "attributes." + parts[0].trim();
                    String value = parts[1].trim();
                    Criteria attrCriteria = Criteria.where(key).is(value);
                    combined = combined == null ? attrCriteria : combined.and(attrCriteria);
                }
            }
        }

        return combined;
    }

    private static boolean isMatchAllSearchTerm(String term) {
        return term.isEmpty() || "*".equals(term);
    }
}
