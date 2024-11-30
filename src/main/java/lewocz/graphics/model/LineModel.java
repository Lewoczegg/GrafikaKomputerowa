package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;

public class LineModel extends ShapeModel {
    private static final long serialVersionUID = 8806187858083320281L;

    private double startX, startY, endX, endY;

    public LineModel(double startX, double startY, double endX, double endY) {
        super();
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public void setStart(double x, double y) {
        this.startX = x;
        this.startY = y;
    }

    public void setEnd(double x, double y) {
        this.endX = x;
        this.endY = y;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(getStrokeColor());
        gc.setLineWidth(getStrokeWidth());
        gc.strokeLine(startX, startY, endX, endY);
    }

    @Override
    public boolean containsPoint(double x, double y) {
        // Implement hit detection for a line
        double tolerance = 5.0; // pixels
        return isPointNearLine(x, y, startX, startY, endX, endY, tolerance);
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        this.startX += deltaX;
        this.startY += deltaY;
        this.endX += deltaX;
        this.endY += deltaY;
    }

    @Override
    public void rotate(double angle, double pivotX, double pivotY) {
        double radians = Math.toRadians(angle);

        double dx = startX - pivotX;
        double dy = startY - pivotY;
        double rotatedX = dx * Math.cos(radians) - dy * Math.sin(radians);
        double rotatedY = dx * Math.sin(radians) + dy * Math.cos(radians);
        startX = rotatedX + pivotX;
        startY = rotatedY + pivotY;

        dx = endX - pivotX;
        dy = endY - pivotY;
        rotatedX = dx * Math.cos(radians) - dy * Math.sin(radians);
        rotatedY = dx * Math.sin(radians) + dy * Math.cos(radians);
        endX = rotatedX + pivotX;
        endY = rotatedY + pivotY;
    }

    @Override
    public void scale(double factor, double pivotX, double pivotY) {
        startX = (startX - pivotX) * factor + pivotX;
        startY = (startY - pivotY) * factor + pivotY;

        endX = (endX - pivotX) * factor + pivotX;
        endY = (endY - pivotY) * factor + pivotY;
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
        double len_sq = C * C + D * D;
        double param = (len_sq != 0) ? dot / len_sq : -1;

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