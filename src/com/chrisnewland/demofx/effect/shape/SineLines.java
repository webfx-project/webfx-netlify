/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.shape;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.effect.addon.HasVolume;
import javafx.scene.paint.Color;

public class SineLines extends AbstractEffect implements HasVolume
{
	public SineLines(DemoConfig config)
	{
		super(config);
	}

	double angle = 0;
	double shift = 0;

	double[] bandVolumes;

	@Override
	public void setBandVolumes(double[] bandVolumes) {
		this.bandVolumes = bandVolumes;
	}

	@Override public void renderForeground()
	{
		gc.setLineWidth(8);

		int step = 8;

		angle = 0;
		shift += 0.007;

		for (int l = 1; l < 9; l++)
		{
			shift += 0.00001;

			double lastX = 0;
			double lastY = 0;

			gc.setStroke(Color.color(1.0, (0.9 - (double) l / 10.0), 0));

			for (int i = 0; i < width; i += step)
			{
				double yDelta = Math.sin(angle + shift * l) * 16;

				double hd;

				if (bandVolumes != null)
					hd = bandVolumes[l - 1];
				else
					hd = Math.sin(shift + (l * 32)) * l;

				double y = halfHeight + yDelta * hd;

				angle += 0.1;

				if (i > 0)
				{
					gc.strokeLine(lastX, lastY, i, y);
				}

				lastX = i;
				lastY = y;
			}
		}
	}
}