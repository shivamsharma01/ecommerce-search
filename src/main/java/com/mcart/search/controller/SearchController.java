package com.mcart.search.controller;

import com.mcart.search.dto.SearchRequest;
import com.mcart.search.dto.SearchResponse;
import com.mcart.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the e-commerce search feature.
 * Accepts search term and filters from the UI, delegates to Elasticsearch via SearchService.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Performs a product search with optional filters.
     *
     * @param request search request containing searchTerm (required) and filters (optional)
     * @return search results with pagination metadata
     */
    @PostMapping
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }
}
