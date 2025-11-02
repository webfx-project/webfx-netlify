/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.text;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.platform.resource.Resource;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Credits extends AbstractEffect
{
	private double yOffset;

	private final List<String> stringList;
	private List<Bounds> stringDimensions;

	private static final double INITIAL_FONT_SIZE = 24;
	private static final double OFFSCREEN = 100;
	private static final double INITIAL_LINE_SPACE = 10;

	private final long duration;
	private final double speed = 3; // speed value when no duration is passed in the constructor
	private double creditsHeight;

	private final boolean loopStringList = false;

	private final Color fontColour;

	private Font currentFont;
	private double currentSpacing = INITIAL_LINE_SPACE;
	private double scaleFactor = 1;

	public Credits(DemoConfig config) {
		this(config, Color.WHITE);
	}

	public Credits(DemoConfig config, Color fontColour) {
		this(config, fontColour, -1);
	}

	public Credits(DemoConfig config, Color fontColour, long duration)
	{
		super(config);
		this.fontColour = fontColour;
		this.duration = duration;

		currentFont = Font.font("Arial", INITIAL_FONT_SIZE);
		yOffset = height;

		stringList = new ArrayList<>();

		String commandH1 = "{family:Arial;size:96;spacing:40;}";
		String commandH2 = "{family:Arial;size:48;spacing:30;}";
		String commandBody = "{family:Apple Chancery;size:32;spacing:20;}";
		
		stringList.add(commandH2);
		stringList.add("You have been watching");
		
		stringList.add(commandH1);
		stringList.add("DemoFX");
		
		stringList.add(commandH2);
		stringList.add("JavaFX Demoscene Benchmarking Harness");
		
		stringList.add(commandBody);
		stringList.add("A layered effect demoscene engine for testing");
		stringList.add("the performance of javafx.scene.canvas.Canvas");
		stringList.add("~~~");
		
		stringList.add(commandH2);
		stringList.add("Coding");

		stringList.add(commandBody);
		stringList.add("Chris Newland (@chriswhocodes)");
		stringList.add("~~~");

		stringList.add(commandH2);
		stringList.add("Music");
		
		stringList.add(commandBody);
		stringList.add("David Newland");
		stringList.add("~~~");

		stringList.add(commandH2);
		stringList.add("Full source code available at");
		
		stringList.add(commandBody);
		stringList.add("https://github.com/chriswhocodes/DemoFX");
		stringList.add("Licensed under Simplified BSD");
		stringList.add("~~~");
		
		stringList.add(commandH2);
		stringList.add("Performance tuned using");
		
		stringList.add(commandBody);
		stringList.add("JITWatch");
		stringList.add(" Log analyser / visualiser for Java HotSpot JIT compiler.");
		stringList.add("https://github.com/AdoptOpenJDK/jitwatch/");
		stringList.add("~~~");
		
		stringList.add(commandH2);
		stringList.add("Greetings");
		
		stringList.add(commandBody);

		stringList.addAll(Arrays.asList(Resource.getText("com/chrisnewland/demofx/text/greetings.txt").split("\n")));

/* Already in greetings
		stringList.add(commandBody);
		stringList.add("~~~");
		stringList.add("Copyright (c) 2015 Chris Newland");
*/

		int size = stringList.size();
		List<Bounds> nullBounds = Collections.nCopies(size, null);
		stringDimensions = new ArrayList<>(nullBounds);
		// Doing a first pass to determine the max line width
		double maxLineWidth = 0;
		for (int index = 0; index < size; index++) {
			String line = stringList.get(index);
			if (line.startsWith("{"))
				handleCommand(line);
			else
				maxLineWidth = Math.max(maxLineWidth, measureText(index).getWidth());
		}
		// If the max line is wider than the canvas width, we set a scale factor
		if (maxLineWidth > width) {
			scaleFactor = width / maxLineWidth; // smaller than 1
			stringDimensions = new ArrayList<>(nullBounds); // Forgetting all previous bounds
		}
	}

/*
	public void customInitialise(List<String> stringList, long startMillis, long stopMillis, boolean loopStringList, Color colour,
			Font font)
	{
		this.stringList = stringList;
		this.effectStartMillis = startMillis;
		this.effectStopMillis = stopMillis;
		this.loopStringList = loopStringList;
		this.fontColour = colour;
		this.currentFont = font;
	}

	public void customInitialise(List<String> stringList, long startMillis, long stopMillis, boolean loopStringList)
	{
		customInitialise(stringList, startMillis, stopMillis, loopStringList, null, gc.getFont());
	}

	public void customInitialise(List<String> stringList, long startMillis, long stopMillis, boolean loopStringList, Color colour)
	{
		customInitialise(stringList, startMillis, stopMillis, loopStringList, colour, gc.getFont());
	}
*/

	Bounds measureText(int index) {
		Bounds bounds = stringDimensions.get(index);
		if (bounds == null) {
			String str = stringList.get(index);
			stringDimensions.set(index, bounds = WebFxKitLauncher.measureText(str, currentFont));
		}
		return bounds;
	}

	long start;

	@Override
	public void renderForeground()
	{
		if (duration <= 0)
			yOffset -= speed;
		else if (creditsHeight > 0)
			yOffset = height - ((creditsHeight + height) * (config.getDemoAnimationTimer().getElapsed() - start)) / duration;
		else {
			yOffset = height;
			start = config.getDemoAnimationTimer().getElapsed();
		}

		double yPos = 0;

		yPos = yOffset;

		for (int index = 0; index < stringList.size(); index++)
		{
			String line = stringList.get(index);

			if (line.startsWith("{"))
			{
				handleCommand(line);
				continue;
			}

			double stringHeight = measureText(index).getHeight(); // TextUtil.getStringDimensions(currentFont, gc, line).getY();

			yPos += stringHeight + currentSpacing;

			if (!effectFinished && isLineOnScreen(yPos))
			{
				plotText(line, yPos, index);
			}
		}

		if (duration > 0 && creditsHeight == 0)
			creditsHeight = yPos - height;

		if (yPos <= 0)
		{
			if (loopStringList)
			{
				yOffset = height;
			}
			else
			{
				effectFinished = true;
			}
		}
	}

	private void handleCommand(String line)
	{
		String trimmed = line.substring(1, line.length() - 2);
		
		String[] commands = trimmed.split(";");
		
		String currentFontFamily = currentFont.getName();
		double currentFontSize = currentFont.getSize();
		
		for (String command : commands)
		{
			String[] parts = command.split(":");
			
			String key = parts[0];
			String value = parts[1];
			
			switch(key)
			{
			case "family":
				currentFontFamily = value;
				break;
			case "size":
				currentFontSize = Double.parseDouble(value) * scaleFactor;
				break;
			case "spacing":
				currentSpacing = Double.parseDouble(value) * scaleFactor;
				break;
			}
		}
		
		currentFont = Font.font(currentFontFamily, currentFontSize);
	}

	private void plotText(String line, double yPos, int index)
	{
		gc.setFont(currentFont);

		gc.setFill(fontColour);

		double strWidth = measureText(index).getWidth();

		double x = halfWidth - strWidth / 2;

		gc.fillText(line, x, yPos);
	}

	private boolean isLineOnScreen(double yPos)
	{
		return yPos > -OFFSCREEN;
	}
}