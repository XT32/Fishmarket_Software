package model;

import javafx.beans.property.*;

public class Ikan {
    private final IntegerProperty idIkan;
    private final StringProperty namaIkan;
    private final DoubleProperty harga;
    private final StringProperty gambarIkan;
    private final IntegerProperty stok;
    private final IntegerProperty idNelayan;

    public Ikan(int idIkan, String namaIkan, double harga, String gambarIkan, int stok, int idNelayan) {
        this.idIkan = new SimpleIntegerProperty(idIkan);
        this.namaIkan = new SimpleStringProperty(namaIkan != null ? namaIkan : "");
        this.harga = new SimpleDoubleProperty(harga);
        this.gambarIkan = new SimpleStringProperty(gambarIkan != null ? gambarIkan : "");
        this.stok = new SimpleIntegerProperty(stok);
        this.idNelayan = new SimpleIntegerProperty(idNelayan);
    }

    // JavaFX Properties
    public IntegerProperty idIkanProperty() { return idIkan; }
    public StringProperty namaIkanProperty() { return namaIkan; }
    public DoubleProperty hargaProperty() { return harga; }
    public StringProperty gambarIkanProperty() { return gambarIkan; }
    public IntegerProperty stokProperty() { return stok; }
    public IntegerProperty idNelayanProperty() { return idNelayan; }

    // Aliases to support multiple FXML column name conventions
    public DoubleProperty hargaIkanProperty() { return harga; }
    public IntegerProperty stokIkanProperty() { return stok; }

    // Getters
    public int getIdIkan() { return idIkan.get(); }
    public String getNamaIkan() { return namaIkan.get(); }
    public double getHarga() { return harga.get(); }
    public double getHargaIkan() { return harga.get(); }
    public String getGambarIkan() { return gambarIkan.get(); }
    public int getStok() { return stok.get(); }
    public int getStokIkan() { return stok.get(); }
    public int getIdNelayan() { return idNelayan.get(); }

    // Setters
    public void setIdIkan(int idIkan) { this.idIkan.set(idIkan); }
    public void setNamaIkan(String namaIkan) { this.namaIkan.set(namaIkan); }
    public void setnamaIkan(String namaIkan) { setNamaIkan(namaIkan); }
    public void setHarga(double harga) { this.harga.set(harga); }
    public void setHargaIkan(double harga) { setHarga(harga); }
    public void setGambarIkan(String gambarIkan) { this.gambarIkan.set(gambarIkan); }
    public void setStok(int stok) { this.stok.set(stok); }
    public void setStokIkan(int stok) { setStok(stok); }
    public void setIdNelayan(int idNelayan) { this.idNelayan.set(idNelayan); }

    @Override
    public String toString() {
        return getNamaIkan() + " (Stok: " + getStok() + ", Rp " + String.format("%,.0f", getHarga()) + ")";
    }
}
