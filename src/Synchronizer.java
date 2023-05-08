import java.util.concurrent.CountDownLatch;

/**
 * The Synchronizer class represents a synchronization mechanism for a multiplayer game.
 * It provides various CountDownLatch objects to synchronize the loading of players, the start of the game, the end of the game,
 * the start and end of each round, and to synchronize the closing of players and the market at the end of each round.
 */
public class Synchronizer {

    /**
     * The CountDownLatch object used to synchronize the loading of all players.
     */
    private static CountDownLatch allPlayersLoaded;

    /**
     * The CountDownLatch object used to synchronize the start of the game.
     */
    private static final CountDownLatch gameStarted = new CountDownLatch(1);

    /**
     * The CountDownLatch object used to synchronize the end of the game.
     */
    private static final CountDownLatch gameFinished = new CountDownLatch(1);

    /**
     * The CountDownLatch object used to synchronize the start of each round.
     */
    private static CountDownLatch roundStarted = new CountDownLatch(1);

    /**
     * The CountDownLatch object used to synchronize the end of each round.
     */
    private static CountDownLatch roundFinished = new CountDownLatch(1);

    /**
     * The CountDownLatch object used to synchronize the completion of all players at the end of each round.
     */
    private static CountDownLatch playersFinishedRound;

    /**
     * The CountDownLatch object used to synchronize the closing of the market at the end of each round.
     */
    private static CountDownLatch marketFinishedRound;

    /**
     * Sets the game as started.
     */
    public static void setGameStarted() {
        gameStarted.countDown();
    }

    /**
     * Blocked until the game is started.
     *
     * @throws InterruptedException when waiting is interrupted.
     */
    public static void waitGameStart() throws InterruptedException {
        gameStarted.await();
    }

    /**
     * Checks if the game has been started.
     *
     * @return true if the game was started, false otherwise.
     */
    public static boolean gameStarted() {
        return gameStarted.getCount() == 0;
    }

    /**
     * Sets the game as finished.
     */
    public static void setGameFinished() {
        gameFinished.countDown();
    }

    /**
     * Checks if the game was terminated.
     *
     * @return true if the game has ended; false otherwise.
     */
    public static boolean gameFinished() {
        return gameFinished.getCount() == 0;
    }

    /**
     * Sets the start of a new round and synchronizes the synchronization mechanism for this round.
     *
     * @param numberOfPlayers the number of players in this round.
     */
    public static void setRoundStarted(int numberOfPlayers) {
        /** Prepare new round*/
        /** Reset sync latches */
        playersFinishedRound = new CountDownLatch(numberOfPlayers);
        marketFinishedRound = new CountDownLatch(1);
        roundFinished = new CountDownLatch(1);

        /** Start the game in not started*/
        if (!gameStarted()) {
            setGameStarted();
        }

        /** start the round*/
        roundStarted.countDown();
    }

    /**
     * Blocks the current thread until the round is started.
     *
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public static void waitRoundStarted() throws InterruptedException {
        roundStarted.await();
    }

    /**
     * Signals the end of the current round and synchronizes the synchronization mechanism for the next round.
     *
     * @throws InterruptedException if the thread is interrupted during execution.
     */
    public static void setRoundFinished() throws InterruptedException {
        roundStarted = new CountDownLatch(1);
        roundFinished.countDown();
        Thread.sleep(10);
    }

    /**
     * Blocks the current thread until the current round is finished.
     *
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public static void waitRoundFinished() throws InterruptedException {
        roundFinished.await();
    }

    /**
     * Blocks the current thread until all players have completed the current round.
     *
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public static void waitForPlayers() throws InterruptedException {
        playersFinishedRound.await();
    }

    /**
     * Blocks the current thread until the market completes the current round.
     *
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public static void waitForMarket() throws InterruptedException {
        marketFinishedRound.await();
    }

    /**
     * Signals that the market has completed the current round.
     */
    public static void setMarketFinished() {
        marketFinishedRound.countDown();
    }

    /**
     * Decrements the count of the playersFinishedRound latch, indicating that a player has finished the current round.
     */
    public static void notifyPlayerLoaded() {
        allPlayersLoaded.countDown();
    }

    /**
     * Waits until all players have finished loading.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static void notifyPlayerFinishedRound() {
        playersFinishedRound.countDown();
    }

    /**
     * Waits until all players have finished loading.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public static void waitAllPlayersLoad() throws InterruptedException {
        allPlayersLoaded.await();
    }

    /**
     * Sets the number of players and initializes the allPlayersLoaded latch with the given count.
     *
     * @param size the number of players
     */
    public static void setNumberOfPlayers(int size) {
        allPlayersLoaded = new CountDownLatch(size);
    }
}
