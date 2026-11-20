package dev.webfx.demo.emptiness;

import java.util.Arrays;
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
        // Only close parts can push each other, so the parts are sorted into a grid of cells of minDistance size, and each
        // part is only compared with the parts of its cell and of the neighbouring cells (instead of all the parts).
        int g = Math.max(1, (int) Math.ceil(2 / minDistance)); // cells per axis (the parts are in the [-1, 1] cube)
        int[] cellStart = new int[g * g * g + 1], cellFill = new int[g * g * g], cellParts = new int[n], partCell = new int[n];
        for (int iteration = 0; iteration < RELAXATION_ITERATIONS; iteration++) {
            boolean moved = false;
            // Sorting the parts into the cells (counting sort)
            Arrays.fill(cellStart, 0);
            for (int i = 0; i < n; i++) {
                partCell[i] = cellOf(cx[i], g) + g * (cellOf(cy[i], g) + g * cellOf(cz[i], g));
                cellStart[partCell[i] + 1]++;
            }
            for (int c = 0; c < g * g * g; c++)
                cellStart[c + 1] += cellStart[c];
            System.arraycopy(cellStart, 0, cellFill, 0, g * g * g);
            for (int i = 0; i < n; i++)
                cellParts[cellFill[partCell[i]]++] = i;
            for (int i = 0; i < n; i++) {
                int ci = partCell[i], ix = ci % g, iy = ci / g % g, iz = ci / (g * g);
                for (int nz = Math.max(0, iz - 1); nz <= Math.min(g - 1, iz + 1); nz++)
                for (int ny = Math.max(0, iy - 1); ny <= Math.min(g - 1, iy + 1); ny++)
                for (int nx = Math.max(0, ix - 1); nx <= Math.min(g - 1, ix + 1); nx++) {
                    int c = nx + g * (ny + g * nz);
                    for (int p = cellStart[c]; p < cellStart[c + 1]; p++) {
                        int j = cellParts[p];
                        if (j <= i) // each pair once
                            continue;
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
        Integer[] order = new Integer[count];
        for (int k = 0; k < count; k++)
            order[k] = k;
        Arrays.sort(order, (k1, k2) -> Double.compare(pairLength[k1], pairLength[k2])); // by length (stable sort)
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

    // Index (0 to g - 1) of the grid cell containing the coordinate v (in [-1, 1])
    private static int cellOf(double v, int g) {
        return Math.max(0, Math.min(g - 1, (int) ((v + 1) / 2 * g)));
    }
}
