/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.pixel;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import dev.webfx.extras.fastpixelreaderwriter.FastPixelReaderWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Twister extends AbstractEffect
{
	FastPixelReaderWriter writer;

	private int columnWidth;

	private double speed = 0;
	private double angle = 0;
	private double twist = 1;

	private int imageWidth;

	private WritableImage imageTexture;

	private final int red1, green1, blue1, red2, green2, blue2, red3, green3, blue3, red4, green4, blue4;

	public Twister(DemoConfig config) {
		this(config, Color.rgb(0, 0, 0xff), Color.rgb(0, 0x55, 0xff));
	}

	public Twister(DemoConfig config, Color color1, Color color2) {
		this(config, color1, color2, color1, color2);
	}

	public Twister(DemoConfig config, Color color1, Color color2, Color color3, Color color4) {
		super(config);
		red1 = (int) (color1.getRed() * 255);
		green1 = (int) (color1.getGreen() * 255);
		blue1 = (int) (color1.getBlue() * 255);

		red2 = (int) (color2.getRed() * 255);
		green2 = (int) (color2.getGreen() * 255);
		blue2 = (int) (color2.getBlue() * 255);

		red3 = (int) (color3.getRed() * 255);
		green3 = (int) (color3.getGreen() * 255);
		blue3 = (int) (color3.getBlue() * 255);

		red4 = (int) (color4.getRed() * 255);
		green4 = (int) (color4.getGreen() * 255);
		blue4 = (int) (color4.getBlue() * 255);

		this.imageWidth = intWidth / 2;
		int imageHeight = intHeight;

		columnWidth = imageWidth / 4;

		imageTexture = new WritableImage(imageWidth, imageHeight);

		writer = FastPixelReaderWriter.create(imageTexture);
		writer.createCache(false);
	}

	@Override
	public void renderForeground()
	{
		//render(angle / 60); // Moved down for first call because render(0) is weird

		speed += 0.5;

		if (speed >= 360)
		{
			speed -= 360;
		}

		angle += 4 * precalc.sin(speed);

		if (angle >= 1080)
		{
			angle -= 1080;
		}

		twist = 16 + precalc.sin(angle) * 8;

        render(angle / 60);

        //pixelWriter.setPixels(0, 0, imageWidth, imageHeight, pixelFormat, pixelData, 0, scanLine);
		writer.writeCache();

		gc.drawImage(imageTexture, 0, 0);
		gc.drawImage(imageTexture, imageWidth, 0);
	}

	private void render(double distort)
	{
		for (int y = 0; y < intHeight; y++)
		{
			double xOffset = precalc.sin(angle + (y - halfHeight)) * distort * 2;

			double start = angle + (double) y / twist;

			int x1 = (int) (xOffset + imageWidth / 2 + precalc.sin(start) * columnWidth);
			int x2 = (int) (xOffset + imageWidth / 2 + precalc.sin(start + 90) * columnWidth);
			int x3 = (int) (xOffset + imageWidth / 2 + precalc.sin(start + 180) * columnWidth);
			int x4 = (int) (xOffset + imageWidth / 2 + precalc.sin(start + 270) * columnWidth);

			makeAlpha(y);


			if (x1 < x2)
			{
				renderLine(y, x1, x2, blue1, green1, red1);
			}

			if (x2 < x3)
			{
				renderLine(y, x2, x3, blue2, green2, red2);
			}

			if (x3 < x4)
			{
				renderLine(y, x3, x4, blue3, green3, red3);
			}

			if (x4 < x1)
			{
				renderLine(y, x4, x1, blue4, green4, red4);
			}
		}
	}

	private void makeAlpha(int y)
	{
		writer.goToPixel(-1, y);
		for (int x = 0; x < imageWidth && writer.gotToNextPixel(); x++)
		{
			writer.setOpacity(0);
		}
	}

	private void renderLine(int y, int start, int end, int blue, int green, int red)
	{
		for (int x = start; x <= end; x++)
		{
			writer.goToPixel(x, y);
			boolean border = x == start || x == end;
			writer.setArgb(255, border ? 255 : red, border ? 255: green, border ? 255 : blue);
		}
	}
}