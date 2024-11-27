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

    public static WritableImage histogramStretching(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();
        WritableImage result = new WritableImage(width, height);
        PixelWriter writer = result.getPixelWriter();

        double minIntensity = 1.0;
        double maxIntensity = 0.0;

        // Find min and max intensity values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                double intensity = color.getBrightness();
                if (intensity < minIntensity) minIntensity = intensity;
                if (intensity > maxIntensity) maxIntensity = intensity;
            }
        }

        double intensityRange = maxIntensity - minIntensity;
        if (intensityRange == 0) {
            // Avoid division by zero if the image has constant intensity
            return image;
        }

        // Apply histogram stretching
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                double intensity = color.getBrightness();
                double stretchedIntensity = (intensity - minIntensity) / intensityRange;
                Color newColor = Color.gray(stretchedIntensity);
                writer.setColor(x, y, newColor);
            }
        }

        return result;
    }

    public static WritableImage histogramEqualization(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int numPixels = width * height;

        PixelReader reader = image.getPixelReader();
        WritableImage result = new WritableImage(width, height);
        PixelWriter writer = result.getPixelWriter();

        // Assuming a grayscale image
        int[] histogram = new int[256];
        double[] cdf = new double[256];

        // Compute histogram
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                int intensity = (int) (color.getBrightness() * 255);
                histogram[intensity]++;
            }
        }

        // Compute cumulative distribution function (CDF)
        cdf[0] = histogram[0] / (double) numPixels;
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + histogram[i] / (double) numPixels;
        }

        // Ensure the last value of CDF is exactly 1.0 to prevent floating-point errors
        cdf[255] = 1.0;

        // Apply histogram equalization
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                int intensity = (int) (color.getBrightness() * 255);
                double equalizedIntensity = cdf[intensity];

                // Clamp the intensity to [0.0, 1.0]
                equalizedIntensity = Math.min(equalizedIntensity, 1.0);

                Color newColor = Color.gray(equalizedIntensity);
                writer.setColor(x, y, newColor);
            }
        }

        return result;
    }

    public static WritableImage manualThresholding(WritableImage image, int threshold) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();
        WritableImage result = new WritableImage(width, height);
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                double intensity = color.getBrightness() * 255;
                if (intensity < threshold) {
                    writer.setColor(x, y, Color.BLACK);
                } else {
                    writer.setColor(x, y, Color.WHITE);
                }
            }
        }

        return result;
    }

    public static WritableImage percentBlackSelection(WritableImage image, double percentBlack) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int numPixels = width * height;
        int desiredBlackPixels = (int) (numPixels * (percentBlack / 100.0));

        PixelReader reader = image.getPixelReader();

        // Create a histogram of intensities
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                int intensity = (int) (color.getBrightness() * 255);
                histogram[intensity]++;
            }
        }

        // Find the threshold that results in the desired number of black pixels
        int cumulativeSum = 0;
        int threshold = 0;
        for (int i = 0; i < 256; i++) {
            cumulativeSum += histogram[i];
            if (cumulativeSum >= desiredBlackPixels) {
                threshold = i;
                break;
            }
        }

        // Apply thresholding
        return manualThresholding(image, threshold);
    }

    public static WritableImage meanIterativeSelection(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();

        // Initialize threshold with the mean intensity of the image
        double totalIntensity = 0;
        int numPixels = width * height;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                totalIntensity += color.getBrightness() * 255;
            }
        }

        double threshold = totalIntensity / numPixels;
        double previousThreshold;
        double epsilon = 0.5; // Convergence criterion

        do {
            previousThreshold = threshold;
            double sumForeground = 0;
            int countForeground = 0;
            double sumBackground = 0;
            int countBackground = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double intensity = reader.getColor(x, y).getBrightness() * 255;
                    if (intensity > threshold) {
                        sumForeground += intensity;
                        countForeground++;
                    } else {
                        sumBackground += intensity;
                        countBackground++;
                    }
                }
            }

            double meanForeground = countForeground > 0 ? sumForeground / countForeground : 0;
            double meanBackground = countBackground > 0 ? sumBackground / countBackground : 0;

            threshold = (meanForeground + meanBackground) / 2;

        } while (Math.abs(threshold - previousThreshold) >= epsilon);

        // Apply thresholding
        return manualThresholding(image, (int) threshold);
    }

    public static WritableImage otsuThresholding(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();

        int[] histogram = new int[256];
        int totalPixels = width * height;

        // Compute histogram
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int intensity = (int) (reader.getColor(x, y).getBrightness() * 255);
                histogram[intensity]++;
            }
        }

        // Total sum of pixel intensities
        double sumTotal = 0;
        for (int t = 0; t < 256; t++) {
            sumTotal += t * histogram[t];
        }

        double sumBackground = 0;
        int weightBackground = 0;
        int weightForeground = 0;

        double maxVariance = 0;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            weightBackground += histogram[t];
            if (weightBackground == 0) continue;

            weightForeground = totalPixels - weightBackground;
            if (weightForeground == 0) break;

            sumBackground += t * histogram[t];

            double meanBackground = sumBackground / weightBackground;
            double meanForeground = (sumTotal - sumBackground) / weightForeground;

            double betweenClassVariance = weightBackground * weightForeground * Math.pow(meanBackground - meanForeground, 2);

            if (betweenClassVariance > maxVariance) {
                maxVariance = betweenClassVariance;
                threshold = t;
            }
        }

        // Apply thresholding
        return manualThresholding(image, threshold);
    }

    public static WritableImage niblackThresholding(WritableImage image, int windowSize, double k) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();
        WritableImage result = new WritableImage(width, height);
        PixelWriter writer = result.getPixelWriter();

        int halfWindow = windowSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double sum = 0;
                double sumSq = 0;
                int count = 0;

                // Compute mean and standard deviation in the window
                for (int wy = -halfWindow; wy <= halfWindow; wy++) {
                    for (int wx = -halfWindow; wx <= halfWindow; wx++) {
                        int nx = x + wx;
                        int ny = y + wy;

                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            double intensity = reader.getColor(nx, ny).getBrightness() * 255;
                            sum += intensity;
                            sumSq += intensity * intensity;
                            count++;
                        }
                    }
                }

                double mean = sum / count;
                double variance = (sumSq / count) - (mean * mean);
                double stdDev = Math.sqrt(variance);

                double threshold = mean + k * stdDev;

                double pixelIntensity = reader.getColor(x, y).getBrightness() * 255;

                if (pixelIntensity < threshold) {
                    writer.setColor(x, y, Color.BLACK);
                } else {
                    writer.setColor(x, y, Color.WHITE);
                }
            }
        }

        return result;
    }

    public static WritableImage sauvolaThresholding(WritableImage image, int windowSize, double k, double r) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();
        WritableImage result = new WritableImage(width, height);
        PixelWriter writer = result.getPixelWriter();

        int halfWindow = windowSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double sum = 0;
                double sumSq = 0;
                int count = 0;

                // Compute mean and standard deviation in the window
                for (int wy = -halfWindow; wy <= halfWindow; wy++) {
                    for (int wx = -halfWindow; wx <= halfWindow; wx++) {
                        int nx = x + wx;
                        int ny = y + wy;

                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            double intensity = reader.getColor(nx, ny).getBrightness() * 255;
                            sum += intensity;
                            sumSq += intensity * intensity;
                            count++;
                        }
                    }
                }

                double mean = sum / count;
                double variance = (sumSq / count) - (mean * mean);
                double stdDev = Math.sqrt(variance);

                double threshold = mean * (1 + k * ((stdDev / r) - 1));

                double pixelIntensity = reader.getColor(x, y).getBrightness() * 255;

                if (pixelIntensity < threshold) {
                    writer.setColor(x, y, Color.BLACK);
                } else {
                    writer.setColor(x, y, Color.WHITE);
                }
            }
        }

        return result;
    }

    public static WritableImage dilation(WritableImage image, boolean[][] structuringElement) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage outputImage = new WritableImage(width, height);
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = outputImage.getPixelWriter();

        int seWidth = structuringElement[0].length;
        int seHeight = structuringElement.length;

        int xOrigin = seWidth / 2;
        int yOrigin = seHeight / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double maxR = 0.0;
                double maxG = 0.0;
                double maxB = 0.0;
                double maxA = 0.0;

                for (int j = 0; j < seHeight; j++) {
                    for (int i = 0; i < seWidth; i++) {
                        if (structuringElement[j][i]) {
                            int offsetX = i - xOrigin;
                            int offsetY = j - yOrigin;

                            int imageX = x - offsetX;
                            int imageY = y - offsetY;

                            if (imageX >= 0 && imageX < width && imageY >= 0 && imageY < height) {
                                Color color = pixelReader.getColor(imageX, imageY);

                                if (color.getRed() > maxR) maxR = color.getRed();
                                if (color.getGreen() > maxG) maxG = color.getGreen();
                                if (color.getBlue() > maxB) maxB = color.getBlue();
                                if (color.getOpacity() > maxA) maxA = color.getOpacity();
                            }
                        }
                    }
                }

                Color maxColor = new Color(maxR, maxG, maxB, maxA);
                pixelWriter.setColor(x, y, maxColor);
            }
        }

        return outputImage;
    }

    public static WritableImage erosion(WritableImage image, boolean[][] structuringElement) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage outputImage = new WritableImage(width, height);
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = outputImage.getPixelWriter();

        int seWidth = structuringElement[0].length;
        int seHeight = structuringElement.length;

        int xOrigin = seWidth / 2;
        int yOrigin = seHeight / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double minR = 1.0;
                double minG = 1.0;
                double minB = 1.0;
                double minA = 1.0;

                for (int j = 0; j < seHeight; j++) {
                    for (int i = 0; i < seWidth; i++) {
                        if (structuringElement[j][i]) {
                            int offsetX = i - xOrigin;
                            int offsetY = j - yOrigin;

                            int imageX = x + offsetX;
                            int imageY = y + offsetY;

                            if (imageX >= 0 && imageX < width && imageY >= 0 && imageY < height) {
                                Color color = pixelReader.getColor(imageX, imageY);

                                if (color.getRed() < minR) minR = color.getRed();
                                if (color.getGreen() < minG) minG = color.getGreen();
                                if (color.getBlue() < minB) minB = color.getBlue();
                                if (color.getOpacity() < minA) minA = color.getOpacity();
                            } else {
                                // If any of the pixels are outside the image bounds,
                                // according to morphological erosion, we consider the
                                // background (which can be considered as maximum intensity).
                                // Therefore, we can set min values to 0 to reflect erosion at the edges.
                                minR = 0.0;
                                minG = 0.0;
                                minB = 0.0;
                                minA = 0.0;
                            }
                        }
                    }
                }

                Color minColor = new Color(minR, minG, minB, minA);
                pixelWriter.setColor(x, y, minColor);
            }
        }

        return outputImage;
    }

    public static WritableImage opening(WritableImage image, boolean[][] structuringElement) {
        WritableImage eroded = erosion(image, structuringElement);
        return dilation(eroded, structuringElement);
    }

    public static WritableImage closing(WritableImage image, boolean[][] structuringElement) {
        WritableImage dilated = dilation(image, structuringElement);
        return erosion(dilated, structuringElement);
    }

    public static WritableImage hitOrMiss(WritableImage image, boolean[][] hitMask, boolean[][] missMask) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();
        WritableImage result = new WritableImage(width, height);
        PixelWriter writer = result.getPixelWriter();

        int maskWidth = hitMask[0].length;
        int maskHeight = hitMask.length;
        int originX = maskWidth / 2;
        int originY = maskHeight / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean hit = true;
                for (int my = 0; my < maskHeight; my++) {
                    for (int mx = 0; mx < maskWidth; mx++) {
                        int ix = x + mx - originX;
                        int iy = y + my - originY;

                        if (ix >= 0 && ix < width && iy >= 0 && iy < height) {
                            Color color = reader.getColor(ix, iy);
                            if (hitMask[my][mx] && !color.equals(Color.BLACK)) {
                                hit = false;
                                break;
                            }
                            if (missMask[my][mx] && !color.equals(Color.WHITE)) {
                                hit = false;
                                break;
                            }
                        } else {
                            if (hitMask[my][mx] || missMask[my][mx]) {
                                hit = false;
                                break;
                            }
                        }
                    }
                    if (!hit) break;
                }
                writer.setColor(x, y, hit ? Color.BLACK : Color.WHITE);
            }
        }

        return result;
    }
}
