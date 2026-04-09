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
            // Each filter must be searchCriteria.and(criterion), not criterion.and(other): Spring's
            // CriteriaQueryProcessor only builds query fragments for the top-level criteriaChain entries.
            // Nesting (e.g. categories.and(brand)) leaves brand/rating/price on the inner chain — they are never
            // translated to the OpenSearch query, so filters appear ignored or wrongly OR-like.
            searchCriteria = appendFiltersAsSiblings(searchCriteria, filters);
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

    /**
     * AND each filter onto {@code base} as its own top-level chain entry so OpenSearch sees every clause.
     */
    private Criteria appendFiltersAsSiblings(Criteria base, SearchFilters filters) {
        Criteria sc = base;

        List<String> categories = trimmedNonEmpty(filters.getCategories());
        if (!categories.isEmpty()) {
            sc = sc.and(Criteria.where(keywordField("categories")).in(categories));
        }

        List<String> brands = trimmedNonEmpty(filters.getBrands());
        if (!brands.isEmpty()) {
            sc = sc.and(Criteria.where(keywordField("brand")).in(brands));
        }

        if (filters.getMinPrice() != null || filters.getMaxPrice() != null) {
            if (filters.getMinPrice() != null && filters.getMaxPrice() != null) {
                sc = sc.and(Criteria.where("price").between(filters.getMinPrice(), filters.getMaxPrice()));
            } else if (filters.getMinPrice() != null) {
                sc = sc.and(Criteria.where("price").greaterThanEqual(filters.getMinPrice()));
            } else {
                sc = sc.and(Criteria.where("price").lessThanEqual(filters.getMaxPrice()));
            }
        }

        if (filters.getInStock() != null) {
            sc = sc.and(Criteria.where("inStock").is(filters.getInStock()));
        }

        if (filters.getMinRating() != null) {
            sc = sc.and(Criteria.where("rating").greaterThanEqual(filters.getMinRating()));
        }

        if (filters.getAttributes() != null && !filters.getAttributes().isEmpty()) {
            for (String attr : filters.getAttributes()) {
                String[] parts = attr.split(":", 2);
                if (parts.length == 2) {
                    String key = "attributes." + parts[0].trim();
                    String value = parts[1].trim();
                    sc = sc.and(Criteria.where(key).is(value));
                }
            }
        }

        return sc;
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
