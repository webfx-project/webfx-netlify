/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.shape;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.util.ImageUtil;
import javafx.scene.image.Image;

import java.util.Arrays;

public class Concentric extends AbstractEffect
{
	private double angleClockwise = 0;
	private double angleAntiClockwise = 0;

	private double radii[];
	private double ringX[];
	private double ringY[];
	private int imageIndex[];
	private Image[] images;

	private boolean clockwise = true;

	private double space;

	private static final int PER_RING = 32;
	private static final double SPEED = 3.0;
	private static final double EACH_ANGLE = 360 / (double) PER_RING;
	private final double OFFSCREEN;

	private long[] pulseTimes;
	private int nextPulseIndex;

	public Concentric(DemoConfig config) {
		this(config, "tomato.png", "satsuma.png", "pear.png", "apple.png", "orange.png", "pineapple.png", "banana.png", "strawberry.png");
	}

	public Concentric(DemoConfig config, String... imageFiles) {
		this(config, Arrays.stream(imageFiles).map(ImageUtil::loadImageFromResources).toArray(Image[]::new));
	}

	public Concentric(DemoConfig config, Image... images) {
		this(config, 10, images);
	}

	public Concentric(DemoConfig config, int maxRings, Image... images)
	{
		super(config);
		OFFSCREEN = Math.max(halfWidth, halfHeight) * 1.2;

		space = width / maxRings / 1.7;

		radii = new double[maxRings];
		radii[0] = 1;

		ringX = new double[maxRings];
		ringY = new double[maxRings];

		this.images = images;

		imageIndex = new int[maxRings];

		for (int i = 0; i < maxRings; i++)
		{
			imageIndex[i] = i % images.length;
			Image image = images[imageIndex[i]];
			ringX[i] = halfWidth - image.getWidth() / 2;
			ringY[i] = halfHeight - image.getHeight() / 2;
		}

		itemCount = maxRings * PER_RING;
	}

	public Concentric setPulseTimes(long... pulseTimes) {
		this.pulseTimes = pulseTimes;
		return this;
	}

	private boolean readyForNextRing() {
		if (pulseTimes == null)
			return lastRadius > space;
		if (nextPulseIndex >= pulseTimes.length)
			return false;
		long elapsed = config.getDemoAnimationTimer().getElapsed();
		if (elapsed < pulseTimes[nextPulseIndex])
			return false;
		nextPulseIndex++;
		return true;
	}

	@Override
	public void renderForeground()
	{
		if (pulseTimes != null && nextPulseIndex == 0 && !readyForNextRing())
			return;

		rotateRings();

		plotRings();
	}

	private final void rotateRings()
	{
		angleClockwise++;

		if (angleClockwise >= 360)
		{
			angleClockwise -= 360;
		}

		angleAntiClockwise--;

		if (angleAntiClockwise <= 0)
		{
			angleAntiClockwise += 360;
		}
	}

	double lastRadius;

	private final void plotRings()
	{

		lastRadius = 0;

		for (int i = 0; i < radii.length; i++)
		{
			if (radii[i] > 0 || readyForNextRing())
			{
				radii[i] += SPEED;

				if (radii[i] > OFFSCREEN)
				{
					respawnRing(i);
					if (pulseTimes != null) {
						radii[i] = 0;
					}
				}

				plotRing(i, clockwise ? angleClockwise : angleAntiClockwise);
			}

			clockwise = !clockwise;

			lastRadius = radii[i];
		}
	}

	private final void respawnRing(int ringIndex)
	{
		radii[ringIndex] = 1;
		Image image = images[imageIndex[ringIndex]];
		ringX[ringIndex] = halfWidth - image.getWidth() / 2;
		ringY[ringIndex] = halfHeight - image.getHeight() / 2;
	}

	private void plotRing(int ringIndex, double angle)
	{
		double radius = radii[ringIndex];
		if (radius == 0)
			return;

		for (int i = 0; i < PER_RING; i++)
		{
			double sAngle = angle + EACH_ANGLE * i;

			double x = ringX[ringIndex] + radius * precalc.sin(sAngle);
			double y = ringY[ringIndex] + radius * precalc.cos(sAngle);

			gc.drawImage(images[imageIndex[ringIndex]], x, y);
		}
	}
}