package org.ilite.frc;


import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MatchTimerFX extends Application {
	private interface IAction {
		public void action();
	}
	
	private static final double sGAUGE_WIDTH = 500d,
		sGAUGE_HEIGHT = 500d;
	
	private static final double sTIMER_UPDATE_RATE_HZ = 1d;
	private static final long sTOUCH_DEBOUNCE = 50;
	private long mLastTouchTime = 0;
	
	private final IAction[] sSTATES = {
		()->startTimer(),
		()->stopTimer(),
		()->resetTimer()
	};

	private int mCurrentState = 0;
	private AtomicBoolean mIsUpdatingState = new AtomicBoolean(false);
	
	private static double sTELEOP_DURATION = 135d;
	
	private final Timer mTimer = new Timer("Gauge Timer");
	
	private Gauge mGauge;
	
	private long mLastTime = -1l;
	private long mStartTime = -1l;
	private class MyTimerTask extends TimerTask {
		@Override public void run() {
			if(mIsUpdatingState.get())
			{
				mLastTime = System.currentTimeMillis();
				double set = sTELEOP_DURATION - (mLastTime - mStartTime)/1000;
				mGauge.setValue(set);
			}
		}
	};
	private TimerTask mTimerTask = null;
	
	@Override
	public void start(Stage primaryStage) {
		mGauge = GaugeBuilder.create()
				.skinType(SkinType.SIMPLE)
				.autoScale(true)
				.minValue(0)
				.maxValue(135)
				.sections(
						new Section(0d, 10d, "End Game", Color.CRIMSON),
						new Section(10d, 30d, "", Color.YELLOW),
						new Section(30d,sTELEOP_DURATION, "Teleop", Color.LIME)
						)
				.animated(true)
				.tickLabelDecimals(0)
				.tickLabelColor(Color.BLACK)
				.tickLabelLocation(TickLabelLocation.OUTSIDE)
				.tickLabelsVisible(true)
				.borderPaint(Color.BLACK)
				.foregroundBaseColor(Color.BLACK)
				.borderWidth(1d)
				.needleBorderColor(Color.BLACK)
				.needleSize(NeedleSize.THIN)
				.maxSize(sGAUGE_WIDTH, sGAUGE_HEIGHT)
				.prefSize(sGAUGE_WIDTH, sGAUGE_HEIGHT)
				.innerShadowEnabled(true)
				.build();
		
		StackPane frame = new StackPane();
		frame.setAlignment(Pos.BASELINE_CENTER);
		frame.getChildren().add(mGauge);
		Scene scene = new Scene(frame, sGAUGE_WIDTH, sGAUGE_HEIGHT);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Match Timer");
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> Platform.exit());
		
		mGauge.setOnTouchReleased(e -> processGaugeClick());
		mGauge.setOnButtonReleased(e -> processGaugeClick());
		mGauge.setOnMouseClicked(e -> processGaugeClick());
		
		resetTimer();
	}
	
	private void processGaugeClick()
	{
		long time = System.currentTimeMillis();
		if(time - mLastTouchTime < sTOUCH_DEBOUNCE)
		{
			// do nothing with this event
			System.out.println("Skipping bounced event");
		}
		else
		{
			sSTATES[mCurrentState++].action();
			if(mCurrentState >= sSTATES.length) mCurrentState = 0;
		}
		mLastTouchTime = time;
	}
	
	private void resetTimer()
	{
		mStartTime = 0;
		mLastTime = 0;
		mGauge.setValue(sTELEOP_DURATION);
		System.out.println("RESET");
		mGauge.setNeedleBorderColor(Color.BLACK);
	}
	
	private void startTimer()
	{
		mIsUpdatingState.set(true);
		mStartTime = System.currentTimeMillis();
		mLastTime = mStartTime;
		mTimerTask = new MyTimerTask();
		mGauge.setNeedleBorderColor(Color.PURPLE);
		mTimer.scheduleAtFixedRate(mTimerTask, (long)(1000d/sTIMER_UPDATE_RATE_HZ), (long)(1000d/sTIMER_UPDATE_RATE_HZ));
		System.out.println("STARTED");
	}
	
	private void stopTimer()
	{
		mIsUpdatingState.set(false);
		mTimerTask.cancel();
		mGauge.setNeedleBorderColor(Color.GRAY);
		System.out.println("STOPPED");
	}

	public static void main(String[] args) {
		launch(args);
	}
}
