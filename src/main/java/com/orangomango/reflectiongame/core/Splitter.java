package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;

public class Splitter extends Mirror{
	public Splitter(int x, int y, boolean f){
		super(x, y, f);
	}

	@Override
	public Laser generateLaser(World world, int dir){
		return new Laser(world, this.x, this.y, dir);
	}

	@Override
	public void render(GraphicsContext gc){
		final int index = this.flipped ? 1 : 0;
		gc.drawImage(AssetLoader.getInstance().getImage("splitter.png"), 1+index*34, 1, 32, 32, this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
}