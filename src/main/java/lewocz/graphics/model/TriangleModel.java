package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriangleModel extends ShapeModel {
    private static final long serialVersionUID = 985137658578246069L;

    private double[] xPoints = new double[3];
    private double[] yPoints = new double[3];

    public TriangleModel(double[] xPoints, double[] yPoints) {
        System.arraycopy(xPoints, 0, this.xPoints, 0, 3);
        System.arraycopy(yPoints, 0, this.yPoints, 0, 3);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(getStrokeColor());
        gc.setFill(getFillColor());
        gc.setLineWidth(getStrokeWidth());
        gc.strokePolygon(xPoints, yPoints, 3);
        gc.fillPolygon(xPoints, yPoints, 3);
    }

    @Override
    public boolean containsPoint(double x, double y) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < 3; i++) {
            polygon.getPoints().addAll(xPoints[i], yPoints[i]);
        }
        return polygon.contains(x, y);
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        for (int i = 0; i < 3; i++) {
            xPoints[i] += deltaX;
            yPoints[i] += deltaY;
        }
    }

    @Override
    public void rotate(double angle, double pivotX, double pivotY) {
        double radians = Math.toRadians(angle);
        for (int i = 0; i < 3; i++) {
            double dx = xPoints[i] - pivotX;
            double dy = yPoints[i] - pivotY;

            double rotatedX = dx * Math.cos(radians) - dy * Math.sin(radians);
            double rotatedY = dx * Math.sin(radians) + dy * Math.cos(radians);

            xPoints[i] = rotatedX + pivotX;
            yPoints[i] = rotatedY + pivotY;
        }
    }

    @Override
    public void scale(double factor, double pivotX, double pivotY) {
        for (int i = 0; i < 3; i++) {
            xPoints[i] = (xPoints[i] - pivotX) * factor + pivotX;
            yPoints[i] = (yPoints[i] - pivotY) * factor + pivotY;
        }
    }
}
