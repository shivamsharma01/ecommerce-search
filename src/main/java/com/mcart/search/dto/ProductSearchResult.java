package com.mcart.search.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ProductSearchResult {

    private String id;
    private String name;
    private String description;
    private List<String> categories;
    private String brand;
    private Double price;
    private List<ProductGalleryImageResult> gallery;
    private Double rating;
    private boolean inStock;
    private Map<String, Object> attributes;
    private Long version;
    private Instant updatedAt;
}
