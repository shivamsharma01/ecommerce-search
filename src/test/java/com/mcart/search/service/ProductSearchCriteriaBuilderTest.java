package com.mcart.search.service;

import com.mcart.search.dto.SearchFilters;
import com.mcart.search.dto.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.List;

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

    @Test
    void categoryAndBrandEachBecomeTopLevelChainEntries() {
        SearchFilters f = new SearchFilters();
        f.setCategories(List.of("Accessories"));
        f.setBrands(List.of("MCart Optics"));
        SearchRequest req = new SearchRequest("*", f);

        Criteria c = builder.buildCriteria(req);

        // If brand were nested under category, chain size would be 2 and brand would never hit OpenSearch.
        assertThat(c.getCriteriaChain()).hasSize(3);
    }
}
