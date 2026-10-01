package model;

public class InventoryItem {
    private int id;
    private String name;
    private String type;
    private int stock;
    private String status;
    private String dateAdded;

    public InventoryItem(int id, String name, String type, int stock, String status, String dateAdded) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.stock = stock;
        this.status = status;
        this.dateAdded = dateAdded;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDateAdded() { return dateAdded; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }
}
