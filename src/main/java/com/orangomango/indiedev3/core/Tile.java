package com.orangomango.indiedev3.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.indiedev3.AssetLoader;

public class Tile{
	protected int x, y;

	public static final double SIZE = 64;

	public Tile(int x, int y){
		this.x = x;
		this.y = y;
	}

	public int updateDirection(int dir){
		return dir;
	}

	public Laser generateLaser(World world, int dir){
		return null;
	}

	public void render(GraphicsContext gc){
		gc.drawImage(AssetLoader.getInstance().getImage("tile.png"), this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}
}