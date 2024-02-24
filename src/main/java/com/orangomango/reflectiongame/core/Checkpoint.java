package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;

public class Checkpoint extends Tile implements Flippable{
	private boolean flipped;

	public Checkpoint(int x, int y){
		super(x, y);
	}

	@Override
	public void flip(){
		this.flipped = !this.flipped;
	}

	@Override
	public void render(GraphicsContext gc){
		final int index = this.flipped ? 1 : 0;
		gc.drawImage(AssetLoader.getInstance().getImage("checkpoint.png"), 1+index*34, 1, 32, 32, this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
}