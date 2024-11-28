package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ShapeModel {
    private Color strokeColor = Color.BLACK;
    private Color fillColor = Color.TRANSPARENT;
    private double strokeWidth = 1.0;

    public abstract void draw(GraphicsContext gc);
    public abstract boolean containsPoint(double x, double y);
    public abstract void moveBy(double deltaX, double deltaY);
    public abstract void rotate(double angle, double pivotX, double pivotY);
    public abstract void scale(double factor, double pivotX, double pivotY);
}
