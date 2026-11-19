package dev.webfx.demo.emptiness;

/**
 * Minimal 3D maths on plain double arrays (no allocation in the per-frame paths). Matrices are 3x3 rotation matrices
 * stored row-major in a double[9]; quaternions are double[4] = (x, y, z, w).
 *
 * @author Bruno Salmon
 */
final class Maths {

    private Maths() {}

    static void identity(double[] m) {
        m[0] = 1; m[1] = 0; m[2] = 0;
        m[3] = 0; m[4] = 1; m[5] = 0;
        m[6] = 0; m[7] = 0; m[8] = 1;
    }

    // Rotation matrix of the given angle around the given (unit) axis (Rodrigues formula)
    static void axisAngleToMatrix(double x, double y, double z, double angle, double[] m) {
        double c = Math.cos(angle), s = Math.sin(angle), t = 1 - c;
        m[0] = t * x * x + c;     m[1] = t * x * y - s * z; m[2] = t * x * z + s * y;
        m[3] = t * x * y + s * z; m[4] = t * y * y + c;     m[5] = t * y * z - s * x;
        m[6] = t * x * z - s * y; m[7] = t * y * z + s * x; m[8] = t * z * z + c;
    }

    static void axisAngleToQuat(double x, double y, double z, double angle, double[] q) {
        double s = Math.sin(angle / 2);
        q[0] = x * s; q[1] = y * s; q[2] = z * s; q[3] = Math.cos(angle / 2);
    }

    // out = a * b (Hamilton product), out may be a or b
    static void quatMul(double[] a, double[] b, double[] out) {
        double ax = a[0], ay = a[1], az = a[2], aw = a[3];
        double bx = b[0], by = b[1], bz = b[2], bw = b[3];
        out[0] = aw * bx + ax * bw + ay * bz - az * by;
        out[1] = aw * by - ax * bz + ay * bw + az * bx;
        out[2] = aw * bz + ax * by - ay * bx + az * bw;
        out[3] = aw * bw - ax * bx - ay * by - az * bz;
    }

    // out = conjugate(a) * b, out may be a or b
    static void quatConjMul(double[] a, double[] b, double[] out) {
        double ax = -a[0], ay = -a[1], az = -a[2], aw = a[3];
        double bx = b[0], by = b[1], bz = b[2], bw = b[3];
        out[0] = aw * bx + ax * bw + ay * bz - az * by;
        out[1] = aw * by - ax * bz + ay * bw + az * bx;
        out[2] = aw * bz + ax * by - ay * bx + az * bw;
        out[3] = aw * bw - ax * bx - ay * by - az * bz;
    }

    // Normalized linear interpolation from a to b (written into a)
    static void quatNlerpInto(double[] a, double[] b, double t) {
        double dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3];
        double sign = dot < 0 ? -1 : 1; // shortest way
        double x = a[0] + (sign * b[0] - a[0]) * t;
        double y = a[1] + (sign * b[1] - a[1]) * t;
        double z = a[2] + (sign * b[2] - a[2]) * t;
        double w = a[3] + (sign * b[3] - a[3]) * t;
        double l = Math.sqrt(x * x + y * y + z * z + w * w);
        a[0] = x / l; a[1] = y / l; a[2] = z / l; a[3] = w / l;
    }

    static void quatToMatrix(double[] q, double[] m) {
        double x = q[0], y = q[1], z = q[2], w = q[3];
        m[0] = 1 - 2 * (y * y + z * z); m[1] = 2 * (x * y - z * w);     m[2] = 2 * (x * z + y * w);
        m[3] = 2 * (x * y + z * w);     m[4] = 1 - 2 * (x * x + z * z); m[5] = 2 * (y * z - x * w);
        m[6] = 2 * (x * z - y * w);     m[7] = 2 * (y * z + x * w);     m[8] = 1 - 2 * (x * x + y * y);
    }

    // out = a * b (out must not be a or b)
    static void matMul(double[] a, double[] b, double[] out) {
        for (int i = 0; i < 3; i++) {
            double a0 = a[3 * i], a1 = a[3 * i + 1], a2 = a[3 * i + 2];
            out[3 * i]     = a0 * b[0] + a1 * b[3] + a2 * b[6];
            out[3 * i + 1] = a0 * b[1] + a1 * b[4] + a2 * b[7];
            out[3 * i + 2] = a0 * b[2] + a1 * b[5] + a2 * b[8];
        }
    }

    // out = transpose(a) * b (out must not be a or b)
    static void matTransposeMul(double[] a, double[] b, double[] out) {
        for (int i = 0; i < 3; i++) {
            double a0 = a[i], a1 = a[3 + i], a2 = a[6 + i];
            out[3 * i]     = a0 * b[0] + a1 * b[3] + a2 * b[6];
            out[3 * i + 1] = a0 * b[1] + a1 * b[4] + a2 * b[7];
            out[3 * i + 2] = a0 * b[2] + a1 * b[5] + a2 * b[8];
        }
    }

    static double[] invert(double[] m) {
        double a = m[0], b = m[1], c = m[2], d = m[3], e = m[4], f = m[5], g = m[6], h = m[7], i = m[8];
        double A = e * i - f * h, B = -(d * i - f * g), C = d * h - e * g;
        double det = a * A + b * B + c * C;
        double k = 1 / det;
        return new double[] {
                A * k, -(b * i - c * h) * k,  (b * f - c * e) * k,
                B * k,  (a * i - c * g) * k, -(a * f - c * d) * k,
                C * k, -(a * h - b * g) * k,  (a * e - b * d) * k
        };
    }

    // Rotates the vector (vx, vy, vz) by the given angle around the given (unit) axis, into out
    static void rotate(double x, double y, double z, double angle, double vx, double vy, double vz, double[] out) {
        double c = Math.cos(angle), s = Math.sin(angle);
        double dot = (x * vx + y * vy + z * vz) * (1 - c);
        out[0] = vx * c + (y * vz - z * vy) * s + x * dot;
        out[1] = vy * c + (z * vx - x * vz) * s + y * dot;
        out[2] = vz * c + (x * vy - y * vx) * s + z * dot;
    }

    static double clamp01(double v) {
        return v < 0 ? 0 : v > 1 ? 1 : v;
    }

    static double smoothstep(double t) {
        t = clamp01(t);
        return t * t * (3 - 2 * t);
    }

    // Integer hash made only of shifts, xors and small additions, so it gives the same exact result on every WebFX
    // target (including the JavaScript ones where large int multiplications would lose precision).
    static int hash(int seed, int i) {
        int x = seed ^ (i * 40503 + 0x7F4A7C15);
        for (int round = 0; round < 3; round++) {
            x ^= x << 13;
            x ^= x >>> 17;
            x ^= x << 5;
            x += 0x6D2B79F5;
        }
        return x;
    }

    // Uniform value in [0, 1) derived from a hash
    static double unit(int hash) {
        return (hash >>> 8) / 16777216d;
    }

    // HSB to RGB, with hue in turns (any real value, wrapped), saturation and brightness in [0, 1]
    static void hsbToRgb(double hue, double saturation, double brightness, double[] rgb) {
        double h = (hue - Math.floor(hue)) * 6;
        int sector = (int) h;
        double f = h - sector;
        double p = brightness * (1 - saturation);
        double q = brightness * (1 - saturation * f);
        double t = brightness * (1 - saturation * (1 - f));
        double r, g, b;
        switch (sector) {
            case 0:  r = brightness; g = t; b = p; break;
            case 1:  r = q; g = brightness; b = p; break;
            case 2:  r = p; g = brightness; b = t; break;
            case 3:  r = p; g = q; b = brightness; break;
            case 4:  r = t; g = p; b = brightness; break;
            default: r = brightness; g = p; b = q; break;
        }
        rgb[0] = r; rgb[1] = g; rgb[2] = b;
    }
}
