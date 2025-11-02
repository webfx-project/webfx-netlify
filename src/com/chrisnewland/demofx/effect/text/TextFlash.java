/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.text;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.util.TextUtil;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextFlash extends AbstractEffect
{
	private List<String> stringList;

	private static final double INITIAL_FONT_SIZE = 80;
	private Font font;

	private long fadeInMillis;
	private long stayMillis;
	private long fadeOutMillis;

	private long time;

	private int stringIndex = 0;

	private boolean loopStringList = true;

	private double yPercent;
	private double textYPos;

	private Color fontColour = Color.WHITE;

	public TextFlash(DemoConfig config) {
		this(config, "The end is the beginning is the end");
	}
	
	public TextFlash(DemoConfig config, String string) {
		this(config, string, true, 0, 0, 200);
	}

	public TextFlash(DemoConfig config, String string, boolean loopStringList, long fadeInMillis, long stayMillis, long fadeOutMillis) {
		this(config, string, loopStringList, Color.WHITE, Font.font("Georgia", FontWeight.BOLD, INITIAL_FONT_SIZE), 50, fadeInMillis, stayMillis, fadeOutMillis);
	}

	public TextFlash(DemoConfig config, String string, Font font, Color colour)  {
		this(config, string, true, colour, font, 50, 0, 0, 200);
	}

	public TextFlash(DemoConfig config, String string, boolean loopStringList, Color colour, Font font, double yPercent, long fadeInMillis, long stayMillis, long fadeOutMillis) {
		super(config);

		String[] strings = string.toUpperCase().split(" ");

		stringList = new ArrayList<>();

		stringList.addAll(Arrays.asList(strings));

		this.loopStringList = loopStringList;

		this.fontColour = colour;
		this.font = font;
		this.yPercent = yPercent;
		this.fadeInMillis = fadeInMillis;
		this.stayMillis = stayMillis;
		this.fadeOutMillis = fadeOutMillis;

		precalulateStringDimensions();
	}

	private void precalulateStringDimensions()
	{
		gc.setFont(font);

		double maxWidth = 0;
		for (String str : stringList)
			maxWidth = Math.max(maxWidth, TextUtil.getStringDimensions(font, str).getX());

		if (maxWidth > width) { // Font size correction for small screens
			font = Font.font(font.getFamily(), font.getSize() * width / maxWidth);
			precalulateStringDimensions();
		} else
			textYPos = yPercent * height / 100;
	}

	@Override
	public void renderForeground()
	{
		long now = System.currentTimeMillis();

		if (time == 0)
			time = now;

		long elapsed = now - time; // 0 .. showMillis

		double opacityFactor = elapsed < fadeInMillis ? ((double) elapsed / (double) fadeOutMillis) : elapsed < fadeInMillis + stayMillis ? 1 : 1 - ((double) (elapsed - fadeInMillis - stayMillis) / (double) fadeOutMillis); // 1
																				// -
																				// (0
																				// ..
																				// 1)

		Color derivedColor = fontColour.deriveColor(0, 1.0, 1.0, opacityFactor);

		gc.setFill(derivedColor);

		if (!effectFinished)
		{
			plotText();
		}

		if (elapsed > fadeInMillis + stayMillis + fadeOutMillis)
		{
			stringIndex++;

			if (stringIndex == stringList.size())
			{
				if (loopStringList)
				{
					stringIndex = 0;
				}
				else
				{
					effectFinished = true;
				}
			}

			time = now;
		}
	}

	private void plotText()
	{
		gc.setFont(font);

		String str = stringList.get(stringIndex);

		Point2D dimensions = TextUtil.getStringDimensions(font, str);

		double strWidth = dimensions.getX();
		double strHeight = dimensions.getY();

		double x = halfWidth - strWidth / 2;
		double y = textYPos + strHeight / 2;

		gc.fillText(str, x, y);
	}
}