package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriangleModel extends ShapeModel {
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
}
