package model;

import javafx.beans.property.*;

public class CartItem {
    private final Ikan ikan;
    private final IntegerProperty kuantitas;
    private final DoubleProperty subtotal;

    public CartItem(Ikan ikan, int kuantitas) {
        this.ikan = ikan;
        this.kuantitas = new SimpleIntegerProperty(kuantitas);
        this.subtotal = new SimpleDoubleProperty(ikan.getHarga() * kuantitas);
    }

    public Ikan getIkan() {
        return ikan;
    }

    public String getNamaIkan() {
        return ikan.getNamaIkan();
    }

    public StringProperty namaIkanProperty() {
        return ikan.namaIkanProperty();
    }

    public int getKuantitas() {
        return kuantitas.get();
    }

    public void setKuantitas(int qty) {
        this.kuantitas.set(qty);
        this.subtotal.set(ikan.getHarga() * qty);
    }

    public IntegerProperty kuantitasProperty() {
        return kuantitas;
    }

    public double getSubtotal() {
        return subtotal.get();
    }

    public DoubleProperty subtotalProperty() {
        return subtotal;
    }

    // Alias for table columns named "harga" or "bayar"
    public DoubleProperty hargaProperty() {
        return subtotal;
    }
}
