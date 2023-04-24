import java.util.ArrayList;

public class Main {
    static int maxRounds = 5;
    static String catalogFilePath = "./src/data/catalog.json";
    static String playersFilePath = "./src/data/players.json";
    static int currentRound = 0;
    static ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        // Load catalog and players from file
        CatalogProduct.loadFromJsonFile(catalogFilePath);
        PlayerController.loadFromJsonFile(playersFilePath);

        // set CountDown to the number of players
        System.out.println("Numbers of players:" + PlayerController.getNumberOfPlayers());
        Synchronizer.setNumberOfPlayers(PlayerController.getNumberOfPlayers());

        // Start market thread
        Thread marketThread = new Thread(Market.getInstance(), "MarketThread");
        marketThread.start();
        threads.add(marketThread);

        // Start player threads
        for (PlayerController playerController : PlayerController.getPlayersControllers()) {
            Thread playerControllerThread = new Thread(playerController, "PlayerThread: " + playerController.getName());
            playerControllerThread.start();
            threads.add(playerControllerThread);
        }

        // Wait all players loaded
        System.out.println("Waiting for all players to load");
        Synchronizer.waitAllPlayersLoad();
        System.out.println("All players loaded");

        // Loop through rounds
        while (currentRound < maxRounds) {
            System.out.println("Round " + (currentRound + 1) + " started");

            // Notify all players that a new round has started
            Synchronizer.setRoundStarted(Player.getNumberOfActivePlayers());

            // Wait for all players to finish their turn
            Synchronizer.waitForPlayers();

            //Wait for Market to finish this round
            Synchronizer.waitForMarket();

            // Clear the market
            //Market.getInstance().clearOrders();

            // Print round's log
            Log.getInstance().printMessagesForRound(currentRound);

            // All players finished their turn, end the round
            Log.getInstance().setRound(++currentRound);
            Synchronizer.setRoundFinished();
        }

        // notify all that the game is finished
        System.out.println("Game finished");
        Synchronizer.setGameFinished();

        // Stop all threads
        for (Thread thread : threads) thread.interrupt();
        System.out.println("Game closed");
        System.exit(0);
    }

}
