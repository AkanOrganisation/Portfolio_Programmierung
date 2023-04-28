public class LoadError extends Exception {
    public LoadError(Exception e) {
        super(e);
        e.printStackTrace();
        System.out.println("Error loading from file");
    }

    public LoadError(String s) {
        super(s);
        System.out.println(s);
    }
}
