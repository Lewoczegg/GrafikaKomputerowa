package lewocz.graphics.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PolygonModel extends ShapeModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Point2D> points;

    public PolygonModel(List<Point2D> points, Color strokeColor, Color fillColor, double strokeWidth) {
        this.points = new ArrayList<>(points);
        setStrokeColor(strokeColor);
        setFillColor(fillColor);
        setStrokeWidth(strokeWidth);
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (points.size() < 2) {
            return;
        }

        gc.setStroke(getStrokeColor());
        gc.setFill(getFillColor());
        gc.setLineWidth(getStrokeWidth());

        double[] xPoints = points.stream().mapToDouble(Point2D::getX).toArray();
        double[] yPoints = points.stream().mapToDouble(Point2D::getY).toArray();

        gc.fillPolygon(xPoints, yPoints, points.size());
        gc.strokePolygon(xPoints, yPoints, points.size());
    }

    @Override
    public boolean containsPoint(double x, double y) {
        Polygon polygon = new Polygon();
        for (Point2D point : points) {
            polygon.getPoints().addAll(point.getX(), point.getY());
        }
        return polygon.contains(x, y);
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);
            points.set(i, point.add(deltaX, deltaY));
        }
    }

    public void rotate(double angle, double pivotX, double pivotY) {
        double radians = Math.toRadians(angle);
        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);
            double translatedX = point.getX() - pivotX;
            double translatedY = point.getY() - pivotY;

            double rotatedX = translatedX * Math.cos(radians) - translatedY * Math.sin(radians);
            double rotatedY = translatedX * Math.sin(radians) + translatedY * Math.cos(radians);

            points.set(i, new Point2D(rotatedX + pivotX, rotatedY + pivotY));
        }
    }

    public void scale(double factor, double pivotX, double pivotY) {
        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);
            double translatedX = point.getX() - pivotX;
            double translatedY = point.getY() - pivotY;

            double scaledX = translatedX * factor;
            double scaledY = translatedY * factor;

            points.set(i, new Point2D(scaledX + pivotX, scaledY + pivotY));
        }
    }
}