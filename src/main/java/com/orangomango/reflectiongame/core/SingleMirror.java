package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;

public class SingleMirror extends Tile implements Rotatable{
	private int rotation = 0;
	private boolean rotDisabled;

	public SingleMirror(int x, int y){
		super(x, y);
	}

	@Override
	public int updateDirection(int dir){
		switch (this.rotation){
			case 0:
				return dir == Util.DIRECTION_N ? Util.DIRECTION_E : (dir == Util.DIRECTION_W ? Util.DIRECTION_S : -1);
			case 1:
				return dir == Util.DIRECTION_N ? Util.DIRECTION_W : (dir == Util.DIRECTION_E ? Util.DIRECTION_S : -1);
			case 2:
				return dir == Util.DIRECTION_S ? Util.DIRECTION_W : (dir == Util.DIRECTION_E ? Util.DIRECTION_N : -1);
			case 3:
				return dir == Util.DIRECTION_S ? Util.DIRECTION_E : (dir == Util.DIRECTION_W ? Util.DIRECTION_N : -1);
			default:
				return -1;
		}
	}

	@Override
	public void render(GraphicsContext gc){
		gc.drawImage(AssetLoader.getInstance().getImage("singlemirror.png"), 1+getRotation()*34, 1, 32, 32, this.x*SIZE, this.y*SIZE, SIZE, SIZE);
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