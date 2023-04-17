import java.util.ArrayList;

public class Player {
    private static int nextId = 1;
    public int id;
    public PlayerType type;
    public Stock stock;
    public double money = Double.POSITIVE_INFINITY;
    public boolean roundFinished = false;
    public ArrayList<Activity> activities = new ArrayList<>();

    public Player(PlayerType type, Stock stock) {
        this.id = nextId;
        nextId++;
        this.type = type;
        this.stock = stock;
    }

    public void playRound() {
        // Implementation to be defined
    }

    public void prioritizeActivities() {
        // Implementation to be defined
    }

    public void log(String message) {
        // Implementation to be defined
    }
}
