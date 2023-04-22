import java.util.concurrent.CountDownLatch;

public class Synchronizer {
    private static CountDownLatch allPlayersLoaded;
    private static final CountDownLatch gameStarted = new CountDownLatch(1);
    private static final CountDownLatch gameFinished = new CountDownLatch(1);
    private static CountDownLatch roundStarted = new CountDownLatch(1);
    private static CountDownLatch roundFinished = new CountDownLatch(1);
    private static CountDownLatch playersFinishedRound;
    private static CountDownLatch marketFinishedRound;

    public static void setGameStarted() {
        gameStarted.countDown();
    }

    public static void waitGameStart() throws InterruptedException {
        gameStarted.await();
    }

    public static boolean gameStarted() {
        return gameStarted.getCount() == 0;
    }

    public static void setGameFinished() {
        gameFinished.countDown();
    }

    public static boolean gameFinished() {
        return gameFinished.getCount() == 0;
    }


    public static void setRoundStarted(int numberOfPlayers) {
        // Prepare new round
        // Reset sync latches
        playersFinishedRound = new CountDownLatch(numberOfPlayers);
        marketFinishedRound = new CountDownLatch(1);
        roundFinished = new CountDownLatch(1);

        // Start the game in not started
        if (!gameStarted()) {
            setGameStarted();
        }

        // start the round
        roundStarted.countDown();
    }

    public static void waitRoundStarted() throws InterruptedException {
        roundStarted.await();
    }

    public static void setRoundFinished() throws InterruptedException {
        roundStarted = new CountDownLatch(1);
        roundFinished.countDown();
        Thread.sleep(10);
    }

    public static void waitRoundFinished() throws InterruptedException {
        roundFinished.await();
    }

    public static void waitForPlayers() throws InterruptedException {
        playersFinishedRound.await();
    }

    public static void waitForMarket() throws InterruptedException {
        marketFinishedRound.await();
    }

    public static void setMarketFinished() {
        marketFinishedRound.countDown();
    }

    public static void notifyPlayerLoaded() {
        allPlayersLoaded.countDown();
    }

    static void notifyPlayerFinishedRound() {
        playersFinishedRound.countDown();
    }

    public static void waitAllPlayersLoad() throws InterruptedException {
        allPlayersLoaded.await();
    }

    public static void setNumberOfPlayers(int size) {
        allPlayersLoaded = new CountDownLatch(size);
    }
}
