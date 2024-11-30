package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BezierCurveModel extends ShapeModel {
    private static final long serialVersionUID = -5702521876949423258L;

    private List<SerializablePoint> controlPoints;
    private SerializablePoint selectedControlPoint;

    public BezierCurveModel() {
        this.controlPoints = new ArrayList<>();
    }

    public void addControlPoint(double x, double y) {
        controlPoints.add(new SerializablePoint(x, y));
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (controlPoints.size() < 2) return;

        gc.setStroke(getStrokeColor());
        gc.setLineWidth(getStrokeWidth());

        // Draw the Bezier curve
        int steps = 100;
        SerializablePoint prevPoint = calculateBezierPoint(0.0);
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            SerializablePoint currentPoint = calculateBezierPoint(t);
            gc.strokeLine(prevPoint.getX(), prevPoint.getY(), currentPoint.getX(), currentPoint.getY());
            prevPoint = currentPoint;
        }

        // Optionally, draw control points
        gc.setFill(Color.RED);
        for (SerializablePoint cp : controlPoints) {
            gc.fillOval(cp.getX() - 3, cp.getY() - 3, 6, 6);
        }
    }

    private SerializablePoint  calculateBezierPoint(double t) {
        List<SerializablePoint > tempPoints = new ArrayList<>(controlPoints);
        int n = tempPoints.size() - 1;

        while (n > 0) {
            for (int i = 0; i < n; i++) {
                double x = (1 - t) * tempPoints.get(i).getX() + t * tempPoints.get(i + 1).getX();
                double y = (1 - t) * tempPoints.get(i).getY() + t * tempPoints.get(i + 1).getY();
                tempPoints.set(i, new SerializablePoint (x, y));
            }
            n--;
        }
        return tempPoints.get(0);
    }

    @Override
    public boolean containsPoint(double x, double y) {
        for (SerializablePoint  cp : controlPoints) {
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
                SerializablePoint  newPoint = selectedControlPoint.add(deltaX, deltaY);
                controlPoints.set(index, newPoint);
                selectedControlPoint = newPoint;
            }
        } else {
            // Move entire curve if no control point is selected
            for (int i = 0; i < controlPoints.size(); i++) {
                SerializablePoint  cp = controlPoints.get(i).add(deltaX, deltaY);
                controlPoints.set(i, cp);
            }
        }
    }

    @Override
    public void rotate(double angle, double pivotX, double pivotY) {
        double radians = Math.toRadians(angle);
        for (int i = 0; i < controlPoints.size(); i++) {
            SerializablePoint point = controlPoints.get(i);
            double translatedX = point.getX() - pivotX;
            double translatedY = point.getY() - pivotY;

            double rotatedX = translatedX * Math.cos(radians) - translatedY * Math.sin(radians);
            double rotatedY = translatedX * Math.sin(radians) + translatedY * Math.cos(radians);

            controlPoints.set(i, new SerializablePoint(rotatedX + pivotX, rotatedY + pivotY));
        }
    }

    @Override
    public void scale(double factor, double pivotX, double pivotY) {
        for (int i = 0; i < controlPoints.size(); i++) {
            SerializablePoint point = controlPoints.get(i);
            double translatedX = point.getX() - pivotX;
            double translatedY = point.getY() - pivotY;

            double scaledX = translatedX * factor;
            double scaledY = translatedY * factor;

            controlPoints.set(i, new SerializablePoint(scaledX + pivotX, scaledY + pivotY));
        }
    }
}
