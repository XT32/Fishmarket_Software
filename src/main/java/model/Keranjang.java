package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Keranjang belanja (Shopping Cart).
 */
public class Keranjang {
    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Ikan ikan, int kuantitas) {
        for (CartItem item : items) {
            if (item.getIkan().getIdIkan() == ikan.getIdIkan()) {
                item.setKuantitas(item.getKuantitas() + kuantitas);
                return;
            }
        }
        items.add(new CartItem(ikan, kuantitas));
    }

    public void removeItem(CartItem item) {
        items.remove(item);
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getKuantitas();
        }
        return total;
    }
}
