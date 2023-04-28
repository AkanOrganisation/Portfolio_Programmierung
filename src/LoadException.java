public class LoadException extends Exception {
    public LoadException(Exception e) {
        super(e);
        e.printStackTrace();
        System.out.println("Error loading catalog from file");
    }
}
