package lewocz.graphics.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import lewocz.graphics.model.*;
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
                    moveCurrentShape(deltaX, deltaY);
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
            default:
                break;
        }

        requestRedraw();
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
    public void saveImage(String fileName, PNMFormat format, boolean binaryFormat, WritableImage image) {
        try {
            PNMImageIO.savePNM(fileName, image, format, binaryFormat);
        } catch (IOException e) {
            logger.error("Failed to save image", e);
        }
    }

    @Override
    public void loadImage(String fileName, PNMFormat format) {
        try {
            WritableImage image = PNMImageIO.loadPNM(fileName, format);
            ImageModel imageModel = new ImageModel(image, 0, 0);

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
    public void setRedrawCanvasCallback(Runnable callback) {
        redrawCanvasCallback.set(callback);
    }

    private void requestRedraw() {
        if (redrawCanvasCallback.get() != null) {
            Platform.runLater(redrawCanvasCallback.get());
        }
    }
}
