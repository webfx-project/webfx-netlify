/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.pixel;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.effect.IPixelSink;
import com.chrisnewland.demofx.effect.IPixelSource;
import dev.webfx.extras.fastpixelreaderwriter.FastPixelReaderWriter;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;

public class Bobs extends AbstractEffect implements IPixelSource
{
	private int imageWidth;
	private int imageHeight;

	private FastPixelReaderWriter fastPixelReaderWriter;

	private int initialBlue;
	private int initialGreen;
	private int initialRed;

	private int saturatedB;
	private int saturatedG;
	private int saturatedR;

	private int bobSize;

	class Bob
	{
		private int bx;
		private int by;

		private int dx;
		private int dy;

		private int bw;
		private int bh;

		private int colourB;
		private int colourG;
		private int colourR;

		public void move()
		{
			dx += 4 * precalc.getSignedRandom();
			dy += 4 * precalc.getSignedRandom();

			bx += dx;
			by += dy;

			if (bx > width || bx < 0)
			{
				bx = (int) halfWidth;
			}

			if (by > height || by < 0)
			{
				by = (int) halfHeight;
			}
		}

		private void render()
		{
			for (int y = by; y < by + bh && y < imageHeight; y++)
			{
				for (int x = bx; x < bx + bw && x < imageWidth ; x++)
				{
					fastPixelReaderWriter.goToPixel(x, y);
					// Note: the original code has been rewritten using the WebFX FastPixelReaderWriter (better JS perf)
					// whose passed values are int (0 to 255) and not bytes (-128 to 127) as before. Normally there is
					// no need to do int <-> byte conversions, but here the visual effect is actually produced by the
					// byte overflow, so exceptionally we do that conversion to reproduce the same original visual effect.
					fastPixelReaderWriter.setBlue( byteToInt((byte) Math.min(saturatedB, int2Byte(fastPixelReaderWriter.getBlue())  + this.colourB)));
					fastPixelReaderWriter.setGreen(byteToInt((byte) Math.min(saturatedG, int2Byte(fastPixelReaderWriter.getGreen()) + this.colourG)));
					fastPixelReaderWriter.setRed(  byteToInt((byte) Math.min(saturatedR, int2Byte(fastPixelReaderWriter.getRed())   + this.colourR)));
				}
			}
		}
	}

	private byte int2Byte(int i) {
		return (byte) (i - 128);
	}

	private int byteToInt(byte b) {
		return ((int) b) + 128;
	}

	private Bob[] bobs;

	public Bobs(DemoConfig config)
	{
		this(config, Color.BLUE);
	}

	public Bobs(DemoConfig config, Color colour)
	{
		this(config, 32, 32, colour);
	}

	public Bobs(DemoConfig config, int count, int bobSize, Color colour)
	{
		super(config);

		int red = (int) (colour.getRed() * 255);
		int green = (int) (colour.getGreen() * 255);
		int blue = (int) (colour.getBlue() * 255);

		int initialReduction = 16;

		this.initialBlue = blue / initialReduction;
		this.initialGreen = green / initialReduction;
		this.initialRed = red / initialReduction;

		this.saturatedB = (blue > 0 ? 255 : 0);
		this.saturatedG = (green > 0 ? 255 : 0);
		this.saturatedR = (red > 0 ? 255 : 0);

		this.bobSize = bobSize;
		this.itemCount = count;

		imageWidth = intWidth;
		imageHeight = intHeight;

		createBobs(itemCount);

		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, imageWidth, imageHeight);
		fastPixelReaderWriter = FastPixelReaderWriter.create(gc.getCanvas().snapshot(new SnapshotParameters(), null));

	}

	private void createBobs(int count)
	{
		bobs = new Bob[count];

		for (int i = 0; i < count; i++)
		{
			Bob bob = new Bob();

			bobs[i] = bob;

			bob.bx = (int) (precalc.getUnsignedRandom() * intWidth);
			bob.by = (int) (precalc.getUnsignedRandom() * intHeight);

			bob.bw = bobSize;
			bob.bh = bobSize;

			bob.bx = Math.max(0, bob.bx - bob.bw);
			bob.by = Math.max(0, bob.by - bob.bh);

			bob.dx = 8;
			bob.dy = 8;

			bob.colourB = initialBlue;
			bob.colourG = initialGreen;
			bob.colourR = initialRed;
		}
	}

	@Override
	public void renderForeground()
	{
		for (Bob bob : bobs)
		{
			bob.move();

			bob.render();
		}

        fastPixelReaderWriter.writeCache();
		gc.drawImage(fastPixelReaderWriter.getImage(), 0, 0);
	}
	
	@Override
	public void setPixelSink(IPixelSink sink)
	{
		this.fastPixelReaderWriter = sink.getFastPixelReaderWriter();
	}
}