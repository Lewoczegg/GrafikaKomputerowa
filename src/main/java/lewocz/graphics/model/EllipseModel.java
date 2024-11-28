package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EllipseModel extends ShapeModel {
    private double centerX;
    private double centerY;
    private double radiusX;
    private double radiusY;

    public EllipseModel(double centerX, double centerY, double radiusX, double radiusY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(getStrokeColor());
        gc.setFill(getFillColor());
        gc.setLineWidth(getStrokeWidth());
        gc.strokeOval(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2);
        gc.fillOval(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2);
    }

    @Override
    public boolean containsPoint(double x, double y) {
        double dx = (x - centerX) / radiusX;
        double dy = (y - centerY) / radiusY;
        return dx * dx + dy * dy <= 1;
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        centerX += deltaX;
        centerY += deltaY;
    }

    @Override
    public void rotate(double angle, double pivotX, double pivotY) {
        double radians = Math.toRadians(angle);
        double translatedX = centerX - pivotX;
        double translatedY = centerY - pivotY;

        double rotatedX = translatedX * Math.cos(radians) - translatedY * Math.sin(radians);
        double rotatedY = translatedX * Math.sin(radians) + translatedY * Math.cos(radians);

        centerX = rotatedX + pivotX;
        centerY = rotatedY + pivotY;
    }

    @Override
    public void scale(double factor, double pivotX, double pivotY) {
        double translatedX = centerX - pivotX;
        double translatedY = centerY - pivotY;

        centerX = translatedX * factor + pivotX;
        centerY = translatedY * factor + pivotY;

        radiusX *= factor;
        radiusY *= factor;
    }
}
