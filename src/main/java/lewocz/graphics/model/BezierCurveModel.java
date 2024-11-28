package lewocz.graphics.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BezierCurveModel extends ShapeModel {
    private List<Point2D> controlPoints;
    private Point2D selectedControlPoint;

    public BezierCurveModel() {
        this.controlPoints = new ArrayList<>();
    }

    public void addControlPoint(double x, double y) {
        controlPoints.add(new Point2D(x, y));
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (controlPoints.size() < 2) return;

        gc.setStroke(getStrokeColor());
        gc.setLineWidth(getStrokeWidth());

        // Draw the Bezier curve
        int steps = 100;
        Point2D prevPoint = calculateBezierPoint(0.0);
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            Point2D currentPoint = calculateBezierPoint(t);
            gc.strokeLine(prevPoint.getX(), prevPoint.getY(), currentPoint.getX(), currentPoint.getY());
            prevPoint = currentPoint;
        }

        // Optionally, draw control points
        gc.setFill(Color.RED);
        for (Point2D cp : controlPoints) {
            gc.fillOval(cp.getX() - 3, cp.getY() - 3, 6, 6);
        }
    }

    private Point2D calculateBezierPoint(double t) {
        List<Point2D> tempPoints = new ArrayList<>(controlPoints);
        int n = tempPoints.size() - 1;

        while (n > 0) {
            for (int i = 0; i < n; i++) {
                double x = (1 - t) * tempPoints.get(i).getX() + t * tempPoints.get(i + 1).getX();
                double y = (1 - t) * tempPoints.get(i).getY() + t * tempPoints.get(i + 1).getY();
                tempPoints.set(i, new Point2D(x, y));
            }
            n--;
        }
        return tempPoints.get(0);
    }

    @Override
    public boolean containsPoint(double x, double y) {
        for (Point2D cp : controlPoints) {
            if (cp.distance(x, y) <= 5) {
                selectedControlPoint = cp;
                return true;
            }
        }
        return false;
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        if (selectedControlPoint != null) {
            int index = controlPoints.indexOf(selectedControlPoint);
            if (index != -1) {
                Point2D newPoint = selectedControlPoint.add(deltaX, deltaY);
                controlPoints.set(index, newPoint);
                selectedControlPoint = newPoint;
            }
        } else {
            // Move entire curve if no control point is selected
            for (int i = 0; i < controlPoints.size(); i++) {
                Point2D cp = controlPoints.get(i).add(deltaX, deltaY);
                controlPoints.set(i, cp);
            }
        }
    }
}
