/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.sprite;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.util.ImageUtil;
import com.chrisnewland.demofx.util.MaskingSystem;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;

public class MaskStack extends AbstractEffect
{
	private final Image image;
	private final int count;
	private Image[] imageRing;
	private double[] ringAngle;
	private double[] angleInc;
	private final boolean slowOnImage;
	private double angleSpeedFactor;

	private static int thickness;

	public MaskStack(DemoConfig config)
	{
		this(config, false);
	}

	public MaskStack(DemoConfig config, Image image)
	{
		this(config, 16, image);
	}

	public MaskStack(DemoConfig config, boolean slowOnImage)
	{
		this(config, 16, ImageUtil.loadImageFromResources("tiger.jpeg"), slowOnImage);
	}

	public MaskStack(DemoConfig config, Image image, boolean slowOnImage)
	{
		this(config, 16, image, slowOnImage);
	}

	public MaskStack(DemoConfig config, int count, Image image) {
		this(config, count, image, false);
	}

	public MaskStack(DemoConfig config, int count, Image image, boolean slowOnImage) {
		super(config);
		this.image = image;
		this.count = count;
		this.slowOnImage = slowOnImage;

		if (image.getProgress() >= 1)
			init(count, image);
		else
			image.progressProperty().addListener((observableValue, number, t1) -> { if (t1.doubleValue() >= 1) init(count, image); });
	}

	private void init(int count, Image image)
	{
		itemCount = count;

		imageRing = new Image[count];

		ringAngle = new double[count];

		angleInc = new double[count];

		double angle = 0;

		double imgWidth = image.getWidth();
		double imgHeight = image.getHeight();
		double outputScaleX = Screen.getPrimary().getOutputScaleX();
		double outputScaleY = Screen.getPrimary().getOutputScaleY();
		if (outputScaleX != 1 || outputScaleY != 1) {
			Canvas canvas = new Canvas(imgWidth, imgHeight);
			canvas.getGraphicsContext2D().drawImage(image, 0, 0);
			SnapshotParameters sp = new SnapshotParameters();
			sp.setTransform(Scale.scale(outputScaleX, outputScaleY));
			image = canvas.snapshot(sp, null);
		}
		int diameterOuter = (int) Math.min(imgWidth, imgHeight);

		thickness = diameterOuter / count;

		for (int i = 0; i < itemCount; i++)
		{
			ringAngle[i] = i * angle;

			angleInc[i] = (i + 1.0);

			int diameterInner = diameterOuter - thickness;

			if (i == itemCount - 1)
			{
				diameterInner = 0;
			}

			Image mask = MaskingSystem.createMaskRing(diameterOuter, diameterInner, true);
			//Image mask = MaskingSystem.createMaskBorder(diameterOuter, diameterOuter, thickness);
			
			int offsetX = (int) (imgWidth * outputScaleX - mask.getWidth()) / 2;
			int offsetY = (int) (imgHeight * outputScaleY - mask.getHeight()) / 2;

			imageRing[i] = MaskingSystem.applyMask(mask, image, offsetX, offsetY);

			diameterOuter = diameterInner;

		}
	}

	@Override
	public void renderForeground()
	{
		if (imageRing == null) {
			if (image.getWidth() > 0)
				init(count, image);
			else
				return;
		}

		if (slowOnImage) {
			double outerRingAngle = ringAngle[0];
			double slowAngle = 15;
			double slowSpeed = 0.1;
			double outerRingAngle360 = outerRingAngle % 360;
			angleSpeedFactor = outerRingAngle360 > slowAngle && outerRingAngle360 < 360 - slowAngle ? 1 : slowSpeed + Math.abs(precalc.sin(outerRingAngle) / precalc.sin(slowAngle)) * (1 - slowSpeed);
		} else
			angleSpeedFactor = 1;

		for (int i = 0; i < itemCount; i++)
		{
			move(i);

			render(i);
		}
	}

	private final void move(int i)
	{
		ringAngle[i] += angleInc[i] * angleSpeedFactor;

	}

	private final void render(int i)
	{
		Image image = imageRing[i];

		double outputScaleX = Screen.getPrimary().getOutputScaleX();
		double outputScaleY = Screen.getPrimary().getOutputScaleY();
		double imageWidth = image.getWidth() / outputScaleX;
		double imageHeight = image.getHeight() / outputScaleY;

		double x = thickness * precalc.sin(ringAngle[i]);
		double y = thickness * precalc.cos(ringAngle[i]);

		x += halfWidth - imageWidth / 2;
		y += halfHeight - imageHeight / 2;

		gc.drawImage(imageRing[i], x, y, imageWidth, imageHeight);
	}
}