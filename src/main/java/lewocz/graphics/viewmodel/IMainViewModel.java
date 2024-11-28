package lewocz.graphics.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lewocz.graphics.model.PNMFormat;
import lewocz.graphics.model.ShapeModel;
import lewocz.graphics.model.Tool;


public interface IMainViewModel {
    // Observable List of Shapes
    ObservableList<ShapeModel> getShapes();

    ShapeModel getTempShape();

    // Tool Selection Property
    void setSelectedTool(Tool tool);

    // RGB Properties
    IntegerProperty redProperty();
    IntegerProperty greenProperty();
    IntegerProperty blueProperty();

    // CMYK Properties
    DoubleProperty cyanProperty();
    DoubleProperty magentaProperty();
    DoubleProperty yellowProperty();
    DoubleProperty keyProperty();

    // HSV Properties
    DoubleProperty hueProperty();
    DoubleProperty saturationProperty();
    DoubleProperty valueProperty();

    // Selected Color Property
    ObjectProperty<Color> selectedColorProperty();

    // Mouse Event Handlers
    void onMousePressed(double x, double y);
    void onMouseDragged(double x, double y);
    void onMouseReleased();

    Group createRGBColoredCube(float size);

    void setRedrawCanvasCallback(Runnable callback);

    void saveImage(String fileName, PNMFormat format, boolean binaryFormat, WritableImage image);
    void loadImage(String fileName, PNMFormat format);
    void loadStandardImage(String fileName);

    void applyAddition(double addRed, double addGreen, double addBlue);
    void applySubtraction(double subRed, double subGreen, double subBlue);
    void applyMultiplication(double mulRed, double mulGreen, double mulBlue);
    void applyDivision(double divRed, double divGreen, double divBlue);
    void adjustBrightness(double brightnessChange);
    void applyGrayscaleAverage();
    void applyGrayscaleMax();
    void applySmoothingFilter();
    void applyMedianFilter();
    void applySobelFilter();
    void applyHighPassFilter();
    void applyGaussianBlur(int kernelSize, double sigma);
    void applyCustomConvolution(double[][] kernel);
    void applyHistogramStretching();
    void applyHistogramEqualization();
    void applyManualThresholding(int threshold);
    void applyPercentBlackSelection(double percentBlack);
    void applyMeanIterativeSelection();
    void applyOtsuThresholding();
    void applyNiblackThresholding(int windowSize, double k);
    void applySauvolaThresholding(int windowSize, double k, double r);
    void applyDilation(boolean[][] structuringElement);
    void applyErosion(boolean[][] structuringElement);
    void applyOpening(boolean[][] structuringElement);
    void applyClosing(boolean[][] structuringElement);
    void applyHitOrMiss(boolean[][] hitMask, boolean[][] missMask);

    BooleanProperty isProcessingProperty();
    void setIsProcessing(boolean isProcessing);

    void setBezierDegree(int degree);
    ShapeModel getCurrentShape();
    ObjectProperty<ShapeModel> currentShapeProperty();
    void finishPolygon();
    void rotateShape(double angle, double pivotX, double pivotY);
    void scaleShape(double factor, double pivotX, double pivotY);
}
