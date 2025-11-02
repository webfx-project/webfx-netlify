/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.text;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.util.TextUtil;
import javafx.scene.image.Image;

public class TextRing extends AbstractEffect
{
	public static class RingData
	{
		private final String text;
		private final double radius;
		private final double fontScale;
		private double angle;
		private final double speed;
		private final double kern;
		private final double spaceAngle;

		public RingData(String text, double radius, double fontScale, double speed, double kern, double spaceAngle)
		{
			this.text = text.toUpperCase();
			this.radius = radius;
			this.fontScale = fontScale * 3.25;
			this.speed = speed * 1.5;
			this.kern = kern;
			this.spaceAngle = spaceAngle * 1.6;
		}

		public void rotate()
		{
			angle += speed;

			if (angle >= 360)
			{
				angle -= 360;
			}
		}
	}

	private RingData[] ringData;

	public TextRing(DemoConfig config)
	{
		this(config, new RingData("Your Text Here", 300, 0.15, 1.6, 3.6, 2));
	}

	public TextRing(DemoConfig config, RingData... ringData)
	{
		super(config);
		this.ringData = ringData;
	}

	@Override
	public void renderForeground()
	{
		int count = ringData.length;

		for (int i = 0; i < count; i++)
		{
			plotText(i);
		}
	}

	private void plotText(int index)
	{
		RingData rd = ringData[index];

		rd.rotate();

		double charAngle = 180.0 + rd.angle;

		if (index % 2 == 0)
		{
			charAngle = 360 - charAngle;
		}

		int length = rd.text.length();

		double inc = 0;

		Character lastCharacter = null;

		for (int i = 0; i < length; i++)
		{
			char character = rd.text.charAt(i);

			if (character == ' ')
			{
				charAngle -= rd.spaceAngle;
				continue;
			}

			Image charImage = TextUtil.getSpriteCharacter(character);

			double plotWidth = charImage.getWidth() * rd.fontScale;
			double plotHeight = charImage.getHeight() * rd.fontScale;

			if (i > 0)
			{
				inc = rd.kern * TextUtil.getKerningForChar(character, lastCharacter, plotHeight, true);

				inc = Math.abs(inc);

				charAngle -= inc;
			}

			double x = halfWidth + rd.radius * precalc.sin(charAngle) - plotWidth / 2;

			double y = halfHeight + rd.radius * precalc.cos(charAngle) - plotHeight / 2;

			rotateCanvasAroundPoint(x + plotWidth / 2, y + plotHeight / 2, 180 - charAngle);

			gc.drawImage(charImage, x, y, plotWidth, plotHeight);

			lastCharacter = character;
		}
	}
}