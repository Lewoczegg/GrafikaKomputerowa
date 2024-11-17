package lewocz.graphics.view.components;

import javafx.scene.control.TextField;

public class IntegerTextField extends TextField {

    public IntegerTextField() {
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                setText(newValue.replaceAll("[^\\d]", "")); // Remove non-digit characters
            }
        });
    }
}