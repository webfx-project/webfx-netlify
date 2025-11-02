/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.sprite;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.util.ImageUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Moire2 extends AbstractEffect
{
	private Image image;
	private boolean clip;

	private double rotationDir;
	private double rotationAngle = 0.0;

	private long lastEffectStart = 0;
	private long millisPerEffect = 0;

	private List<MoireParameters> paramList;
	private int currentParamIndex = 0;

	private Random random = new Random();

	private double zoom = 1;
	private double zoomAngle = 0;

	private double clipAngle;

	private int minDimension;

	private double clipWidthOuter;
	private double clipHeightOuter;

	private double clipWidthInner;
	private double clipHeightInner;

	static class MoireParameters
	{
		private Image image;
		private double rotationSpeed;

		public MoireParameters(Image image, double rotationSpeed)
		{
			this.image = image;
			this.rotationSpeed = rotationSpeed;
		}

		public Image getImage()
		{
			return image;
		}

		public double getRotationSpeed()
		{
			return rotationSpeed;
		}
	}

	enum Shape
	{
		CIRCLE, SQUARE
	}

	public Moire2(DemoConfig config)
	{
		super(config);

		//long t0 = System.currentTimeMillis();
		init();
		//Console.log("Init duration: " + (System.currentTimeMillis() - t0) + "ms");
	}

	private void init()
	{
		int intWidth = (int) width;
		int intHeight = (int) height;

		minDimension = clip ? Math.min(intWidth, intHeight) : Math.max(intWidth, intHeight);
		if (!clip) {
			gc.getCanvas().setWidth(minDimension);
			gc.getCanvas().setHeight(minDimension);
		}

		clipWidthOuter = (minDimension / 2) * 0.94;
		clipHeightOuter = (minDimension / 2) * 0.94;

		clipWidthInner = (minDimension / 2) * 0.92;
		clipHeightInner = (minDimension / 2) * 0.92;

		paramList = new ArrayList<>();

		paramList.add(new MoireParameters(buildImageCheckerboard(4, Shape.SQUARE), 0.32 * 3));
		//paramList.add(new MoireParameters(buildImageCheckerboard(6, Shape.CIRCLE), 0.16 * 3));

		millisPerEffect = 2667 * 2;// 2 bars @ 90bpm

		//System.out.println("millisPerEffect: " + millisPerEffect);

		lastEffectStart = System.currentTimeMillis();

		currentParamIndex = 0;

		MoireParameters params = paramList.get(currentParamIndex);

		this.image = params.getImage();
		this.rotationDir = params.getRotationSpeed();

		if (!clip) {
			gc.getCanvas().setWidth(width);
			gc.getCanvas().setHeight(height);
		}
	}

	private Image buildImageCheckerboard(double side, Shape shape)
	{
		// Following Google Chrome advise, and preventing this warning: Canvas2D: Multiple readback operations using getImageData are faster with the willReadFrequently attribute set to true
		Canvas canvas = WebFxKitLauncher.createWillReadFrequentlyCanvas(minDimension, minDimension);
		GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, minDimension, minDimension);

		int changeColourEvery = (int) (minDimension / side / 16);

		boolean offset = false;

		double gapX = side * 2;
		double gapY = side;

		int colourCount = 0;
		//int iterations = 0;

		for (double y = 0; y < minDimension; y += gapY)
		{
			offset = !offset;

			for (double x = 0; x < minDimension; x += gapX)
			{
				//iterations++;
				double x2 = x + (offset ? 0 : side);

				if (colourCount++ == changeColourEvery)
				{
					gc.setFill(getCycleColour());
					colourCount = 0;
				}

				if (shape == Shape.SQUARE)
				{
					gc.fillRect(x2, y, side, side);
				}
				else
				{
					gc.fillOval(x2, y, side, side);
				}
			}
		}

		//Console.log("Iterations: " + iterations);

		return ImageUtil.replaceColour(ImageUtil.createImageFromCanvas(canvas, minDimension, minDimension, true, true),
				Color.BLACK, Color.TRANSPARENT);
	}

	private void clip()
	{
		clipAngle += 0.72;

		if (clipAngle >= 360.0)
		{
			clipAngle -= 360.0;
		}

		double modX = (precalc.sin(clipAngle) + 0.8) * 48.0;
		double modY = (precalc.cos(clipAngle) + 0.8) * 48.0;

		gc.beginPath();
		gc.arc(width / 2.0, height / 2.0, clipWidthOuter - modX, clipHeightOuter - modY, 0, 360);
		gc.closePath();
		gc.clip();

		fillBackground(Color.WHITE);

		gc.beginPath();
		gc.arc(width / 2.0, height / 2.0, clipWidthInner - modX, clipHeightInner - modY, 0, 360);
		gc.closePath();
		gc.clip();
	}

	@Override public void renderForeground()
	{
		if (clip)
			clip();

		fillBackground(Color.BLACK);

		/* long now = System.currentTimeMillis();

		if (now - lastEffectStart > millisPerEffect)
		{
			currentParamIndex++;

			if (currentParamIndex >= paramList.size())
			{
				currentParamIndex = 0;
			}

			lastEffectStart = now;

			MoireParameters params = paramList.get(currentParamIndex);

			this.image = params.getImage();
			this.rotationDir = params.getRotationSpeed();
			rotationAngle = 0;
			zoom = 1;
		}*/

		zoomAngle += 1.4;

		if (zoomAngle >= 360)
		{
			zoomAngle -= 360;
		}

		zoom = 2.15 + precalc.cos(zoomAngle);

		double scaledDimension = minDimension * zoom;

		double newX = width / 2 - scaledDimension / 2;
		double newY = height / 2 - scaledDimension / 2;

		gc.drawImage(image, newX, newY, scaledDimension, scaledDimension);

		rotateCanvasAroundCentre(rotationDir);

		rotationAngle += rotationDir;

		gc.drawImage(image, newX, newY, scaledDimension, scaledDimension);
	}
}