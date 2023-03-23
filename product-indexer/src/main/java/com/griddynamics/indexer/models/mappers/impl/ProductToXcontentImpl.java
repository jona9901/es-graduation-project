package com.griddynamics.indexer.models.mappers.impl;

import com.griddynamics.indexer.models.Product;
import com.griddynamics.indexer.models.Skus;
import com.griddynamics.indexer.models.mappers.ProductToXcontent;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ProductToXcontentImpl implements ProductToXcontent {
    @Override
    public XContentBuilder productToXcontentBuilder(Product product) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("brand")
                    .field("title", product.getBrand())
                    .field("shingles", "brand shingles")
                    .endObject()
                    .startObject("name")
                    .field("title", product.getName())
                    .field("shingles", product.getNameShingles())
                    .endObject()
                    .field("price", product.getPrice())
                    .startArray("skus");
            for (Skus sku : product.getSkus()) {
                builder.startObject()
                        .field("color", sku.getColor())
                        .field("size", sku.getSize())
                        .endObject();
            }
            return builder.endArray().endObject();
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            return null;
        }
    }
}
