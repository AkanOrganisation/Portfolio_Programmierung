package Catalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Component {
    int id;
    CatalogProduct product;
    int quantity;

    @JsonCreator
    Component(@JsonProperty("id") int id, @JsonProperty("quantity") int quantity) {
        this.id = id;
        this.product = CatalogProduct.getProductById(id);
        this.quantity = quantity;
    }

    public CatalogProduct getProduct() {
        return this.product;
    }

    public int getQuantity() {
        return this.quantity;
    }


}
