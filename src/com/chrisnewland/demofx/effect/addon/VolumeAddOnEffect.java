package com.chrisnewland.demofx.effect.addon;

import com.chrisnewland.demofx.ISpectrumDataProvider;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.effect.spectral.ISpectralEffect;

/**
 * @author Bruno Salmon
 */
public class VolumeAddOnEffect extends AbstractAddOnEffect implements ISpectralEffect {

    private final HasVolume hasVolume;
    private final double volumeFactor;
    private final int[] bandIndexes;
    private final double[] bandVolumes;

    private ISpectrumDataProvider spectrumProvider;

    public VolumeAddOnEffect(AbstractEffect effect, double volumeFactor, int... bandIndexes) {
        super(effect);
        hasVolume = (HasVolume) effect;
        this.volumeFactor = volumeFactor;
        this.bandIndexes = bandIndexes;
        bandVolumes = new double[bandIndexes.length];
    }

    @Override
    public void setSpectrumDataProvider(ISpectrumDataProvider provider) {
        spectrumProvider = provider;
    }

    @Override
    public void renderForeground() {
        if (spectrumProvider != null) {
            int n = bandIndexes.length;
            float[] data = spectrumProvider.getData();
            double gap = volumeFactor / 10;
            for (int i = 0; i < n; i++) {
                double newVolume = (data[bandIndexes[i]] + 60) / 60 * volumeFactor;
                double oldVolume = bandVolumes[i];
                if (oldVolume != 0 && Math.abs(newVolume - oldVolume) > gap)
                    newVolume = oldVolume + (newVolume < oldVolume ? -gap : gap);
                bandVolumes[i] = newVolume;
            }
            hasVolume.setBandVolumes(bandVolumes);
        }
        super.renderForeground();
    }
}
