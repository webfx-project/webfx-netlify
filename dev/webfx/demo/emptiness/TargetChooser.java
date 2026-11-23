package dev.webfx.demo.emptiness;

import java.util.Arrays;
import java.util.Random;

/**
 * Chooses which part of a sphere the camera dives into next.
 *
 * <p>To keep the feeling of emptiness, the camera should fly through empty space only: the target is a part that the
 * camera sees unobstructed during the dive (once it is big enough to be noticed), and the camera never passes close to
 * another part or stem on its way. Any such part can be chosen, on the front of the sphere or deeper inside when a
 * clear way leads to it. When the spiral path to a part is blocked, curved paths (detours) are tried, so that parts
 * hidden behind others can be reached too. The deeper parts are preferred, so that the camera often dives inside.</p>
 *
 * @author Bruno Salmon
 */
final class TargetChooser {

    // Number of camera positions checked along the path
    private static final int PATH_SAMPLES = 24;
    // Number of camera positions from which the target must be visible...
    private static final int SIGHT_SAMPLES = 9;
    // ... from that point of the dive (before, the target is still too small to be noticed if hidden for a moment)
    private static final double SIGHT_FROM = 0.3;
    // Minimal distances (in part radii) from the camera to the other parts, from the camera to the stems, and from the
    // line of sight to the other parts (the thickest stem is 0.4 r)
    private static final double PART_CLEARANCE = 1.2, STEM_CLEARANCE = 0.6, SIGHT_CLEARANCE = 1.1;
    // Detours tried when the spiral path is blocked: their sizes (in the root radius) and number of directions
    private static final double[] DETOUR_SIZES = {0.15, 0.3, 0.5};
    private static final int DETOUR_DIRECTIONS = 8;
    // Detours are tried for that number of parts at most (in the random order), to keep the choice fast (trying more
    // doesn't reach more inner parts in practice, but can make the choice more than twice slower with many parts)
    private static final int DETOUR_PARTS = 4;
    // How much the deeper parts are preferred: a part at the center is e^DEPTH_BIAS (~20) times more likely to be chosen
    // than a part at the surface (if both can be reached). With 3, about half of the dives go into the inner 60% of the
    // sphere (about 20% without the bias, as the outer layers hold most of the parts)
    private static final double DEPTH_BIAS = 3;

    private TargetChooser() {}

    static ZoomPath choosePath(SphereLayout layout, double d0, Random random) {
        int n = layout.n;
        // Parts checked in a random order: the first valid one is a random choice among all valid parts. The order is
        // biased towards the deeper parts (weighted random order: each part gets a random key -log(u) / weight, and the
        // parts are checked by increasing key), as most parts are near the surface (where most of the volume is).
        double[] keys = new double[n];
        Integer[] order = new Integer[n];
        for (int j = 0; j < n; j++) {
            double depth = Maths.clamp01(1 - Math.sqrt(sq(layout.cx[j]) + sq(layout.cy[j]) + sq(layout.cz[j])) / (1 - layout.r));
            keys[j] = -Math.log(1 - random.nextDouble()) / Math.exp(DEPTH_BIAS * depth);
            order[j] = j;
        }
        Arrays.sort(order, (j1, j2) -> Double.compare(keys[j1], keys[j2]));
        ZoomPath best = null;
        int bestViolations = Integer.MAX_VALUE;
        double bestDistance = Double.MAX_VALUE;
        double[] p = new double[3];
        for (int o = 0; o < n; o++) {
            int j = order[o];
            // 1) The spiral path
            ZoomPath path = new ZoomPath(layout, j, d0);
            int violations = countViolations(layout, j, path, bestViolations, p);
            if (violations == 0)
                return path;
            // 2) A curved path (only for the first parts, to keep the choice fast)
            if (o < DETOUR_PARTS) {
                ZoomPath detour = findDetour(layout, j, d0, random, p);
                if (detour != null)
                    return detour;
            }
            // If no part is valid, we take the least bad spiral path (the front-most one in case of equality)
            double distance = sq(layout.cx[j]) + sq(layout.cy[j]) + sq(d0 - layout.cz[j]);
            if (violations < bestViolations || violations == bestViolations && distance < bestDistance) {
                bestViolations = violations;
                bestDistance = distance;
                best = path;
            }
        }
        return best;
    }

    // Returns a clear detour to the part j, or null if none: sideways shifts perpendicular to the way from the start
    // position (0, 0, d0) to the part, from the smallest one, in all directions
    private static ZoomPath findDetour(SphereLayout layout, int j, double d0, Random random, double[] p) {
        double tx = layout.cx[j], ty = layout.cy[j], tz = layout.cz[j] - d0, tl = Math.sqrt(tx * tx + ty * ty + tz * tz);
        tx /= tl; ty /= tl; tz /= tl;
        double ux = -ty, uy = tx, ul = Math.sqrt(ux * ux + uy * uy); // u = a unit vector perpendicular to t
        if (ul < 1e-6) { ux = 1; uy = 0; ul = 1; }
        ux /= ul; uy /= ul;
        double vx = -tz * uy, vy = tz * ux, vz = tx * uy - ty * ux; // v = t x u
        int firstDirection = random.nextInt(DETOUR_DIRECTIONS); // (no preferred side)
        for (double size : DETOUR_SIZES) {
            for (int q = 0; q < DETOUR_DIRECTIONS; q++) {
                double a = 2 * Math.PI * (firstDirection + q) / DETOUR_DIRECTIONS, c = Math.cos(a), s = Math.sin(a);
                ZoomPath detour = new ZoomPath(layout, j, d0, size * (c * ux + s * vx), size * (c * uy + s * vy), size * s * vz);
                if (countViolations(layout, j, detour, 0, p) == 0)
                    return detour;
            }
        }
        return null;
    }

    // Counts the obstacles on that path to the part j (stops counting as soon as above maxViolations)
    private static int countViolations(SphereLayout layout, int j, ZoomPath path, int maxViolations, double[] p) {
        int n = layout.n;
        double r = layout.r;
        double partClearance2 = sq(PART_CLEARANCE * r), stemClearance2 = sq(STEM_CLEARANCE * r), sightClearance2 = sq(SIGHT_CLEARANCE * r);
        int violations = 0;
        // 1) The camera doesn't pass close to another part or stem
        for (int k = 1; k <= PATH_SAMPLES; k++) {
            path.position((double) k / PATH_SAMPLES, p);
            for (int m = 0; m < n; m++) {
                if (m != j && sq(p[0] - layout.cx[m]) + sq(p[1] - layout.cy[m]) + sq(p[2] - layout.cz[m]) < partClearance2
                        && ++violations > maxViolations)
                    return violations;
            }
            for (int b = 0; b < layout.stemA.length; b++) {
                if (distanceToSegment2(p[0], p[1], p[2], layout, layout.stemA[b], layout.stemB[b]) < stemClearance2
                        && ++violations > maxViolations)
                    return violations;
            }
        }
        // 2) The target is visible during the dive (nothing between the camera and the target)
        for (int k = 0; k < SIGHT_SAMPLES; k++) {
            double s = (double) k / (SIGHT_SAMPLES - 1);
            if (s < SIGHT_FROM)
                continue;
            path.position(s, p);
            double ex = layout.cx[j] - p[0], ey = layout.cy[j] - p[1], ez = layout.cz[j] - p[2];
            double el2 = ex * ex + ey * ey + ez * ez;
            for (int m = 0; m < n; m++) {
                if (m != j) {
                    double t = Maths.clamp01(((layout.cx[m] - p[0]) * ex + (layout.cy[m] - p[1]) * ey + (layout.cz[m] - p[2]) * ez) / el2);
                    if (sq(p[0] + t * ex - layout.cx[m]) + sq(p[1] + t * ey - layout.cy[m]) + sq(p[2] + t * ez - layout.cz[m]) < sightClearance2
                            && ++violations > maxViolations)
                        return violations;
                }
            }
        }
        return violations;
    }

    // Squared distance from the point (x, y, z) to the segment between the parts a and b of the layout
    private static double distanceToSegment2(double x, double y, double z, SphereLayout layout, int a, int b) {
        double ax = layout.cx[a], ay = layout.cy[a], az = layout.cz[a];
        double ex = layout.cx[b] - ax, ey = layout.cy[b] - ay, ez = layout.cz[b] - az;
        double t = Maths.clamp01(((x - ax) * ex + (y - ay) * ey + (z - az) * ez) / (ex * ex + ey * ey + ez * ez));
        return sq(ax + t * ex - x) + sq(ay + t * ey - y) + sq(az + t * ez - z);
    }

    private static double sq(double v) {
        return v * v;
    }
}
