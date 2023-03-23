package com.griddynamics.indexer.models.mappers;

import com.griddynamics.indexer.models.Product;
import org.elasticsearch.common.xcontent.XContentBuilder;

public interface ProductToXcontent {
    public XContentBuilder productToXcontentBuilder(Product product);
}
