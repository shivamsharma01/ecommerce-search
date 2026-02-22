package com.mcart.search.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Product result returned from search.
 * Maps to the Elasticsearch product document structure.
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
    private String imageUrl;
    private Double rating;
    private boolean inStock;
    private Map<String, Object> attributes;
}
