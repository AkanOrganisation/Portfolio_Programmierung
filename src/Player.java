import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

class Player {

    private final static ArrayList<Player> players = new ArrayList<>();

    private static int nextId = 0;
    final String name;
    public int id;
    public PlayerType type;
    public Stock stock;
    public double money = Double.POSITIVE_INFINITY;
    public ArrayList<Activity> activities;
    public Player(String name, PlayerType type, ArrayList<ActivityData> activities) {
        this.id = getNextId();
        this.name = name;
        this.type = type;
        this.activities = activities.stream().map(activityData -> new Activity(this, activityData.type, activityData.product, activityData.minQuantity, activityData.maxQuantity)).collect(Collectors.toCollection(ArrayList::new));
        this.stock = new Stock();

        //add a reference to self
        addToList(this);
    }

    public static int getNumberOfActivePlayers() {
        return players.size();
    }

    public static void removePlayer(Player player) {
        players.remove(player);
    }


    private synchronized int getNextId() {
        return nextId++;
    }

    private synchronized static void addToList(Player player) {
        players.add(player);
    }

    public void playRound() throws InterruptedException {
        prioritizeActivities();
        for (Activity activity : activities) activity.execute();
    }

    public void prioritizeActivities() {
        //TODO:
        // Implementation to be defined
    }

    public void log(String message) {
        Log.getInstance().addMessage(message);
    }


}
class PlayerController implements Runnable {

    private static final ArrayList<PlayerController> playerControllers = new ArrayList<>();

    private final String name;
    private final PlayerType type;
    private final ArrayList<ActivityData> activities;

    @JsonCreator
    PlayerController(@JsonProperty("name") String name,
                     @JsonProperty("type") String type,
                     @JsonProperty("activities") ArrayList<ActivityData> activities) {
        this.name = name;
        this.type = PlayerType.fromName(type);
        this.activities = activities;

        playerControllers.add(this);
    }

    // Read JSON data from file
    public static void loadFromJsonFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(new File(filePath), PlayerController[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNumberOfPlayers() {
        return playerControllers.size();
    }

    public static ArrayList<PlayerController> getPlayersControllers() {
        return playerControllers;
    }


    @Override
    public void run() {
        Player player = new Player(this.name, this.type, activities);

        // Notify the player is loaded
        Synchronizer.notifyPlayerLoaded();

        // Wait until the game starts
        try {
            Synchronizer.waitGameStart();
        } catch (InterruptedException e) {
            player.log("PlayerController.Player %s left before the game started".formatted(this.name));
            throw new RuntimeException(e);
        }

        // Play the game until finished
        while (!Synchronizer.gameFinished()) {
            try {
                // Wait for a new round
                Synchronizer.waitRoundStarted();

                // Play the round
                player.log("PlayerController.Player %s starting a new round".formatted(this.name));
                player.playRound();

                // Mark turn as finished
                Synchronizer.notifyPlayerFinishedRound();
                player.log("PlayerController.Player %s finished the round".formatted(this.name));

                // Wait for round's end
                Synchronizer.waitRoundFinished();

            } catch (InterruptedException e) {
                if (!Synchronizer.gameFinished()) {
                    player.log("PlayerController.Player %s left before the game finished".formatted(this.name));
                    Player.removePlayer(player);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

}
class ActivityData {

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

}
