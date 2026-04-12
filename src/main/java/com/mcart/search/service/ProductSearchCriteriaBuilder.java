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

@Component
public class ProductSearchCriteriaBuilder {

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
            searchCriteria = appendFiltersAsSiblings(searchCriteria, filters);
        }

        return searchCriteria;
    }

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
