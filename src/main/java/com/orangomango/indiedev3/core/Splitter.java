package com.orangomango.indiedev3.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.indiedev3.AssetLoader;
import com.orangomango.indiedev3.Util;

public class Splitter extends Mirror{
	public Splitter(int x, int y, boolean f){
		super(x, y, f);
	}

	@Override
	public Laser generateLaser(World world, int dir){
		int outDir = -1;
		switch (dir){
			case Util.DIRECTION_N:
				outDir = this.flipped ? Util.DIRECTION_E : Util.DIRECTION_W;
				break;
			case Util.DIRECTION_E:
				outDir = this.flipped ? Util.DIRECTION_N : Util.DIRECTION_S;
				break;
			case Util.DIRECTION_S:
				outDir = this.flipped ? Util.DIRECTION_W : Util.DIRECTION_E;
				break;
			case Util.DIRECTION_W:
				outDir = this.flipped ? Util.DIRECTION_S : Util.DIRECTION_N;
				break;
		}

		return new Laser(world, this.x, this.y, outDir);
	}

	@Override
	public void render(GraphicsContext gc){
		final int index = this.flipped ? 1 : 0;
		gc.drawImage(AssetLoader.getInstance().getImage("splitter.png"), 1+index*34, 1, 32, 32, this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
}