/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.fake3d;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.util.ImageUtil;
import javafx.scene.image.Image;

public class SnowfieldSprite extends AbstractEffect
{
	private double[] starX;
	private double[] starY;
	private double[] starZ;
	private double[] dX;
	private double[] dY;
	private double[] dZ;

	private double windX = 1;

	private double sine = 0;

	private final boolean spin = true;

	private final Image sprite;

	private double scaleFactor = -1; // Scale factor for small screens

	public SnowfieldSprite(DemoConfig config)
	{
		this(config, 5000, ImageUtil.loadImageFromResources("flake.png"));
	}

	public SnowfieldSprite(DemoConfig config, int starCount, Image sprite)
	{
		this(config, starCount, sprite, -1, -1);
	}

	public SnowfieldSprite(DemoConfig config, int starCount, Image sprite, long startMillis, long stopMillis)
	{
		super(config);

		this.itemCount = starCount;
		this.sprite = sprite;
		this.effectStartMillis = startMillis;
		this.effectStopMillis = stopMillis;

		buildStars();
	}

	private void buildStars()
	{
		starX = new double[itemCount];
		starY = new double[itemCount];
		starZ = new double[itemCount];

		dX = new double[itemCount];
		dY = new double[itemCount];
		dZ = new double[itemCount];

		for (int i = 0; i < itemCount; i++)
		{
			starX[i] = precalc.getSignedRandom() * halfWidth;
			starY[i] = -halfHeight - precalc.getUnsignedRandom() * height;
			starZ[i] = precalc.getUnsignedRandom();

			dX[i] = precalc.getSignedRandom();
			dY[i] = 2 + precalc.getUnsignedRandom();
			dZ[i] = precalc.getSignedRandom() / 512;
		}
	}

	@Override
	public void renderForeground()
	{
		sine += 0.5;

		if (sine > 360)
		{
			sine -= 360;
		}

		windX = precalc.sin(sine);

		for (int i = 0; i < itemCount; i++)
		{
			moveStar(i);

			plotStar(i);
		}
	}

	// TODO move all then plot all
	private void moveStar(int i)
	{
		starX[i] += dX[i] + windX;
		starY[i] += dY[i] - windX / 4;
		starZ[i] += dZ[i] + windX / 512;
	}

	private double translateX(int i)
	{
		return starX[i] / starZ[i];
	}

	private double translateY(int i)
	{
		return starY[i] / starZ[i];
	}

	private void plotStar(int i)
	{
		double x = halfWidth + translateX(i);
		double y = halfHeight + translateY(i);
		double z = starZ[i];

		if (x < -64)
		{
			starX[i] += width;
		}
		else if (x > width)
		{
			starX[i] -= width;
		}

		if (y > height)
		{
			starY[i] -= height;
		}

		if (z > 2 || z <= 0)
		{
			starZ[i] = precalc.getUnsignedRandom();
		}

		x = halfWidth + translateX(i);
		y = halfHeight + translateY(i);
		z = starZ[i];

		if (scaleFactor == -1 && sprite.getWidth() > 0)
			scaleFactor = Math.min(1, width / sprite.getWidth() / 40); // Scale factor for small screens
		int size = (int) (8.0 / z * scaleFactor);

		if (size > 1)
		{
			gc.drawImage(sprite, x, y, size, size);
		}
	}
}
