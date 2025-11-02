package com.chrisnewland.demofx.effect.addon;

import com.chrisnewland.demofx.effect.AbstractEffect;

/**
 * @author Bruno Salmon
 */
public class FadeOutAddOnEffect extends AbstractFadeAddOnEffect {

    public FadeOutAddOnEffect(AbstractEffect effect) {
        this(effect, 4000);
    }

    public FadeOutAddOnEffect(AbstractEffect effect, long duration) {
        super(effect, duration, false, true);
    }
}
