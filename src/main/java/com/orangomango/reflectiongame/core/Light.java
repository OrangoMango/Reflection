package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Light{
	private boolean on;
	private int x, y;

	public Light(int x, int y){
		this.x = x;
		this.y = y;
	}

	public void render(GraphicsContext gc){
		gc.setFill(this.on ? Color.GREEN : Color.RED);
		gc.fillOval(this.x*Tile.SIZE+Tile.SIZE/3, this.y*Tile.SIZE+Tile.SIZE/3, Tile.SIZE/3, Tile.SIZE/3);
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	public void setOn(boolean value){
		this.on = value;
	}

	public boolean isOn(){
		return this.on;
	}
}