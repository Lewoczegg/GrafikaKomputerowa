package lewocz.graphics.utils;

public class ColorUtils {

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double[] rgbToCmyk(int r, int g, int b) {
        double c, m, y, k;

        double rNorm = r / 255.0;
        double gNorm = g / 255.0;
        double bNorm = b / 255.0;

        k = 1.0 - Math.max(rNorm, Math.max(gNorm, bNorm));

        if (k < 1.0) {
            c = (1.0 - rNorm - k) / (1.0 - k);
            m = (1.0 - gNorm - k) / (1.0 - k);
            y = (1.0 - bNorm - k) / (1.0 - k);
        } else {
            c = 0;
            m = 0;
            y = 0;
        }

        return new double[] { c * 100, m * 100, y * 100, k * 100 };
    }

    public static int[] cmykToRgb(double c, double m, double y, double k) {
        c /= 100;
        m /= 100;
        y /= 100;
        k /= 100;

        int r = (int) ((1 - Math.min(1, c * (1 - k) + k)) * 255);
        int g = (int) ((1 - Math.min(1, m * (1 - k) + k)) * 255);
        int b = (int) ((1 - Math.min(1, y * (1 - k) + k)) * 255);

        return new int[] { clamp(r, 0, 255), clamp(g, 0, 255), clamp(b, 0, 255) };
    }

    public static double[] rgbToHsv(int r, int g, int b) {
        double rNorm = r / 255.0;
        double gNorm = g / 255.0;
        double bNorm = b / 255.0;

        double max = Math.max(rNorm, Math.max(gNorm, bNorm));
        double min = Math.min(rNorm, Math.min(gNorm, bNorm));
        double delta = max - min;

        double h, s, v;
        v = max;

        if (delta == 0) {
            h = 0;
        } else if (max == rNorm) {
            h = 60 * (((gNorm - bNorm) / delta) % 6);
        } else if (max == gNorm) {
            h = 60 * (((bNorm - rNorm) / delta) + 2);
        } else {
            h = 60 * (((rNorm - gNorm) / delta) + 4);
        }

        if (h < 0) {
            h += 360;
        }

        s = (max == 0) ? 0 : (delta / max);

        return new double[] { h, s * 100, v * 100 };
    }

    public static int[] hsvToRgb(double h, double s, double v) {
        s /= 100;
        v /= 100;

        double c = v * s;
        double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
        double m = v - c;

        double rPrime = 0, gPrime = 0, bPrime = 0;

        if (h >= 0 && h < 60) {
            rPrime = c; gPrime = x; bPrime = 0;
        } else if (h >= 60 && h < 120) {
            rPrime = x; gPrime = c; bPrime = 0;
        } else if (h >= 120 && h < 180) {
            rPrime = 0; gPrime = c; bPrime = x;
        } else if (h >= 180 && h < 240) {
            rPrime = 0; gPrime = x; bPrime = c;
        } else if (h >= 240 && h < 300) {
            rPrime = x; gPrime = 0; bPrime = c;
        } else if (h >= 300 && h < 360) {
            rPrime = c; gPrime = 0; bPrime = x;
        }

        int r = (int) ((rPrime + m) * 255);
        int g = (int) ((gPrime + m) * 255);
        int b = (int) ((bPrime + m) * 255);

        return new int[] { clamp(r, 0, 255), clamp(g, 0, 255), clamp(b, 0, 255) };
    }
}