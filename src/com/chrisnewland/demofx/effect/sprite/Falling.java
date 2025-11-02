/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.sprite;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.util.ImageUtil;
import javafx.scene.image.Image;

public class Falling extends AbstractEffect
{
	private final double[] bx;
	private final double[] by;
	private final double[] dx;
	private final double[] dy;
	private final Image[] sprites;

	private final Image[] spriteChoices;

	private static final double ACCELERATION = 1.02;

	private final boolean directionDown;

	private double scaleFactor = -1; // Scale factor for small screens

	public Falling(DemoConfig config)
	{
		// By Wilson Bentley - http://snowflakebentley.com/snowflakes.htm,
		// Public Domain,
		// https://commons.wikimedia.org/w/index.php?curid=1282792
		this(config, "flake.png");
	}

	public Falling(DemoConfig config, String imageFileName) {
		this(config, ImageUtil.loadImageFromResources(imageFileName));
	}
	public Falling(DemoConfig config, Image... images) {
		this(config, images, true);
	}

	public Falling(DemoConfig config, Image[] spriteChoices, boolean directionDown)
	{
		super(config);

		this.spriteChoices = spriteChoices;

		this.directionDown = directionDown;

		if (itemCount == -1)
		{
			itemCount = 512;
		}

		bx = new double[itemCount];
		by = new double[itemCount];
		dx = new double[itemCount];
		dy = new double[itemCount];
		sprites = new Image[itemCount];

		for (int i = 0; i < itemCount; i++)
		{
			respawn(i);
		}
	}

	private void respawn(int i)
	{
		bx[i] = width * precalc.getUnsignedRandom();

		dx[i] = 0;

		sprites[i] = spriteChoices[i % spriteChoices.length];

		if (directionDown)
		{
			by[i] = -height * precalc.getUnsignedRandom();
			dy[i] = precalc.getUnsignedRandom() / 2;
		}
		else
		{
			by[i] = height + height * precalc.getUnsignedRandom();
			dy[i] = -precalc.getUnsignedRandom() / 2;
		}
	}

	@Override
	public void renderForeground()
	{
		for (int i = 0; i < itemCount; i++)
		{
			moveSprite(i);

			drawSprite(i);
		}
	}

	private void moveSprite(int i)
	{
		dx[i] += precalc.getSignedRandom() / 8;
		dy[i] *= ACCELERATION;

		bx[i] += dx[i];
		by[i] += dy[i];

		if (directionDown)
		{
			if (by[i] > height)
			{
				respawn(i);
			}
		}
		else
		{
			if (by[i] < -100)
			{
				respawn(i);
			}
		}
	}

	private void drawSprite(int i)
	{
		Image sprite = sprites[i];
		if (scaleFactor == -1 && sprite.getWidth() > 0)
			scaleFactor = Math.min(1, width / sprite.getWidth() / 20); // Scale factor for small screens
		if (scaleFactor > 0)
			gc.drawImage(sprite, bx[i], by[i], scaleFactor * sprite.getWidth(), scaleFactor * sprite.getHeight());
		else
			gc.drawImage(sprite, bx[i], by[i]);
	}
}