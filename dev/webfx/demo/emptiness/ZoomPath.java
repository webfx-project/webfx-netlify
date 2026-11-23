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
 * <p>The spiral is fully determined by the target, so another part may block it. The path can then make a detour: the
 * camera is shifted sideways by D sin²(πs) r^s (D = detour, in the root coordinates), which is 0 at both ends (with a
 * zero derivative, so the dive starts and ends as on the spiral) and shrinks with the zoom like the rest of the path.
 * During the detour, the camera turns so that the target stays where it would be seen from the spiral.</p>
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
    private final double dx, dy, dz; // detour (0 = none)
    private final double[] tmp = new double[4];

    ZoomPath(SphereLayout layout, int childIndex, double d0) {
        this(layout, childIndex, d0, 0, 0, 0);
    }

    ZoomPath(SphereLayout layout, int childIndex, double d0, double dx, double dy, double dz) {
        this.childIndex = childIndex;
        this.dx = dx; this.dy = dy; this.dz = dz;
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
        spiralPosition(s, out);
        double k = detourFactor(s);
        out[0] += k * dx;
        out[1] += k * dy;
        out[2] += k * dz;
    }

    private void spiralPosition(double s, double[] out) {
        Maths.rotate(ax, ay, az, s * angle, -sx, -sy, d0 - sz, out);
        double k = Math.pow(r, s);
        out[0] = sx + k * out[0];
        out[1] = sy + k * out[1];
        out[2] = sz + k * out[2];
    }

    // How much of the detour applies at s (0 outside the dive)
    private double detourFactor(double s) {
        if (s <= 0 || s >= 1 || dx == 0 && dy == 0 && dz == 0)
            return 0;
        double bump = Math.sin(Math.PI * s);
        return bump * bump * Math.pow(r, s);
    }

    // Camera orientation at s (in the root coordinates)
    void orientation(double s, double[] q) {
        Maths.axisAngleToQuat(ax, ay, az, s * angle, q);
        double k = detourFactor(s);
        if (k == 0)
            return;
        // Rotation from the direction of the target seen from the spiral to its direction seen from the detour
        double[] p = tmp;
        spiralPosition(s, p);
        double ux = cx - p[0], uy = cy - p[1], uz = cz - p[2]; // from the spiral
        double vx = ux - k * dx, vy = uy - k * dy, vz = uz - k * dz; // from the detour
        double w = Math.sqrt((ux * ux + uy * uy + uz * uz) * (vx * vx + vy * vy + vz * vz)) + ux * vx + uy * vy + uz * vz;
        double qx = uy * vz - uz * vy, qy = uz * vx - ux * vz, qz = ux * vy - uy * vx;
        double l = Math.sqrt(qx * qx + qy * qy + qz * qz + w * w);
        if (l < 1e-12)
            return;
        p[0] = qx / l; p[1] = qy / l; p[2] = qz / l; p[3] = w / l;
        Maths.quatMul(p, q, q); // (world space correction)
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
