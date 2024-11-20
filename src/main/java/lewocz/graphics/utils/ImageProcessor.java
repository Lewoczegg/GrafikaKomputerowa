package lewocz.graphics.utils;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.*;

public class ImageProcessor {

    /**
     * Applies a point operation to the input image and returns a new image.
     *
     * @param inputImage The input WritableImage.
     * @param operation  The operation to apply to each pixel.
     * @return A new WritableImage with the operation applied.
     */
    public static WritableImage applyPointOperation(WritableImage inputImage, PixelOperation operation) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage outputImage = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                Color newColor = operation.apply(color);
                writer.setColor(x, y, newColor);
            }
        }

        return outputImage;
    }

    @FunctionalInterface
    public interface PixelOperation {
        Color apply(Color color);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    // Point Transformations

    public static WritableImage addRGB(WritableImage inputImage, double addRed, double addGreen, double addBlue) {
        return applyPointOperation(inputImage, color -> {
            double r = clamp(color.getRed() + addRed / 255.0);
            double g = clamp(color.getGreen() + addGreen / 255.0);
            double b = clamp(color.getBlue() + addBlue / 255.0);
            return new Color(r, g, b, color.getOpacity());
        });
    }

    public static WritableImage subtractRGB(WritableImage inputImage, double subRed, double subGreen, double subBlue) {
        return applyPointOperation(inputImage, color -> {
            double r = clamp(color.getRed() - subRed / 255.0);
            double g = clamp(color.getGreen() - subGreen / 255.0);
            double b = clamp(color.getBlue() - subBlue / 255.0);
            return new Color(r, g, b, color.getOpacity());
        });
    }

    public static WritableImage multiplyRGB(WritableImage inputImage, double mulRed, double mulGreen, double mulBlue) {
        return applyPointOperation(inputImage, color -> {
            double r = clamp(color.getRed() * mulRed);
            double g = clamp(color.getGreen() * mulGreen);
            double b = clamp(color.getBlue() * mulBlue);
            return new Color(r, g, b, color.getOpacity());
        });
    }

    public static WritableImage divideRGB(WritableImage inputImage, double divRed, double divGreen, double divBlue) {
        return applyPointOperation(inputImage, color -> {
            double r = clamp(color.getRed() / divRed);
            double g = clamp(color.getGreen() / divGreen);
            double b = clamp(color.getBlue() / divBlue);
            return new Color(r, g, b, color.getOpacity());
        });
    }

    public static WritableImage adjustBrightness(WritableImage inputImage, double brightnessChange) {
        return applyPointOperation(inputImage, color -> {
            double r = clamp(color.getRed() + brightnessChange);
            double g = clamp(color.getGreen() + brightnessChange);
            double b = clamp(color.getBlue() + brightnessChange);
            return new Color(r, g, b, color.getOpacity());
        });
    }

    public static WritableImage grayscaleAverage(WritableImage inputImage) {
        return applyPointOperation(inputImage, color -> {
            double average = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
            return new Color(average, average, average, color.getOpacity());
        });
    }

    public static WritableImage grayscaleMax(WritableImage inputImage) {
        return applyPointOperation(inputImage, color -> {
            double max = Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()));
            return new Color(max, max, max, color.getOpacity());
        });
    }

    // Filters

    public static WritableImage applySmoothingFilter(WritableImage inputImage) {
        double[][] kernel = {
                {1 / 9.0, 1 / 9.0, 1 / 9.0},
                {1 / 9.0, 1 / 9.0, 1 / 9.0},
                {1 / 9.0, 1 / 9.0, 1 / 9.0}
        };
        return applyConvolutionFilter(inputImage, kernel);
    }

    public static WritableImage applyMedianFilter(WritableImage inputImage) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage outputImage = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                List<Double> reds = new ArrayList<>();
                List<Double> greens = new ArrayList<>();
                List<Double> blues = new ArrayList<>();

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        Color color = reader.getColor(x + dx, y + dy);
                        reds.add(color.getRed());
                        greens.add(color.getGreen());
                        blues.add(color.getBlue());
                    }
                }

                Collections.sort(reds);
                Collections.sort(greens);
                Collections.sort(blues);

                double medianRed = reds.get(4); // Middle of 9 elements
                double medianGreen = greens.get(4);
                double medianBlue = blues.get(4);

                writer.setColor(x, y, new Color(medianRed, medianGreen, medianBlue, 1.0));
            }
        }

        return outputImage;
    }

    public static WritableImage applySobelFilter(WritableImage inputImage) {
        double[][] gx = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };

        double[][] gy = {
                {-1, -2, -1},
                { 0,  0,  0},
                { 1,  2,  1}
        };

        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage outputImage = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sumX = 0;
                double sumY = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        Color color = reader.getColor(x + dx, y + dy);
                        double brightness = color.getBrightness();
                        sumX += gx[dy + 1][dx + 1] * brightness;
                        sumY += gy[dy + 1][dx + 1] * brightness;
                    }
                }

                double magnitude = Math.sqrt(sumX * sumX + sumY * sumY);
                magnitude = clamp(magnitude);

                Color edgeColor = new Color(magnitude, magnitude, magnitude, 1.0);
                writer.setColor(x, y, edgeColor);
            }
        }

        return outputImage;
    }

    public static WritableImage applyHighPassFilter(WritableImage inputImage) {
        double[][] kernel = {
                { 0, -1,  0},
                {-1,  5, -1},
                { 0, -1,  0}
        };

        return applyConvolutionFilter(inputImage, kernel);
    }

    public static WritableImage applyGaussianBlur(WritableImage inputImage, int kernelSize, double sigma) {
        double[][] kernel = generateGaussianKernel(kernelSize, sigma);
        return applyConvolutionFilter(inputImage, kernel);
    }

    private static double[][] generateGaussianKernel(int size, double sigma) {
        double[][] kernel = new double[size][size];
        double sum = 0.0;
        int halfSize = size / 2;

        for (int y = -halfSize; y <= halfSize; y++) {
            for (int x = -halfSize; x <= halfSize; x++) {
                double exponent = -(x * x + y * y) / (2 * sigma * sigma);
                double value = Math.exp(exponent);
                kernel[y + halfSize][x + halfSize] = value;
                sum += value;
            }
        }

        // Normalize kernel
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                kernel[y][x] /= sum;
            }
        }

        return kernel;
    }

    public static WritableImage applyConvolutionFilter(WritableImage inputImage, double[][] kernel) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        int kernelWidth = kernel[0].length;
        int kernelHeight = kernel.length;
        int kHalfWidth = kernelWidth / 2;
        int kHalfHeight = kernelHeight / 2;

        WritableImage outputImage = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double sumRed = 0.0;
                double sumGreen = 0.0;
                double sumBlue = 0.0;
                double sumKernel = 0.0;

                for (int ky = -kHalfHeight; ky <= kHalfHeight; ky++) {
                    for (int kx = -kHalfWidth; kx <= kHalfWidth; kx++) {
                        int pixelX = x + kx;
                        int pixelY = y + ky;

                        if (pixelX < 0 || pixelX >= width || pixelY < 0 || pixelY >= height) {
                            continue;
                        }

                        Color color = reader.getColor(pixelX, pixelY);
                        double kernelValue = kernel[ky + kHalfHeight][kx + kHalfWidth];

                        sumRed += color.getRed() * kernelValue;
                        sumGreen += color.getGreen() * kernelValue;
                        sumBlue += color.getBlue() * kernelValue;
                        sumKernel += kernelValue;
                    }
                }

                if (sumKernel != 0) {
                    sumRed /= sumKernel;
                    sumGreen /= sumKernel;
                    sumBlue /= sumKernel;
                }

                Color newColor = new Color(
                        clamp(sumRed),
                        clamp(sumGreen),
                        clamp(sumBlue),
                        reader.getColor(x, y).getOpacity()
                );
                writer.setColor(x, y, newColor);
            }
        }

        return outputImage;
    }
}
