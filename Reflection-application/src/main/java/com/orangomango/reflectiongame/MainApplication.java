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

import dev.webfx.extras.canvas.pane.CanvasPane;

public class MainApplication extends Application{
	private GameScreen currentScreen;
	private HashMap<KeyCode, Boolean> keys = new HashMap<>();
	private MediaPlayer player;

	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static int WINDOW_WIDTH, WINDOW_HEIGHT;
	private static double SCALE, OFFSET_X;

	@Override
	public void start(Stage stage){
		GameScreen.SCREEN_SWITCHER = screen -> this.currentScreen = screen;
		this.currentScreen = new HomeScreen(WIDTH, HEIGHT, this.keys);
		this.player = new MediaPlayer(AssetLoader.getInstance().getMusic("background.wav"));
		this.player.setCycleCount(MediaPlayer.INDEFINITE);
		this.player.play();

		Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
		CanvasPane pane = new CanvasPane(canvas, (w, h, newY, sizeChanged) -> {
			WINDOW_WIDTH = (int)w;
			WINDOW_HEIGHT = (int)h;
			SCALE = (double)WINDOW_HEIGHT/HEIGHT;
			OFFSET_X = (WINDOW_WIDTH-WIDTH*SCALE)/2;
		});
		GraphicsContext gc = canvas.getGraphicsContext2D();

		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		canvas.setOnMousePressed(e -> this.currentScreen.handleMouseInput(e, SCALE, OFFSET_X));
		canvas.setOnMouseMoved(e -> this.currentScreen.handleMouseMovement(e, SCALE, OFFSET_X));
		canvas.setOnMouseDragged(e -> this.currentScreen.handleMouseDragged(e, SCALE, OFFSET_X));
		canvas.setOnMouseReleased(e -> this.currentScreen.handleMouseReleased(e, SCALE, OFFSET_X));

		AnimationTimer loop = new AnimationTimer(){
			@Override
			public void handle(long time){
				update(gc);
			}
		};
		loop.start();

		Scene scene = new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT);
		scene.setFill(Color.BLACK);
		stage.setScene(scene);

		stage.setTitle("Reflection");
		stage.setResizable(false);
		stage.getIcons().add(AssetLoader.getInstance().getImage("icon.png"));
		stage.show();
	}

	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
		gc.translate(OFFSET_X, 0);
		this.currentScreen.update(gc, SCALE);
		gc.translate(-OFFSET_X, 0);
	}

	public static void main(String[] args){
		launch(args);
	}
}
