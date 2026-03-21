package com.mcart.search.service;

import com.mcart.search.dto.ProductSearchResult;
import com.mcart.search.dto.SearchRequest;
import com.mcart.search.dto.SearchResponse;
import com.mcart.search.mapper.ProductMapper;
import com.mcart.search.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Runs product search against Elasticsearch and maps hits to API DTOs.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductMapper productMapper;
    private final ProductSearchCriteriaBuilder criteriaBuilder;

    public SearchResponse search(SearchRequest request) {
        CriteriaQuery query = new CriteriaQuery(criteriaBuilder.buildCriteria(request));
        query.setPageable(PageRequest.of(request.getPage(), request.getSize()));
        query.addSort(criteriaBuilder.buildSort(request));

        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);

        List<ProductSearchResult> results = searchHits.getSearchHits().stream()
                .map(hit -> productMapper.toProductSearchResult(hit.getContent()))
                .toList();

        return new SearchResponse(
                results,
                searchHits.getTotalHits(),
                request.getPage(),
                request.getSize()
        );
    }
}
