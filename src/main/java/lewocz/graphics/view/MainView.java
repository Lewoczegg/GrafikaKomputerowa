package lewocz.graphics.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.util.converter.NumberStringConverter;
import lewocz.graphics.command.*;
import lewocz.graphics.event.EventQueue;
import lewocz.graphics.model.PNMFormat;
import lewocz.graphics.model.ShapeModel;
import lewocz.graphics.model.Tool;
import lewocz.graphics.view.components.DoubleTextField;
import lewocz.graphics.view.components.IntegerTextField;
import lewocz.graphics.viewmodel.IMainViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Optional;

@Component
public class MainView {
    private static final Logger log = LoggerFactory.getLogger(MainView.class);
    @FXML
    private ToggleGroup toolToggleGroup;
    @FXML
    private ToggleButton selectToolButton;
    @FXML
    private ToggleButton triangleToolButton;
    @FXML
    private ToggleButton quadrilateralToolButton;
    @FXML
    private ToggleButton ellipseToolButton;
    @FXML
    private ToggleButton lineToolButton;
    @FXML
    private ToggleButton freehandToolButton;

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

    @FXML
    private Pane canvasPane;
    @FXML
    private Canvas canvas;

    @FXML
    private Pane cubePane;

    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem loadMenuItem;

    @FXML
    private IntegerTextField addRedField;
    @FXML
    private IntegerTextField addGreenField;
    @FXML
    private IntegerTextField addBlueField;
    @FXML
    private Button applyAdditionButton;

    @FXML
    private IntegerTextField subRedField;
    @FXML
    private IntegerTextField subGreenField;
    @FXML
    private IntegerTextField subBlueField;
    @FXML
    private Button applySubtractionButton;

    // Multiplication Controls
    @FXML
    private TextField mulRedField;
    @FXML
    private TextField mulGreenField;
    @FXML
    private TextField mulBlueField;
    @FXML
    private Button applyMultiplicationButton;

    // Division Controls
    @FXML
    private TextField divRedField;
    @FXML
    private TextField divGreenField;
    @FXML
    private TextField divBlueField;
    @FXML
    private Button applyDivisionButton;

    @FXML
    private TextField brightnessField;
    @FXML
    private Button adjustBrightnessButton;

    @FXML
    private Button grayscaleAverageButton;
    @FXML
    private Button grayscaleMaxButton;

    @FXML
    private Button applySmoothingFilterButton;
    @FXML
    private Button applyMedianFilterButton;
    @FXML
    private Button applySobelFilterButton;
    @FXML
    private Button applyHighPassFilterButton;
    @FXML
    private IntegerTextField gaussianKernelSizeField;
    @FXML
    private TextField gaussianSigmaField;
    @FXML
    private Button applyGaussianBlurButton;

    @FXML
    private Button applyCustomConvolutionButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Button applyHistogramStretchingButton;
    @FXML
    private Button applyHistogramEqualizationButton;

    @FXML
    private TextField manualThresholdField;
    @FXML
    private Button applyManualThresholdingButton;

    @FXML
    private TextField percentBlackField;
    @FXML
    private Button applyPercentBlackSelectionButton;

    @FXML
    private Button applyMeanIterativeSelectionButton;

    private Group root3D;
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private double rotateX = 0;
    private double rotateY = 0;

    private final IMainViewModel mainViewModel;
    private final EventQueue eventQueue = new EventQueue();

    private GraphicsContext gc;

    public MainView(IMainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        bindProperties();
        setupCanvasListeners();
        bindColorProperties();
        setUp3DScene();
        mainViewModel.setRedrawCanvasCallback(this::redrawCanvas);
        redrawCanvas();
    }

    private void onApplyAddition() {
        try {
            double addRed = Double.parseDouble(addRedField.getText());
            double addGreen = Double.parseDouble(addGreenField.getText());
            double addBlue = Double.parseDouble(addBlueField.getText());

            Command command = new ApplyAdditionCommand(mainViewModel, addRed, addGreen, addBlue);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for addition values.");
        }
    }

    private void onApplySubtraction() {
        try {
            double subRed = Double.parseDouble(subRedField.getText());
            double subGreen = Double.parseDouble(subGreenField.getText());
            double subBlue = Double.parseDouble(subBlueField.getText());
            Command command = new ApplySubtractionCommand(mainViewModel, subRed, subGreen, subBlue);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for subtraction values.");
        }
    }

    private void onApplyMultiplication() {
        try {
            double mulRed = Double.parseDouble(mulRedField.getText());
            double mulGreen = Double.parseDouble(mulGreenField.getText());
            double mulBlue = Double.parseDouble(mulBlueField.getText());
            Command command = new ApplyMultiplicationCommand(mainViewModel, mulRed, mulGreen, mulBlue);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for multiplication values.");
        }
    }

    private void onApplyDivision() {
        try {
            double divRed = Double.parseDouble(divRedField.getText());
            double divGreen = Double.parseDouble(divGreenField.getText());
            double divBlue = Double.parseDouble(divBlueField.getText());

            if (divRed == 0 || divGreen == 0 || divBlue == 0) {
                showAlert("Division by Zero", "Division values cannot be zero.");
                return;
            }

            Command command = new ApplyDivisionCommand(mainViewModel, divRed, divGreen, divBlue);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for division values.");
        }
    }

    private void onAdjustBrightness() {
        try {
            double brightnessChange = Double.parseDouble(brightnessField.getText().replace(',', '.'));
            Command command = new AdjustBrightnessCommand(mainViewModel, brightnessChange);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for brightness change.");
        }
    }

    private void onApplyGrayscaleAverage() {
        Command command = new ApplyGrayscaleAverageCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void onApplyGrayscaleMax() {
        Command command = new ApplyGrayscaleMaxCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void onApplySmoothingFilter() {
        Command command = new ApplySmoothingFilterCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void onApplyMedianFilter() {
        Command command = new ApplyMedianFilterCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void onApplySobelFilter() {
        Command command = new ApplySobelFilterCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void onApplyHighPassFilter() {
        Command command = new ApplyHighPassFilterCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void onApplyGaussianBlur() {
        try {
            int kernelSize = Integer.parseInt(gaussianKernelSizeField.getText());
            double sigma = Double.parseDouble(gaussianSigmaField.getText());

            if (kernelSize % 2 == 0 || kernelSize <= 0) {
                showAlert("Invalid Kernel Size", "Kernel size must be a positive odd integer.");
                return;
            }

            if (sigma <= 0) {
                showAlert("Invalid Sigma Value", "Sigma must be a positive number.");
                return;
            }

            Command command = new ApplyGaussianBlurCommand(mainViewModel, kernelSize, sigma);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for kernel size and sigma.");
        }
    }

    private void onApplyCustomConvolution() {
        try {
            TextInputDialog sizeDialog = new TextInputDialog("3");
            sizeDialog.setTitle("Custom Convolution");
            sizeDialog.setHeaderText("Enter Kernel Size");
            sizeDialog.setContentText("Kernel Size (positive odd integer):");
            Optional<String> sizeResult = sizeDialog.showAndWait();

            if (sizeResult.isPresent()) {
                int size = Integer.parseInt(sizeResult.get());

                if (size % 2 == 0 || size <= 0) {
                    showAlert("Invalid Kernel Size", "Kernel size must be a positive odd integer.");
                    return;
                }

                double[][] kernel = new double[size][size];

                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        TextInputDialog valueDialog = new TextInputDialog("0");
                        valueDialog.setTitle("Custom Convolution");
                        valueDialog.setHeaderText("Enter Kernel Value");
                        valueDialog.setContentText("Value at (" + x + ", " + y + "):");
                        Optional<String> valueResult = valueDialog.showAndWait();

                        if (valueResult.isPresent()) {
                            try {
                                double value = Double.parseDouble(valueResult.get());
                                kernel[y][x] = value;
                            } catch (NumberFormatException e) {
                                showAlert("Invalid Input", "Please enter a valid number for kernel value.");
                                return;
                            }
                        } else {
                            return; // User cancelled
                        }
                    }
                }

                Command command = new ApplyCustomConvolutionCommand(mainViewModel, kernel);
                eventQueue.enqueue(command);
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for kernel size.");
        }
    }

    private void onApplyHistogramStretching() {
        Command command = new ApplyHistogramStretchingCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void onApplyHistogramEqualization() {
        Command command = new ApplyHistogramEqualizationCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void onApplyManualThresholding() {
        try {
            int threshold = Integer.parseInt(manualThresholdField.getText());
            if (threshold < 0 || threshold > 255) {
                showAlert("Invalid Threshold", "Threshold must be between 0 and 255.");
                return;
            }
            Command command = new ApplyManualThresholdingCommand(mainViewModel, threshold);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid integer for threshold.");
        }
    }

    private void onApplyPercentBlackSelection() {
        try {
            double percentBlack = Double.parseDouble(percentBlackField.getText().replace(',', '.'));
            if (percentBlack < 0 || percentBlack > 100) {
                showAlert("Invalid Percentage", "Percentage must be between 0 and 100.");
                return;
            }
            Command command = new ApplyPercentBlackSelectionCommand(mainViewModel, percentBlack);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for percent black.");
        }
    }

    private void onApplyMeanIterativeSelection() {
        Command command = new ApplyMeanIterativeSelectionCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void setUp3DScene() {
        root3D = new Group();

        SubScene subScene3D = new SubScene(root3D, 200, 200, true, SceneAntialiasing.BALANCED);
        subScene3D.widthProperty().bind(cubePane.widthProperty());
        subScene3D.heightProperty().bind(cubePane.heightProperty());

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
                new Rotate(-20, Rotate.Y_AXIS),
                new Rotate(-20, Rotate.X_AXIS),
                new Translate(0, 0, -500)
        );
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        subScene3D.setCamera(camera);

        Group rgbCube =  mainViewModel.createRGBColoredCube(30);
        root3D.getChildren().add(rgbCube);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        root3D.getChildren().add(ambientLight);

        addMouseControl(root3D, subScene3D);

        cubePane.getChildren().add(subScene3D);
    }

    private void addMouseControl(Group group, SubScene scene) {
        scene.setOnMousePressed((MouseEvent event) -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        scene.setOnMouseDragged((MouseEvent event) -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            rotateX += (mousePosY - mouseOldY);
            rotateY += (mousePosX - mouseOldX);
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            group.getTransforms().clear();
            group.getTransforms().addAll(
                    new Rotate(rotateY, Rotate.Y_AXIS),
                    new Rotate(rotateX, Rotate.X_AXIS)
            );
        });
    }

    private void bindProperties() {
        selectToolButton.setUserData(Tool.SELECT);
        triangleToolButton.setUserData(Tool.TRIANGLE);
        quadrilateralToolButton.setUserData(Tool.QUADRILATERAL);
        ellipseToolButton.setUserData(Tool.ELLIPSE);
        lineToolButton.setUserData(Tool.LINE);
        freehandToolButton.setUserData(Tool.FREEHAND);

        toolToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                ToggleButton selectedButton = (ToggleButton) newToggle;
                Tool selectedTool = (Tool) selectedButton.getUserData();
                Command command = new SetToolCommand(mainViewModel, selectedTool);
                eventQueue.enqueue(command);
            }
        });

        loadingIndicator.visibleProperty().bind(mainViewModel.isProcessingProperty());
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        saveMenuItem.setOnAction(e -> onSaveMenuItemClicked());
        loadMenuItem.setOnAction(e -> onLoadMenuItemClicked());

        applyAdditionButton.setOnAction(e -> onApplyAddition());
        applySubtractionButton.setOnAction(e -> onApplySubtraction());
        applyMultiplicationButton.setOnAction(e -> onApplyMultiplication());
        applyDivisionButton.setOnAction(e -> onApplyDivision());

        adjustBrightnessButton.setOnAction(e -> onAdjustBrightness());
        grayscaleAverageButton.setOnAction(e -> onApplyGrayscaleAverage());
        grayscaleMaxButton.setOnAction(e -> onApplyGrayscaleMax());

        applySmoothingFilterButton.setOnAction(e -> onApplySmoothingFilter());
        applyMedianFilterButton.setOnAction(e -> onApplyMedianFilter());
        applySobelFilterButton.setOnAction(e -> onApplySobelFilter());
        applyHighPassFilterButton.setOnAction(e -> onApplyHighPassFilter());
        applyGaussianBlurButton.setOnAction(e -> onApplyGaussianBlur());
        applyCustomConvolutionButton.setOnAction(e -> onApplyCustomConvolution());

        applyHistogramStretchingButton.setOnAction(e -> onApplyHistogramStretching());
        applyHistogramEqualizationButton.setOnAction(e -> onApplyHistogramEqualization());

        applyManualThresholdingButton.setOnAction(e -> onApplyManualThresholding());
        applyPercentBlackSelectionButton.setOnAction(e -> onApplyPercentBlackSelection());
        applyMeanIterativeSelectionButton.setOnAction(e -> onApplyMeanIterativeSelection());
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
        textField.textProperty().bindBidirectional(property, new NumberStringConverter());
    }

    private void setupDoubleField(TextField textField, DoubleProperty property, Slider slider) {
        slider.valueProperty().bindBidirectional(property);
        textField.textProperty().bindBidirectional(property, new NumberStringConverter());
    }

    private void setupCanvasListeners() {
        canvas.setOnMousePressed(mouseEvent -> {
            Command command = new MousePressedCommand(mainViewModel, mouseEvent.getX(), mouseEvent.getY());
            eventQueue.enqueue(command);
        });
        canvas.setOnMouseDragged(mouseEvent -> {
            Command command = new MouseDraggedCommand(mainViewModel, mouseEvent.getX(), mouseEvent.getY());
            eventQueue.enqueue(command);
        });
        canvas.setOnMouseReleased(mouseEvent -> {
            Command command = new MouseReleasedCommand(mainViewModel);
            eventQueue.enqueue(command);
        });

        canvas.widthProperty().addListener(evt -> redrawCanvas());
        canvas.heightProperty().addListener(evt -> redrawCanvas());
    }

    private void redrawCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (ShapeModel shape : mainViewModel.getShapes()) {
            shape.draw(gc);
        }

        if (mainViewModel.getTempShape() != null) {
            mainViewModel.getTempShape().draw(gc);
        }
    }

    private void onSaveMenuItemClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNM Files", "*.pbm", "*.pgm", "*.ppm")
        );
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Select Image Format");
            alert.setHeaderText("Choose the format to save:");
            ButtonType pbmTextButton = new ButtonType("PBM Textual (P1)");
            ButtonType pbmBinaryButton = new ButtonType("PBM Binary (P4)");
            ButtonType pgmTextButton = new ButtonType("PGM Textual (P2)");
            ButtonType pgmBinaryButton = new ButtonType("PGM Binary (P5)");
            ButtonType ppmTextButton = new ButtonType("PPM Textual (P3)");
            ButtonType ppmBinaryButton = new ButtonType("PPM Binary (P6)");
            alert.getButtonTypes().setAll(
                    pbmTextButton, pbmBinaryButton,
                    pgmTextButton, pgmBinaryButton,
                    ppmTextButton, ppmBinaryButton,
                    ButtonType.CANCEL
            );

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.CANCEL) {
                WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, snapshot);

                String filePath = file.getAbsolutePath();
                PNMFormat format = null;
                boolean binaryFormat = false;

                if (result.get() == pbmTextButton) {
                    format = PNMFormat.PBM;
                    binaryFormat = false;
                    // Ensure file extension is .pbm
                    if (!filePath.toLowerCase().endsWith(".pbm")) {
                        filePath += ".pbm";
                    }
                } else if (result.get() == pbmBinaryButton) {
                    format = PNMFormat.PBM;
                    binaryFormat = true;
                    if (!filePath.toLowerCase().endsWith(".pbm")) {
                        filePath += ".pbm";
                    }
                } else if (result.get() == pgmTextButton) {
                    format = PNMFormat.PGM;
                    binaryFormat = false;
                    if (!filePath.toLowerCase().endsWith(".pgm")) {
                        filePath += ".pgm";
                    }
                } else if (result.get() == pgmBinaryButton) {
                    format = PNMFormat.PGM;
                    binaryFormat = true;
                    if (!filePath.toLowerCase().endsWith(".pgm")) {
                        filePath += ".pgm";
                    }
                } else if (result.get() == ppmTextButton) {
                    format = PNMFormat.PPM;
                    binaryFormat = false;
                    if (!filePath.toLowerCase().endsWith(".ppm")) {
                        filePath += ".ppm";
                    }
                } else if (result.get() == ppmBinaryButton) {
                    format = PNMFormat.PPM;
                    binaryFormat = true;
                    if (!filePath.toLowerCase().endsWith(".ppm")) {
                        filePath += ".ppm";
                    }
                }

                Command command = new SaveCommand(mainViewModel, filePath, binaryFormat, snapshot, format);
                eventQueue.enqueue(command);
            }
        }
    }

    private void onLoadMenuItemClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Image Files", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif", "*.pbm", "*.pgm", "*.ppm"),
                new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("BMP Files", "*.bmp"),
                new FileChooser.ExtensionFilter("GIF Files", "*.gif"),
                new FileChooser.ExtensionFilter("PNM Files", "*.pbm", "*.pgm", "*.ppm")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String fileName = file.getName().toLowerCase();
            PNMFormat format = null;

            if (fileName.endsWith(".pbm")) {
                format = PNMFormat.PBM;
            } else if (fileName.endsWith(".pgm")) {
                format = PNMFormat.PGM;
            } else if (fileName.endsWith(".ppm")) {
                format = PNMFormat.PPM;
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
                    || fileName.endsWith(".bmp") || fileName.endsWith(".gif")) {
                format = null; // Standard image formats
            } else {
                showAlert("Unsupported File Format", "The selected file format is not supported.");
                return;
            }

            Command command = new LoadCommand(mainViewModel, file.getAbsolutePath(), format);
            eventQueue.enqueue(command);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
