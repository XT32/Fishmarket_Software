package controller;

import model.Ikan;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BiConsumer;

public class FishCardController {

    @FXML private AnchorPane card_form;
    @FXML private ImageView fishImage;
    @FXML private Label fishName;
    @FXML private Label fishPrice;
    @FXML private Label stockBadge;
    @FXML private Spinner<Integer> fishSpinner;
    @FXML private Button addButton;

    private Ikan currentIkan;
    private BiConsumer<Ikan, Integer> onAddToCartCallback;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public void setData(Ikan ikan, BiConsumer<Ikan, Integer> onAddToCart) {
        this.currentIkan = ikan;
        this.onAddToCartCallback = onAddToCart;

        fishName.setText(ikan.getNamaIkan());

        String formattedPrice = currencyFormatter.format(ikan.getHarga()).replace(",00", "") + " / kg";
        fishPrice.setText(formattedPrice);

        int stock = ikan.getStok();
        if (stock > 0) {
            stockBadge.setText("Stok: " + stock + " kg");
            stockBadge.getStyleClass().setAll("stock-badge");

            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, stock, 1);
            fishSpinner.setValueFactory(valueFactory);
            addButton.setDisable(false);
        } else {
            stockBadge.setText("Habis");
            stockBadge.getStyleClass().setAll("stock-badge-low");
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0);
            fishSpinner.setValueFactory(valueFactory);
            addButton.setDisable(true);
        }

        addButton.setOnAction(e -> {
            if (currentIkan != null && onAddToCartCallback != null && fishSpinner.getValue() > 0) {
                onAddToCartCallback.accept(currentIkan, fishSpinner.getValue());
            }
        });
    }
}
