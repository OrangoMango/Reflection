package com.orangomango.indiedev3.core;

import javafx.scene.canvas.GraphicsContext;

public class World{
	private int width, height;
	private Tile[][] map;

	public World(int w, int h){
		this.width = w;
		this.height = h;
		this.map = new Tile[w][h];
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				this.map[x][y] = new Tile(x, y);
			}
		}

		// DEBUG
		this.map[2][0] = new Mirror(2, 0, false);
		this.map[2][3] = new Mirror(2, 3, false);
		this.map[0][3] = new Splitter(0, 3, false);
		this.map[0][1] = new Mirror(0, 1, false);
		this.map[5][1] = new Mirror(5, 1, true);
	}

	public void render(GraphicsContext gc){
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				Tile tile = this.map[x][y];
				tile.render(gc);
			}
		}
	}

	public Tile setTileAt(Tile tile){
		Tile t = getTileAt(tile.getX(), tile.getY());
		if (t != null){
			this.map[tile.getX()][tile.getY()] = tile;
		}
		return t;
	}

	public Tile getTileAt(int x, int y){
		if (containsPoint(x, y)){
			return this.map[x][y];
		} else {
			return null;
		}
	}

	public boolean containsPoint(int x, int y){
		return x >= 0 && y >= 0 && x < this.width && y < this.height;
	}

	public int getWidth(){
		return this.width;
	}

	public int getHeight(){
		return this.height;
	}
}