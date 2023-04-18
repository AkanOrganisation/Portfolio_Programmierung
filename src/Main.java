import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Main {
    static int maxRounds;
    static String catalogFilePath;
    static String playersFilePath;
    public static boolean finished;
    static int currentRound = 0;

    static ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        // Load catalog and players from file
        CatalogProduct.catalog =  CatalogProductLoader.loadCatalog(catalogFilePath);
        ArrayList<PlayerData> playersData = PlayerData.loadFromFile(playersFilePath);

        // Start market thread
        Thread marketThread = new Thread(Market.getInstance());
        marketThread.start();
        threads.add(marketThread);

        // Start player threads
        for (PlayerData playerData : playersData) {
            Thread playerThread = new Thread(new Player(playerData.getName(), playerData.getType(), playerData.getActivities()));
            playerThread.start();
            threads.add(playerThread);
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
