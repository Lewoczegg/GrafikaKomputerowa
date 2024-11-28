package lewocz.graphics.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import lewocz.graphics.model.*;
import lewocz.graphics.utils.ImageProcessor;
import lewocz.graphics.utils.PNMImageIO;
import lewocz.graphics.utils.ColorUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class MainViewModel implements IMainViewModel {
    private static final Logger logger = LoggerFactory.getLogger(MainViewModel.class);

    @Getter
    private final ObservableList<ShapeModel> shapes = FXCollections.observableArrayList();

    private ObjectProperty<ShapeModel> tempShape = new SimpleObjectProperty<>();

    private final ObjectProperty<ShapeModel> currentShape = new SimpleObjectProperty<>();
    private final ObjectProperty<Tool> toolSelected = new SimpleObjectProperty<>(Tool.SELECT);

    // Color Properties
    private final IntegerProperty red = new SimpleIntegerProperty(0);
    private final IntegerProperty green = new SimpleIntegerProperty(0);
    private final IntegerProperty blue = new SimpleIntegerProperty(0);

    private final DoubleProperty cyan = new SimpleDoubleProperty(0.0);
    private final DoubleProperty magenta = new SimpleDoubleProperty(0.0);
    private final DoubleProperty yellow = new SimpleDoubleProperty(0.0);
    private final DoubleProperty key = new SimpleDoubleProperty(0.0);

    private final DoubleProperty hue = new SimpleDoubleProperty(0.0);
    private final DoubleProperty saturation = new SimpleDoubleProperty(0.0);
    private final DoubleProperty value = new SimpleDoubleProperty(0.0);

    private final ObjectProperty<Color> selectedColor = new SimpleObjectProperty<>(Color.BLACK);

    private final ObjectProperty<Color> strokeColor = new SimpleObjectProperty<>(Color.BLACK);
    private final ObjectProperty<Color> fillColor = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(1.0);

    private boolean isUpdating = false;

    private final ObjectProperty<Runnable> redrawCanvasCallback = new SimpleObjectProperty<>();

    private double startX, startY;
    private boolean isDragging;
    private final BooleanProperty isProcessing = new SimpleBooleanProperty(false);

    public MainViewModel() {
        // Initialize listeners for color properties
        initializeColorListeners();
    }

    // Getters for properties
    public IntegerProperty redProperty() { return red; }
    public IntegerProperty greenProperty() { return green; }
    public IntegerProperty blueProperty() { return blue; }

    public DoubleProperty cyanProperty() { return cyan; }
    public DoubleProperty magentaProperty() { return magenta; }
    public DoubleProperty yellowProperty() { return yellow; }
    public DoubleProperty keyProperty() { return key; }

    public DoubleProperty hueProperty() { return hue; }
    public DoubleProperty saturationProperty() { return saturation; }
    public DoubleProperty valueProperty() { return value; }

    public ObjectProperty<Color> selectedColorProperty() { return selectedColor; }

    public ShapeModel getTempShape() {
        return tempShape.get();
    }

    public void setSelectedTool(Tool tool) {
        toolSelected.set(tool);
    }

    public Tool getSelectedTool() {
        return toolSelected.get();
    }

    private ImageModel currentImageModel;

    public ImageModel getCurrentImageModel() {
        return currentImageModel;
    }

    public void setCurrentImageModel(ImageModel imageModel) {
        this.currentImageModel = imageModel;
    }

    private int bezierDegree = 3;

    public void setBezierDegree(int degree) {
        this.bezierDegree = degree;
    }

    public int getBezierDegree() {
        return bezierDegree;
    }


    @Override
    public void onMousePressed(double x, double y) {
        startX = x;
        startY = y;
        isDragging = true;

        Tool tool = getSelectedTool();
        switch (tool) {
            case SELECT:
                selectShapeAt(x, y);
                break;
            case TRIANGLE:
            case QUADRILATERAL:
            case ELLIPSE:
            case LINE:
                tempShape.set(createShape(tool, startX, startY, startX, startY));
                break;
            case FREEHAND:
                FreehandModel freehand = new FreehandModel();
                freehand.addPoint(x, y);
                tempShape.set(freehand);
                break;
            case BEZIER:
                if (tempShape.get() == null) {
                    BezierCurveModel bezierCurve = new BezierCurveModel();
                    bezierCurve.addControlPoint(x, y);
                    tempShape.set(bezierCurve);
                } else {
                    BezierCurveModel bezierCurve = (BezierCurveModel) tempShape.get();
                    bezierCurve.addControlPoint(x, y);
                }
                break;
            default:
                break;
        }

        requestRedraw();
    }

    @Override
    public void onMouseDragged(double x, double y) {
        if (!isDragging) return;

        double endX = x;
        double endY = y;

        Tool tool = getSelectedTool();
        switch (tool) {
            case SELECT:
                if (currentShape.get() != null) {
                    double deltaX = endX - startX;
                    double deltaY = endY - startY;
                    currentShape.get().moveBy(deltaX, deltaY);
                    startX = endX;
                    startY = endY;
                }
                break;
            case TRIANGLE:
            case QUADRILATERAL:
            case ELLIPSE:
            case LINE:
                updateTempShape(tempShape.get(), startX, startY, endX, endY);
                break;
            case FREEHAND:
                if (tempShape.get() instanceof FreehandModel) {
                    ((FreehandModel) tempShape.get()).addPoint(endX, endY);
                }
                break;
            case BEZIER:
                if (tempShape.get() instanceof BezierCurveModel) {
                    BezierCurveModel bezierCurve = (BezierCurveModel) tempShape.get();
                    // Update the last control point to the current mouse position
                    int lastIndex = bezierCurve.getControlPoints().size() - 1;
                    if (lastIndex >= 0) {
                        bezierCurve.getControlPoints().set(lastIndex, new Point2D(x, y));
                    }
                }
                break;
            default:
                break;
        }

        requestRedraw();
    }

    @Override
    public void onMouseReleased() {
        if (!isDragging) return;
        isDragging = false;

        Tool tool = getSelectedTool();
        switch (tool) {
            case TRIANGLE:
            case QUADRILATERAL:
            case ELLIPSE:
            case LINE:
                if (tempShape != null) {
                    tempShape.get().setStrokeColor(strokeColor.get());
                    tempShape.get().setFillColor(fillColor.get());
                    tempShape.get().setStrokeWidth(strokeWidth.get());
                    addShape(tempShape.get());
                    tempShape.set(null);
                }
                break;
            case FREEHAND:
                if (tempShape.get() instanceof FreehandModel) {
                    tempShape.get().setStrokeColor(strokeColor.get());
                    tempShape.get().setStrokeWidth(strokeWidth.get());
                    addShape(tempShape.get());
                    tempShape.set(null);
                }
                break;
            case BEZIER:
                if (tempShape.get() instanceof BezierCurveModel) {
                    BezierCurveModel bezierCurve = (BezierCurveModel) tempShape.get();
                    if (bezierCurve.getControlPoints().size() >= (bezierDegree + 1)) {
                        bezierCurve.setStrokeColor(strokeColor.get());
                        bezierCurve.setStrokeWidth(strokeWidth.get());
                        addShape(bezierCurve);
                        tempShape.set(null);
                    }
                }
                break;
            case SELECT:
                if (currentShape.get() instanceof BezierCurveModel) {
                    ((BezierCurveModel) currentShape.get()).setSelectedControlPoint(null);
                }
                break;
            default:
                break;
        }

        requestRedraw();
    }

    public ShapeModel getCurrentShape() {
        return currentShape.get();
    }

    public ObjectProperty<ShapeModel> currentShapeProperty() {
        return currentShape;
    }

    private void addShape(ShapeModel shape) {
        shapes.add(shape);
    }

    private void selectShapeAt(double x, double y) {
            for (ShapeModel shape : getShapes()) {
                if (shape.containsPoint(x, y)) {
                    currentShape.set(shape);
                    return;
                }
            }
            currentShape.set(null);
    }

    private void moveCurrentShape(double deltaX, double deltaY) {
        if (currentShape.get() != null) {
            currentShape.get().moveBy(deltaX, deltaY);
        }
    }

    private ShapeModel createShape(Tool tool, double x1, double y1, double x2, double y2) {
        switch (tool) {
            case TRIANGLE:
                double[] xPoints = {x1, x2, x1};
                double[] yPoints = {y1, y2, y2};
                return new TriangleModel(xPoints, yPoints);
            case QUADRILATERAL:
                double[] quadXPoints = {x1, x2, x2, x1};
                double[] quadYPoints = {y1, y1, y2, y2};
                return new QuadrilateralModel(quadXPoints, quadYPoints);
            case ELLIPSE:
                double centerX = (x1 + x2) / 2;
                double centerY = (y1 + y2) / 2;
                double radiusX = Math.abs(x2 - x1) / 2;
                double radiusY = Math.abs(y2 - y1) / 2;
                return new EllipseModel(centerX, centerY, radiusX, radiusY);
            case LINE:
                return new LineModel(x1, y1, x2, y2);
            default:
                return null;
        }
    }

    private void updateTempShape(ShapeModel shape, double x1, double y1, double x2, double y2) {
        if (shape instanceof TriangleModel) {
            double[] xPoints = {x1, x2, x1};
            double[] yPoints = {y1, y2, y2};
            ((TriangleModel) shape).setXPoints(xPoints);
            ((TriangleModel) shape).setYPoints(yPoints);
        } else if (shape instanceof QuadrilateralModel) {
            double[] xPoints = {x1, x2, x2, x1};
            double[] yPoints = {y1, y1, y2, y2};
            ((QuadrilateralModel) shape).setXPoints(xPoints);
            ((QuadrilateralModel) shape).setYPoints(yPoints);
        } else if (shape instanceof EllipseModel) {
            double centerX = (x1 + x2) / 2;
            double centerY = (y1 + y2) / 2;
            double radiusX = Math.abs(x2 - x1) / 2;
            double radiusY = Math.abs(y2 - y1) / 2;
            ((EllipseModel) shape).setCenterX(centerX);
            ((EllipseModel) shape).setCenterY(centerY);
            ((EllipseModel) shape).setRadiusX(radiusX);
            ((EllipseModel) shape).setRadiusY(radiusY);
        } else if (shape instanceof LineModel) {
            ((LineModel) shape).setEnd(x2, y2);
        }
    }

    private void initializeColorListeners() {
        // Update CMYK and HSV when RGB changes
        red.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromRGB();
        });
        green.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromRGB();
        });
        blue.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromRGB();
        });

        // Update RGB when CMYK changes
        cyan.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromCMYK();
        });
        magenta.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromCMYK();
        });
        yellow.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromCMYK();
        });
        key.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromCMYK();
        });

        // Update RGB when HSV changes
        hue.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromHSV();
        });
        saturation.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromHSV();
        });
        value.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateFromHSV();
        });

        // Update stroke and fill colors when selected color changes
        selectedColor.addListener((obs, oldColor, newColor) -> {
            strokeColor.set(newColor);
            fillColor.set(newColor);
        });
    }

    private void updateFromRGB() {
        isUpdating = true;
        try {
            int r = ColorUtils.clamp(red.get(), 0, 255);
            int g = ColorUtils.clamp(green.get(), 0, 255);
            int b = ColorUtils.clamp(blue.get(), 0, 255);

            double[] cmykValues = ColorUtils.rgbToCmyk(r, g, b);
            cyan.set(cmykValues[0]);
            magenta.set(cmykValues[1]);
            yellow.set(cmykValues[2]);
            key.set(cmykValues[3]);

            double[] hsvValues = ColorUtils.rgbToHsv(r, g, b);
            hue.set(hsvValues[0]);
            saturation.set(hsvValues[1]);
            value.set(hsvValues[2]);

            selectedColor.set(Color.rgb(r, g, b));
        } finally {
            isUpdating = false;
        }
    }

    private void updateFromCMYK() {
        isUpdating = true;
        try {
            double c = ColorUtils.clamp(cyan.get(), 0, 100);
            double m = ColorUtils.clamp(magenta.get(), 0, 100);
            double y = ColorUtils.clamp(yellow.get(), 0, 100);
            double k = ColorUtils.clamp(key.get(), 0, 100);

            int[] rgbValues = ColorUtils.cmykToRgb(c, m, y, k);
            red.set(rgbValues[0]);
            green.set(rgbValues[1]);
            blue.set(rgbValues[2]);

            double[] hsvValues = ColorUtils.rgbToHsv(rgbValues[0], rgbValues[1], rgbValues[2]);
            hue.set(hsvValues[0]);
            saturation.set(hsvValues[1]);
            value.set(hsvValues[2]);

            selectedColor.set(Color.rgb(rgbValues[0], rgbValues[1], rgbValues[2]));
        } finally {
            isUpdating = false;
        }
    }

    private void updateFromHSV() {
        isUpdating = true;
        try {
            double h = ColorUtils.clamp(hue.get(), 0, 360);
            double s = ColorUtils.clamp(saturation.get(), 0, 100);
            double v = ColorUtils.clamp(value.get(), 0, 100);

            int[] rgbValues = ColorUtils.hsvToRgb(h, s, v);
            red.set(rgbValues[0]);
            green.set(rgbValues[1]);
            blue.set(rgbValues[2]);

            double[] cmykValues = ColorUtils.rgbToCmyk(rgbValues[0], rgbValues[1], rgbValues[2]);
            cyan.set(cmykValues[0]);
            magenta.set(cmykValues[1]);
            yellow.set(cmykValues[2]);
            key.set(cmykValues[3]);

            selectedColor.set(Color.rgb(rgbValues[0], rgbValues[1], rgbValues[2]));
        } finally {
            isUpdating = false;
        }
    }

    @Override
    public Group createRGBColoredCube(float size) {
        final int STEP = 8;
        Group cubeGroup = new Group();

        for (int r = 0; r <= 255; r += STEP) {
            for (int g = 0; g <= 255; g += STEP) {
                for (int b = 0; b <= 255; b += STEP) {
                    if ((r > 0 && r < 255 - STEP) && (g > 0 && g < 255 - STEP) && (b > 0 && b < 255 - STEP)) {
                        continue;
                    }
                    Color color = Color.rgb(r, g, b);
                    Box cube = createColoredCube( size / (255 / STEP), color);

                    cube.setTranslateX((r - 128) * (size / 255.0));
                    cube.setTranslateY((g - 128) * (size / 255.0));
                    cube.setTranslateZ((b - 128) * (size / 255.0));

                    cubeGroup.getChildren().add(cube);
                }
            }
        }

        return cubeGroup;
    }

    private Box createColoredCube(float cubeSize, Color color) {
        Box cube = new Box(cubeSize, cubeSize, cubeSize);
        PhongMaterial material = new PhongMaterial(color);
        cube.setMaterial(material);
        return cube;
    }

    @Override
    public void applyAddition(double addRed, double addGreen, double addBlue) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.addRGB(currentImageModel.getImage(), addRed, addGreen, addBlue);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applySubtraction(double subRed, double subGreen, double subBlue) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.subtractRGB(currentImageModel.getImage(), subRed, subGreen, subBlue);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyMultiplication(double mulRed, double mulGreen, double mulBlue) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.multiplyRGB(currentImageModel.getImage(), mulRed, mulGreen, mulBlue);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyDivision(double divRed, double divGreen, double divBlue) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.divideRGB(currentImageModel.getImage(), divRed, divGreen, divBlue);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void adjustBrightness(double brightnessChange) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.adjustBrightness(currentImageModel.getImage(), brightnessChange);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyGrayscaleAverage() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.grayscaleAverage(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyGrayscaleMax() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.grayscaleMax(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    // Filter Methods

    @Override
    public void applySmoothingFilter() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.applySmoothingFilter(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyMedianFilter() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.applyMedianFilter(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applySobelFilter() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.applySobelFilter(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyHighPassFilter() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.applyHighPassFilter(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyGaussianBlur(int kernelSize, double sigma) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.applyGaussianBlur(currentImageModel.getImage(), kernelSize, sigma);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyCustomConvolution(double[][] kernel) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.applyConvolutionFilter(currentImageModel.getImage(), kernel);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyHistogramStretching() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.histogramStretching(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyHistogramEqualization() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.histogramEqualization(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyManualThresholding(int threshold) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.manualThresholding(currentImageModel.getImage(), threshold);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyPercentBlackSelection(double percentBlack) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.percentBlackSelection(currentImageModel.getImage(), percentBlack);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyMeanIterativeSelection() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.meanIterativeSelection(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyOtsuThresholding() {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.otsuThresholding(currentImageModel.getImage());
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyNiblackThresholding(int windowSize, double k) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.niblackThresholding(currentImageModel.getImage(), windowSize, k);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applySauvolaThresholding(int windowSize, double k, double r) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.sauvolaThresholding(currentImageModel.getImage(), windowSize, k, r);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyDilation(boolean[][] structuringElement) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.dilation(currentImageModel.getImage(), structuringElement);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyErosion(boolean[][] structuringElement) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.erosion(currentImageModel.getImage(), structuringElement);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyOpening(boolean[][] structuringElement) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.opening(currentImageModel.getImage(), structuringElement);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyClosing(boolean[][] structuringElement) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.closing(currentImageModel.getImage(), structuringElement);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    @Override
    public void applyHitOrMiss(boolean[][] hitMask, boolean[][] missMask) {
        if (currentImageModel != null) {
            WritableImage result = ImageProcessor.hitOrMiss(currentImageModel.getImage(), hitMask, missMask);
            currentImageModel.setImage(result);
            requestRedraw();
        }
    }

    // Load and Save Methods

    @Override
    public void loadImage(String fileName, PNMFormat format) {
        try {
            WritableImage image = PNMImageIO.loadPNM(fileName, format);
            ImageModel imageModel = new ImageModel(image, 0, 0);
            setCurrentImageModel(imageModel);
            Platform.runLater(() -> {
                shapes.clear();
                shapes.add(imageModel);
                requestRedraw();
            });
        } catch (IOException e) {
            logger.error("Failed to load image", e);
        }
    }

    @Override
    public void loadStandardImage(String fileName) {
        try {
            // Load the image using JavaFX's built-in methods
            Image image = new Image(new FileInputStream(fileName));

            // Convert Image to WritableImage
            WritableImage writableImage = new WritableImage(
                    image.getPixelReader(),
                    (int) image.getWidth(),
                    (int) image.getHeight()
            );

            ImageModel imageModel = new ImageModel(writableImage, 0, 0);
            setCurrentImageModel(imageModel);
            Platform.runLater(() -> {
                shapes.clear();
                shapes.add(imageModel);
                requestRedraw();
            });
        } catch (IOException e) {
            logger.error("Failed to load image", e);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Load Error");
                alert.setHeaderText("Failed to load the image.");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }

    @Override
    public void saveImage(String fileName, PNMFormat format, boolean binaryFormat, WritableImage image) {
        try {
            PNMImageIO.savePNM(fileName, image, format, binaryFormat);
        } catch (IOException e) {
            logger.error("Failed to save image", e);
        }
    }

    @Override
    public BooleanProperty isProcessingProperty() {
        return isProcessing;
    }

    @Override
    public void setIsProcessing(boolean isProcessing) {
        this.isProcessing.set(isProcessing);
    }

    @Override
    public void setRedrawCanvasCallback(Runnable callback) {
        redrawCanvasCallback.set(callback);
    }

    private void requestRedraw() {
        if (redrawCanvasCallback.get() != null) {
            Platform.runLater(redrawCanvasCallback.get());
        }
    }
}
