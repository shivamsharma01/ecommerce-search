package com.mcart.search.service;

import com.mcart.search.dto.ProductSearchResult;
import com.mcart.search.dto.SearchRequest;
import com.mcart.search.dto.SearchResponse;
import com.mcart.search.mapper.ProductMapper;
import com.mcart.search.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

        long total = searchHits.getTotalHits();
        log.debug("Search page={} size={} totalHits={} returned={}", request.getPage(), request.getSize(), total, results.size());
        return new SearchResponse(
                results,
                total,
                request.getPage(),
                request.getSize()
        );
    }
}
