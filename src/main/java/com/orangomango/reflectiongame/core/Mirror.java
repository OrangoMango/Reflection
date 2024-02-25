package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;

public class Mirror extends Tile implements Flippable{
	protected boolean flipped;

	public Mirror(int x, int y, boolean f){
		super(x, y);
		this.flipped = f;
		this.id = 1;
	}

	@Override
	public void flip(){
		this.flipped = !this.flipped;
	}

	@Override
	public int updateDirection(int dir){
		switch (dir){
			case Util.DIRECTION_N:
				return this.flipped ? Util.DIRECTION_W : Util.DIRECTION_E;
			case Util.DIRECTION_E:
				return this.flipped ? Util.DIRECTION_S : Util.DIRECTION_N;
			case Util.DIRECTION_S:
				return this.flipped ? Util.DIRECTION_E : Util.DIRECTION_W;
			case Util.DIRECTION_W:
				return this.flipped ? Util.DIRECTION_N : Util.DIRECTION_S;
			default:
				return -1;
		}
	}

	@Override
	public void render(GraphicsContext gc){
		final int index = this.flipped ? 1 : 0;
		gc.drawImage(AssetLoader.getInstance().getImage("mirror.png"), 1+index*34, 1, 32, 32, this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
}