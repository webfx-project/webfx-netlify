package com.chrisnewland.demofx.effect.addon;

import com.chrisnewland.demofx.effect.AbstractEffect;

/**
 * @author Bruno Salmon
 */
public class RotateAddOnEffect extends AbstractAddOnEffect {

    private final HasAngle hasAngle;
    private final long[] times;
    private final double[] deltaAngles;
    private long start;
    private int index;
    public RotateAddOnEffect(AbstractEffect effect, double... timeAndAngles) {
        super(effect);
        hasAngle = (HasAngle) effect;
        int n = timeAndAngles.length / 2;
        times = new long[n];
        deltaAngles = new double[n];
        for (int i = 0; i < n; i++) {
            times[i] = (long) timeAndAngles[2*i];
            deltaAngles[i] = timeAndAngles[2*i + 1];
        }
    }

    @Override
    public void renderForeground() {
        long now = System.currentTimeMillis();
        if (start == 0)
            start = now - effect.getStartOffsetMillis();
        long elapsed = now - start;
        if (elapsed >= times[index]) {
            if (index + 1 < times.length && elapsed >= times[index + 1])
                index++;
            hasAngle.setAngle(hasAngle.getAngle() + deltaAngles[index]);
        }
        super.renderForeground();
    }
}
