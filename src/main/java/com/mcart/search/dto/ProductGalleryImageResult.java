package com.mcart.search.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductGalleryImageResult {
    private String thumbnailUrl;
    private String hdUrl;
    private String alt;
}

