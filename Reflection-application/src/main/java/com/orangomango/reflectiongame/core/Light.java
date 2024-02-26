package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;

public class Light{
	private boolean on;
	private int x, y;

	public Light(int x, int y){
		this.x = x;
		this.y = y;
	}

	public void render(GraphicsContext gc, Rotatable tile){
		int[] dir = tile == null ? null : Util.getDirection(getDirection(tile.getRotation()));
		gc.save();
		gc.translate(this.x*Tile.SIZE+Tile.SIZE/2, this.y*Tile.SIZE+Tile.SIZE/2);
		if (dir != null) gc.translate(Tile.SIZE/3*dir[0], Tile.SIZE/3*dir[1]);
		final double size = Tile.SIZE/4;
		gc.drawImage(AssetLoader.getInstance().getImage("light.png"), this.on ? 1 : 19, 1, 16, 16, -size/2, -size/2, size, size);
		gc.restore();
	}

	private int getDirection(int dir){
		switch (dir){
			case Util.DIRECTION_N:
				return Util.DIRECTION_S;
			case Util.DIRECTION_E:
				return Util.DIRECTION_W;
			case Util.DIRECTION_S:
				return Util.DIRECTION_N;
			case Util.DIRECTION_W:
				return Util.DIRECTION_E;
			default:
				return -1;
		}
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