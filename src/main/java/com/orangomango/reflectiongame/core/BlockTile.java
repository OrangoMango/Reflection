package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;

public class BlockTile extends Tile{
	public BlockTile(int x, int y){
		super(x, y);
		this.id = 5;
	}

	@Override
	public void render(GraphicsContext gc){
		gc.drawImage(AssetLoader.getInstance().getImage("blocktile.png"), this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}
}