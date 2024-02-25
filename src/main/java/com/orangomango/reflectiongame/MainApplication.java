package com.orangomango.reflectiongame;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.animation.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.media.MediaPlayer;

import java.util.HashMap;

import com.orangomango.reflectiongame.ui.*;

public class MainApplication extends Application{
	private GameScreen currentScreen;
	private HashMap<KeyCode, Boolean> keys = new HashMap<>();
	private int fps, frames;
	private MediaPlayer player;

	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static final double SCALE = 1;

	@Override
	public void start(Stage stage){
		GameScreen.SCREEN_SWITCHER = screen -> this.currentScreen = screen;
		this.currentScreen = new HomeScreen(WIDTH, HEIGHT, this.keys);
		this.player = new MediaPlayer(AssetLoader.getInstance().getMusic("background.wav"));
		this.player.setCycleCount(MediaPlayer.INDEFINITE);
		this.player.play();

		Thread frameCounter = new Thread(() -> {
			while (true){
				try {
					this.fps = this.frames;
					this.frames = 0;
					Thread.sleep(1000);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		frameCounter.setDaemon(true);
		frameCounter.start();

		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		pane.getChildren().add(canvas);

		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		canvas.setOnMousePressed(e -> this.currentScreen.handleMouseInput(e)); // TODO: Fix scale
		canvas.setOnMouseMoved(e -> this.currentScreen.handleMouseMovement(e));

		AnimationTimer loop = new AnimationTimer(){
			@Override
			public void handle(long time){
				update(gc);
				MainApplication.this.frames++;
			}
		};
		loop.start();

		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		scene.setFill(Color.BLACK);
		stage.setScene(scene);

		stage.setTitle("Reflection");
		stage.setResizable(false);
		stage.show();
	}

	private void update(GraphicsContext gc){
		this.currentScreen.update(gc, SCALE);

		// Display FPS
		gc.setFill(Color.WHITE);
		gc.fillText("FPS: "+this.fps, 20, HEIGHT-20);
	}

	public static void main(String[] args){
		launch(args);
	}
}