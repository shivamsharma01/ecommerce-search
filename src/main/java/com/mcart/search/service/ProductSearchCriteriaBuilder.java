package com.mcart.search.service;

import com.mcart.search.dto.SearchFilters;
import com.mcart.search.dto.SearchRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.Field;
import org.springframework.data.elasticsearch.core.query.SimpleField;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

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

        List<String> categories = trimmedNonEmpty(filters.getCategories());
        if (!categories.isEmpty()) {
            // Criteria.where(String) leaves FieldType unset; IN then uses query_string, which does not match our
            // keyword-only "categories" mapping. Mark Keyword so the client emits a terms query.
            Criteria cat = Criteria.where(keywordField("categories")).in(categories);
            combined = combined == null ? cat : combined.and(cat);
        }

        List<String> brands = trimmedNonEmpty(filters.getBrands());
        if (!brands.isEmpty()) {
            Criteria brand = Criteria.where(keywordField("brand")).in(brands);
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
            // Range on "rating" excludes documents with no rating field (typical until products have ratings indexed).
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

    private static Field keywordField(String name) {
        SimpleField f = new SimpleField(name);
        f.setFieldType(FieldType.Keyword);
        return f;
    }

    private static List<String> trimmedNonEmpty(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        return raw.stream()
                .flatMap(s -> s == null ? Stream.empty() : Stream.of(s.trim()))
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
