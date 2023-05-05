/**
 * The LoadError class is an exception used to indicate that an error occurred while loading data from a file.
 */
public class LoadError extends Exception {

    /**
     * Constructs a new LoadError object with the specified cause and prints the stack trace.
     *
     * @param e the cause of the error
     */
    public LoadError(Exception e) {
        super(e);
        e.printStackTrace();
        System.out.println("Error loading from file");
    }

    /**
     * Constructs a new LoadError object with the specified detail message.
     *
     * @param s the detail message
     */
    public LoadError(String s) {
        super(s);
        System.out.println(s);
    }
}