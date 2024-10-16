package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuadrilateralModel extends ShapeModel {
    private double[] xPoints = new double[4];
    private double[] yPoints = new double[4];

    public QuadrilateralModel(double[] xPoints, double[] yPoints) {
        if (xPoints.length != 4 || yPoints.length != 4) {
            throw new IllegalArgumentException("Quadrilateral requires exactly 4 x and y points.");
        }
        System.arraycopy(xPoints, 0, this.xPoints, 0, 4);
        System.arraycopy(yPoints, 0, this.yPoints, 0, 4);
    }

    @Override
    public boolean containsPoint(double x, double y) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < 4; i++) {
            polygon.getPoints().addAll(xPoints[i], yPoints[i]);
        }
        return polygon.contains(x, y);
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        for (int i = 0; i < 4; i++) {
            xPoints[i] += deltaX;
            yPoints[i] += deltaY;
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(getStrokeColor());
        gc.setFill(getFillColor());
        gc.setLineWidth(getStrokeWidth());
        gc.strokePolygon(xPoints, yPoints, 4);
        gc.fillPolygon(xPoints, yPoints, 4);
    }
}