package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageModel extends ShapeModel {
    private static final long serialVersionUID = -3681584073044751562L;

    private WritableImage image;
    private double x;
    private double y;

    public ImageModel(WritableImage image, double x, double y) {
        this.image = image;
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.drawImage(image, x, y);
    }

    @Override
    public boolean containsPoint(double x, double y) {
        return x >= this.x && x <= this.x + image.getWidth() &&
                y >= this.y && y <= this.y + image.getHeight();
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }

    @Override
    public void rotate(double angle, double pivotX, double pivotY) {
    }

    @Override
    public void scale(double factor, double pivotX, double pivotY) {
    }
}