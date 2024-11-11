package lewocz.graphics.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import lewocz.graphics.model.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class MainViewModel implements IMainViewModel {
    @Getter
    private final ObservableList<ShapeModel> shapes = FXCollections.observableArrayList();

    private ObjectProperty<ShapeModel> tempShape = new SimpleObjectProperty<>();

    private final ObjectProperty<ShapeModel> currentShape = new SimpleObjectProperty<>();
    private final StringProperty toolSelected = new SimpleStringProperty("Select");

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

    public void setToolSelected(String tool) {
        toolSelected.set(tool);
    }

    @Override
    public void onMousePressed(double x, double y) {
        startX = x;
        startY = y;
        isDragging = true;

        String tool = toolSelected.get();
        switch (tool) {
            case "Select":
                selectShapeAt(x, y);
                break;
            case "Triangle":
            case "Quadrilateral":
            case "Ellipse":
            case "Line":
                tempShape.set(createShape(tool, startX, startY, startX, startY));
                break;
            case "Freehand":
                FreehandModel freehand = new FreehandModel();
                freehand.addPoint(x, y);
                tempShape.set(freehand);
                break;
            default:
                break;
        }
    }

    @Override
    public void onMouseDragged(double x, double y) {
        if (!isDragging) return;

        double endX = x;
        double endY = y;

        String tool = toolSelected.get();
        switch (tool) {
            case "Select":
                if (currentShape.get() != null) {
                    double deltaX = endX - startX;
                    double deltaY = endY - startY;
                    moveCurrentShape(deltaX, deltaY);
                    startX = endX;
                    startY = endY;
                }
                break;
            case "Triangle":
            case "Quadrilateral":
            case "Ellipse":
            case "Line":
                updateTempShape(tempShape.get(), startX, startY, endX, endY);
                break;
            case "Freehand":
                if (tempShape.get() instanceof FreehandModel) {
                    ((FreehandModel) tempShape.get()).addPoint(endX, endY);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onMouseReleased() {
        if (!isDragging) return;
        isDragging = false;

        String tool = toolSelected.get();
        switch (tool) {
            case "Triangle":
            case "Quadrilateral":
            case "Ellipse":
            case "Line":
                if (tempShape != null) {
                    tempShape.get().setStrokeColor(strokeColor.get());
                    tempShape.get().setFillColor(fillColor.get());
                    tempShape.get().setStrokeWidth(strokeWidth.get());
                    addShape(tempShape.get());
                    tempShape.set(null);
                }
                break;
            case "Freehand":
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

    private ShapeModel createShape(String tool, double x1, double y1, double x2, double y2) {
        switch (tool) {
            case "Triangle":
                double[] xPoints = {x1, x2, x1};
                double[] yPoints = {y1, y2, y2};
                return new TriangleModel(xPoints, yPoints);
            case "Quadrilateral":
                double[] quadXPoints = {x1, x2, x2, x1};
                double[] quadYPoints = {y1, y1, y2, y2};
                return new QuadrilateralModel(quadXPoints, quadYPoints);
            case "Ellipse":
                double centerX = (x1 + x2) / 2;
                double centerY = (y1 + y2) / 2;
                double radiusX = Math.abs(x2 - x1) / 2;
                double radiusY = Math.abs(y2 - y1) / 2;
                return new EllipseModel(centerX, centerY, radiusX, radiusY);
            case "Line":
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

    // Initialize listeners for color properties
    private void initializeColorListeners() {
        // Update CMYK and HSV when RGB changes
        red.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateCMYKFromRGB();
            updateHSVFromRGB();
            updateSelectedColor();
        });
        green.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateCMYKFromRGB();
            updateHSVFromRGB();
            updateSelectedColor();
        });
        blue.addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            updateCMYKFromRGB();
            updateHSVFromRGB();
            updateSelectedColor();
        });

        // Update RGB when CMYK changes
        cyan.addListener((obs, oldVal, newVal) -> {
            updateRGBFromCMYK();
            updateHSVFromRGB();
            updateSelectedColor();
        });
        magenta.addListener((obs, oldVal, newVal) -> {
            updateRGBFromCMYK();
            updateHSVFromRGB();
            updateSelectedColor();
        });
        yellow.addListener((obs, oldVal, newVal) -> {
            updateRGBFromCMYK();
            updateHSVFromRGB();
            updateSelectedColor();
        });
        key.addListener((obs, oldVal, newVal) -> {
            updateRGBFromCMYK();
            updateHSVFromRGB();
            updateSelectedColor();
        });

        // Update RGB when HSV changes
        hue.addListener((obs, oldVal, newVal) -> {
            updateRGBFromHSV();
            updateCMYKFromRGB();
            updateSelectedColor();
        });
        saturation.addListener((obs, oldVal, newVal) -> {
            updateRGBFromHSV();
            updateCMYKFromRGB();
            updateSelectedColor();
        });
        value.addListener((obs, oldVal, newVal) -> {
            updateRGBFromHSV();
            updateCMYKFromRGB();
            updateSelectedColor();
        });

        // Update stroke and fill colors when selected color changes
        selectedColor.addListener((obs, oldColor, newColor) -> {
            strokeColor.set(newColor);
            fillColor.set(newColor);
        });
    }

    private void updateSelectedColor() {
        int r = clamp(red.get(), 0, 255);
        int g = clamp(green.get(), 0, 255);
        int b = clamp(blue.get(), 0, 255);
        selectedColor.set(Color.rgb(r, g, b));
    }

    private void updateCMYKFromRGB() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            double r = red.get() / 255.0;
            double g = green.get() / 255.0;
            double b = blue.get() / 255.0;

            double k = 1.0 - Math.max(r, Math.max(g, b));
            double c = (1.0 - r - k) / (1.0 - k);
            double m = (1.0 - g - k) / (1.0 - k);
            double y = (1.0 - b - k) / (1.0 - k);

            if (Double.isNaN(c)) c = 0;
            if (Double.isNaN(m)) m = 0;
            if (Double.isNaN(y)) y = 0;

            cyan.set(c * 100);
            magenta.set(m * 100);
            yellow.set(y * 100);
            key.set(k * 100);
        } finally {
            isUpdating = false;
        }
    }

    private void updateHSVFromRGB() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            double r = red.get() / 255.0;
            double g = green.get() / 255.0;
            double b = blue.get() / 255.0;

            double max = Math.max(r, Math.max(g, b));
            double min = Math.min(r, Math.min(g, b));
            double delta = max - min;

            double h, s, v;
            v = max;

            if (delta == 0) {
                h = 0;
            } else if (max == r) {
                h = 60 * (((g - b) / delta) % 6);
            } else if (max == g) {
                h = 60 * (((b - r) / delta) + 2);
            } else {
                h = 60 * (((r - g) / delta) + 4);
            }

            if (h < 0) h += 360;

            s = (max == 0) ? 0 : (delta / max);

            hue.set(h);
            saturation.set(s * 100);
            value.set(v * 100);
        } finally {
            isUpdating = false;
        }
    }

    private void updateRGBFromCMYK() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            double c = cyan.get() / 100;
            double m = magenta.get() / 100;
            double y = yellow.get() / 100;
            double k = key.get() / 100;

            int r = (int)((1 - Math.min(1, c * (1 - k) + k)) * 255);
            int g = (int)((1 - Math.min(1, m * (1 - k) + k)) * 255);
            int b = (int)((1 - Math.min(1, y * (1 - k) + k)) * 255);

            red.set(clamp(r, 0, 255));
            green.set(clamp(g, 0, 255));
            blue.set(clamp(b, 0, 255));
        } finally {
            isUpdating = false;
        }
    }

    private void updateRGBFromHSV() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            double h = hue.get();
            double s = saturation.get() / 100;
            double v = value.get() / 100;

            double c = v * s;
            double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
            double m = v - c;

            double r1 = 0, g1 = 0, b1 = 0;

            if (h >= 0 && h < 60) {
                r1 = c; g1 = x; b1 = 0;
            } else if (h >= 60 && h < 120) {
                r1 = x; g1 = c; b1 = 0;
            } else if (h >= 120 && h < 180) {
                r1 = 0; g1 = c; b1 = x;
            } else if (h >= 180 && h < 240) {
                r1 = 0; g1 = x; b1 = c;
            } else if (h >= 240 && h < 300) {
                r1 = x; g1 = 0; b1 = c;
            } else if (h >= 300 && h < 360) {
                r1 = c; g1 = 0; b1 = x;
            }

            int r = (int)((r1 + m) * 255);
            int g = (int)((g1 + m) * 255);
            int b = (int)((b1 + m) * 255);

            red.set(clamp(r, 0, 255));
            green.set(clamp(g, 0, 255));
            blue.set(clamp(b, 0, 255));
        } finally {
            isUpdating = false;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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
}
