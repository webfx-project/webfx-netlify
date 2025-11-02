/*
 * Copyright (c) 2015-2017 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx.effect.text;

import com.chrisnewland.demofx.DemoConfig;
import com.chrisnewland.demofx.effect.AbstractEffect;
import com.chrisnewland.demofx.util.TextUtil;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextWaveSprite extends AbstractEffect
{
	private double xOffset;
	private double lastCharX;

	private final List<String> stringList;

	private static final double OFFSCREEN = 100;

	private final double speed;
	private final double amplitude;
	private final double waveYPos;

	private final double fontScale;

	private int stringIndex = 0;

	private String currentString;
	private Character[] chars;

	private final boolean repeat;

	public TextWaveSprite(DemoConfig config) {
		this(config, new String[] {"DemoFX"}, config.getHeight() / 2 + 48, 1, 10);
	}

	public TextWaveSprite(DemoConfig config, String[] strings, double yPos, double fontScale, double speed) {
		this(config, strings, yPos, fontScale, speed, false);
	}

	public TextWaveSprite(DemoConfig config, String[] strings, double yPos, double fontScale, double speed, boolean repeat)
	{
		this(config, strings, yPos, fontScale, speed, 40, repeat);
	}

	public TextWaveSprite(DemoConfig config, String[] strings, double yPos, double fontScale, double speed, double amplitude, boolean repeat) {
		super(config);

		this.repeat = repeat;

		waveYPos = yPos;
		this.fontScale = fontScale;
		this.speed = speed;
		this.amplitude = amplitude;

		xOffset = width;

		stringList = new ArrayList<>();

		stringList.addAll(Arrays.asList(strings));

		currentString = stringList.get(stringIndex);

		chars = explodeString(currentString.toUpperCase());
	}

	private Character[] explodeString(String str)
	{
		final int length = str.length();

		Character[] result = new Character[length];

		for (int i = 0; i < length; i++)
		{
			result[i] = str.charAt(i);
		}

		return result;
	}

	@Override
	public void renderForeground()
	{
		xOffset -= speed;

		if (lastCharX < 0)
		{
			xOffset = width;

			stringIndex++;

			if (stringIndex == stringList.size())
			{
				if (repeat)
				{
					stringIndex = 0;
				}
				else
				{
					effectFinished = true;
				}
			}

			if (!effectFinished)
			{
				currentString = stringList.get(stringIndex);

				chars = explodeString(currentString.toUpperCase());
			}

		}

		if (!effectFinished)
		{
			plotText();
		}
	}

	private void plotText()
	{
		double y;

		double charX = xOffset + OFFSCREEN;

		for (int i = 0; i < chars.length; i++)
		{
			Character character = chars[i];

			if (character == ' ')
			{
				charX += 40 * fontScale;
				continue;
			}

			Image charImage = TextUtil.getSpriteCharacter(character);
			if (charImage == null)
				continue;

			double charWidth = charImage.getWidth();
			double charHeight = charImage.getHeight();

			double plotWidth = charWidth * fontScale;
			double plotHeight = charHeight * fontScale;

			if (i > 0)
			{
				charX += TextUtil.getKerningForChar(character, chars[i - 1], plotHeight);
			}

			if (isLetterOnScreen(charX))
			{
				y = waveYPos + precalc.sin(charX / 2 + OFFSCREEN) * amplitude;

				gc.drawImage(charImage, charX, y, plotWidth, plotHeight);
			}

			charX += plotWidth;

		}

		lastCharX = charX;
	}

	private boolean isLetterOnScreen(double charX)
	{
		return (charX > -OFFSCREEN && charX < width);
	}
}