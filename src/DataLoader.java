import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


class CatalogProductLoader {

    public static ArrayList<CatalogProduct> loadCatalog(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        try {
            // Read JSON data from file
            CatalogProduct[] catalogProducts = mapper.readValue(new File(filePath), CatalogProduct[].class);

            // Add the catalog products to the CatalogProduct catalog
            for (CatalogProduct catalogProduct : catalogProducts) {
                CatalogProduct.catalog.add(catalogProduct);
            }

            /// Create components for the products
            for (CatalogProduct catalogProduct : CatalogProduct.catalog) {
                for (Component component : catalogProduct.components) {
                    CatalogProduct componentProduct = CatalogProduct.catalog.stream()
                            .filter(p -> p.id == component.id)
                            .findFirst()
                            .orElse(null);
                    if (componentProduct != null) {
                        Component componentToAdd = new Component(componentProduct, component.quantity);
                        catalogProduct.components.add(componentToAdd);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return CatalogProduct.catalog;
    }
}

class PlayerData implements Runnable {

    static ArrayList<PlayerData> playersData = new ArrayList<>();

    String name;
    PlayerType type;
    ArrayList<ActivityData> activities;

    PlayerData(String name, String type, ArrayList<ActivityData> activities){
        this.name = name;
        this.type = PlayerType.fromName(type);
        this.activities = activities;
    }

    public static ArrayList<PlayerData> loadFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        try {
            // Read JSON data from file
            PlayerData[] PlayersDataJson = mapper.readValue(new File(filePath), PlayerData[].class);
            // Add the playerData to the PlayersData from json
            for (PlayerData PlayerDataJson : PlayersDataJson) {
                PlayerData.playersData.add(PlayerDataJson);
                for(ActivityData activityDataJson: PlayerDataJson.activities){
                    PlayerDataJson.activities.add(activityDataJson);
                }
            }




        } catch (IOException e) {
            e.printStackTrace();
        }
        return PlayerData.playersData;
    }

    @Override
    public void run() {
        Thread thread = new Thread(new Player(this.name, this.type, activities ));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    String getName(){
        return this.name;
    }

    PlayerType getType(){
        return this.type;
    }

    ArrayList<ActivityData> getActivities(){
        return this.activities;
    }

}
class ActivityData {
    ActivityType type;
    CatalogProduct product;
    int minQuantity;
    int maxQuantity;

    ActivityData(String type, String product, int minQuantity, int maxQuantity ){
        this.type = ActivityType.fromName(type);
        this.product = CatalogProduct.getProductByName(product);
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }
}
