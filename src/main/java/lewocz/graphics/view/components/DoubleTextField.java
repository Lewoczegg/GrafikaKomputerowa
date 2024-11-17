package lewocz.graphics.view.components;

import javafx.scene.control.TextField;

public class DoubleTextField extends TextField {

    public DoubleTextField() {
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,3}(,\\d{3})*(\\.\\d*)?")) {
                setText(newValue.replaceAll("[^\\d,\\.]", "")); // Remove invalid characters
            }
        });
    }
}