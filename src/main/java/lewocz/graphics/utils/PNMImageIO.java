package lewocz.graphics.utils;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lewocz.graphics.model.PNMFormat;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PNMImageIO {

    public static WritableImage loadPNM(String fileName, PNMFormat format) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileName);
             PushbackInputStream pbis = new PushbackInputStream(fis, 1024)) {

            // Read the magic number
            String magicNumber = readNextToken(pbis);
            boolean isBinary = magicNumber.equals("P4") || magicNumber.equals("P5") || magicNumber.equals("P6");

            // Validate magic number
            if (!magicNumber.matches("P[1-6]")) {
                throw new IOException("Unsupported PNM format: " + magicNumber);
            }

            // Read the dimensions
            int width = Integer.parseInt(readNextNonCommentToken(pbis));
            int height = Integer.parseInt(readNextNonCommentToken(pbis));

            int maxColorValue = 1; // Default for PBM
            if (magicNumber.equals("P2") || magicNumber.equals("P3") ||
                magicNumber.equals("P5") || magicNumber.equals("P6")) {
                maxColorValue = Integer.parseInt(readNextNonCommentToken(pbis));
            }

            // Consume whitespace after header
            int b;
            do {
                b = pbis.read();
                if (b == -1) {
                    throw new IOException("Unexpected end of file after header");
                }
            } while (Character.isWhitespace(b));
            pbis.unread(b);

            WritableImage image = new WritableImage(width, height);
            PixelWriter pixelWriter = image.getPixelWriter();

            if (!isBinary) {
                BufferedReader br = new BufferedReader(new InputStreamReader(pbis, StandardCharsets.US_ASCII));
                loadFromTextPNM(br, width, height, maxColorValue, pixelWriter, format);
            } else {
                loadFromBinaryPNM(pbis, width, height, maxColorValue, pixelWriter, format);
            }

            return image;

        }
    }

    public static void savePNM(String fileName, WritableImage image, PNMFormat format, boolean binaryFormat) throws IOException {
        if (binaryFormat) {
            saveToBinaryPNM(fileName, image, format);
        } else {
            saveToTextPNM(fileName, image, format);
        }
    }

    private static void loadFromTextPNM(BufferedReader br, int width, int height, int maxColorValue,
                                        PixelWriter pixelWriter, PNMFormat format) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(br);
        tokenizer.resetSyntax();
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('-', '-');
        tokenizer.eolIsSignificant(false);
        tokenizer.commentChar('#');

        int x = 0;
        int y = 0;

        while (y < height) {
            if (format == PNMFormat.PPM) {
                int red = nextIntToken(tokenizer, maxColorValue, "red");
                int green = nextIntToken(tokenizer, maxColorValue, "green");
                int blue = nextIntToken(tokenizer, maxColorValue, "blue");

                processColorValue(red, green, blue, maxColorValue, pixelWriter, x, y);

                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            } else if (format == PNMFormat.PGM) {
                int grayValue = nextIntToken(tokenizer, maxColorValue, "gray");
                double brightness = grayValue / (double) maxColorValue;
                Color color = Color.gray(brightness);
                pixelWriter.setColor(x, y, color);

                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            } else if (format == PNMFormat.PBM) {
                int pixelValue = nextIntToken(tokenizer, 1, "pixel");
                if (pixelValue != 0 && pixelValue != 1) {
                    throw new IOException("Invalid pixel value in PBM file: " + pixelValue);
                }
                Color color = (pixelValue == 1) ? Color.BLACK : Color.WHITE;
                pixelWriter.setColor(x, y, color);

                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            }
        }
    }

    private static void loadFromBinaryPNM(InputStream is, int width, int height, int maxColorValue,
                                          PixelWriter pixelWriter, PNMFormat format) throws IOException {
        if (format == PNMFormat.PPM) {
            int bytesPerSample = (maxColorValue < 256) ? 1 : 2;
            int totalSamples = width * height * 3;
            int totalBytes = totalSamples * bytesPerSample;
            byte[] pixelData = new byte[totalBytes];
            int bytesRead = 0;

            while (bytesRead < totalBytes) {
                int result = is.read(pixelData, bytesRead, totalBytes - bytesRead);
                if (result == -1) {
                    throw new IOException("Unexpected end of file when reading pixel data");
                }
                bytesRead += result;
            }

            int index = 0;
            double maxColorDouble = (double) maxColorValue;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int red, green, blue;

                    if (bytesPerSample == 1) {
                        red = pixelData[index++] & 0xFF;
                        green = pixelData[index++] & 0xFF;
                        blue = pixelData[index++] & 0xFF;
                    } else {
                        red = ((pixelData[index++] & 0xFF) << 8) | (pixelData[index++] & 0xFF);
                        green = ((pixelData[index++] & 0xFF) << 8) | (pixelData[index++] & 0xFF);
                        blue = ((pixelData[index++] & 0xFF) << 8) | (pixelData[index++] & 0xFF);
                    }

                    double r = red / maxColorDouble;
                    double g = green / maxColorDouble;
                    double b = blue / maxColorDouble;

                    Color color = new Color(r, g, b, 1.0);
                    pixelWriter.setColor(x, y, color);
                }
            }
        } else if (format == PNMFormat.PGM) {
            int bytesPerSample = (maxColorValue < 256) ? 1 : 2;
            int totalSamples = width * height;
            int totalBytes = totalSamples * bytesPerSample;
            byte[] pixelData = new byte[totalBytes];
            int bytesRead = 0;

            while (bytesRead < totalBytes) {
                int result = is.read(pixelData, bytesRead, totalBytes - bytesRead);
                if (result == -1) {
                    throw new IOException("Unexpected end of file when reading pixel data");
                }
                bytesRead += result;
            }

            int index = 0;
            double maxColorDouble = (double) maxColorValue;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int grayValue;
                    if (bytesPerSample == 1) {
                        grayValue = pixelData[index++] & 0xFF;
                    } else {
                        grayValue = ((pixelData[index++] & 0xFF) << 8) | (pixelData[index++] & 0xFF);
                    }

                    double brightness = grayValue / maxColorDouble;
                    Color color = Color.gray(brightness);
                    pixelWriter.setColor(x, y, color);
                }
            }
        } else if (format == PNMFormat.PBM) {
            int rowSize = (width + 7) / 8;
            byte[] rowData = new byte[rowSize];

            for (int y = 0; y < height; y++) {
                int bytesRead = 0;
                while (bytesRead < rowSize) {
                    int result = is.read(rowData, bytesRead, rowSize - bytesRead);
                    if (result == -1) {
                        throw new IOException("Unexpected end of file when reading pixel data");
                    }
                    bytesRead += result;
                }

                for (int x = 0; x < width; x++) {
                    int byteIndex = x / 8;
                    int bitIndex = 7 - (x % 8);
                    int bit = (rowData[byteIndex] >> bitIndex) & 1;
                    Color color = (bit == 0) ? Color.WHITE : Color.BLACK;
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
    }

    private static void saveToTextPNM(String fileName, WritableImage image, PNMFormat format) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int maxColorValue = 255;
        String magicNumber;
        switch (format) {
            case PGM:
                magicNumber = "P2";
                break;
            case PPM:
                magicNumber = "P3";
                break;
            default:
                magicNumber = "P1"; // PBM
                break;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // Write header
            writePNMHeader(bw, magicNumber, width, height, maxColorValue);

            PixelReader pixelReader = image.getPixelReader();
            for (int y = 0; y < height; y++) {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < width; x++) {
                    Color color = pixelReader.getColor(x, y);
                    if (format == PNMFormat.PPM) {
                        int red = (int) (color.getRed() * maxColorValue);
                        int green = (int) (color.getGreen() * maxColorValue);
                        int blue = (int) (color.getBlue() * maxColorValue);
                        line.append(red).append(" ").append(green).append(" ").append(blue).append(" ");
                    } else if (format == PNMFormat.PGM) {
                        int grayValue = (int) (color.getBrightness() * maxColorValue);
                        line.append(grayValue).append(" ");
                    } else {
                        int value = (color.getBrightness() < 0.5) ? 1 : 0;
                        line.append(value).append(" ");
                    }
                }
                bw.write(line.toString().trim() + "\n");
            }
        }
    }

    private static void saveToBinaryPNM(String fileName, WritableImage image, PNMFormat format) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int maxColorValue = 255;
        String magicNumber;
        switch (format) {
            case PGM:
                magicNumber = "P5";
                break;
            case PPM:
                magicNumber = "P6";
                break;
            default:
                magicNumber = "P4";
                break;
        }

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            // Write header
            writePNMHeader(fos, magicNumber, width, height, maxColorValue);

            PixelReader pixelReader = image.getPixelReader();

            if (format == PNMFormat.PPM) {
                byte[] pixelData = new byte[width * height * 3];
                int index = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        pixelData[index++] = (byte) (color.getRed() * maxColorValue);
                        pixelData[index++] = (byte) (color.getGreen() * maxColorValue);
                        pixelData[index++] = (byte) (color.getBlue() * maxColorValue);
                    }
                }
                fos.write(pixelData);
            } else if (format == PNMFormat.PGM) {
                byte[] pixelData = new byte[width * height];
                int index = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        int grayValue = (int) (color.getBrightness() * maxColorValue);
                        pixelData[index++] = (byte) grayValue;
                    }
                }
                fos.write(pixelData);
            } else {
                int bytesPerRow = (width + 7) / 8;
                byte[] rowData = new byte[bytesPerRow];

                for (int y = 0; y < height; y++) {
                    for (int i = 0; i < bytesPerRow; i++) rowData[i] = 0;
                    for (int x = 0; x < width; x++) {
                        Color color = pixelReader.getColor(x, y);
                        int bit = (color.getBrightness() < 0.5) ? 1 : 0;
                        int bitPosition = 7 - (x % 8);
                        if (bit == 1) {
                            rowData[x / 8] |= (1 << bitPosition);
                        }
                    }
                    fos.write(rowData);
                }
            }
        }
    }

    private static void writePNMHeader(BufferedWriter bw, String magicNumber, int width, int height, int maxColorValue) throws IOException {
        bw.write(magicNumber + "\n");
        bw.write(width + " " + height + "\n");
        if (!magicNumber.equals("P1")) {
            bw.write(maxColorValue + "\n");
        }
    }

    private static void writePNMHeader(OutputStream os, String magicNumber, int width, int height, int maxColorValue) throws IOException {
        StringBuilder header = new StringBuilder();
        header.append(magicNumber).append("\n");
        header.append(width).append(" ").append(height).append("\n");
        if (!magicNumber.equals("P4")) {
            header.append(maxColorValue).append("\n");
        }
        os.write(header.toString().getBytes(StandardCharsets.US_ASCII));
    }

    private static int nextIntToken(StreamTokenizer tokenizer, int maxValue, String componentName) throws IOException {
        int tokenType;
        while ((tokenType = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
            if (tokenType == StreamTokenizer.TT_NUMBER || tokenType == StreamTokenizer.TT_WORD) {
                String tokenStr = tokenizer.sval != null ? tokenizer.sval : String.valueOf((int) tokenizer.nval);
                try {
                    int value = Integer.parseInt(tokenStr);
                    if (value < 0 || value > maxValue) {
                        throw new IOException(componentName + " value out of bounds: " + value);
                    }
                    return value;
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid " + componentName + " value: " + tokenStr, e);
                }
            }
        }
        throw new IOException("Unexpected end of file while reading " + componentName + " value");
    }

    private static void processColorValue(int red, int green, int blue, int maxColorValue,
                                          PixelWriter pixelWriter, int x, int y) {
        double r = red / (double) maxColorValue;
        double g = green / (double) maxColorValue;
        double b = blue / (double) maxColorValue;
        Color color = new Color(r, g, b, 1.0);
        pixelWriter.setColor(x, y, color);
    }

    private static String readNextToken(PushbackInputStream pbis) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;

        // Skip initial whitespace
        while (true) {
            b = pbis.read();
            if (b == -1) {
                break;
            }
            if (!Character.isWhitespace(b)) {
                break;
            }
        }

        if (b == -1) {
            return null;
        }

        // Read the token
        do {
            sb.append((char) b);
            b = pbis.read();
        } while (b != -1 && !Character.isWhitespace(b));

        if (b != -1) {
            pbis.unread(b);
        }

        return sb.toString();
    }

    private static String readNextNonCommentToken(PushbackInputStream pbis) throws IOException {
        String token;
        while (true) {
            token = readNextToken(pbis);
            if (token == null) {
                return null;
            }
            if (token.startsWith("#")) {
                skipComment(pbis);
                continue;
            }
            return token;
        }
    }

    private static void skipComment(PushbackInputStream pbis) throws IOException {
        int b;
        do {
            b = pbis.read();
        } while (b != -1 && b != '\n');
    }
}