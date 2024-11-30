package lewocz.graphics.model;

import java.io.Serializable;

public class SerializablePoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;

    public SerializablePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Getters and setters
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    // Utility methods
    public SerializablePoint add(double deltaX, double deltaY) {
        return new SerializablePoint(x + deltaX, y + deltaY);
    }

    public double distance(double x2, double y2) {
        double dx = x - x2;
        double dy = y - y2;
        return Math.hypot(dx, dy);
    }

    public double distance(SerializablePoint other) {
        return distance(other.x, other.y);
    }
}