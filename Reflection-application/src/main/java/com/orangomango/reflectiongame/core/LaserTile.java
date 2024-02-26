package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;

public class LaserTile extends Tile implements Rotatable{
	private int rotation = 0;
	private boolean rotDisabled;

	public LaserTile(int x, int y){
		super(x, y);
		this.id = 6;
	}

	@Override
	public int updateDirection(int dir){
		return -1;
	}

	@Override
	public void render(GraphicsContext gc){
		gc.drawImage(AssetLoader.getInstance().getImage("lasertile.png"), 1+getRotation()*66, 1, 64, 64, this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}

	@Override
	public int getRotation(){
		return this.rotation;
	}

	@Override
	public void rotate90(){
		this.rotation = (this.rotation+1) % 4;
	}

	@Override
	public void setRotationDisabled(boolean value){
		this.rotDisabled = value;
	}

	@Override
	public boolean isRotationDisabled(){
		return this.rotDisabled;
	}
}