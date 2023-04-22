import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


class CatalogProductData {

    // Read JSON data from file
    public static CatalogProduct[] loadFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(filePath), CatalogProduct[].class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CatalogProduct[0];
    }
}

class PlayerData implements Runnable {

    static ArrayList<PlayerData> playersData = new ArrayList<>();

    private final String name;
    private final PlayerType type;
    private final ArrayList<ActivityData> activities;

    @JsonCreator
    PlayerData(@JsonProperty("name") String name,
               @JsonProperty("type") String type,
               @JsonProperty("activities") ArrayList<ActivityData> activities){
        this.name = name;
        this.type = PlayerType.fromName(type);
        this.activities = activities;
    }

    // Read JSON data from file
    public static PlayerData[] loadFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        try {
            return mapper.readValue(new File(filePath), PlayerData[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new PlayerData[0];
    }

    public static int getNumberOfPlayers() {
        return playersData.size();
    }

    @Override
    public void run() {
        try {
            Thread thread = new Thread(new Player(this.name, this.type, activities ), "PlayerThread: %s".formatted(this.name));
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
