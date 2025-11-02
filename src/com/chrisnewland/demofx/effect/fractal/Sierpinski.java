/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.fractal;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Sierpinski extends AbstractEffect
{
	private final double smallestTriangle;

	private final double[] pointsX = new double[3];
	private final double[] pointsY = new double[3];

	private double rootHeight;

	private final static boolean memoriseImages = true;
	private final List<Image> memorisedImages;
	private final Canvas memorisedCanvas;
	private final GraphicsContext memorisedContext;
	private int memorisedImageIndex = -1;

	static class Triangle
	{
		private final double topX;
		private final double topY;
		private final double height;

		public Triangle(double topX, double topY, double height)
		{
			this.topX = topX;
			this.topY = topY;
			this.height = height;
		}

		public final double getTopX()
		{
			return topX;
		}

		public final double getTopY()
		{
			return topY;
		}

		public final double getHeight()
		{
			return height;
		}
	}

	private List<Triangle> keep;

	public Sierpinski(DemoConfig config)
	{
		this(config, config.getHeight() / 64);
	}
	
	public Sierpinski(DemoConfig config, double smallestTriangle)
	{
		super(config);
		
		this.smallestTriangle = smallestTriangle;

		memorisedImages = memoriseImages ? new ArrayList<>() : null;
		// Following Google Chrome advise, and preventing this warning: Canvas2D: Multiple readback operations using getImageData are faster with the willReadFrequently attribute set to true
		memorisedCanvas = memoriseImages ? WebFxKitLauncher.createWillReadFrequentlyCanvas(width, height) : null;
		memorisedContext = memoriseImages ? memorisedCanvas.getGraphicsContext2D() : null;
		keep = new ArrayList<>();
		rootHeight = height;
		// Precomputing images now if memoriseImages is on (if not done now, it will be done during the animation but will be less smooth)
		while (memoriseImages && memorisedImageIndex < 0) // memorisedImageIndex becomes 0 when finished
			renderForeground();
	}
	
	@Override
	public void renderForeground()
	{
		if (memorisedImageIndex < 0) {
			GraphicsContext oldGc = gc;
			if (memoriseImages) {
				gc = memorisedContext;
				gc.clearRect(0, 0, width, height);
			}
			calcTriangles();
			drawTriangles();
			if (memoriseImages && memorisedImageIndex < 0) {
				SnapshotParameters params = new SnapshotParameters();
				params.setFill(Color.TRANSPARENT);
				WritableImage image = memorisedCanvas.snapshot(params, null);
				memorisedImages.add(image);
				oldGc.drawImage(image, 0, 0);
			}
			gc = oldGc;
		} else {
			if (++memorisedImageIndex >= memorisedImages.size())
				memorisedImageIndex = 0;
			Image memorisedImage = memorisedImages.get(memorisedImageIndex);
			gc.drawImage(memorisedImage, 0, 0);
		}
	}

	private void calcTriangles()
	{
		keep.clear();

		double acceleration = rootHeight * 0.02;
		
		rootHeight += acceleration;

		if (rootHeight >= 2 * height)
		{
			rootHeight = height;
			if (memoriseImages) {
				memorisedImageIndex = 0;
				return;
			}
		}

		Triangle root = new Triangle(halfWidth, 0, rootHeight);

		shrink(root);

		itemCount = keep.size();
	}

	private void shrink(Triangle tri)
	{
		double topX = tri.getTopX();
		double topY = tri.getTopY();
		double h = tri.getHeight();

		if (topY >= height)
		{
			return;
		}

		if (h < smallestTriangle)
		{
			keep.add(tri);
		}
		else
		{
			Triangle top = new Triangle(topX, topY, h / 2);
			Triangle left = new Triangle(topX - h / 4, topY + h / 2, h / 2);
			Triangle right = new Triangle(topX + h / 4, topY + h / 2, h / 2);

			shrink(top);
			shrink(left);
			shrink(right);
		}
	}

	private void drawTriangles()
	{
		gc.setFill(Color.WHITE);
		
		int triangleCount = keep.size();

		for (int i = 0; i < triangleCount; i++)
		{
			Triangle tri = keep.get(i);			

			if (tri.getTopY() < height)
			{
				drawTriangle(tri);
			}
		}
	}

	private void drawTriangle(Triangle tri)
	{
		double topX = tri.getTopX();
		double topY = tri.getTopY();
		double h = tri.getHeight();

		pointsX[0] = topX;
		pointsY[0] = topY;

		pointsX[1] = topX + h / 2;
		pointsY[1] = topY + h;

		pointsX[2] = topX - h / 2;
		pointsY[2] = topY + h;

		gc.fillPolygon(pointsX, pointsY, 3);		
	}
}