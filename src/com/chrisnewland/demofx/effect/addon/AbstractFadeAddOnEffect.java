package com.chrisnewland.demofx.effect.addon;

import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.util.PreCalc;
import javafx.scene.canvas.GraphicsContext;

/**
 * @author Bruno Salmon
 */
public class AbstractFadeAddOnEffect extends AbstractAddOnEffect {

    private final long duration;
    private final boolean fadeIn, fadeOut;
    private long fadeInStart, fadeOutStart;

    public AbstractFadeAddOnEffect(AbstractEffect effect, long duration, boolean fadeIn, boolean fadeOut) {
        super(effect);
        this.duration = duration;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
    }

    @Override
    public void renderForeground() {
        long now = ((AbstractEffect) effect).getConfig().getDemoAnimationTimer().getElapsed();
        if (fadeInStart == 0)
            fadeInStart = now;
        if (fadeOutStart == 0)
            fadeOutStart = now + (effect.getStopOffsetMillis() - effect.getStartOffsetMillis()) - duration;
        if (!applyFade(now - fadeInStart, true) && !applyFade(now - fadeOutStart, false))
            super.renderForeground();
    }

    private boolean applyFade(double elapsed, boolean fadeIn) {
        if (fadeIn && !this.fadeIn || !fadeIn && !this.fadeOut || fadeOut && elapsed < 0 || fadeIn && elapsed > duration)
            return false;
        if (fadeIn && elapsed < 0 || fadeOut && elapsed > duration)
            return true;
        double alpha = elapsed / (double) duration;
        alpha = PreCalc.clampDouble(alpha, 0, 1);
        if (!fadeIn)
            alpha = 1 - alpha;
        GraphicsContext gc = ((AbstractEffect) effect).gc;
        double previousAlpha = gc.getGlobalAlpha();
        gc.setGlobalAlpha(alpha);
        super.renderForeground();
        gc.setGlobalAlpha(previousAlpha);
        return true;
    }
}
