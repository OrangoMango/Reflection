package com.orangomango.reflectiongame.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.HashMap;
import java.io.*;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;

public class LevelsScreen extends GameScreen{
	private int levels;
	private int hoverIndex = -1;

	private static final double BUTTON_SIZE = 75;
	private static final Font FONT = Font.loadFont(LevelsScreen.class.getResourceAsStream("/misc/font.ttf"), 30);

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
	public void handleMouseMovement(MouseEvent e, double scale, double offsetX){
		final double mouseX = (e.getX()-offsetX)/scale;
		final double mouseY = e.getY()/scale;
		this.hoverIndex = -1;
		for (int i = 0; i < this.levels; i++){
			Point2D pos = getPosition(i);
			Rectangle2D rect = new Rectangle2D(pos.getX(), pos.getY(), BUTTON_SIZE, BUTTON_SIZE);
			if (rect.contains(mouseX, mouseY)){
				this.hoverIndex = i;
				break;
			}
		}
	}

	@Override
	public void handleMouseInput(MouseEvent e, double scale, double offsetX){
		final double mouseX = (e.getX()-offsetX)/scale;
		final double mouseY = e.getY()/scale;
		for (int i = 0; i < this.levels; i++){
			Point2D pos = getPosition(i);
			Rectangle2D rect = new Rectangle2D(pos.getX(), pos.getY(), BUTTON_SIZE, BUTTON_SIZE);
			if (rect.contains(mouseX, mouseY)){
				Util.playSound("button_click.wav");
				SCREEN_SWITCHER.accept(new PlayScreen(this.width, this.height, this.keys, i));
				return;
			}
		}
	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		gc.save();
		gc.scale(scale, scale);
		gc.drawImage(AssetLoader.getInstance().getImage("background.jpg"), 0, 0, this.width, this.height);
		for (int i = 0; i < this.levels; i++){
			gc.setFill(Color.ORANGE);
			Point2D pos = getPosition(i);
			if (this.hoverIndex == i){
				gc.fillRect(pos.getX()-BUTTON_SIZE*0.15, pos.getY()-BUTTON_SIZE*0.15, BUTTON_SIZE*1.3, BUTTON_SIZE*1.3);
			} else {
				gc.fillRect(pos.getX(), pos.getY(), BUTTON_SIZE, BUTTON_SIZE);
			}
			gc.setTextAlign(TextAlignment.CENTER);
			gc.setFont(FONT);
			gc.setFill(Color.BLACK);
			gc.fillText(Integer.toString(i), pos.getX()+BUTTON_SIZE/2, pos.getY()+BUTTON_SIZE*0.6);
		}
		gc.restore();
	}

	private Point2D getPosition(int i){
		final int rows = (int)Math.ceil(this.levels/8.0);
		double px = (this.width-BUTTON_SIZE*8-20*7)/2+(i % 8)*(BUTTON_SIZE+20);
		double py = (this.height-BUTTON_SIZE*rows-20*(rows-1))/2+(i / 8)*(BUTTON_SIZE+20);
		return new Point2D(px, py);
	}
}