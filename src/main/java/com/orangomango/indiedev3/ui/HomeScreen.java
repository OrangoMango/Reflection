package com.orangomango.indiedev3.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;

public class HomeScreen extends GameScreen{
	public HomeScreen(int w, int h, HashMap<KeyCode, Boolean> keys){
		super(w, h, keys);
	}

	@Override
	public void handleMouseInput(MouseEvent e){
		SCREEN_SWITCHER.accept(new PlayScreen(this.width, this.height, this.keys));
	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		gc.save();
		gc.scale(scale, scale);



		gc.restore();
	}
}