import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Main {
    static int maxRounds;
    static String catalogFilePath = "./src/Catalog.json";
    static String playersFilePath = "./src/players.json";
    public static boolean finished;
    static int currentRound = 0;

    static ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        // Load catalog and players from file
        Arrays.stream(CatalogProductLoader.loadCatalog(catalogFilePath)).map(product -> CatalogProduct.catalog.add(product));
        Arrays.stream(PlayerData.loadFromFile(playersFilePath)).map(playersData -> PlayerData.playersData.add(playersData));
        System.out.println(PlayerData.playersData.size());


        // Start market thread
        Thread marketThread = new Thread(Market.getInstance());
        marketThread.start();
        threads.add(marketThread);

        // Start player threads
        for (PlayerData playerData : PlayerData.playersData) {
            Thread playerDataThread = new Thread(playerData);
            playerDataThread.start();
            threads.add(playerDataThread);
        }

        // Loop through rounds
        while (currentRound < maxRounds) {
            System.out.println("Round " + (currentRound + 1) + " started");

            // Notify all players that a new round has started
            final CountDownLatch roundLatch = new CountDownLatch(Player.players.size());
            for (Player player : Player.players) {
                player.setLatch(roundLatch);
            }

            // Wait for all players to finish their turn
            roundLatch.await();

            // clear the market
            Market.getInstance().clearOrders();

            // All players finished their turn, start a new round
            currentRound++;
        }

        // notify all that the game is finished
        finished = true;

        // Stop all threads
        for(Thread thread: threads) thread.join();
    }

}
