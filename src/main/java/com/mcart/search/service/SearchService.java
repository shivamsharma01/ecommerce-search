package com.mcart.search.service;

import com.mcart.search.dto.ProductSearchResult;
import com.mcart.search.dto.SearchFilters;
import com.mcart.search.dto.SearchRequest;
import com.mcart.search.dto.SearchResponse;
import com.mcart.search.mapper.ProductMapper;
import com.mcart.search.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that handles search operations by delegating to Elasticsearch.
 * Builds queries from search term and filters, then maps results to DTOs.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductMapper productMapper;

    /**
     * Performs a search with the given search term and optional filters.
     *
     * @param request the search request containing term, filters, and pagination
     * @return search response with results and metadata
     */
    public SearchResponse search(SearchRequest request) {
        Criteria criteria = buildSearchCriteria(request);
        Query query = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(request.getPage(), request.getSize()));

        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);

        List<ProductSearchResult> results = searchHits.getSearchHits().stream()
                .map(hit -> productMapper.toProductSearchResult(hit.getContent()))
                .collect(Collectors.toList());

        return new SearchResponse(
                results,
                searchHits.getTotalHits(),
                request.getPage(),
                request.getSize()
        );
    }

    private Criteria buildSearchCriteria(SearchRequest request) {
        // Main search: match name OR description (text search)
        Criteria searchCriteria = Criteria.where("name").matches(request.getSearchTerm())
                .or(Criteria.where("description").matches(request.getSearchTerm()));

        // Apply filters
        SearchFilters filters = request.getFilters();
        if (filters != null && filters.hasFilters()) {
            Criteria filterCriteria = applyFilters(filters);
            if (filterCriteria != null) {
                searchCriteria = searchCriteria.and(filterCriteria);
            }
        }

        return searchCriteria;
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
                price = Criteria.where("price")
                        .between(filters.getMinPrice(), filters.getMaxPrice());
            } else if (filters.getMinPrice() != null) {
                price = Criteria.where("price").greaterThanEqual(filters.getMinPrice());
            } else {
                price = Criteria.where("price").lessThanEqual(filters.getMaxPrice());
            }
            combined = combined == null ? price : combined.and(price);
        }

        if (filters.getInStock() != null && filters.getInStock()) {
            Criteria stock = Criteria.where("inStock").is(true);
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
}
