package com.mcart.search.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Product result returned from search.
 * Field names align with the Elasticsearch {@code products} document (same as product-indexer writes).
 */
@Data
@NoArgsConstructor
public class ProductSearchResult {

    private String id;
    private String name;
    private String description;
    private List<String> categories;
    private String brand;
    private Double price;
    private List<String> imageUrls;
    private Double rating;
    private boolean inStock;
    private Map<String, Object> attributes;
    private Long version;
    private Instant updatedAt;
}
