package Player.Activity;

import Catalog.CatalogProduct;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivityData {

    ActivityType type;
    CatalogProduct product;
    int minQuantity;
    int maxQuantity;

    @JsonCreator
    ActivityData(@JsonProperty("type") String type,
                 @JsonProperty("product") String product,
                 @JsonProperty("min") int minQuantity,
                 @JsonProperty("max") int maxQuantity) {
        this.type = ActivityType.fromName(type);
        this.product = CatalogProduct.getProductByName(product);
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }

    public ActivityType getType() {
        return type;
    }

    public CatalogProduct getProduct() {
        return product;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }
}
