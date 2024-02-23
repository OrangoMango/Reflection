package com.orangomango.indiedev3.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashMap;

public class PlayScreen extends GameScreen{
	public PlayScreen(int w, int h, HashMap<KeyCode, Boolean> keys){
		super(w, h, keys);
	}

	@Override
	public void handleMouseInput(MouseEvent e){

	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		gc.save();
		gc.scale(scale, scale);

		gc.setFill(Color.GREEN);
		gc.fillRect(0, 0, this.width, this.height);

		gc.restore();
	}
}