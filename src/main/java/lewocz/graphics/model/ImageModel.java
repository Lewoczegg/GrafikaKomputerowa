package lewocz.graphics.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

public class ImageModel extends ShapeModel {
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
        double width = image.getWidth();
        double height = image.getHeight();
        return x >= this.x && x <= (this.x + width) && y >= this.y && y <= (this.y + height);
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }
}