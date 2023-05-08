import java.util.ArrayList;

/**
 * The Main class represents the entry point of the program. It initializes the
 * game by loading the catalog and players from JSON files, starting the market
 * thread, and creating player threads. The class also manages the game's rounds
 * by notifying all players that a new round has started, waiting for them to
 * finish their turn, and waiting for the market to finish the round. The class
 * uses the Synchronizer class to coordinate the players' turns and the market's
 * activity, and the Log class to print the round's log. The Main class also
 * manages the game's termination by stopping all threads and exiting the
 * program. This program uses the ArrayList and Thread classes from the
 * java.util package, the CatalogProduct and Player classes, the Market and Log
 * singleton instances, and the Synchronizer class.
 *
 * @see CatalogProduct
 * @see Player
 * @see Market
 * @see Log
 * @see Synchronizer
 */

public class Main {
    /**
     * The maximum number of rounds for the game.
     */
    static int maxRounds = 200;
    /**
     * The file path of the catalog JSON file.
     */
    static String catalogFilePath = "./src/data/catalog.json";
    /**
     * The file path of the players JSON file.
     */
    static String playersFilePath = "./src/data/players.json";
    /**
     * The current round number.
     */
    static int currentRound = 0;
    /**
     * The list of all threads in the program.
     */
    static ArrayList<Thread> threads = new ArrayList<>();

    /**
     * The main method of the program. It initializes the game by loading the
     * catalog and players from JSON files, starting the market thread, and creating
     * player threads. It manages the game's rounds by notifying all players that a
     * new round has started, waiting for them to finish their turn, and waiting for
     * the market to finish the round. It also manages the game's termination by
     * stopping all threads and exiting the program.
     *
     * @param args the command line arguments
     * @throws InterruptedException if any thread is interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        /**
         * Load catalog and players from file
         */
        try {
            CatalogProduct.loadFromJsonFile(catalogFilePath);
        } catch (LoadError e) {
            System.exit(1);
        }
        try {
            Player.Controller.loadFromJsonFile(playersFilePath);
        } catch (LoadError e) {
            System.exit(1);
        }


        /**
         * Set CountDown to the number of players
         */
        System.out.println("Numbers of players:" + Player.Controller.getNumberOfPlayers());
        Synchronizer.setNumberOfPlayers(Player.Controller.getNumberOfPlayers());

        /**
         * Starts the market thread
         */
        Thread marketThread = new Thread(Market.getInstance(), "MarketThread");
        marketThread.start();
        threads.add(marketThread);

        /**
         * Starts the player thread
         */
        for (Player.Controller playerController : Player.Controller.getPlayersControllers()) {
            Thread playerControllerThread = new Thread(playerController, "PlayerThread: " + playerController.getName());
            playerControllerThread.start();
            threads.add(playerControllerThread);
        }

        /**
         * Will wait that all players are loaded and give out as message
         */
        System.out.println("Waiting for all players to load");
        Synchronizer.waitAllPlayersLoad();
        System.out.println("All players loaded");

        /**
         * Loop through rounds
         */
        while (currentRound < maxRounds) {
            System.out.println("Round " + (currentRound + 1) + " started");

            /**
             * Notify all players that a new round has started
             */
            Synchronizer.setRoundStarted(Player.getNumberOfActivePlayers());

            /**
             * Wait for all players to finish their turn
             */
            Synchronizer.waitForPlayers();

            /**
             * Wait for Market to finish this round
             */
            Synchronizer.waitForMarket();

            //TODO: ????
            /*
             * Clear the market
             */
            //Market.getInstance().clearOrders();

            /**
             * Print round's log
             */
            Log.getInstance().printMessagesForRound(currentRound);

            /**
             * All players finished their turn, end set the round as finished
             */
            Log.getInstance().setRound(++currentRound);
            Synchronizer.setRoundFinished();
        }

        /**
         * Notify all that the game is finished
         */
        System.out.println("Game finished");
        Synchronizer.setGameFinished();

        /**
         * Will Stop all threads and gives out that the game is closed for now
         */
        for (Thread thread : threads)
            thread.interrupt();
        System.out.println("Game closed");
        System.exit(0);
    }

    public static int getRound() {
        return currentRound;
    }
}
