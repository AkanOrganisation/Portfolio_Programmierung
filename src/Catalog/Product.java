package Catalog;

public class Product {
    private static int nextId = 1;

    public int id;

    public Product() {
        this.id = nextId;
        nextId++;
    }
}
