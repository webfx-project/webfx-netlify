/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx;

import com.chrisnewland.demofx.effect.IEffect;
import com.chrisnewland.demofx.effect.effectfactory.IEffectFactory;
import com.chrisnewland.demofx.effect.effectfactory.SimpleEffectFactory;
import com.chrisnewland.demofx.effect.spectral.ISpectralEffect;
import com.chrisnewland.demofx.util.PreCalc;
import dev.webfx.platform.console.Console;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DemoFX implements AudioSpectrumListener, ISpectrumDataProvider
{
	private final DemoConfig config;

	private Text statsLabel;
	private Text fxLabel;

	private AnimationTimer timer;

	private List<IEffect> effects;

	private static final int SPECTRUM_BANDS = 256;

	private static final int SAMPLES_PER_SECOND = 60;

	private final float[] spectrumData = new float[SPECTRUM_BANDS];

	private MediaPlayer mediaPlayer;

	private Group group;
	private BorderPane pane;

	private IEffectFactory effectFactory;

	private Runnable onCompleted;


	public DemoFX(DemoConfig config)
	{
		this(config, (IEffectFactory) null);
	}

	public DemoFX(DemoConfig config, Function<DemoConfig, IEffect> effectConstructor) {
		this(config, (IEffectFactory) config1 -> Collections.singletonList(effectConstructor.apply(config1)));
	}

	public DemoFX(DemoConfig config, IEffectFactory effectFactory) {
		this.config = config;
		this.effectFactory = effectFactory;
		initialiseAudio1(); // Will start loading the audio buffer in the web version
		if (config.getPreCalc() == null)
			config.setPrecalc(new PreCalc(config));
	}

	public DemoFX setOnCompleted(Runnable onCompleted) {
		this.onCompleted = onCompleted;
		return this;
	}

	public void stopDemo()
	{
		for (IEffect effect : effects)
		{
			effect.stop();
		}

		if (mediaPlayer != null)
			mediaPlayer.stop();

		if (timer != null)
		{
			timer.stop();
			timer = null;
		}

	}

	@Override public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases)
	{
		for (int i = 0; i < SPECTRUM_BANDS; i++)
		{
			// average last 2 bars for smoothness
			spectrumData[i] = (spectrumData[i] + magnitudes[i]) / 2;
		}
	}

	@Override public float[] getData()
	{
		return spectrumData;
	}

	public Scene getScene()
	{
		if (pane == null)
			getPane();

		int topHeight = 50;

		//if (config.isFullScreen())
		{
			topHeight = 0;
		}
		/*else
		{
			VBox statsPane = getStatsPane();

			statsPane.setMinHeight(topHeight);

			pane.setTop(statsPane);
		}*/

		boolean depthSorted = false;

		Scene scene = new Scene(pane, config.getWidth(), config.getHeight() + topHeight); //, depthSorted, SceneAntialiasing.DISABLED);

		/*scene.setCamera(new PerspectiveCamera());

		try
		{
			String styleSheet = DemoFX.class.getResource("/style.css").toExternalForm();

			scene.getStylesheets().add(styleSheet);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Could not load style.css stylesheet. Please add it to the classpath");
		}*/

		return scene;
	}

	public IEffectFactory getEffectFactory() {
		if (effectFactory == null) {
			//IEffectFactory effectFactory;

			/*String scriptName = config.getDemoScriptName();

			if (scriptName != null)
			{
				switch (scriptName)
				{
				case "xmas":
					effectFactory = new Christmas();
					break;
				case "moire":
					effectFactory = new Moire();
					break;
				default:
					effectFactory = new DemoFX3();
					break;
				}
			}
			else
			{*/
			effectFactory = new SimpleEffectFactory();
			//}

		}
		return effectFactory;
	}

	public BorderPane getPane()
	{
		group = new Group();

		double maxDimension = Math.max(config.getWidth(), config.getHeight());

		Canvas canvasOnScreen = new Canvas(config.getWidth(), config.getHeight());
		Canvas canvasOffScreen = new Canvas(maxDimension, maxDimension);

		group.getChildren().add(canvasOnScreen);

		GraphicsContext onScreenGC = canvasOnScreen.getGraphicsContext2D();
		GraphicsContext offScreenGC = canvasOffScreen.getGraphicsContext2D();

		Group offScreenRoot = new Group();
		offScreenRoot.getChildren().add(canvasOffScreen);

		config.setLayers(onScreenGC, offScreenGC, group);

		try
		{

			effects = getEffectFactory().getEffects(config);

		}
		catch (RuntimeException re)
		{
			re.printStackTrace();
			System.err.println(re.getMessage());
			//System.err.print(DemoConfig.getUsageError());
			Console.log(re);
			//Shutdown.softwareShutdown(true, -1); //System.exit(-1);
		}

		pane = new BorderPane();

		//pane.setStyle("-fx-background-color:black;");
		pane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

		int topHeight = 50;

		//if (config.isFullScreen())
		{
			topHeight = 0;
		}
		/*else
		{
			VBox statsPane = getStatsPane();

			statsPane.setMinHeight(topHeight);

			pane.setTop(statsPane);
		}*/

		pane.setCenter(group);

		return pane;
	}

	/*private VBox getStatsPane()
	{
		final String FONT_STYLE = "-fx-font-family:monospace; -fx-font-size:16px; -fx-text-fill:white;";

		statsLabel = new Label();
		statsLabel.setStyle(FONT_STYLE);
		statsLabel.setAlignment(Pos.BASELINE_LEFT);
		statsLabel.prefWidthProperty().bind(pane.widthProperty());

		fxLabel = new Label();
		fxLabel.setStyle(FONT_STYLE);
		fxLabel.setAlignment(Pos.BASELINE_LEFT);
		fxLabel.prefWidthProperty().bind(pane.widthProperty());
		fxLabel.setText(getFXLabelText(config));

		VBox vbox = new VBox();
		vbox.getChildren().add(statsLabel);
		vbox.getChildren().add(fxLabel);

		return vbox;
	}*/

	public void runDemo()
	{
/*
		String audioFilename = config.getAudioFilename();

		if (audioFilename != null)
		{
			initialiseAudio(audioFilename, effects);
		}
*/

		initialiseAudio2();

		timer = new DemoAnimationTimer(this, config, statsLabel, effects);
		timer.start();
	}

	public void timerCompleted(/*Measurements measurements*/)
	{
		if (onCompleted != null)
			onCompleted.run();
		/*
		LineChart<Number, Number> chartHeap = MeasurementChartBuilder.buildChartHeap(measurements);
		LineChart<Number, Number> chartFPS = MeasurementChartBuilder.buildChartFPS(measurements);

		pane.setPadding(new Insets(4, 4, 4, 4));

		double averageFPS = measurements.getAverageFPS();
		long totalFrames = measurements.getTotalFrameCount();

		NumberFormat numberFormat = new DecimalFormat("0.00");

		double maxFrames = 60.0 * measurements.getDurationMillis() / DemoAnimationTimer.UPDATE_STATS_MILLIS;
		double percentPerfect = totalFrames / maxFrames * 100;

		Label lblBenchmarkComplete = new Label("DemoFX Benchmark Complete!");
		lblBenchmarkComplete.setStyle("-fx-font-family:Tahoma; -fx-font-size:22px; -fx-text-fill:#ffff22;");
		lblBenchmarkComplete.setAlignment(Pos.CENTER);

		StringBuilder builder = new StringBuilder();

		builder.append("Total frames: ").append(totalFrames);
		builder.append("    Duration: ").append(measurements.getDurationMillis()).append("ms");
		builder.append("    Avg FPS: ").append(numberFormat.format(averageFPS));

		Label lblStats = new Label(builder.toString());
		lblStats.setStyle("-fx-font-family:Tahoma; -fx-font-size:12px; -fx-text-fill:#ddddff;");
		lblStats.setAlignment(Pos.CENTER);

		Label lblScore = new Label("Awesomeness: " + numberFormat.format(percentPerfect) + '%');
		lblScore.setStyle("-fx-font-family:Tahoma; -fx-font-size:28px; -fx-text-fill:#22ff22;");
		lblScore.setAlignment(Pos.CENTER);

		VBox vboxLabels = new VBox();

		vboxLabels.setFillWidth(true);

		lblBenchmarkComplete.prefWidthProperty().bind(vboxLabels.widthProperty());
		lblStats.prefWidthProperty().bind(vboxLabels.widthProperty());
		lblScore.prefWidthProperty().bind(vboxLabels.widthProperty());

		vboxLabels.getChildren().add(lblBenchmarkComplete);
		vboxLabels.getChildren().add(lblStats);
		vboxLabels.getChildren().add(lblScore);

		VBox vboxCharts = new VBox();

		vboxCharts.setFillWidth(true);
		vboxCharts.getChildren().add(chartFPS);
		vboxCharts.getChildren().add(chartHeap);

		pane.setTop(vboxLabels);
		pane.setCenter(vboxCharts);
		 */
	}

	private boolean initialiseSpectralEffects(MediaPlayer mediaPlayer)
	{
		boolean result = false;

		for (IEffect effect : effects)
		{
			if (effect instanceof ISpectralEffect)
			{
				result = true;
				System.out.println("Found effect that uses spectral analyser: " + effect);

				((ISpectralEffect) effect).setSpectrumDataProvider(this);
			}
		}

		if (result)
		{
			mediaPlayer.setAudioSpectrumListener(this);
			mediaPlayer.setAudioSpectrumInterval(1f / SAMPLES_PER_SECOND);
			mediaPlayer.setAudioSpectrumNumBands(SPECTRUM_BANDS);
			Arrays.fill(spectrumData, -60);
		}

		return result;
	}

	private void initialiseAudio1() {
		String audioFilename = config.getAudioFilename();
		if (audioFilename != null) {
			Media media = new Media(audioFilename);
			mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setVolume(config.getAudioVolume());
			mediaPlayer.setCycleCount(1);
		}
	}

	private void initialiseAudio2() {
		if (mediaPlayer != null) {
			initialiseSpectralEffects(mediaPlayer);
			mediaPlayer.play();
			mediaPlayer.setOnEndOfMedia(this::stopDemo);
		}
	}

	MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}

	/*private String getFXLabelText(DemoConfig config)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Precalc: ");

		StringBuilder lookupBuilder = new StringBuilder();

		boolean anyLookups = false;

		if (config.isLookupRandom())
		{
			anyLookups = true;
			lookupBuilder.append("rand").append(",");
		}

		if (config.isLookupSqrt())
		{
			anyLookups = true;
			lookupBuilder.append("sqrt").append(",");
		}

		if (config.isLookupTrig())
		{
			anyLookups = true;
			lookupBuilder.append("trig");
		}

		if (!anyLookups)
		{
			lookupBuilder.append("none");
		}
		else if (lookupBuilder.charAt(lookupBuilder.length() - 1) == ',')
		{
			lookupBuilder.deleteCharAt(lookupBuilder.length() - 1);
		}

		builder.append(lookupBuilder.toString());

		builder.append(" | Java: ").append(getJavaVersion());
		builder.append(" | JavaFX: ").append(getJavaFxVersion());

		return builder.toString();
	}

	private String getJavaVersion() {
		return System.getProperty("java.version");
	}

	private String getJavaFxVersion() {
		return System.getProperty("javafx.version");
	}*/

	@Override public int getBandCount()
	{
		return spectrumData.length;
	}
}