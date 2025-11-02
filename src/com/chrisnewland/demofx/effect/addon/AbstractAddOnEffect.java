package com.chrisnewland.demofx.effect.addon;

import com.chrisnewland.demofx.effect.IEffect;

/**
 * @author Bruno Salmon
 */
public class AbstractAddOnEffect implements IEffect {

    protected final IEffect effect;

    public AbstractAddOnEffect(IEffect effect) {
        this.effect = effect;
    }

    @Override
    public void renderForeground() {
        effect.renderForeground();
    }

    @Override
    public void start() {
        effect.start();
    }

    @Override
    public void stop() {
        effect.stop();
    }

    @Override
    public void setStartOffsetMillis(long start) {
        effect.setStartOffsetMillis(start);
    }

    @Override
    public void setStopOffsetMillis(long stop) {
        effect.setStopOffsetMillis(stop);
    }

    @Override
    public long getStartOffsetMillis() {
        return effect.getStartOffsetMillis();
    }

    @Override
    public long getStopOffsetMillis() {
        return effect.getStopOffsetMillis();
    }

    @Override
    public boolean isVisible(long elapsed) {
        return effect.isVisible(elapsed);
    }
}
