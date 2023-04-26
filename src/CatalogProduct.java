import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * The CatalogProduct class represents a product in a catalog. It contains
 * information about the product's name, recommended price, and components that
 * make up the product. It also provides static methods to retrieve products by
 * name or ID.
 */
public class CatalogProduct {
    /**
     * Static attribute to store all created instances.
     */
    public static ArrayList<CatalogProduct> catalog = new ArrayList<>();
    /**
     * The ID of the product.
     */
    public int id;
    /**
     * The name of the product.
     */
    public String name;
    /**
     * The recommended price of the product.
     */
    public double recommendedPrice;
    /**
     * The list of components that make up the product.
     */
    private final ArrayList<Component> components;

    /**
     * Constructor for CatalogProduct class.
     *
     * @param id               the ID of the product
     * @param name             the name of the product
     * @param recommendedPrice the recommended price of the product
     * @param components       the list of components that make up the product
     */
    @JsonCreator
    public CatalogProduct(@JsonProperty("id") int id, @JsonProperty("name") String name,
                          @JsonProperty("recommendedPrice") double recommendedPrice,
                          @JsonProperty("components") ArrayList<Component> components) {
        this.id = id;
        this.name = name.toLowerCase();
        this.recommendedPrice = recommendedPrice;
        this.components = components;

        // Add the newly created instance to the catalog
        catalog.add(this);
    }

    /**
     * Loads CatalogProduct instances from a JSON file.
     *
     * @param filePath the path of the JSON file
     */
    public static void loadFromJsonFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(new File(filePath), CatalogProduct[].class);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a CatalogProduct instance by its name.
     *
     * @param productName the name of the product to retrieve
     * @return The CatalogProduct instance with the specified name, or null if no
     *         such product exists.
     */
    public static CatalogProduct getProductByName(String productName) {
        return catalog.stream().filter(product -> product.name.equals(productName.toLowerCase())).findFirst()
                .orElse(null);
    }

    /**
     * Retrieves a CatalogProduct instance by its ID.
     *
     * @param id the ID of the product to retrieve
     * @return The CatalogProduct instance with the specified ID, or null if no such
     *         product exists.
     */
    public static CatalogProduct getProductById(int id) {
        return catalog.stream().filter(product -> product.id == id).findFirst().orElse(null);
    }

    /**
     * Retrieves the list of components that make up the product.
     *
     * @return The list of components that make up the product.
     */
    public static ArrayList<CatalogProduct> getCatalog() {
        return catalog;
    }

    /**
     * Retrieves the list of components that make up the product.
     *
     * @return The list of components that make up the product.
     */

    public List<Component> getComponents() {
        return this.components;
    }

    /**
     * Retrieves the recommended price of the product.
     *
     * @return The recommended price of the product.
     */
    public double getRecommendedPrice() {
        return recommendedPrice;
    }

    /**
     * Retrieves the name of the product.
     *
     * @return The name of the product.
     */
    public String getName() {
        return name;
    }

    /**
     * The Component class represents a component of a product, including its ID,
     * quantity, and associated CatalogProduct instance.
     */
    public static class Component {
        int id;
        CatalogProduct product;
        int quantity;

        /**
         * Constructor for Component class.
         *
         * @param id       the ID of the product
         * @param product  - select product of the assigned id
         * @param quantity of the selected products
         */
        @JsonCreator
        Component(@JsonProperty("id") int id, @JsonProperty("quantity") int quantity) {
            this.id = id;
            this.product = getProductById(id);
            this.quantity = quantity;
        }

        /**
         * Retrieves the the product of the catalog.
         *
         * @return The product.
         */
        public CatalogProduct getProduct() {
            return this.product;
        }

        /**
         * Retrieves the the quantity of the product.
         *
         * @return The quantity of the product.
         */
        public int getQuantity() {
            return this.quantity;
        }

    }

    /**
     * The Product class sets the id of the products.
     */
    public static class Product {
        private static int nextId = 1;

        public int id;

        /**
         * Constructor for Product class.
         *
         * @param id     the ID of the product
         * @param nextid - gets value of the id and increases the value
         */
        public Product() {
            this.id = nextId;
            nextId++;
        }
    }
}
