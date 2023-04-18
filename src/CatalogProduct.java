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
    public CatalogProduct(int id, String name, double recommendedPrice, ArrayList<Component> components) {
        this.id = id;
        this.name = name;
        this.recommendedPrice = recommendedPrice;
        this.components = components;

        // Add the newly created instance to the catalog
        catalog.add(this);
    }

    @JsonCreator
    public CatalogProduct(@JsonProperty("id") String id,
                          @JsonProperty("name") String name,
                          @JsonProperty("recommendedPrice") double price) {
        this.id = Integer.parseInt(id);
        this.name = name;
        this.recommendedPrice = recommendedPrice;
    }

    public static CatalogProduct getProductByName(String productName) {
        return catalog.stream().filter(product -> product.name == productName).findFirst().orElse(null);
    }

    public String getName() {
        return name;
    }

    public double getRecommendedPrice() {
        return recommendedPrice;
    }

    public List<Component> getComponents() {
        return this.components;
    }
}
