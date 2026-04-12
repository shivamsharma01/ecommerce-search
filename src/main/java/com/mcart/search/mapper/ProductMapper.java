package com.mcart.search.mapper;

import com.mcart.search.dto.ProductSearchResult;
import com.mcart.search.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    ProductSearchResult toProductSearchResult(Product product);
}
