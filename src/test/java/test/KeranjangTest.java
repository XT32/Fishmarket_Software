package test;

import model.CartItem;
import model.Ikan;
import model.Keranjang;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KeranjangTest {

    @Test
    public void testAddToCartAndTotalCalculation() {
        Keranjang keranjang = new Keranjang();

        Ikan salmon = new Ikan(1, "Ikan Salmon", 80000.0, "salmon.png", 50, 1);
        Ikan tuna = new Ikan(2, "Ikan Tuna", 60000.0, "tuna.png", 30, 1);

        keranjang.addItem(salmon, 2); // 2 x 80,000 = 160,000
        keranjang.addItem(tuna, 1);   // 1 x 60,000 = 60,000

        assertEquals(2, keranjang.getItems().size());
        assertEquals(3, keranjang.getTotalQuantity());
        assertEquals(220000.0, keranjang.getTotalPrice(), 0.001);

        // Add more salmon
        keranjang.addItem(salmon, 1); // now 3 salmon = 240,000 + 60,000 = 300,000
        assertEquals(2, keranjang.getItems().size());
        assertEquals(4, keranjang.getTotalQuantity());
        assertEquals(300000.0, keranjang.getTotalPrice(), 0.001);

        // Clear cart
        keranjang.clear();
        assertEquals(0, keranjang.getItems().size());
        assertEquals(0.0, keranjang.getTotalPrice(), 0.001);
    }
}
