/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.fractal;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.effect.addon.HasAngle;
import com.chrisnewland.demofx.util.ImageUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

public class FractalRings extends AbstractEffect implements HasAngle
{
	private static final double SPEED = 1.05;

	private static final double SPAWN_AT_RADIUS = 8;

	private static final double ROOT_2 = Math.sqrt(2.0);

	private List<FractalRing> renderListOld = new ArrayList<>();

	private List<FractalRing> renderListNew = new ArrayList<>();

	private int colourIndex = 0;

	private final Image[] image = new Image[64];
	private final Color[] colors = new Color[64];

	private final static double RING_DIAMETER = 256;
	private final static boolean adaptive = true;

	private /*static*/ class FractalRing
	{
		private double centreX;
		private double centreY;
		private double radius;
		private boolean hasChildren = false;
		private int colourIndex;

		public FractalRing(double x, double y, double radius, int index)
		{
			this.centreX = x;
			this.centreY = y;
			this.radius = radius;
			this.colourIndex = index;
		}

		public void grow(double cx, double cy)
		{
			this.radius *= SPEED;

			centreX = cx + ((centreX - cx) * SPEED);
			centreY = cy + ((centreY - cy) * SPEED);
		}

		public boolean isOnScreen(double width, double height)
		{
			double testRadius = radius / ROOT_2;

			if (testRadius > width && testRadius > height) // To eliminate centered rings that became bigger than the screen
				return false;

			double cx = centreX - width / 2;
			double cy = centreY - height / 2;
			double x = cx * rotate.getMxx() + cy * rotate.getMxy();
			double y = cx * rotate.getMyx() + cy * rotate.getMyy();
			x+= width / 2;
			y+= height / 2;

			double left = x - testRadius;
			double right = x + testRadius;
			double top = y - testRadius;
			double bottom = y + testRadius;

			return left < width && right > 0 && bottom > 0 && top < height;
		}
	}

	public FractalRings(DemoConfig config)
	{
		super(config);

		// Following Google Chrome advise, and preventing this warning: Canvas2D: Multiple readback operations using getImageData are faster with the willReadFrequently attribute set to true
		Canvas ringCanvas = WebFxKitLauncher.createWillReadFrequentlyCanvas(RING_DIAMETER, RING_DIAMETER);
		GraphicsContext ringContext = ringCanvas.getGraphicsContext2D();

		for (int i = 0; i < image.length; i++)
		{
			image[i] = createRing(RING_DIAMETER, 7, colors[i] = getRandomColour(), ringContext);
		}

		renderListNew.add(new FractalRing(halfWidth, halfHeight, 5, colourIndex));
	}

	private Image createRing(double diameter, double thickness, Color color, GraphicsContext ringContext)
	{
		ringContext.clearRect(0, 0, diameter, diameter);

		ringContext.setStroke(color);
		ringContext.setLineWidth(thickness);
		ringContext.strokeOval(thickness, thickness, diameter - 2 * thickness, diameter - 2 * thickness);

		return ImageUtil.createImageFromCanvas(ringContext.getCanvas(), diameter, diameter, true, true);
	}

	private long lastDuration; // Used for adaptive
	private double minDisplayRadius; // Small rings under that value won't be display in adaptive mode for low devices
	private double maxDisplayCenter = 1; // Will grow, for initial concentric effect (only rings inside that distance will be displayed)

	@Override public void renderForeground()
	{
		long now;
		if (!adaptive)
			minDisplayRadius = 0;
		else {
			now = System.currentTimeMillis();
			if (lastDuration != 0) {
				double exceedFactor = (double) lastDuration / 16; // We aim 16ms for the plot
				if (exceedFactor < 0.8)
					minDisplayRadius -= 0.5;
				else if (exceedFactor > 1.2)
					minDisplayRadius += 0.5;
				if (minDisplayRadius < 0)
					minDisplayRadius = 0;
			}
		}
		if (maxDisplayCenter > width && maxDisplayCenter > height)
			maxDisplayCenter = 0;

		buildRenderList();

		incColourIndex();

		render();

		if (adaptive)
			lastDuration = System.currentTimeMillis() - now;
	}

	private void incColourIndex()
	{
		colourIndex++;

		if (colourIndex >= image.length)
		{
			colourIndex = 0;
		}
	}

	private void buildRenderList()
	{
		renderListOld = renderListNew;

		final int oldRenderListSize = renderListOld.size();

		renderListNew = new ArrayList<>(oldRenderListSize);

		for (int i = 0; i < oldRenderListSize; i++)
		{
			update(i);
		}
	}

	private final Rotate rotate = new Rotate(0);

	@Override
	public double getAngle() {
		return rotate.getAngle();
	}

	@Override
	public void setAngle(double angle) {
		rotate.setAngle(angle);
	}

	private void render()
	{
		final int size = renderListNew.size();

		for (int i = 0; i < size; i++)
		{
			FractalRing rc = renderListNew.get(i);
			if (maxDisplayCenter > 0 && i == 0)
				maxDisplayCenter = rc.radius;
			if (rc.radius < minDisplayRadius)
				continue;

			double x = rc.centreX - width / 2;
			double y = rc.centreY - height / 2;
			if (maxDisplayCenter > 0 && i > 0) {
				if (rc.radius > maxDisplayCenter || Math.sqrt(x * x + y * y) > maxDisplayCenter)
					continue;
			}
			if (rc.radius < RING_DIAMETER) // Drawing image if shrank (for fractal effect)
				gc.drawImage(image[rc.colourIndex],width / 2 + x * rotate.getMxx() + y * rotate.getMxy() - rc.radius, height / 2 + x * rotate.getMyx() + y * rotate.getMyy() - rc.radius, rc.radius * 2, rc.radius * 2);
			else { // Otherwise there is no fractal effect, so we just redraw it as an oval (faster and sharper than drawing image)
				gc.setStroke(colors[rc.colourIndex]);
				double thickness = 7d / RING_DIAMETER * rc.radius * 2;
				gc.setLineWidth(thickness);
				gc.strokeOval(width / 2 + x * rotate.getMxx() + y * rotate.getMxy() - rc.radius + thickness, height / 2 + x * rotate.getMyx() + y * rotate.getMyy() - rc.radius + thickness, rc.radius * 2 - 2 * thickness, rc.radius * 2 - 2 * thickness);
			}
		}
	}

	private int getSectorCount()
	{
		return 10 - (int) (precalc.getUnsignedRandom() * 3);
	}

	private void update(int index)
	{
		FractalRing rc = renderListOld.get(index);

		rc.grow(halfWidth, halfHeight);

		if (!rc.isOnScreen(width, height))
		{
			return;
		}

		renderListNew.add(rc);

		if (!rc.hasChildren && rc.radius > SPAWN_AT_RADIUS)
		{
			rc.hasChildren = true;

			double subRadius = rc.radius * 0.15;

			renderListNew.add(new FractalRing(rc.centreX, rc.centreY, subRadius, colourIndex));

			double distanceInner = (rc.radius - subRadius) * .9;

			for (int i = 0; i < 3; i++)
			{
				generateChildren(rc, getSectorCount(), distanceInner, subRadius);

				distanceInner *= 0.6;
				subRadius *= 0.65;
			}
		}
	}

	private void generateChildren(FractalRing parent, int sectors, double distance, double radius)
	{
		double sectorAngle = 360.0 / sectors;

		double startAngle = sectorAngle / 2;

		for (double angle = 0; angle < 360; angle += sectorAngle)
		{
			double subX = precalc.sin(startAngle + angle) * distance;
			double subY = precalc.cos(startAngle + angle) * distance;

			renderListNew.add(new FractalRing(parent.centreX + subX, parent.centreY + subY, radius, colourIndex));
		}
	}
}