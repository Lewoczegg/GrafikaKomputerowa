package lewocz.graphics.view.components.imageoperations;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import lewocz.graphics.command.*;
import lewocz.graphics.event.EventQueue;
import lewocz.graphics.view.components.IntegerTextField;
import lewocz.graphics.viewmodel.IMainViewModel;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static lewocz.graphics.view.utils.AlertUtils.showAlert;

@Component
public class ImageOperationsComponent {

    // Addition Controls
    @FXML
    private IntegerTextField addRedField;
    @FXML
    private IntegerTextField addGreenField;
    @FXML
    private IntegerTextField addBlueField;
    @FXML
    private Button applyAdditionButton;

    // Subtraction Controls
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

    // Brightness Adjustment
    @FXML
    private TextField brightnessField;
    @FXML
    private Button adjustBrightnessButton;

    // Grayscale Buttons
    @FXML
    private Button grayscaleAverageButton;
    @FXML
    private Button grayscaleMaxButton;

    // Filter Buttons
    @FXML
    private Button applySmoothingFilterButton;
    @FXML
    private Button applyMedianFilterButton;
    @FXML
    private Button applySobelFilterButton;
    @FXML
    private Button applyHighPassFilterButton;

    // Gaussian Blur Controls
    @FXML
    private IntegerTextField gaussianKernelSizeField;
    @FXML
    private TextField gaussianSigmaField;
    @FXML
    private Button applyGaussianBlurButton;

    // Custom Convolution
    @FXML
    private Button applyCustomConvolutionButton;

    // Histogram Operations
    @FXML
    private Button applyHistogramStretchingButton;
    @FXML
    private Button applyHistogramEqualizationButton;

    // Binarization Controls
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

    // Otsu Thresholding
    @FXML
    private Button applyOtsuThresholdingButton;

    // Niblack Thresholding
    @FXML
    private TextField niblackWindowSizeField;
    @FXML
    private TextField niblackKField;
    @FXML
    private Button applyNiblackThresholdingButton;

    // Sauvola Thresholding
    @FXML
    private TextField sauvolaWindowSizeField;
    @FXML
    private TextField sauvolaKField;
    @FXML
    private TextField sauvolaRField;
    @FXML
    private Button applySauvolaThresholdingButton;

    // Morphological Operations
    @FXML
    private Button applyDilationButton;
    @FXML
    private Button applyErosionButton;
    @FXML
    private Button applyOpeningButton;
    @FXML
    private Button applyClosingButton;
    @FXML
    private TextArea hitMaskArea;
    @FXML
    private TextArea missMaskArea;
    @FXML
    private Button applyHitOrMissButton;

    @FXML
    private Button calculateColorPercentageButton;
    @FXML
    private Button detectColorAreaButton;
    @FXML
    private ColorPicker targetColorPicker;
    @FXML
    private Slider toleranceSlider;


    private final IMainViewModel mainViewModel;
    private final EventQueue eventQueue;

    public ImageOperationsComponent(IMainViewModel mainViewModel, EventQueue eventQueue) {
        this.mainViewModel = mainViewModel;
        this.eventQueue = eventQueue;
    }

    @FXML
    public void initialize() {
        bindActions();
    }

    private void bindActions() {
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
        applyOtsuThresholdingButton.setOnAction(e -> onApplyOtsuThresholding());
        applyNiblackThresholdingButton.setOnAction(e -> onApplyNiblackThresholding());
        applySauvolaThresholdingButton.setOnAction(e -> onApplySauvolaThresholding());
        applyDilationButton.setOnAction(e -> onApplyDilation());
        applyErosionButton.setOnAction(e -> onApplyErosion());
        applyOpeningButton.setOnAction(e -> onApplyOpening());
        applyClosingButton.setOnAction(e -> onApplyClosing());
        applyHitOrMissButton.setOnAction(e -> onApplyHitOrMiss());
        calculateColorPercentageButton.setOnAction(e -> onCalculateColorPercentage());
        detectColorAreaButton.setOnAction(e -> onDetectLargestColorArea());
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

    private void onApplyOtsuThresholding() {
        Command command = new ApplyOtsuThresholdingCommand(mainViewModel);
        eventQueue.enqueue(command);
    }

    private void onApplyNiblackThresholding() {
        try {
            int windowSize = Integer.parseInt(niblackWindowSizeField.getText());
            double k = Double.parseDouble(niblackKField.getText().replace(',', '.'));

            if (windowSize % 2 == 0 || windowSize <= 0) {
                showAlert("Invalid Window Size", "Window size must be a positive odd integer.");
                return;
            }

            Command command = new ApplyNiblackThresholdingCommand(mainViewModel, windowSize, k);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for window size and k.");
        }
    }

    private void onApplySauvolaThresholding() {
        try {
            int windowSize = Integer.parseInt(sauvolaWindowSizeField.getText());
            double k = Double.parseDouble(sauvolaKField.getText().replace(',', '.'));
            double r = Double.parseDouble(sauvolaRField.getText().replace(',', '.'));

            if (windowSize % 2 == 0 || windowSize <= 0) {
                showAlert("Invalid Window Size", "Window size must be a positive odd integer.");
                return;
            }

            Command command = new ApplySauvolaThresholdingCommand(mainViewModel, windowSize, k, r);
            eventQueue.enqueue(command);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for window size, k, and r.");
        }
    }

    private void onApplyDilation() {
        boolean[][] structuringElement = getDefaultStructuringElement();
        Command command = new ApplyDilationCommand(mainViewModel, structuringElement);
        eventQueue.enqueue(command);
    }

    private void onApplyErosion() {
        boolean[][] structuringElement = getDefaultStructuringElement();
        Command command = new ApplyErosionCommand(mainViewModel, structuringElement);
        eventQueue.enqueue(command);
    }

    private void onApplyOpening() {
        boolean[][] structuringElement = getDefaultStructuringElement();
        Command command = new ApplyOpeningCommand(mainViewModel, structuringElement);
        eventQueue.enqueue(command);
    }

    private void onApplyClosing() {
        boolean[][] structuringElement = getDefaultStructuringElement();
        Command command = new ApplyClosingCommand(mainViewModel, structuringElement);
        eventQueue.enqueue(command);
    }

    private void onApplyHitOrMiss() {
        try {
            boolean[][] hitMask = parseMask(hitMaskArea.getText());
            boolean[][] missMask = parseMask(missMaskArea.getText());
            Command command = new ApplyHitOrMissCommand(mainViewModel, hitMask, missMask);
            eventQueue.enqueue(command);
        } catch (IllegalArgumentException e) {
            showAlert("Invalid Mask", e.getMessage());
        }
    }

    private boolean[][] getDefaultStructuringElement() {
        // Default 3x3 square structuring element
        return new boolean[][] {
                { true, true, true },
                { true, true, true },
                { true, true, true }
        };
    }

    private void onCalculateColorPercentage() {
        Color targetColor = targetColorPicker.getValue();
        double tolerance = toleranceSlider.getValue();
        Command command = new CalculateColorPercentageCommand(mainViewModel, targetColor, tolerance);
        eventQueue.enqueue(command);
    }

    private void onDetectLargestColorArea() {
        Color targetColor = targetColorPicker.getValue();
        double tolerance = toleranceSlider.getValue();
        Command command = new DetectLargestColorAreaCommand(mainViewModel, targetColor, tolerance);
        eventQueue.enqueue(command);
    }

    private boolean[][] parseMask(String maskText) throws IllegalArgumentException {
        String[] lines = maskText.trim().split("\\n");
        int numRows = lines.length;
        int numCols = lines[0].trim().split("\\s+").length;

        boolean[][] mask = new boolean[numRows][numCols];

        for (int y = 0; y < numRows; y++) {
            String[] tokens = lines[y].trim().split("\\s+");
            if (tokens.length != numCols) {
                throw new IllegalArgumentException("All rows must have the same number of elements.");
            }
            for (int x = 0; x < numCols; x++) {
                String token = tokens[x];
                if (token.equals("1")) {
                    mask[y][x] = true;
                } else if (token.equals("0")) {
                    mask[y][x] = false;
                } else {
                    throw new IllegalArgumentException("Mask elements must be 0 or 1.");
                }
            }
        }
        return mask;
    }
}
