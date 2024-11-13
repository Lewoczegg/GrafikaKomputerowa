package lewocz.graphics.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import lewocz.graphics.model.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

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

        requestRedraw();
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

        requestRedraw();
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

        requestRedraw();
    }

    @Override
    public void setRedrawCanvasCallback(Runnable callback) {
        redrawCanvasCallback.set(callback);
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

    private void requestRedraw() {
        if (redrawCanvasCallback.get() != null) {
            Platform.runLater(redrawCanvasCallback.get());
        }
    }

    @Override
    public void saveToPBM(String fileName, boolean binaryFormat, WritableImage image) {
        saveToPNM(fileName, image, "PBM", binaryFormat);
    }

    @Override
    public void saveToPGM(String fileName, boolean binaryFormat, WritableImage image) {
        saveToPNM(fileName, image, "PGM", binaryFormat);
    }

    // New method for saving PPM files
    @Override
    public void saveToPPM(String fileName, boolean binaryFormat, WritableImage image) {
        saveToPNM(fileName, image, "PPM", binaryFormat);
    }

    private void saveToPNM(String fileName, WritableImage image, String formatType, boolean binaryFormat) {
        try {
            if (binaryFormat) {
                saveToBinaryPNM(fileName, image, formatType);
            } else {
                saveToTextPNM(fileName, image, formatType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToTextPNM(String fileName, WritableImage image, String formatType) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int maxColorValue = 255;
        String magicNumber = switch (formatType) {
            case "PGM" -> "P2";
            case "PPM" -> "P3";
            default -> "P1"; // PBM
        };

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // Write header
            writePNMHeader(bw, magicNumber, width, height, maxColorValue);

            PixelReader pixelReader = image.getPixelReader();
            for (int y = 0; y < height; y++) {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < width; x++) {
                    Color color = pixelReader.getColor(x, y);
                    if (formatType.equals("PPM")) {
                        int red = (int) (color.getRed() * maxColorValue);
                        int green = (int) (color.getGreen() * maxColorValue);
                        int blue = (int) (color.getBlue() * maxColorValue);
                        line.append(red).append(" ").append(green).append(" ").append(blue).append(" ");
                    } else if (formatType.equals("PGM")) {
                        int grayValue = (int) (color.getBrightness() * maxColorValue);
                        line.append(grayValue).append(" ");
                    } else {
                        int value = (color.getBrightness() < 0.5) ? 1 : 0;
                        line.append(value).append(" ");
                    }
                }
                bw.write(line.toString().trim() + "\n");
            }
        }
    }

    private void saveToBinaryPNM(String fileName, WritableImage image, String formatType) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int maxColorValue = 255;
        String magicNumber = switch (formatType) {
            case "PGM" -> "P5";
            case "PPM" -> "P6";
            default -> "P4"; // PBM
        };

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            // Write header
            writePNMHeader(fos, magicNumber, width, height, maxColorValue);

            PixelReader pixelReader = image.getPixelReader();

            if (formatType.equals("PPM")) {
                // Save binary PPM data
                byte[] pixelData = new byte[width * height * 3];
                int index = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        pixelData[index++] = (byte) (color.getRed() * maxColorValue);
                        pixelData[index++] = (byte) (color.getGreen() * maxColorValue);
                        pixelData[index++] = (byte) (color.getBlue() * maxColorValue);
                    }
                }
                fos.write(pixelData);
            } else if (formatType.equals("PGM")) {
                // Save binary PGM data
                byte[] pixelData = new byte[width * height];
                int index = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        int grayValue = (int) (color.getBrightness() * maxColorValue);
                        pixelData[index++] = (byte) grayValue;
                    }
                }
                fos.write(pixelData);
            } else {
                // Save binary PBM data
                int bytesPerRow = (width + 7) / 8;
                byte[] rowData = new byte[bytesPerRow];

                for (int y = 0; y < height; y++) {
                    for (int i = 0; i < bytesPerRow; i++) rowData[i] = 0;
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        int bit = (color.getBrightness() < 0.5) ? 1 : 0;
                        int bitPosition = 7 - (x % 8);
                        if (bit == 1) {
                            rowData[x / 8] |= (1 << bitPosition);
                        }
                    }
                    fos.write(rowData);
                }
            }
        }
    }

    private void writePNMHeader(BufferedWriter bw, String magicNumber, int width, int height, int maxColorValue) throws IOException {
        bw.write(magicNumber + "\n");
        bw.write(width + " " + height + "\n");
        if (!magicNumber.equals("P1")) {
            bw.write(maxColorValue + "\n");
        }
    }

    private void writePNMHeader(OutputStream os, String magicNumber, int width, int height, int maxColorValue) throws IOException {
        StringBuilder header = new StringBuilder();
        header.append(magicNumber).append("\n");
        header.append(width).append(" ").append(height).append("\n");
        if (!magicNumber.equals("P4")) {
            header.append(maxColorValue).append("\n");
        }
        os.write(header.toString().getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public void loadFromPBM(String fileName) {
        loadFromPNM(fileName, "PBM");
    }

    @Override
    public void loadFromPGM(String fileName) {
        loadFromPNM(fileName, "PGM");
    }

    @Override
    public void loadFromPPM(String fileName) {
        loadFromPNM(fileName, "PPM");
    }

    private void loadFromPNM(String fileName, String formatType) {
        try (FileInputStream fis = new FileInputStream(fileName);
             PushbackInputStream pbis = new PushbackInputStream(fis, 1024)) {

            // Read the magic number
            String magicNumber = readNextToken(pbis);
            boolean isBinary = magicNumber.equals("P4") || magicNumber.equals("P5") || magicNumber.equals("P6");

            // Validate magic number
            if (!magicNumber.matches("P[1-6]")) {
                throw new IOException("Unsupported PNM format: " + magicNumber);
            }

            // Read the dimensions
            int width = Integer.parseInt(readNextNonCommentToken(pbis));
            int height = Integer.parseInt(readNextNonCommentToken(pbis));

            int maxColorValue = 1; // Default for PBM
            if (magicNumber.equals("P2") || magicNumber.equals("P3") || magicNumber.equals("P5") || magicNumber.equals("P6")) {
                maxColorValue = Integer.parseInt(readNextNonCommentToken(pbis));
            }

            // Consume whitespace after header
            int b;
            do {
                b = pbis.read();
                if (b == -1) {
                    throw new IOException("Unexpected end of file after header");
                }
            } while (Character.isWhitespace(b));
            pbis.unread(b);

            WritableImage image = new WritableImage(width, height);
            PixelWriter pixelWriter = image.getPixelWriter();

            if (!isBinary) {
                BufferedReader br = new BufferedReader(new InputStreamReader(pbis, StandardCharsets.US_ASCII));
                loadFromTextPNM(br, width, height, maxColorValue, pixelWriter, formatType);
            } else {
                loadFromBinaryPNM(pbis, width, height, maxColorValue, pixelWriter, formatType);
            }

            ImageModel imageModel = new ImageModel(image, 0, 0);

            Platform.runLater(() -> {
                shapes.clear();
                shapes.add(imageModel);
                requestRedraw();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromTextPNM(BufferedReader br, int width, int height, int maxColorValue,
                                 PixelWriter pixelWriter, String formatType) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(br);
        tokenizer.resetSyntax();
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('-', '-'); // Allow negative numbers if necessary
        tokenizer.eolIsSignificant(false);
        tokenizer.commentChar('#');

        int x = 0;
        int y = 0;

        while (y < height) {
            if (formatType.equals("PPM")) {
                int red = nextIntToken(tokenizer, maxColorValue, "red");
                int green = nextIntToken(tokenizer, maxColorValue, "green");
                int blue = nextIntToken(tokenizer, maxColorValue, "blue");

                processColorValue(red, green, blue, maxColorValue, pixelWriter, x, y);

                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            } else if (formatType.equals("PGM")) {
                int grayValue = nextIntToken(tokenizer, maxColorValue, "gray");
                double brightness = grayValue / (double) maxColorValue;
                Color color = Color.gray(brightness);
                pixelWriter.setColor(x, y, color);

                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            } else if (formatType.equals("PBM")) {
                int pixelValue = nextIntToken(tokenizer, 1, "pixel");
                if (pixelValue != 0 && pixelValue != 1) {
                    throw new IOException("Invalid pixel value in PBM file: " + pixelValue);
                }
                Color color = (pixelValue == 1) ? Color.BLACK : Color.WHITE;
                pixelWriter.setColor(x, y, color);

                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            }
        }
    }

    private int nextIntToken(StreamTokenizer tokenizer, int maxValue, String componentName) throws IOException {
        int tokenType;
        while ((tokenType = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
            if (tokenType == StreamTokenizer.TT_NUMBER || tokenType == StreamTokenizer.TT_WORD) {
                String tokenStr = tokenizer.sval != null ? tokenizer.sval : String.valueOf((int) tokenizer.nval);
                try {
                    int value = Integer.parseInt(tokenStr);
                    if (value < 0 || value > maxValue) {
                        throw new IOException(componentName + " value out of bounds: " + value);
                    }
                    return value;
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid " + componentName + " value: " + tokenStr, e);
                }
            }
        }
        throw new IOException("Unexpected end of file while reading " + componentName + " value");
    }

    private void processColorValue(int red, int green, int blue, int maxColorValue, PixelWriter pixelWriter, int x, int y) throws IOException {
        if (red < 0 || red > maxColorValue || green < 0 || green > maxColorValue || blue < 0 || blue > maxColorValue) {
            throw new IOException("Color value out of bounds at pixel (" + x + ", " + y + "): (" + red + ", " + green + ", " + blue + ")");
        }
        double r = red / (double) maxColorValue;
        double g = green / (double) maxColorValue;
        double b = blue / (double) maxColorValue;
        Color color = new Color(r, g, b, 1.0);
        pixelWriter.setColor(x, y, color);
    }

    private void loadFromBinaryPNM(InputStream is, int width, int height, int maxColorValue, PixelWriter pixelWriter, String formatType) throws IOException {
        if (formatType.equals("PPM")) {
            int bytesPerSample = (maxColorValue < 256) ? 1 : 2;
            int totalSamples = width * height * 3;
            int totalBytes = totalSamples * bytesPerSample;
            byte[] pixelData = new byte[totalBytes];
            int bytesRead = 0;

            while (bytesRead < totalBytes) {
                int result = is.read(pixelData, bytesRead, totalBytes - bytesRead);
                if (result == -1) {
                    throw new IOException("Unexpected end of file when reading pixel data");
                }
                bytesRead += result;
            }

            int index = 0;
            double maxColorDouble = (double) maxColorValue;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int red, green, blue;

                    if (bytesPerSample == 1) {
                        red = pixelData[index++] & 0xFF;
                        green = pixelData[index++] & 0xFF;
                        blue = pixelData[index++] & 0xFF;
                    } else {
                        red = ((pixelData[index++] & 0xFF) << 8) | (pixelData[index++] & 0xFF);
                        green = ((pixelData[index++] & 0xFF) << 8) | (pixelData[index++] & 0xFF);
                        blue = ((pixelData[index++] & 0xFF) << 8) | (pixelData[index++] & 0xFF);
                    }

                    double r = red / maxColorDouble;
                    double g = green / maxColorDouble;
                    double b = blue / maxColorDouble;

                    Color color = new Color(r, g, b, 1.0);
                    pixelWriter.setColor(x, y, color);
                }
            }
        } else if (formatType.equals("PGM")) {
            int bytesPerSample = (maxColorValue < 256) ? 1 : 2;
            int totalSamples = width * height;
            int totalBytes = totalSamples * bytesPerSample;
            byte[] pixelData = new byte[totalBytes];
            int bytesRead = 0;

            while (bytesRead < totalBytes) {
                int result = is.read(pixelData, bytesRead, totalBytes - bytesRead);
                if (result == -1) {
                    throw new IOException("Unexpected end of file when reading pixel data");
                }
                bytesRead += result;
            }

            int index = 0;
            double maxColorDouble = (double) maxColorValue;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int grayValue;
                    if (bytesPerSample == 1) {
                        grayValue = pixelData[index++] & 0xFF;
                    } else {
                        grayValue = ((pixelData[index++] & 0xFF) << 8) | (pixelData[index++] & 0xFF);
                    }

                    double brightness = grayValue / maxColorDouble;
                    Color color = Color.gray(brightness);
                    pixelWriter.setColor(x, y, color);
                }
            }
        } else if (formatType.equals("PBM")) {
            int rowSize = (width + 7) / 8;
            byte[] rowData = new byte[rowSize];

            for (int y = 0; y < height; y++) {
                int bytesRead = 0;
                while (bytesRead < rowSize) {
                    int result = is.read(rowData, bytesRead, rowSize - bytesRead);
                    if (result == -1) {
                        throw new IOException("Unexpected end of file when reading pixel data");
                    }
                    bytesRead += result;
                }

                for (int x = 0; x < width; x++) {
                    int byteIndex = x / 8;
                    int bitIndex = 7 - (x % 8);
                    int bit = (rowData[byteIndex] >> bitIndex) & 1;
                    Color color = (bit == 0) ? Color.WHITE : Color.BLACK;
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
    }

    private String readNextToken(PushbackInputStream pbis) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;

        // Skip initial whitespace
        while (true) {
            b = pbis.read();
            if (b == -1) {
                break;
            }
            if (!Character.isWhitespace(b)) {
                break;
            }
        }

        if (b == -1) {
            return null;
        }

        // Read the token
        do {
            sb.append((char) b);
            b = pbis.read();
        } while (b != -1 && !Character.isWhitespace(b));

        if (b != -1) {
            pbis.unread(b);
        }

        return sb.toString();
    }

    private String readNextNonCommentToken(PushbackInputStream pbis) throws IOException {
        String token;
        while (true) {
            token = readNextToken(pbis);
            if (token == null) {
                return null;
            }
            if (token.startsWith("#")) {
                skipComment(pbis);
                continue;
            }
            return token;
        }
    }

    private void skipComment(PushbackInputStream pbis) throws IOException {
        int b;
        do {
            b = pbis.read();
        } while (b != -1 && b != '\n');
    }
}
