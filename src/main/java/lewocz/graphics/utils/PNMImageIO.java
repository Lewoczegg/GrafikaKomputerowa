package lewocz.graphics.utils;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lewocz.graphics.model.PNMFormat;
import lewocz.graphics.exception.InvalidFormatException;
import lewocz.graphics.exception.UnsupportedFormatException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PNMImageIO {

    public static WritableImage loadPNM(String fileName, PNMFormat format) throws IOException {
        // Input validation
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        if (format == null) {
            throw new IllegalArgumentException("PNM format cannot be null.");
        }

        try (FileInputStream fis = new FileInputStream(fileName);
             BufferedInputStream bis = new BufferedInputStream(fis);
             PushbackInputStream pbis = new PushbackInputStream(bis, 1024)) {

            // Read the magic number
            String magicNumber = readNextToken(pbis);
            if (magicNumber == null) {
                throw new InvalidFormatException("Empty file or unable to read magic number in file '" + fileName + "'.");
            }

            boolean isBinary = magicNumber.equals("P4") || magicNumber.equals("P5") || magicNumber.equals("P6");

            // Validate magic number against expected format
            String expectedMagicNumber;
            switch (format) {
                case PBM:
                    expectedMagicNumber = isBinary ? "P4" : "P1";
                    break;
                case PGM:
                    expectedMagicNumber = isBinary ? "P5" : "P2";
                    break;
                case PPM:
                    expectedMagicNumber = isBinary ? "P6" : "P3";
                    break;
                default:
                    throw new UnsupportedFormatException("Unsupported format: " + format);
            }

            if (!magicNumber.equals(expectedMagicNumber)) {
                throw new InvalidFormatException("Magic number does not match the expected format for file '" + fileName +
                        "'. Expected: " + expectedMagicNumber + ", Found: " + magicNumber);
            }

            // Read the dimensions
            int width = Integer.parseInt(readNextNonCommentToken(pbis));
            int height = Integer.parseInt(readNextNonCommentToken(pbis));

            int maxColorValue = 1; // Default for PBM
            if (!magicNumber.equals("P1") && !magicNumber.equals("P4")) {
                maxColorValue = Integer.parseInt(readNextNonCommentToken(pbis));
            }

            // Consume whitespace after header
            int b;
            do {
                b = pbis.read();
                if (b == -1) {
                    throw new IOException("Unexpected end of file after header in file '" + fileName + "'.");
                }
            } while (Character.isWhitespace(b));
            pbis.unread(b);

            WritableImage image = new WritableImage(width, height);
            PixelWriter pixelWriter = image.getPixelWriter();

            if (!isBinary) {
                BufferedReader br = new BufferedReader(new InputStreamReader(pbis, StandardCharsets.US_ASCII));
                switch (format) {
                    case PPM:
                        loadPPMText(br, width, height, maxColorValue, pixelWriter);
                        break;
                    case PGM:
                        loadPGMText(br, width, height, maxColorValue, pixelWriter);
                        break;
                    case PBM:
                        loadPBMText(br, width, height, pixelWriter);
                        break;
                }
            } else {
                switch (format) {
                    case PPM:
                        loadPPMBinary(pbis, width, height, maxColorValue, pixelWriter);
                        break;
                    case PGM:
                        loadPGMBinary(pbis, width, height, maxColorValue, pixelWriter);
                        break;
                    case PBM:
                        loadPBMBinary(pbis, width, height, pixelWriter);
                        break;
                }
            }

            return image;

        } catch (IOException e) {
            throw new IOException("Error reading PNM file '" + fileName + "': " + e.getMessage(), e);
        }
    }

    public static void savePNM(String fileName, WritableImage image, PNMFormat format, boolean binaryFormat) throws IOException {
        int defaultMaxColorValue = 255; // Default value for maxColorValue
        savePNM(fileName, image, format, binaryFormat, defaultMaxColorValue);
    }

    public static void savePNM(String fileName, WritableImage image, PNMFormat format,
                               boolean binaryFormat, int maxColorValue) throws IOException {
        // Input validation
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }
        if (format == null) {
            throw new IllegalArgumentException("PNM format cannot be null.");
        }
        if (maxColorValue <= 0 || maxColorValue > 65535) {
            throw new IllegalArgumentException("maxColorValue must be between 1 and 65535.");
        }

        if (binaryFormat) {
            saveToBinaryPNM(fileName, image, format, maxColorValue);
        } else {
            saveToTextPNM(fileName, image, format, maxColorValue);
        }
    }

    private static void loadPPMText(BufferedReader br, int width, int height, int maxColorValue,
                                    PixelWriter pixelWriter) throws IOException {
        StreamTokenizer tokenizer = createTokenizer(br);
        int x = 0;
        int y = 0;
        while (y < height) {
            int red = nextIntToken(tokenizer, maxColorValue, "red");
            int green = nextIntToken(tokenizer, maxColorValue, "green");
            int blue = nextIntToken(tokenizer, maxColorValue, "blue");

            double r = red / (double) maxColorValue;
            double g = green / (double) maxColorValue;
            double b = blue / (double) maxColorValue;
            Color color = new Color(r, g, b, 1.0);
            pixelWriter.setColor(x, y, color);

            x++;
            if (x >= width) {
                x = 0;
                y++;
            }
        }
    }

    private static void loadPGMText(BufferedReader br, int width, int height, int maxColorValue,
                                    PixelWriter pixelWriter) throws IOException {
        StreamTokenizer tokenizer = createTokenizer(br);
        int x = 0;
        int y = 0;
        while (y < height) {
            int grayValue = nextIntToken(tokenizer, maxColorValue, "gray");
            double brightness = grayValue / (double) maxColorValue;
            Color color = Color.gray(brightness);
            pixelWriter.setColor(x, y, color);

            x++;
            if (x >= width) {
                x = 0;
                y++;
            }
        }
    }

    private static void loadPBMText(BufferedReader br, int width, int height,
                                    PixelWriter pixelWriter) throws IOException {
        StreamTokenizer tokenizer = createTokenizer(br);
        int x = 0;
        int y = 0;
        while (y < height) {
            int pixelValue = nextIntToken(tokenizer, 1, "pixel");
            if (pixelValue != 0 && pixelValue != 1) {
                throw new InvalidFormatException("Invalid pixel value in PBM file at line " + tokenizer.lineno() +
                        ": " + pixelValue + ". Expected 0 or 1.");
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

    private static void loadPPMBinary(InputStream is, int width, int height, int maxColorValue,
                                      PixelWriter pixelWriter) throws IOException {
        int bytesPerSample = (maxColorValue < 256) ? 1 : 2;
        double maxColorDouble = (double) maxColorValue;

        byte[] buffer = new byte[width * 3 * bytesPerSample];

        for (int y = 0; y < height; y++) {
            int bytesRead = 0;
            while (bytesRead < buffer.length) {
                int result = is.read(buffer, bytesRead, buffer.length - bytesRead);
                if (result == -1) {
                    throw new IOException("Unexpected end of file when reading pixel data at row " + y + ".");
                }
                bytesRead += result;
            }

            int index = 0;
            for (int x = 0; x < width; x++) {
                int red, green, blue;
                if (bytesPerSample == 1) {
                    red = buffer[index++] & 0xFF;
                    green = buffer[index++] & 0xFF;
                    blue = buffer[index++] & 0xFF;
                } else {
                    red = ((buffer[index++] & 0xFF) << 8) | (buffer[index++] & 0xFF);
                    green = ((buffer[index++] & 0xFF) << 8) | (buffer[index++] & 0xFF);
                    blue = ((buffer[index++] & 0xFF) << 8) | (buffer[index++] & 0xFF);
                }

                double r = red / maxColorDouble;
                double g = green / maxColorDouble;
                double b = blue / maxColorDouble;
                Color color = new Color(r, g, b, 1.0);
                pixelWriter.setColor(x, y, color);
            }
        }
    }

    private static void loadPGMBinary(InputStream is, int width, int height, int maxColorValue,
                                      PixelWriter pixelWriter) throws IOException {
        int bytesPerSample = (maxColorValue < 256) ? 1 : 2;
        double maxColorDouble = (double) maxColorValue;

        byte[] buffer = new byte[width * bytesPerSample];

        for (int y = 0; y < height; y++) {
            int bytesRead = 0;
            while (bytesRead < buffer.length) {
                int result = is.read(buffer, bytesRead, buffer.length - bytesRead);
                if (result == -1) {
                    throw new IOException("Unexpected end of file when reading pixel data at row " + y + ".");
                }
                bytesRead += result;
            }

            int index = 0;
            for (int x = 0; x < width; x++) {
                int grayValue;
                if (bytesPerSample == 1) {
                    grayValue = buffer[index++] & 0xFF;
                } else {
                    grayValue = ((buffer[index++] & 0xFF) << 8) | (buffer[index++] & 0xFF);
                }

                double brightness = grayValue / maxColorDouble;
                Color color = Color.gray(brightness);
                pixelWriter.setColor(x, y, color);
            }
        }
    }

    private static void loadPBMBinary(InputStream is, int width, int height,
                                      PixelWriter pixelWriter) throws IOException {
        int rowSize = (width + 7) / 8;
        byte[] rowData = new byte[rowSize];

        for (int y = 0; y < height; y++) {
            int bytesRead = 0;
            while (bytesRead < rowSize) {
                int result = is.read(rowData, bytesRead, rowSize - bytesRead);
                if (result == -1) {
                    throw new IOException("Unexpected end of file when reading pixel data at row " + y + ".");
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

    // Saving methods
    private static void saveToTextPNM(String fileName, WritableImage image, PNMFormat format,
                                      int maxColorValue) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        String magicNumber;

        switch (format) {
            case PGM:
                magicNumber = "P2";
                break;
            case PPM:
                magicNumber = "P3";
                break;
            case PBM:
                magicNumber = "P1";
                break;
            default:
                throw new UnsupportedFormatException("Unsupported format: " + format);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // Write header
            writePNMHeader(bw, magicNumber, width, height, maxColorValue);

            PixelReader pixelReader = image.getPixelReader();

            switch (format) {
                case PPM:
                    savePPMText(bw, pixelReader, width, height, maxColorValue);
                    break;
                case PGM:
                    savePGMText(bw, pixelReader, width, height, maxColorValue);
                    break;
                case PBM:
                    savePBMText(bw, pixelReader, width, height);
                    break;
            }
        }
    }

    private static void savePPMText(BufferedWriter bw, PixelReader pixelReader, int width, int height,
                                    int maxColorValue) throws IOException {
        for (int y = 0; y < height; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                int red = (int) (color.getRed() * maxColorValue);
                int green = (int) (color.getGreen() * maxColorValue);
                int blue = (int) (color.getBlue() * maxColorValue);
                line.append(red).append(" ").append(green).append(" ").append(blue).append(" ");
            }
            bw.write(line.toString().trim());
            bw.newLine();
        }
    }

    private static void savePGMText(BufferedWriter bw, PixelReader pixelReader, int width, int height,
                                    int maxColorValue) throws IOException {
        for (int y = 0; y < height; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                int grayValue = (int) (color.getBrightness() * maxColorValue);
                line.append(grayValue).append(" ");
            }
            bw.write(line.toString().trim());
            bw.newLine();
        }
    }

    private static void savePBMText(BufferedWriter bw, PixelReader pixelReader, int width, int height)
            throws IOException {
        for (int y = 0; y < height; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                int value = (color.getBrightness() < 0.5) ? 1 : 0;
                line.append(value).append(" ");
            }
            bw.write(line.toString().trim());
            bw.newLine();
        }
    }

    private static void saveToBinaryPNM(String fileName, WritableImage image, PNMFormat format,
                                        int maxColorValue) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        String magicNumber;

        switch (format) {
            case PGM:
                magicNumber = "P5";
                break;
            case PPM:
                magicNumber = "P6";
                break;
            case PBM:
                magicNumber = "P4";
                break;
            default:
                throw new UnsupportedFormatException("Unsupported format: " + format);
        }

        try (FileOutputStream fos = new FileOutputStream(fileName);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            // Write header
            writePNMHeader(bos, magicNumber, width, height, maxColorValue);

            PixelReader pixelReader = image.getPixelReader();

            switch (format) {
                case PPM:
                    savePPMBinary(bos, pixelReader, width, height, maxColorValue);
                    break;
                case PGM:
                    savePGMBinary(bos, pixelReader, width, height, maxColorValue);
                    break;
                case PBM:
                    savePBMBinary(bos, pixelReader, width, height);
                    break;
            }
        }
    }

    private static void savePPMBinary(OutputStream os, PixelReader pixelReader, int width, int height,
                                      int maxColorValue) throws IOException {
        int bytesPerSample = (maxColorValue < 256) ? 1 : 2;

        byte[] buffer = new byte[width * 3 * bytesPerSample];

        for (int y = 0; y < height; y++) {
            int index = 0;
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                int red = (int) (color.getRed() * maxColorValue);
                int green = (int) (color.getGreen() * maxColorValue);
                int blue = (int) (color.getBlue() * maxColorValue);

                if (bytesPerSample == 1) {
                    buffer[index++] = (byte) red;
                    buffer[index++] = (byte) green;
                    buffer[index++] = (byte) blue;
                } else {
                    buffer[index++] = (byte) ((red >> 8) & 0xFF);
                    buffer[index++] = (byte) (red & 0xFF);
                    buffer[index++] = (byte) ((green >> 8) & 0xFF);
                    buffer[index++] = (byte) (green & 0xFF);
                    buffer[index++] = (byte) ((blue >> 8) & 0xFF);
                    buffer[index++] = (byte) (blue & 0xFF);
                }
            }
            os.write(buffer, 0, index);
        }
    }

    private static void savePGMBinary(OutputStream os, PixelReader pixelReader, int width, int height,
                                      int maxColorValue) throws IOException {
        int bytesPerSample = (maxColorValue < 256) ? 1 : 2;

        byte[] buffer = new byte[width * bytesPerSample];

        for (int y = 0; y < height; y++) {
            int index = 0;
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                int grayValue = (int) (color.getBrightness() * maxColorValue);

                if (bytesPerSample == 1) {
                    buffer[index++] = (byte) grayValue;
                } else {
                    buffer[index++] = (byte) ((grayValue >> 8) & 0xFF);
                    buffer[index++] = (byte) (grayValue & 0xFF);
                }
            }
            os.write(buffer, 0, index);
        }
    }

    private static void savePBMBinary(OutputStream os, PixelReader pixelReader, int width, int height)
            throws IOException {
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
            os.write(rowData);
        }
    }

    // Header writing methods
    private static void writePNMHeader(BufferedWriter bw, String magicNumber, int width, int height,
                                       int maxColorValue) throws IOException {
        bw.write(magicNumber);
        bw.newLine();
        bw.write(width + " " + height);
        bw.newLine();
        if (!magicNumber.equals("P1")) {
            bw.write(String.valueOf(maxColorValue));
            bw.newLine();
        }
    }

    private static void writePNMHeader(OutputStream os, String magicNumber, int width, int height,
                                       int maxColorValue) throws IOException {
        StringBuilder header = new StringBuilder();
        header.append(magicNumber).append("\n");
        header.append(width).append(" ").append(height).append("\n");
        if (!magicNumber.equals("P4")) {
            header.append(maxColorValue).append("\n");
        }
        os.write(header.toString().getBytes(StandardCharsets.US_ASCII));
    }

    private static StreamTokenizer createTokenizer(BufferedReader br) {
        StreamTokenizer tokenizer = new StreamTokenizer(br);
        tokenizer.resetSyntax();
        tokenizer.whitespaceChars(0, ' '); // Up to space ' '
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('-','-'); // Allow negative numbers
        tokenizer.eolIsSignificant(false);
        tokenizer.commentChar('#');
        return tokenizer;
    }

    private static int nextIntToken(StreamTokenizer tokenizer, int maxValue, String componentName)
            throws IOException {
        int tokenType;
        while ((tokenType = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
            if (tokenType == StreamTokenizer.TT_NUMBER || tokenType == StreamTokenizer.TT_WORD) {
                String tokenStr = tokenizer.sval != null ? tokenizer.sval : String.valueOf((int) tokenizer.nval);
                try {
                    int value = Integer.parseInt(tokenStr);
                    if (value < 0 || value > maxValue) {
                        throw new InvalidFormatException(componentName + " value out of bounds at line " +
                                tokenizer.lineno() + ": " + value + ". Expected range: 0 to " + maxValue + ".");
                    }
                    return value;
                } catch (NumberFormatException e) {
                    throw new InvalidFormatException("Invalid " + componentName + " value at line " +
                            tokenizer.lineno() + ": " + tokenStr, e);
                }
            }
        }
        throw new IOException("Unexpected end of file while reading " + componentName + " value.");
    }

    private static String readNextToken(PushbackInputStream pbis) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;

        // Skip initial whitespace
        while (true) {
            b = pbis.read();
            if (b == -1) {
                return null;
            }
            if (!Character.isWhitespace(b)) {
                break;
            }
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