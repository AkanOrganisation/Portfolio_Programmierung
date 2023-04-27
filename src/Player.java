import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**

 The Player class represents a player in a stock trading game.
 Each player has a unique ID, a name, a type, a stock portfolio, and a balance of money.
 */
public class Player {

    /**
     A list of all players in the game.
     */
    private final static ArrayList<Player> players = new ArrayList<>();

    /**
     The ID of the next player to be created.
     */
    private static int nextId = 0;
    /**
     The name of the player.
     */
    private final String name;
    /**
     The ID of the player.
     */
    private final int id;
    /**

     The type of the player.
     */
    private Type type;
    /**

     The stock portfolio of the player.
     */
    private final Stock stock;
    /**
     The balance of money of the player.
     */
    private double money = Double.POSITIVE_INFINITY;
    /**
     The list of activities of the player.
     */
    private final ArrayList<Activity> activities;


    private final History history;

    private double priceTolerance = 10/100.0;

    /**
     Constructs a player with the given name and type.
     @param name the name of the player
     @param type the type of the player
     */
    public Player(String name, Type type, ArrayList<Activity.Data> activities, double priceTolerance) {
        this.id = getNextId();
        this.name = name;
        this.type = type;
        this.activities = activities.stream().map(activityData -> new Activity(this, activityData.getType(), activityData.getProduct(), activityData.getMinQuantity(), activityData.getMaxQuantity())).collect(Collectors.toCollection(ArrayList::new));
        this.stock = new Stock();
        this.history = new History();
        this.priceTolerance = priceTolerance;

        //add a reference to self
        addPlayerToList(this);
    }

    /**
     Returns the number of active players currently in the game.
     @return the number of active players currently in the game
     */
    public static int getNumberOfActivePlayers() {
        return players.size();
    }

    /**

     Removes a player from the list of all players in the game.
     @param player the player to be removed
     */
    public static void removePlayer(Player player) {
        players.remove(player);
    }

    /**

     Returns the next available ID for a player and increments the ID counter.
     @return The next available ID for a player.
     */
    private synchronized int getNextId() {
        return nextId++;
    }

    /**
     Adds the given player to the list of active players.
     @param player The player to add to the list of active players.
     */
    private synchronized static void addPlayerToList(Player player) {
        players.add(player);
    }

    /**

     Executes a round of activities for the player.
     The activities are executed in the order of priority.
     @throws InterruptedException if the thread is interrupted while waiting
     */
    public void playRound() throws InterruptedException {
        prioritizeActivities();
        for (Activity activity : activities) activity.execute();
    }

    /**

     Defines the priority order for the activities of the player.
     The implementation is yet to be defined.
     */
    public void prioritizeActivities() {
        //TODO:
        // Implementation to be defined
    }

    /**

     Logs a message to the application log.
     @param message the message to be logged
     */
    public void log(String message) {
        Log.getInstance().addMessage(message);
    }

    /**

     Returns the name of the player.
     @return the name of the player
     */
    public String getName() {
        return name;
    }

    /**

     Returns the type of the player.
     @return the type of the player
     */
    public Type getType() {
        return type;
    }

    /**

     Returns the stock portfolio of the player.
     @return the stock portfolio of the player
     */
    public Stock getStock() {
        return stock;
    }

    /**

     Returns the balance of money of the player.
     @return the balance of money of the player
     */
    public double getMoney() {
        return money;
    }

    /**

     Adds the given amount of money to the player's account.
     @param amount the amount of money to be added
     */
    public void addMoney(double amount) {
        this.money += amount;
    }

    /**

     Removes the given amount of money from the player's account.
     @param amount the amount of money to be removed
     */
    public void removeMoney(double amount) {
        this.money -= amount;
    }

    public History getHistory() {
        return history;
    }

    public int getRound() {
        return Main.getRound();
    }


    public double getPriceTolerance() {
        return this.priceTolerance;
    }

    /**

     The Controller class represents a controller for a player.

     It defines the set of activities and their priorities for the player.
     */
    public static class Controller implements Runnable {

        private static final ArrayList<Controller> CONTROLLERS = new ArrayList<>();

        private final String name;
        private final Type type;
        private final ArrayList<Activity.Data> activities;
        private double priceTolerance;

        /**

         Constructs a new Controller object.
         @param name the name of the controller
         @param type the type of the controller
         @param activities the list of activities for the controller
         */
        @JsonCreator
        Controller(@JsonProperty("name") String name, @JsonProperty("type") String type, @JsonProperty("activities") ArrayList<Activity.Data> activities, @JsonProperty("priceTolerance") double priceTolerance) {
            this.name = name;
            this.type = Type.fromName(type);
            this.activities = activities;

            CONTROLLERS.add(this);
        }

        // Read JSON data from file
        /**

         Loads the list of controllers from a JSON file.
         @param filePath the path to the JSON file
         */
        // Read JSON data from file
        public static void loadFromJsonFile(String filePath) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.readValue(new File(filePath), Controller[].class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**

         Returns the number of loaded controllers.
         @return the number of loaded controllers
         */
        public static int getNumberOfPlayers() {
            return CONTROLLERS.size();
        }

        /**

         Returns the list of loaded controllers.
         @return the list of loaded controllers
         */
        public static ArrayList<Controller> getPlayersControllers() {
            return CONTROLLERS;
        }

        /**

         Returns the list of loaded controllers.
         @return the list of loaded controllers
         */
        @Override
        public void run() {
            Player player = new Player(this.name, this.type, this.activities, this.priceTolerance);

            // Notify the player is loaded
            Synchronizer.notifyPlayerLoaded();

            // Wait until the game starts
            try {
                Synchronizer.waitGameStart();
            } catch (InterruptedException e) {
                player.log("Player %s left before the game started".formatted(this.name));
                throw new RuntimeException(e);
            }

            /**

             Executes the game loop for the player, playing rounds until the game is finished.
             @param player the player participating in the game
             @throws RuntimeException if an interruption occurs during the waiting period for a new round and the game is not finished
             */
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

        /**

         Returns the name of the player.
         @return the name of the player
         */
        public String getName() {
            return name;
        }

    }

    /**

     The Type enum represents the possible types of players in the game.

     The available types are MANUFACTURER, CONSUMER and SUPPLIER.
     */
    public enum Type {
        MANUFACTURER, CONSUMER, SUPPLIER;

        /**

         Returns the Type instance that corresponds to the given type name.
         The name can be provided in any case, as the comparison is case-insensitive.
         @param name a String with the name of the type to be retrieved.
         @return the Type instance that corresponds to the given type name.
         @throws IllegalArgumentException if the given type name does not match any available type.
         */
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

/**

 The Stock class represents the stock of a player's products.
 */
class Stock {

    /**

     A map that associates each catalog product to a list of available products in stock.
     */
    private final Map<CatalogProduct, List<CatalogProduct.Product>> stock = new HashMap<>();

    /**

     Removes a given quantity of products of a specified catalog product from the stock.
     @param catalogProduct the catalog product to remove products from
     @param quantity the quantity of products to remove
     */
    public void removeProducts(CatalogProduct catalogProduct, int quantity) {
        if (stock.containsKey(catalogProduct)) {
            List<CatalogProduct.Product> products = stock.get(catalogProduct);
            quantity = Math.min(products.size(), quantity);
            if (quantity > 0) {
                products.subList(0, quantity).clear();
            }
        }
    }

    /**

     Adds a given quantity of a given product to the stock.
     If the product already exists in the stock, it adds the new products to the existing list of products.
     @param catalogProduct The product to add.
     @param quantity The quantity of the product to add.
     */
    public void addProducts(CatalogProduct catalogProduct, int quantity) {
        List<CatalogProduct.Product> products = stock.computeIfAbsent(catalogProduct, k -> new ArrayList<>());
        for (int i = 0; i < quantity; i++) {
            CatalogProduct.Product product = new CatalogProduct.Product();
            products.add(product);
        }
    }

    /**

     Returns the list of products in the stock for a given product.
     @param catalogProduct The product to get the list of products for.
     @return The list of products in the stock for the given product.
     */
    public List<CatalogProduct.Product> getProducts(CatalogProduct catalogProduct) {
        return stock.getOrDefault(catalogProduct, new ArrayList<>());
    }

    /**

     Returns a map of product quantities for all products in the stock.
     @return A map of product quantities for all products in the stock.
     */
    public Map<CatalogProduct, Integer> getProductQuantities() {
        Map<CatalogProduct, Integer> productQuantities = new HashMap<>();
        for (CatalogProduct product : stock.keySet()) {
            int quantity = stock.get(product).size();

            productQuantities.merge(product, quantity, Integer::sum);
        }
        return productQuantities;
    }
}
