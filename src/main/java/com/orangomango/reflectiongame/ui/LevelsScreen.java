package com.orangomango.reflectiongame.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.io.*;

import com.orangomango.reflectiongame.AssetLoader;

public class LevelsScreen extends GameScreen{
	private int levels;

	public LevelsScreen(int w, int h, HashMap<KeyCode, Boolean> keys){
		super(w, h, keys);

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/misc/levels.data")));
			String line;
			while ((line = reader.readLine()) != null){
				if (line.equals("end")){
					this.levels++;
				}
			}
			reader.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		gc.save();
		gc.scale(scale, scale);
		gc.drawImage(AssetLoader.getInstance().getImage("background.jpg"), 0, 0, this.width, this.height);
		gc.restore();
	}
}