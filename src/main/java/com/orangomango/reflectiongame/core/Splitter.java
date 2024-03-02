package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;

public class Splitter extends Mirror{
	public Splitter(int x, int y, boolean f){
		super(x, y, f);
		this.id = 2;
	}

	@Override
	public Laser generateLaser(Laser parent, World world, int dir){
		return new Laser(parent, world, this.x, this.y, dir);
	}

	@Override
	public void render(GraphicsContext gc){
		final int index = this.flipped ? 1 : 0;
		gc.drawImage(AssetLoader.getInstance().getImage("splitter.png"), 1+index*66, 1, 64, 64, this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
}