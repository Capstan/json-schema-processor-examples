package com.github.fge.jjschema;

import com.github.reinert.jjschema.SchemaProperty;

import java.math.BigDecimal;
import java.util.List;

@SchemaProperty(title="Product", description="A product from Acme's catalog")
public class Product {

    @SchemaProperty(required=true, description="The unique identifier for a product")
    private long id;
    @SchemaProperty(required=true, description="Name of the product")
    private String name;
    @SchemaProperty(required=true, minimum=0, exclusiveMinimum=true)
    private BigDecimal price;
    @SchemaProperty(minItems=1,uniqueItems=true)
    private List<String> tags;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
