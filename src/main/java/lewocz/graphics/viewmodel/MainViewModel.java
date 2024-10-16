package lewocz.graphics.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import lewocz.graphics.model.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class MainViewModel {
    @Getter
    private final ObservableList<ShapeModel> shapes = FXCollections.observableArrayList();

    private ObjectProperty<ShapeModel> tempShape = new SimpleObjectProperty<>();

    private final ObjectProperty<ShapeModel> currentShape = new SimpleObjectProperty<>();
    private final StringProperty toolSelected = new SimpleStringProperty("Select");

    private final ObjectProperty<Color> strokeColor = new SimpleObjectProperty<>(Color.RED);
    private final ObjectProperty<Color> fillColor = new SimpleObjectProperty<>(Color.RED);
    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(1.0);

    private double startX, startY;
    private boolean isDragging;

    public ShapeModel getTempShape() {
        return tempShape.get();
    }

    public void setToolSelected(String tool) {
        toolSelected.set(tool);
    }

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
}
