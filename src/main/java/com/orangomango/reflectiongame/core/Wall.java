package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;

public class Wall extends Tile{
	public Wall(int x, int y){
		super(x, y);
	}

	@Override
	public int updateDirection(int dir){
		return -1; // The laser can't go through walls
	}

	@Override
	public void render(GraphicsContext gc){
		gc.drawImage(AssetLoader.getInstance().getImage("wall.png"), this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
}