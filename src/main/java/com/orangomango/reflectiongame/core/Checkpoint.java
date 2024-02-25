package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;

public class Checkpoint extends Tile implements Flippable{
	private boolean flipped;
	private boolean flipDisabled;
	private boolean activated;

	public Checkpoint(int x, int y){
		super(x, y);
		this.id = 3;
	}

	public boolean laserPassed(int dir){
		if (this.flipped){
			return dir == Util.DIRECTION_E || dir == Util.DIRECTION_W;
		} else {
			return dir == Util.DIRECTION_N || dir == Util.DIRECTION_S;
		}
	}

	@Override
	public void flip(){
		this.flipped = !this.flipped;
	}

	@Override
	public void render(GraphicsContext gc){
		final int index = this.flipped ? 1 : 0;
		gc.drawImage(AssetLoader.getInstance().getImage("checkpoint"+(this.activated ? "_on" : "")+".png"), 1+index*66, 1, 64, 64, this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}

	@Override
	public void setFlippingDisabled(boolean value){
		this.flipDisabled = value;
	}

	@Override
	public boolean isFlippingDisabled(){
		return this.flipDisabled;
	}

	public void setActivated(boolean value){
		this.activated = value;
	}
}