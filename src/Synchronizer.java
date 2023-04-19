import java.util.concurrent.CountDownLatch;

public class Synchronizer {
    static CountDownLatch allPlayersLoaded;
    static CountDownLatch gameStarted = new CountDownLatch(1);
    static CountDownLatch gameFinished = new CountDownLatch(1);
    static CountDownLatch newRound;
    static CountDownLatch allPlayersFinishedRound;

    public static boolean gameFinished() {
        return gameFinished.getCount() == 0;
    }

    public static void setGameFinished() {
        gameFinished.countDown();
    }

    public static boolean gameStarted() {
        return gameStarted.getCount() == 0;
    }

    public static void setGameStarted() {
        if (!(gameStarted.getCount() == 0))
            gameStarted.countDown();
    }

    public static void startNewRound(int numberOfPlayers) {
        if (!gameStarted()) setGameStarted();
        newRound = new CountDownLatch(1);
        allPlayersFinishedRound = new CountDownLatch(numberOfPlayers);
        newRound.countDown();
    }

    public static void finishTheRound() {
        newRound = new CountDownLatch(1);
    }
}
