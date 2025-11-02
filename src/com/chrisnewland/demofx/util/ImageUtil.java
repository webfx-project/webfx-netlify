/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.util;

import dev.webfx.extras.fastpixelreaderwriter.FastPixelReaderWriter;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;

public class ImageUtil
{
	public static Image loadImageFromResources(String filename)
	{
		return loadImageFromResources(filename, 0, 0);
	}

	public static Image loadImageFromResources(String filename, int newWidth, int newHeight)
	{
		return new Image("com/chrisnewland/demofx/images/" + filename, newWidth, newHeight, false, false, true);
	}

	/*public static WritableImage loadWritableImageFromResources(String filename)
	{
		Image image = loadImageFromResources(filename);

		int imageWidth = (int) image.getWidth();
		int imageheight = (int) image.getHeight();

		WritableImage writableImage = new WritableImage(image.getPixelReader(), 0, 0, imageWidth, imageheight);

		return writableImage;
	}*/

	/*public static void saveImage(Image image, File file)
	{
		try
		{
			Class<?> classSwingFXUtils = Class.forName("javafx.embed.swing.SwingFXUtils");

			Method methodFromFXImage = classSwingFXUtils.getMethod("fromFXImage", new Class[] { Image.class, BufferedImage.class });

			RenderedImage ri = (RenderedImage) methodFromFXImage.invoke(null, new Object[] { image, null });

			ImageIO.write(ri, "PNG", file);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}*/

	public static WritableImage createImageFromCanvas(Canvas canvas, double width, double height, boolean transparent, boolean hdpi)
	{
		double outputScaleX = hdpi ? Screen.getPrimary().getOutputScaleX() : 1;
		double outputScaleY = hdpi ? Screen.getPrimary().getOutputScaleY() : 1;

		WritableImage image = new WritableImage((int) (width * outputScaleX), (int) (height * outputScaleY));

		SnapshotParameters params = new SnapshotParameters();
		if (hdpi)
			params.setTransform(Scale.scale(outputScaleX, outputScaleY));

		if (transparent)
		{
			params.setFill(Color.TRANSPARENT);
		}

		canvas.snapshot(params, image);

		return image;
	}

	/*public static int[] readImageToIntArray(Image image)
	{
		int imageWidth = (int) image.getWidth();
		int imageHeight = (int) image.getHeight();
		
		int[] dest = new int[imageWidth * imageHeight];

		PixelReader reader = image.getPixelReader();

		int pixel = 0;

		for (int y = 0; y < imageHeight; y++)
		{
			for (int x = 0; x < imageWidth; x++)
			{
				dest[pixel++] = reader.getArgb(x, y);
			}
		}

		return dest;
	}*/
	
	/*public static byte[] readImageToByteArrayBGRA(Image image)
	{
		int imageWidth = (int) image.getWidth();
		int imageHeight = (int) image.getHeight();
		
		byte[] dest = new byte[imageWidth * imageHeight * 4];

		PixelReader reader = image.getPixelReader();
				
		int pixel = 0;

		for (int y = 0; y < imageHeight; y++)
		{
			for (int x = 0; x < imageWidth; x++)
			{
				dest[pixel++] = (byte)((reader.getArgb(x, y) & 0x000000ff) >> 0);
				dest[pixel++] = (byte)((reader.getArgb(x, y) & 0x0000ff00) >> 8);
				dest[pixel++] = (byte)((reader.getArgb(x, y) & 0x00ff0000) >> 16);
				dest[pixel++] = (byte)((reader.getArgb(x, y) & 0xff000000) >> 24);
			}
		}

		return dest;
	}*/

	/*public static Image createBorderedImage(Image image, int borderX, int borderY)
	{
		double width = image.getWidth() + 2 * borderX;
		double height = image.getHeight() + 2 * borderY;

		Canvas canvas = new Canvas(width, height);

		GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, width, height);

		gc.drawImage(image, borderX, borderY);

		Image snap = ImageUtil.createImageFromCanvas(gc.getCanvas(), width, height, false);

		return snap;
	}*/

	public static Image replaceColour(Image image, Color colorOld, Color colorNew)
	{
		WritableImage result;
		// If the passed image is already a writable image, we use it (assuming it's ok to apply the changes on it)
		if (image instanceof WritableImage)
			result = (WritableImage) image;
		else // Otherwise we create a writable copy of the image
			result = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());

		int oldR = (int) (colorOld.getRed() * 255);
		int oldG = (int) (colorOld.getGreen() * 255);
		int oldB = (int) (colorOld.getBlue() * 255);
		int oldA = (int) (colorOld.getOpacity() * 255);

		int newR = (int) (colorNew.getRed() * 255);
		int newG = (int) (colorNew.getGreen() * 255);
		int newB = (int) (colorNew.getBlue() * 255);
		int newA = (int) (colorNew.getOpacity() * 255);

		FastPixelReaderWriter pixelWriter = FastPixelReaderWriter.create(result);
		while (pixelWriter.gotToNextPixel())
			if (pixelWriter.getRed() == oldR && pixelWriter.getGreen() == oldG && pixelWriter.getBlue() == oldB && pixelWriter.getOpacity() == oldA) {
				pixelWriter.setOpacity(newA);
				if (newA > 0)
					pixelWriter.setRgb(newR, newG, newB);
			}

		return result;
	}

	public static Image tintImage(Image image, double hue) {
		return tintImage(image, hue, 1);
	}

	public static Image tintImage(Image image, double hue, double saturation)
	{
		WritableImage result;
		// If the passed image is already a writable image, we use it (assuming it's ok to apply the changes on it)
		if (image instanceof WritableImage)
			result = (WritableImage) image;
		else // Otherwise we create a writable copy of the image
			result = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
		tintImage(image, hue, saturation, result);
		return result;

	}
	public static Image tintImage(Image image, double hue, WritableImage result) {
		return tintImage(image, hue, 1, result);
	}

	public static Image tintImage(Image image, double hue, double saturation, WritableImage result)
	{
		FastPixelReaderWriter pixelReader = FastPixelReaderWriter.create(image);
		FastPixelReaderWriter pixelWriter = FastPixelReaderWriter.create(result);
        pixelWriter.createCache(false);
		pixelReader.goToPixel(-1, 0);
		pixelWriter.goToPixel(-1, 0);
		while (pixelReader.gotToNextPixel() && pixelWriter.gotToNextPixel()) {
			int opacity = pixelReader.getOpacity();
			Color color = Color.rgb(pixelReader.getRed(), pixelReader.getGreen(), pixelReader.getBlue(), 1d / 255 * opacity);

			Color newColour = Color.hsb(hue, saturation, color.getBrightness());

			//byte alpha = (byte) (color.getOpacity() * 255);
			int red =   (int) (newColour.getRed() *   255);
			int green = (int) (newColour.getGreen() * 255);
			int blue =  (int) (newColour.getBlue() *  255);

			pixelWriter.setArgb(opacity, red, green, blue);
		}
        pixelWriter.writeCache();

		return result;
	}

	public static Image makeContentricRings(double imgWidth, double imgHeight, int rings, Color color)
	{
		//Color colourOff = Color.BLACK;

		Canvas canvas = new Canvas(imgWidth, imgHeight);

		GraphicsContext gc = canvas.getGraphicsContext2D();

		drawContentricRings(0, 0, imgWidth, imgHeight, rings, color, gc);

		Image snap = ImageUtil.createImageFromCanvas(gc.getCanvas(), imgWidth, imgHeight, true, true);

		//snap = replaceColour(snap, colourOff, Color.TRANSPARENT);

		return snap;
	}

	public static void drawContentricRings(double x, double y, double imgWidth, double imgHeight, int rings, Color color, GraphicsContext gc)
	{
		double diameterX = imgWidth;
		double diameterY = imgHeight;

		gc.setStroke(color);
		gc.setLineWidth(imgWidth / rings / 2);

		for (int i = 0; i < rings; i++)
		{
			if (i % 2 == 0)
			{
				gc.strokeOval(x + (imgWidth / 2) - (diameterX / 2), y + (imgHeight / 2) - (diameterY / 2), diameterX, diameterY);
			}

			diameterX -= imgWidth / rings;
			diameterY -= imgHeight / rings;
		}

	}

	public static Image makeCubes(double imgWidth, double imgHeight)
	{
		Canvas canvas = new Canvas(imgWidth, imgWidth);
		GraphicsContext gc = canvas.getGraphicsContext2D();

		double gap = 8;

		double x0 = gap;
		double x1 = imgWidth / 2;
		double x2 = imgWidth - gap;

		double y0 = gap;
		double y1 = gap + (imgHeight - 2 * gap) * (1.0 / 4.0);
		double y2 = gap + (imgHeight - 2 * gap) * (2.0 / 4.0);
		double y3 = gap + (imgHeight - 2 * gap) * (3.0 / 4.0);
		double y4 = (imgHeight - gap) * (4.0 / 4.0);

		gc.setLineCap(StrokeLineCap.ROUND);

		gc.setFill(Color.rgb(200, 200, 200));
		gc.fillPolygon(new double[] { x0, x1, x2, x1 }, new double[] { y1, y0, y1, y2 }, 4);

		gc.setFill(Color.rgb(100, 100, 100));
		gc.fillPolygon(new double[] { x0, x1, x1, x0 }, new double[] { y1, y2, y4, y3 }, 4);

		gc.setFill(Color.rgb(150, 150, 150));
		gc.fillPolygon(new double[] { x1, x2, x2, x1 }, new double[] { y2, y1, y3, y4 }, 4);

		gc.setStroke(Color.BLACK);
		gc.setLineWidth(4);
		gc.setLineJoin(StrokeLineJoin.ROUND);
		gc.strokePolygon(new double[] { x0, x1, x2, x1 }, new double[] { y1, y0, y1, y2 }, 4);
		gc.strokePolygon(new double[] { x0, x1, x1, x0 }, new double[] { y1, y2, y4, y3 }, 4);
		gc.strokePolygon(new double[] { x1, x2, x2, x1 }, new double[] { y2, y1, y3, y4 }, 4);

		return ImageUtil.createImageFromCanvas(gc.getCanvas(), imgWidth, imgHeight, true, true);
	}

	public static Image makeHearts(double imgWidth, double imgHeight)
	{
		Canvas canvas = new Canvas(imgWidth, imgWidth);
		GraphicsContext gc = canvas.getGraphicsContext2D();

		double heartSize = imgWidth / Math.sqrt(2) * 1.14;

		double halfWidth = heartSize / 2;
		double diameterX = Math.sqrt(halfWidth * halfWidth + halfWidth * halfWidth);
		double diameterY = diameterX;

		double offset = (imgWidth - heartSize) / 2;

		double x0 = offset;
		double x1 = x0 + heartSize / 2;
		double x2 = x0 + heartSize;

		double y0 = offset;
		double y1 = y0 + heartSize / 2;
		double y2 = y0 + heartSize;

		gc.setFill(Color.BLACK);
		gc.fillPolygon(new double[] { x0, x1, x2, x1 }, new double[] { y1, y0, y1, y2 }, 4);
		gc.fillOval(offset + heartSize * .25 - diameterX / 2, offset + heartSize / 4 - diameterY / 2, diameterX, diameterY);
		gc.fillOval(offset + heartSize * .75 - diameterX / 2, offset + heartSize / 4 - diameterY / 2, diameterX, diameterY);

		heartSize = imgWidth / Math.sqrt(2) * 0.9;

		halfWidth = heartSize / 2;
		diameterX = Math.sqrt(halfWidth * halfWidth + halfWidth * halfWidth);
		diameterY = diameterX;

		offset = (imgWidth - heartSize) / 2;

		x0 = offset;
		x1 = x0 + heartSize / 2;
		x2 = x0 + heartSize;

		y0 = offset;
		y1 = y0 + heartSize / 2;
		y2 = y0 + heartSize;

		gc.setFill(Color.RED);
		gc.fillPolygon(new double[] { x0, x1, x2, x1 }, new double[] { y1, y0, y1, y2 }, 4);
		gc.fillOval(offset + heartSize * .25 - diameterX / 2, offset + heartSize / 4 - diameterY / 2, diameterX, diameterY);
		gc.fillOval(offset + heartSize * .75 - diameterX / 2, offset + heartSize / 4 - diameterY / 2, diameterX, diameterY);

		return ImageUtil.createImageFromCanvas(gc.getCanvas(), imgWidth, imgHeight, true, true);
	}
}