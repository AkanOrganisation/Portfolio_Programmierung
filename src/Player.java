import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Player {

    private final static ArrayList<Player> players = new ArrayList<>();

    private static int nextId = 0;
    private final String name;
    private final int id;
    private Type type;
    private final Stock stock;
    private double money = Double.POSITIVE_INFINITY;
    public ArrayList<Activity> activities;

    public Player(String name, Type type, ArrayList<Activity.Data> activities) {
        this.id = getNextId();
        this.name = name;
        this.type = type;
        this.activities = activities.stream().map(activityData -> new Activity(this, activityData.getType(), activityData.getProduct(), activityData.getMinQuantity(), activityData.getMaxQuantity())).collect(Collectors.toCollection(ArrayList::new));
        this.stock = new Stock();

        //add a reference to self
        addPlayerToList(this);
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

    private synchronized static void addPlayerToList(Player player) {
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


    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Stock getStock() {
        return stock;
    }

    public double getMoney() {
        return money;
    }

    public void addMoney(double amount) {
        this.money += amount;
    }

    public void removeMoney(double amount) {
        this.money -= amount;
    }

    public static class Controller implements Runnable {

        private static final ArrayList<Controller> CONTROLLERS = new ArrayList<>();

        private final String name;
        private final Type type;
        private final ArrayList<Activity.Data> activities;

        @JsonCreator
        Controller(@JsonProperty("name") String name, @JsonProperty("type") String type, @JsonProperty("activities") ArrayList<Activity.Data> activities) {
            this.name = name;
            this.type = Type.fromName(type);
            this.activities = activities;

            CONTROLLERS.add(this);
        }

        // Read JSON data from file
        public static void loadFromJsonFile(String filePath) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.readValue(new File(filePath), Controller[].class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static int getNumberOfPlayers() {
            return CONTROLLERS.size();
        }

        public static ArrayList<Controller> getPlayersControllers() {
            return CONTROLLERS;
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
                player.log("Player %s left before the game started".formatted(this.name));
                throw new RuntimeException(e);
            }

            // Play the game until finished
            while (!Synchronizer.gameFinished()) {
                try {
                    // Wait for a new round
                    Synchronizer.waitRoundStarted();

                    // Play the round
                    player.log("Player %s starting a new round".formatted(this.name));
                    player.playRound();

                    // Mark turn as finished
                    Synchronizer.notifyPlayerFinishedRound();
                    player.log("Player %s finished the round".formatted(this.name));

                    // Wait for round's end
                    Synchronizer.waitRoundFinished();

                } catch (InterruptedException e) {
                    if (!Synchronizer.gameFinished()) {
                        player.log("Player %s left before the game finished".formatted(this.name));
                        removePlayer(player);
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        public String getName() {
            return name;
        }

    }

    public enum Type {
        MANUFACTURER, CONSUMER, SUPPLIER;

        public static Type fromName(String name) {
            for (Type type : Type.values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid player type name: " + name);
        }
    }
}

class Stock {
    private final Map<CatalogProduct, List<CatalogProduct.Product>> stock = new HashMap<>();

    public void removeProducts(CatalogProduct catalogProduct, int quantity) {
        if (stock.containsKey(catalogProduct)) {
            List<CatalogProduct.Product> products = stock.get(catalogProduct);
            quantity = Math.min(products.size(), quantity);
            if (quantity > 0) {
                products.subList(0, quantity).clear();
            }
        }
    }


    public void addProducts(CatalogProduct catalogProduct, int quantity) {
        List<CatalogProduct.Product> products = stock.computeIfAbsent(catalogProduct, k -> new ArrayList<>());
        for (int i = 0; i < quantity; i++) {
            CatalogProduct.Product product = new CatalogProduct.Product();
            products.add(product);
        }
    }

    public List<CatalogProduct.Product> getProducts(CatalogProduct catalogProduct) {
        return stock.getOrDefault(catalogProduct, new ArrayList<>());
    }

    public Map<CatalogProduct, Integer> getProductQuantities() {
        Map<CatalogProduct, Integer> productQuantities = new HashMap<>();
        for (CatalogProduct product : stock.keySet()) {
            int quantity = stock.get(product).size();
            productQuantities.merge(product, quantity, Integer::sum);
        }
        return productQuantities;
    }
}
