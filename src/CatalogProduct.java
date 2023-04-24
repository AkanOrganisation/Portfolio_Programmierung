import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CatalogProduct {
    // Static attribute to store all created instances
    private static final ArrayList<CatalogProduct> catalog = new ArrayList<>();
    private final int id;

    // Instance attributes
    private final String name;
    private double recommendedPrice;
    private final ArrayList<Component> components;

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

    public static void loadFromJsonFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(new File(filePath), CatalogProduct[].class);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static CatalogProduct getProductByName(String productName) {
        return catalog.stream().filter(product -> product.name.equals(productName.toLowerCase())).findFirst().orElse(null);
    }

    public static CatalogProduct getProductById(int id) {
        return catalog.stream().filter(product -> product.id == id).findFirst().orElse(null);
    }

    public static ArrayList<CatalogProduct> getCatalog() {
        return catalog;
    }


    public List<Component> getComponents() {
        return this.components;
    }

    public double getRecommendedPrice() {
        return recommendedPrice;
    }

    public String getName() {
        return name;
    }

    public static class Component {
        int id;
        CatalogProduct product;
        int quantity;

        @JsonCreator
        Component(@JsonProperty("id") int id, @JsonProperty("quantity") int quantity) {
            this.id = id;
            this.product = getProductById(id);
            this.quantity = quantity;
        }

        public CatalogProduct getProduct() {
            return this.product;
        }

        public int getQuantity() {
            return this.quantity;
        }


    }

    public static class Product {
        private static int nextId = 1;

        public int id;

        public Product() {
            this.id = nextId;
            nextId++;
        }
    }
}
