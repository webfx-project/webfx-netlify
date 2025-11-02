/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.util;

import dev.webfx.extras.fastpixelreaderwriter.FastPixelReaderWriter;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.nio.IntBuffer;

public class MaskingSystem
{
	private PixelFormat<IntBuffer> pixelFormat;

	private int[] layer0;
	private int[] layer1;
	private int[] mask;

	private int layerWidth;
	private int layerHeight;

	private int maskWidth;
	private int maskHeight;

	private int pixelCount;

	private int[] combined;

	public MaskingSystem(int[] layer0, int[] layer1, int[] mask, int layerWidth, int layerHeight, int maskWidth, int maskHeight)
	{
		pixelFormat = PixelFormat.getIntArgbInstance();

		this.layerWidth = layerWidth;
		this.layerHeight = layerHeight;

		this.maskWidth = maskWidth;
		this.maskHeight = maskHeight;

		this.layer0 = layer0;
		this.layer1 = layer1;
		this.mask = mask;

		pixelCount = layerWidth * layerHeight;

		combined = new int[pixelCount];
	}

	private void applyMask(int maskX, int maskY)
	{
		int maskXEnd = Math.min(maskX + maskWidth, layerWidth);
		int maskYEnd = Math.min(maskY + maskHeight, layerHeight);

		for (int my = maskY; my < maskYEnd - 1; my++)
		{
			int readRow = my * layerWidth;
			int writeRow = my * layerWidth;

			for (int mx = maskX; mx < maskXEnd - 1; mx++)
			{
				int readIndex = readRow + mx;

				int writeIndex = writeRow + mx;

				int[] readLayer = layer1;

				int maskIndex = (my - maskY) * maskWidth + (mx - maskX);

				if (mask[maskIndex] == 0)
				{
					readLayer = layer0;
				}

				combined[writeIndex] = readLayer[readIndex];
			}
		}
	}

	public void writeCombinedPixels(PixelWriter pixelWriter, int xPos, int yPos, int maskX, int maskY)
	{
		System.arraycopy(layer0, 0, combined, 0, pixelCount);

		applyMask(maskX, maskY);

		pixelWriter.setPixels(xPos, yPos, layerWidth, layerHeight, pixelFormat, combined, 0, layerWidth);
	}

	public static Image applyMask(Image maskImage, Image background, int offsetX, int offsetY)
	{

		int maskWidth = (int) maskImage.getWidth();
		int maskHeight = (int) maskImage.getHeight();

		WritableImage result = new WritableImage(maskWidth, maskHeight);

		FastPixelReaderWriter writer = FastPixelReaderWriter.create(result);
        writer.createCache(true);

		FastPixelReaderWriter readerMask = FastPixelReaderWriter.create(maskImage);
		FastPixelReaderWriter readerBackground = FastPixelReaderWriter.create(background);

		//while (readerMask.gotToNextPixel() && writer.gotToNextPixel())
		for (int y = 0; y < maskHeight; y++)
		{
			for (int x = 0; x < maskWidth; x++)
			{
				int red = 0, green = 0, blue = 0, opacity = 0;

				readerMask.goToPixel(x, y);
				if (readerMask.getRed() > 0) {
					readerBackground.goToPixel(x + offsetX, y + offsetY);
					red = readerBackground.getRed();
					green = readerBackground.getGreen();
					blue = readerBackground.getBlue();
					opacity = readerBackground.getOpacity();
				}

				writer.goToPixel(x, y);
				writer.setArgb(opacity, red, green, blue);
			}
		}

        writer.writeCache();

		return result;
	}

	public static Image createMaskCircle(int diameter, boolean hdpi)
	{
		double width = diameter;

		double height = diameter;

		Canvas canvas = new Canvas(width, height);

		GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, width, height);

		gc.setFill(Color.WHITE);
		gc.fillOval(0, 0, diameter, diameter);

		Image snap = ImageUtil.createImageFromCanvas(gc.getCanvas(), width, height, false, hdpi);

		return snap;
	}

	public static Image createMaskRing(int diameterOuter, int diameterInner, boolean hdpi)
	{
		double width = diameterOuter;

		double height = diameterOuter;

		Canvas canvas = new Canvas(width, height);

		GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, width, height);

		gc.setFill(Color.WHITE);
		gc.fillOval(0, 0, diameterOuter, diameterOuter);

		if (diameterInner > 0)
		{
			gc.setFill(Color.BLACK);
			gc.fillOval((width - diameterInner) / 2, (height - diameterInner) / 2, diameterInner, diameterInner);
		}

		Image snap = ImageUtil.createImageFromCanvas(gc.getCanvas(), width-1, height-1, false, hdpi);

		return snap;
	}

	public static Image createMaskBorder(int width, int height, int thickness, boolean hdpi)
	{
		Canvas canvas = new Canvas(width, height);

		GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, width, height);

		gc.setFill(Color.BLACK);
		gc.fillRect(thickness, thickness, width - thickness * 2, height - thickness * 2);

		Image snap = ImageUtil.createImageFromCanvas(gc.getCanvas(), width, height, false, hdpi);

		return snap;
	}
}