package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public abstract class ShapeModel implements Serializable {
    private static final long serialVersionUID = 3424372029161197990L;

    private transient Color strokeColor = Color.BLACK;
    private transient Color fillColor = Color.TRANSPARENT;
    private double strokeWidth = 1.0;

    public abstract void draw(GraphicsContext gc);
    public abstract boolean containsPoint(double x, double y);
    public abstract void moveBy(double deltaX, double deltaY);
    public abstract void rotate(double angle, double pivotX, double pivotY);
    public abstract void scale(double factor, double pivotX, double pivotY);
}
