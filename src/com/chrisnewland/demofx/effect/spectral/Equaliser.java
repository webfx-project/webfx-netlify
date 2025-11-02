/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.spectral;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.ISpectrumDataProvider;
import com.chrisnewland.demofx.util.ImageUtil;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.*;

import java.util.Arrays;

public class Equaliser extends AbstractSpectralEffect {
	private Color[] decibelColors;
	private Paint[] bandBallPaints;

	public Equaliser(DemoConfig config) {
		super(config);
		heightFactor = (float) (halfHeight / (DECIBEL_RANGE * 0.75));
	}

	@Override
	public void setSpectrumDataProvider(ISpectrumDataProvider provider) {
		super.setSpectrumDataProvider(provider);
		decibelColors = captureGradiantColors("#26DD7B, yellow, red 40%, red", 60);
		bandBallPaints = Arrays.stream(captureGradiantColors("#2B318F, #00ACEB, #00A656, #FCE400, #F36126, #CE0166, #91248D", usableBandCount))
				.map(Equaliser::createBallGradient).toArray(Paint[]::new);
	}

	private Color[] captureGradiantColors(String gradient60px, int colorCounts) {
		LinearGradient gradient = LinearGradient.valueOf("from 0px 0px to 60px 0px, " + gradient60px);
		PixelReader gradientReader = create60pxLineImage(gradient).getPixelReader();
		Color[] colors = new Color[colorCounts];
		double factor = 60d / colorCounts;
		for (int i = 0; i < colorCounts; i++) {
			int colorX = (int) (i * factor);
			colors[i] = gradientReader.getColor(colorX, 0);
		}
		return colors;
	}

	private static Image create60pxLineImage(Paint paint) {
		Canvas canvas = new Canvas(60, 1);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(paint);
		gc.fillRect(0, 0, 60, 1);
		return ImageUtil.createImageFromCanvas(canvas, 60, 1, false, false); // No need for HDPI
	}

	private static Paint createBallGradient(Color ledColor) {
		boolean pressed = true;
		return new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0.0, ledColor.deriveColor(0d, 1d, 1, 1d)),
				new Stop(0.49, ledColor.deriveColor(0d, 1d, pressed ? 0.65 : 0.7, 1d)),
				new Stop(1.0, ledColor.deriveColor(0d, 1d, pressed ? 1.3 : 0.35, 1d)));
	}

	@Override
	public void renderForeground() {
		if (renderOffScreen)
			fillBackground(getCycleColour());

		if (spectrumProvider != null) {
			float[] data = spectrumProvider.getData();
			for (int i = 0; i < usableBandCount; i++) {
				float mag = data[i];
				if (mag > bandMax[i])
					bandMax[i] = mag;
				else
					bandMax[i] -= DECAY;

				int decibelIndex = (int) (60 + mag);
				mag = convertY(mag);

				double x1 = i * bandWidth;

				gc.setFill(decibelColors[decibelIndex]);
				gc.fillRect(x1, halfHeight - mag, barWidth, 2 * mag);

				float y = convertY(bandMax[i]);
				double y1 = halfHeight - y -barWidth;
				double y2 = halfHeight + y;

				gc.setFill(bandBallPaints[i]);
				gc.fillOval(x1, y1, barWidth, barWidth);
				gc.fillOval(x1, y2, barWidth, barWidth);
			}
		}
	}

	private float convertY(float magnitude) {
		float result = (magnitude + DECIBEL_RANGE) * heightFactor;
		return result < 1 ? 1 : result;
	}
}