package lewocz.graphics.utils;

import javafx.application.Platform;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ImageProcessorTest {

    @BeforeAll
    public static void initJFX() throws InterruptedException {
        // Initializes JavaFX Toolkit
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(() -> {
            // No need to do anything here
            latch.countDown();
        });
        latch.await();
    }

    /**
     * Helper method to create a 10x10 grayscale gradient test image.
     */
    private WritableImage createTestImage() {
        int width = 10;
        int height = 10;
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        // Create a gradient from black (0.0) to white (1.0)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = ((double) (x + y)) / (width + height - 2); // value ranges from 0.0 to 1.0
                Color color = new Color(value, value, value, 1.0);
                writer.setColor(x, y, color);
            }
        }
        return image;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private void assertImagesEqual(WritableImage expected, WritableImage actual) {
        int width = (int) expected.getWidth();
        int height = (int) expected.getHeight();
        assertEquals(width, (int) actual.getWidth(), "Widths differ");
        assertEquals(height, (int) actual.getHeight(), "Heights differ");

        PixelReader expectedReader = expected.getPixelReader();
        PixelReader actualReader = actual.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color expectedColor = expectedReader.getColor(x, y);
                Color actualColor = actualReader.getColor(x, y);
                assertColorsEqual("Pixel at (" + x + ", " + y + ") differs", expectedColor, actualColor);
            }
        }
    }

    private void assertColorsEqual(String message, Color expected, Color actual) {
        int expectedRed = (int) Math.round(expected.getRed() * 255);
        int expectedGreen = (int) Math.round(expected.getGreen() * 255);
        int expectedBlue = (int) Math.round(expected.getBlue() * 255);
        int expectedOpacity = (int) Math.round(expected.getOpacity() * 255);

        int actualRed = (int) Math.round(actual.getRed() * 255);
        int actualGreen = (int) Math.round(actual.getGreen() * 255);
        int actualBlue = (int) Math.round(actual.getBlue() * 255);
        int actualOpacity = (int) Math.round(actual.getOpacity() * 255);

        assertWithinTolerance(expectedRed, actualRed, 1, message + " - Red component");
        assertWithinTolerance(expectedGreen, actualGreen, 1, message + " - Green component");
        assertWithinTolerance(expectedBlue, actualBlue, 1, message + " - Blue component");
        assertWithinTolerance(expectedOpacity, actualOpacity, 1, message + " - Opacity component");
    }

    private void assertWithinTolerance(int expected, int actual, int tolerance, String message) {
        int diff = Math.abs(expected - actual);
        assertTrue(diff <= tolerance, message + " (Expected: " + expected + ", Actual: " + actual + ")");
    }

    @Test
    public void testAdjustBrightness() {
        WritableImage inputImage = createTestImage();
        double brightnessChange = 0.1;

        WritableImage expectedImage = createExpectedBrightnessAdjustedImage(brightnessChange);
        WritableImage outputImage = ImageProcessor.adjustBrightness(inputImage, brightnessChange);

        assertImagesEqual(expectedImage, outputImage);
    }

    private WritableImage createExpectedBrightnessAdjustedImage(double brightnessChange) {
        int width = 10;
        int height = 10;
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double originalValue = ((double) (x + y)) / (width + height - 2);
                double adjustedValue = clamp(originalValue + brightnessChange);
                Color color = new Color(adjustedValue, adjustedValue, adjustedValue, 1.0);
                writer.setColor(x, y, color);
            }
        }
        return image;
    }

    @Test
    public void testGrayscaleAverage() {
        WritableImage inputImage = createTestImage();

        WritableImage expectedImage = createTestImage();
        WritableImage outputImage = ImageProcessor.grayscaleAverage(inputImage);

        assertImagesEqual(expectedImage, outputImage);
    }

    @Test
    public void testApplySmoothingFilter() {
        WritableImage inputImage = createTestImage();
        WritableImage expectedImage = createExpectedSmoothingFilterImage();
        WritableImage outputImage = ImageProcessor.applySmoothingFilter(inputImage);

        assertImagesEqual(expectedImage, outputImage);
    }

    private WritableImage createExpectedSmoothingFilterImage() {
        int width = 10;
        int height = 10;
        WritableImage inputImage = createTestImage();
        WritableImage outputImage = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        double[][] kernel = {
                {1 / 9.0, 1 / 9.0, 1 / 9.0},
                {1 / 9.0, 1 / 9.0, 1 / 9.0},
                {1 / 9.0, 1 / 9.0, 1 / 9.0}
        };

        int kHalf = 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double sum = 0.0;
                double sumKernel = 0.0;

                for (int ky = -kHalf; ky <= kHalf; ky++) {
                    for (int kx = -kHalf; kx <= kHalf; kx++) {
                        int pixelX = x + kx;
                        int pixelY = y + ky;

                        if (pixelX < 0 || pixelX >= width || pixelY < 0 || pixelY >= height) {
                            continue;
                        }

                        Color color = reader.getColor(pixelX, pixelY);
                        double value = color.getRed();
                        double kernelValue = kernel[ky + kHalf][kx + kHalf];

                        sum += value * kernelValue;
                        sumKernel += kernelValue;
                    }
                }

                double normalizedValue = sum / sumKernel;

                Color newColor = new Color(clamp(normalizedValue), clamp(normalizedValue), clamp(normalizedValue), 1.0);
                writer.setColor(x, y, newColor);
            }
        }

        return outputImage;
    }
}
