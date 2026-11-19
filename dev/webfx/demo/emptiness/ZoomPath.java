package dev.webfx.demo.emptiness;

/**
 * The camera path diving from a sphere (the "root", radius 1 at the origin) into one of its parts (the "target").
 *
 * <p>The map f(x) = c + r R x sends the root onto the target (c = target center, r = target radius, R = target
 * rotation). The camera starts at P0 = (0, 0, d0) looking at the root, and ends at f(P0) looking at the target in the
 * very same way, so the target fills the screen exactly as the root did. Between the two, the camera follows the
 * one-parameter group of f: P(s) = x* + r^s R^s (P0 - x*), where x* is the fixed point of f. This is a logarithmic
 * spiral, so the zoom speed looks constant, and it extends naturally to s &lt; 0 (coming from far away) and s &gt; 1.</p>
 *
 * @author Bruno Salmon
 */
final class ZoomPath {

    final int childIndex;
    final double cx, cy, cz, r;
    private final double ax, ay, az, angle;
    final double[] rot = new double[9];
    final double[] quat = new double[4];
    private final double sx, sy, sz; // fixed point x*
    private final double d0;

    ZoomPath(SphereLayout layout, int childIndex, double d0) {
        this.childIndex = childIndex;
        this.d0 = d0;
        cx = layout.cx[childIndex]; cy = layout.cy[childIndex]; cz = layout.cz[childIndex];
        r = layout.r;
        ax = layout.ax[childIndex]; ay = layout.ay[childIndex]; az = layout.az[childIndex];
        angle = layout.angle[childIndex];
        Maths.axisAngleToMatrix(ax, ay, az, angle, rot);
        Maths.axisAngleToQuat(ax, ay, az, angle, quat);
        // x* = (I - r R)^-1 c
        double[] a = new double[9];
        for (int k = 0; k < 9; k++)
            a[k] = -r * rot[k];
        a[0] += 1; a[4] += 1; a[8] += 1;
        double[] inv = Maths.invert(a);
        sx = inv[0] * cx + inv[1] * cy + inv[2] * cz;
        sy = inv[3] * cx + inv[4] * cy + inv[5] * cz;
        sz = inv[6] * cx + inv[7] * cy + inv[8] * cz;
    }

    // Camera position at s (in the root coordinates)
    void position(double s, double[] out) {
        Maths.rotate(ax, ay, az, s * angle, -sx, -sy, d0 - sz, out);
        double k = Math.pow(r, s);
        out[0] = sx + k * out[0];
        out[1] = sy + k * out[1];
        out[2] = sz + k * out[2];
    }

    // Camera orientation at s (in the root coordinates)
    void orientation(double s, double[] q) {
        Maths.axisAngleToQuat(ax, ay, az, s * angle, q);
    }

    // Target coordinates -> root coordinates
    void toParent(double[] p) {
        double x = p[0], y = p[1], z = p[2];
        p[0] = cx + r * (rot[0] * x + rot[1] * y + rot[2] * z);
        p[1] = cy + r * (rot[3] * x + rot[4] * y + rot[5] * z);
        p[2] = cz + r * (rot[6] * x + rot[7] * y + rot[8] * z);
    }

    // Root coordinates -> target coordinates
    void toChild(double[] p) {
        double x = (p[0] - cx) / r, y = (p[1] - cy) / r, z = (p[2] - cz) / r;
        p[0] = rot[0] * x + rot[3] * y + rot[6] * z;
        p[1] = rot[1] * x + rot[4] * y + rot[7] * z;
        p[2] = rot[2] * x + rot[5] * y + rot[8] * z;
    }
}
