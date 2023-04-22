import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CatalogProduct {
    // Static attribute to store all created instances
    public static ArrayList<CatalogProduct> catalog = new ArrayList<CatalogProduct>();
    public int id;

    // Instance attributes
    public String name;
    public double recommendedPrice;
    public ArrayList<Component> components;

    /**
     * Constructor for CatalogProduct class.
     *
     * @param name the name of the product
     * @param recommendedPrice the recommended price of the product
     * @param components the list of components that make up the product
     */
    @JsonCreator
    public CatalogProduct(@JsonProperty("id") int id,
                          @JsonProperty("name") String name,
                          @JsonProperty("recommendedPrice") double recommendedPrice,
                          @JsonProperty("components") ArrayList<Component> components) {
        this.id = id;
        this.name = name.toLowerCase();
        this.recommendedPrice = recommendedPrice;
        this.components = components;

        // Add the newly created instance to the catalog
        catalog.add(this);
    }


    public static CatalogProduct getProductByName(String productName) {
        return catalog.stream().filter(product -> product.name.equals(productName.toLowerCase())).findFirst().orElse(null);
    }

    public static CatalogProduct getProductById(int id) {
        return catalog.stream().filter(product -> product.id == id).findFirst().orElse(null);
    }

    public List<Component> getComponents() {
        return this.components;
    }
}
