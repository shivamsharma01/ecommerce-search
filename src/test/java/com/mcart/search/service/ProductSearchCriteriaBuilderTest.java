package com.mcart.search.service;

import com.mcart.search.dto.SearchFilters;
import com.mcart.search.dto.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.query.Criteria;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSearchCriteriaBuilderTest {

    private final ProductSearchCriteriaBuilder builder = new ProductSearchCriteriaBuilder();

    @Test
    void starSearchTermWithPriceFilterDoesNotUseTextMatchOnStar() {
        SearchFilters filters = new SearchFilters();
        filters.setMinPrice(1000.0);
        filters.setMaxPrice(1000.0);
        SearchRequest req = new SearchRequest("*", filters);

        Criteria criteria = builder.buildCriteria(req);

        assertThat(criteria.toString()).doesNotContain("*");
    }

    @Test
    void concreteSearchTermStillMatchesNameOrDescription() {
        SearchRequest req = new SearchRequest("kurta");
        Criteria criteria = builder.buildCriteria(req);
        assertThat(criteria.toString()).containsIgnoringCase("kurta");
    }
}
