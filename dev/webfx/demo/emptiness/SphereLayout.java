package dev.webfx.demo.emptiness;

import java.util.Random;

/**
 * How a sphere of radius 1 is composed of smaller spheres: the centers of its parts (all of the same radius r, all
 * fully inside the unit ball) and the rotation of each part (so that the parts of a part are not simply a scaled copy
 * of the same arrangement).
 *
 * <p>The parts fill a fixed fraction of the volume, so that from far they merge into the appearance of one solid sphere,
 * while leaving enough empty space between them to reveal, from close, that there is no such solid sphere.</p>
 *
 * @author Bruno Salmon
 */
final class SphereLayout {

    // Fraction of the sphere volume occupied by its parts, from tight (no spacing) to sparse (max spacing)
    // (the sparse packing is what a spacing of 150% gave with the first range, 27% to 4%)
    static final double TIGHT_PACKING = 0.27, SPARSE_PACKING = TIGHT_PACKING * Math.pow(0.04 / TIGHT_PACKING, 1.5); // ~1.5%
    // Minimal distance between parts, as a factor of the average space available for each part
    private static final double EVEN_SPREAD = 0.85;
    // Minimal gap between two parts, as a factor of their diameter
    private static final double GAP = 1.12;
    private static final int RELAXATION_ITERATIONS = 120;
    // Stems link each part to (at most) this number of its nearest neighbours...
    private static final int MAX_STEMS_PER_PART = 4;
    // ... closer than this distance (as a factor of the minimal distance between two parts)
    private static final double MAX_STEM_LENGTH_FACTOR = 1.3;

    final int n;
    final double r;
    final double[] cx, cy, cz;       // centers of the parts (in the unit sphere coordinates)
    final double[] ax, ay, az, angle; // rotation of each part (axis + angle)
    final double[][] rot;             // same rotations as matrices
    final int[] stemA, stemB;         // stems between parts (pairs of part indexes)

    // spacing: 0 = tight packing, 1 = sparse packing
    SphereLayout(int n, double spacing, long seed) {
        this.n = n;
        r = Math.cbrt(TIGHT_PACKING * Math.pow(SPARSE_PACKING / TIGHT_PACKING, spacing) / n);
        cx = new double[n]; cy = new double[n]; cz = new double[n];
        ax = new double[n]; ay = new double[n]; az = new double[n]; angle = new double[n];
        rot = new double[n][9];
        Random random = new Random(seed);
        double maxCenterRadius = 1 - r;
        // Random initial positions inside the ball
        for (int i = 0; i < n; i++) {
            double x, y, z;
            do {
                x = 2 * random.nextDouble() - 1;
                y = 2 * random.nextDouble() - 1;
                z = 2 * random.nextDouble() - 1;
            } while (x * x + y * y + z * z > 1);
            cx[i] = x * maxCenterRadius; cy[i] = y * maxCenterRadius; cz[i] = z * maxCenterRadius;
        }
        // Relaxation: pushing overlapping parts apart, while keeping them inside the ball
        // Parts not closer than GAP times their diameter, and spread evenly (so the stems between them can be seen)
        double averageSpace = Math.cbrt(4 * Math.PI / 3 * maxCenterRadius * maxCenterRadius * maxCenterRadius / n);
        double minDistance = Math.max(2 * r * GAP, EVEN_SPREAD * averageSpace), minDistance2 = minDistance * minDistance;
        for (int iteration = 0; iteration < RELAXATION_ITERATIONS; iteration++) {
            boolean moved = false;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double dx = cx[j] - cx[i], dy = cy[j] - cy[i], dz = cz[j] - cz[i];
                    double d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 < minDistance2) {
                        double d = Math.sqrt(d2);
                        if (d < 1e-9) { // same position => random direction
                            dx = random.nextDouble() - 0.5; dy = random.nextDouble() - 0.5; dz = random.nextDouble() - 0.5;
                            d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        }
                        double push = (minDistance - Math.sqrt(d2)) / 2 / d;
                        cx[i] -= dx * push; cy[i] -= dy * push; cz[i] -= dz * push;
                        cx[j] += dx * push; cy[j] += dy * push; cz[j] += dz * push;
                        moved = true;
                    }
                }
            }
            for (int i = 0; i < n; i++) {
                double d = Math.sqrt(cx[i] * cx[i] + cy[i] * cy[i] + cz[i] * cz[i]);
                if (d > maxCenterRadius) {
                    double k = maxCenterRadius / d;
                    cx[i] *= k; cy[i] *= k; cz[i] *= k;
                }
            }
            if (!moved)
                break;
        }
        // A random moderate rotation for each part (15° to 45°, random axis, random sign)
        for (int i = 0; i < n; i++) {
            double z = 2 * random.nextDouble() - 1, phi = 2 * Math.PI * random.nextDouble(), s = Math.sqrt(1 - z * z);
            ax[i] = s * Math.cos(phi); ay[i] = s * Math.sin(phi); az[i] = z;
            angle[i] = (random.nextBoolean() ? 1 : -1) * Math.toRadians(15 + 30 * random.nextDouble());
            Maths.axisAngleToMatrix(ax[i], ay[i], az[i], angle[i], rot[i]);
        }
        // Stems: candidate pairs of close parts, accepted from the shortest, while both parts have stems left
        double maxStemLength = minDistance * MAX_STEM_LENGTH_FACTOR;
        int capacity = n * 16, count = 0;
        int[] pairA = new int[capacity], pairB = new int[capacity];
        double[] pairLength = new double[capacity];
        for (int i = 0; i < n && count < capacity; i++) {
            for (int j = i + 1; j < n && count < capacity; j++) {
                double dx = cx[j] - cx[i], dy = cy[j] - cy[i], dz = cz[j] - cz[i];
                double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (d < maxStemLength) {
                    pairA[count] = i; pairB[count] = j; pairLength[count] = d;
                    count++;
                }
            }
        }
        int[] order = new int[count];
        for (int k = 0; k < count; k++)
            order[k] = k;
        for (int k = 1; k < count; k++) { // insertion sort by length
            int o = order[k];
            int m = k - 1;
            while (m >= 0 && pairLength[order[m]] > pairLength[o]) {
                order[m + 1] = order[m];
                m--;
            }
            order[m + 1] = o;
        }
        int[] stemsPerPart = new int[n];
        int[] a = new int[count], b = new int[count];
        int stems = 0;
        for (int k = 0; k < count; k++) {
            int i = pairA[order[k]], j = pairB[order[k]];
            if (stemsPerPart[i] < MAX_STEMS_PER_PART && stemsPerPart[j] < MAX_STEMS_PER_PART) {
                a[stems] = i; b[stems] = j;
                stemsPerPart[i]++; stemsPerPart[j]++;
                stems++;
            }
        }
        stemA = new int[stems]; stemB = new int[stems];
        System.arraycopy(a, 0, stemA, 0, stems);
        System.arraycopy(b, 0, stemB, 0, stems);
    }
}
