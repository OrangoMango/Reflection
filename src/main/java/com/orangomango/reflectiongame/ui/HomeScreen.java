package com.orangomango.reflectiongame.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;

public class HomeScreen extends GameScreen{
	private UiButton playButton, creditsButton;

	private static final double BUTTON_WIDTH = 300;
	private static final double BUTTON_HEIGHT = 100;

	public HomeScreen(int w, int h, HashMap<KeyCode, Boolean> keys){
		super(w, h, keys);
		this.playButton = new UiButton("button_play.png", w*0.5-BUTTON_WIDTH*0.5, h*0.5-BUTTON_HEIGHT*0.5, BUTTON_WIDTH, BUTTON_HEIGHT, () -> SCREEN_SWITCHER.accept(new PlayScreen(this.width, this.height, this.keys)));
		this.creditsButton = new UiButton("button_play.png", w*0.5-BUTTON_WIDTH*0.5, h*0.5-BUTTON_HEIGHT*0.5+BUTTON_HEIGHT*1.3, BUTTON_WIDTH, BUTTON_HEIGHT, () -> System.out.println("Credits"));
	}

	@Override
	public void handleMouseInput(MouseEvent e){
		this.playButton.click(e.getX(), e.getY());
		this.creditsButton.click(e.getX(), e.getY());
	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		gc.save();
		gc.scale(scale, scale);
		this.playButton.render(gc);
		this.creditsButton.render(gc);
		gc.restore();
	}
}