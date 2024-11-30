package lewocz.graphics.view.components.colorcontrols;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import lewocz.graphics.view.components.DoubleTextField;
import lewocz.graphics.view.components.IntegerTextField;
import lewocz.graphics.viewmodel.IMainViewModel;
import org.springframework.stereotype.Component;

@Component
public class ColorControlsComponent {

    @FXML
    private VBox colorControlsPane;

    // Color Preview and Labels
    @FXML
    private Rectangle colorPreview;
    @FXML
    private Label rgbValueLabel;
    @FXML
    private Label cmykValueLabel;
    @FXML
    private Label hsvValueLabel;

    // RGB Controls
    @FXML
    private Slider redSlider;
    @FXML
    private IntegerTextField redTextField;
    @FXML
    private Slider greenSlider;
    @FXML
    private IntegerTextField greenTextField;
    @FXML
    private Slider blueSlider;
    @FXML
    private IntegerTextField blueTextField;

    // CMYK Controls
    @FXML
    private Slider cyanSlider;
    @FXML
    private DoubleTextField cyanTextField;
    @FXML
    private Slider magentaSlider;
    @FXML
    private DoubleTextField magentaTextField;
    @FXML
    private Slider yellowSlider;
    @FXML
    private DoubleTextField yellowTextField;
    @FXML
    private Slider keySlider;
    @FXML
    private DoubleTextField keyTextField;

    // HSV Controls
    @FXML
    private Slider hueSlider;
    @FXML
    private DoubleTextField hueTextField;
    @FXML
    private Slider saturationSlider;
    @FXML
    private DoubleTextField saturationTextField;
    @FXML
    private Slider valueSlider;
    @FXML
    private DoubleTextField valueTextField;

    private final IMainViewModel mainViewModel;

    public ColorControlsComponent(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @FXML
    public void initialize() {
        bindColorProperties();
    }

    private void bindColorProperties() {
        setupIntegerField(redTextField, mainViewModel.redProperty(), redSlider);
        setupIntegerField(greenTextField, mainViewModel.greenProperty(), greenSlider);
        setupIntegerField(blueTextField, mainViewModel.blueProperty(), blueSlider);

        setupDoubleField(cyanTextField, mainViewModel.cyanProperty(), cyanSlider);
        setupDoubleField(magentaTextField, mainViewModel.magentaProperty(), magentaSlider);
        setupDoubleField(yellowTextField, mainViewModel.yellowProperty(), yellowSlider);
        setupDoubleField(keyTextField, mainViewModel.keyProperty(), keySlider);

        setupDoubleField(hueTextField, mainViewModel.hueProperty(), hueSlider);
        setupDoubleField(saturationTextField, mainViewModel.saturationProperty(), saturationSlider);
        setupDoubleField(valueTextField, mainViewModel.valueProperty(), valueSlider);

        // Update color preview and labels
        mainViewModel.selectedColorProperty().addListener((obs, oldColor, newColor) -> {
            colorPreview.setFill(newColor);

            rgbValueLabel.setText(String.format("RGB: %d, %d, %d",
                    mainViewModel.redProperty().get(),
                    mainViewModel.greenProperty().get(),
                    mainViewModel.blueProperty().get()));

            cmykValueLabel.setText(String.format("CMYK: %.1f%%, %.1f%%, %.1f%%, %.1f%%",
                    mainViewModel.cyanProperty().get(),
                    mainViewModel.magentaProperty().get(),
                    mainViewModel.yellowProperty().get(),
                    mainViewModel.keyProperty().get()));

            hsvValueLabel.setText(String.format("HSV: %.1f°, %.1f%%, %.1f%%",
                    mainViewModel.hueProperty().get(),
                    mainViewModel.saturationProperty().get(),
                    mainViewModel.valueProperty().get()));
        });

        // Initialize the color preview
        colorPreview.setFill(mainViewModel.selectedColorProperty().get());
    }

    private void setupIntegerField(TextField textField, IntegerProperty property, Slider slider) {
        slider.valueProperty().bindBidirectional(property);
        textField.textProperty().bindBidirectional(property, new javafx.util.converter.NumberStringConverter());
    }

    private void setupDoubleField(TextField textField, DoubleProperty property, Slider slider) {
        slider.valueProperty().bindBidirectional(property);
        textField.textProperty().bindBidirectional(property, new javafx.util.converter.NumberStringConverter());
    }
}
