import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Main {
    static int maxRounds = 200;
    static String catalogFilePath = "./src/Catalog.json";
    static String playersFilePath = "./src/players.json";
    static int currentRound = 0;
    static ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        // Load catalog and players from file
        CatalogProduct.catalog.addAll(Arrays.asList(CatalogProductData.loadFromFile(catalogFilePath)));
        PlayerData.playersData.addAll(Arrays.asList(PlayerData.loadFromFile(playersFilePath)));

        // set CountDown to the number of players
        System.out.println("Numbers of players:" + PlayerData.playersData.size());
        Synchronizer.allPlayersLoaded = new CountDownLatch(PlayerData.playersData.size());

        // Start market thread
        Thread marketThread = new Thread(Market.getInstance(), "MarketThread");
        marketThread.start();
        threads.add(marketThread);

        // Start player threads
        for (PlayerData playerData : PlayerData.playersData) {
            Thread playerDataThread = new Thread(playerData);
            playerDataThread.start();
            threads.add(playerDataThread);
        }

        // Wait all players loaded
        System.out.println("Waiting for all players to load");
        Synchronizer.allPlayersLoaded.await();
        System.out.println("All players loaded");

        // Loop through rounds
        while (currentRound < maxRounds) {
            System.out.println("Round " + (currentRound + 1) + " started");

            // Notify all players that a new round has started
            Synchronizer.setRoundStarted(Player.players.size());

            // Wait for all players to finish their turn
            Synchronizer.waitForPlayers();

            //Wait for Market to finish this round
            Synchronizer.waitForMarket();

            // Clear the market
            //Market.getInstance().clearOrders();

            // Print round's log
            Log.getInstance().printMessagesForRound(currentRound);

            // All players finished their turn, end the round
            Log.getInstance().setRound(currentRound++);
            Synchronizer.setRoundFinished();
        }

        // notify all that the game is finished
        System.out.println("game Finished");
        Synchronizer.setGameFinished();

        // Stop all threads
        for(Thread thread: threads) thread.join();
        System.out.println("finished");
    }

}
