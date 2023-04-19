import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


class CatalogProductData {

    public static CatalogProduct[] loadFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        try {
            // Read JSON data from file
            CatalogProduct[] catalogProducts = mapper.readValue(new File(filePath), CatalogProduct[].class);
            return catalogProducts;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CatalogProduct[0];
    }
}

class PlayerData implements Runnable {

    static ArrayList<PlayerData> playersData = new ArrayList<>();

    String name;
    PlayerType type;
    ArrayList<ActivityData> activities;

    @JsonCreator
    PlayerData(@JsonProperty("name") String name,
               @JsonProperty("type") String type,
               @JsonProperty("activities") ArrayList<ActivityData> activities){
        this.name = name;
        this.type = PlayerType.fromName(type);
        this.activities = activities;
    }

    public static PlayerData[] loadFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        try {
            // Read JSON data from file
            PlayerData[] playersData = mapper.readValue(new File(filePath), PlayerData[].class);
            return playersData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new PlayerData[0];
    }

    @Override
    public void run() {
        try {
            Thread thread = new Thread(new Player(this.name, this.type, activities ));
            thread.start();
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

    @JsonCreator
    ActivityData(@JsonProperty("type")String type,
                 @JsonProperty("product") String product,
                 @JsonProperty("min") int minQuantity,
                 @JsonProperty("max") int maxQuantity ){
        this.type = ActivityType.fromName(type);
        this.product = CatalogProduct.getProductByName(product);
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }
}
