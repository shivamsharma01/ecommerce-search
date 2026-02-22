package com.mcart.search.repository;

import com.mcart.search.model.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch repository for Product documents.
 * Provides basic CRUD operations; complex search is handled by SearchService.
 */
@Repository
public interface ProductRepository extends ElasticsearchRepository<Product, String> {
}
