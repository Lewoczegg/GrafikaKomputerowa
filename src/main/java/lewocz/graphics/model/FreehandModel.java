package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.ArrayList;
import java.util.List;

public class FreehandModel extends ShapeModel {
    private static final long serialVersionUID = 7836716480390598071L;

    private final List<Double> xPoints = new ArrayList<>();
    private final List<Double> yPoints = new ArrayList<>();

    public FreehandModel() {
        super();
    }

    public void addPoint(double x, double y) {
        xPoints.add(x);
        yPoints.add(y);
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (xPoints.size() < 2) return;

        gc.setStroke(getStrokeColor());
        gc.setLineWidth(getStrokeWidth());
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        gc.beginPath();
        gc.moveTo(xPoints.get(0), yPoints.get(0));

        for (int i = 1; i < xPoints.size(); i++) {
            gc.lineTo(xPoints.get(i), yPoints.get(i));
        }

        gc.stroke();
    }

    @Override
    public boolean containsPoint(double x, double y) {
        double tolerance = getStrokeWidth() / 2 + 2;

        for (int i = 0; i < xPoints.size() - 1; i++) {
            double x1 = xPoints.get(i);
            double y1 = yPoints.get(i);
            double x2 = xPoints.get(i + 1);
            double y2 = yPoints.get(i + 1);

            if (isPointNearLine(x, y, x1, y1, x2, y2, tolerance)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        for (int i = 0; i < xPoints.size(); i++) {
            xPoints.set(i, xPoints.get(i) + deltaX);
            yPoints.set(i, yPoints.get(i) + deltaY);
        }
    }

    @Override
    public void rotate(double angle, double pivotX, double pivotY) {
        double radians = Math.toRadians(angle);
        for (int i = 0; i < xPoints.size(); i++) {
            double x = xPoints.get(i) - pivotX;
            double y = yPoints.get(i) - pivotY;

            double rotatedX = x * Math.cos(radians) - y * Math.sin(radians);
            double rotatedY = x * Math.sin(radians) + y * Math.cos(radians);

            xPoints.set(i, rotatedX + pivotX);
            yPoints.set(i, rotatedY + pivotY);
        }
    }

    @Override
    public void scale(double factor, double pivotX, double pivotY) {
        for (int i = 0; i < xPoints.size(); i++) {
            double x = xPoints.get(i) - pivotX;
            double y = yPoints.get(i) - pivotY;

            xPoints.set(i, x * factor + pivotX);
            yPoints.set(i, y * factor + pivotY);
        }
    }

    private boolean isPointNearLine(double px, double py, double x1, double y1, double x2, double y2, double tolerance) {
        double distance = distanceFromPointToLine(px, py, x1, y1, x2, y2);
        return distance <= tolerance;
    }

    private double distanceFromPointToLine(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? dot / lenSq : -1;

        double xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}