package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class World{
	private int width, height;
	private Tile[][] map;
	private ArrayList<Light> lights;

	public World(ArrayList<String> world, ArrayList<Light> lights){
		this.width = world.get(0).split(" ").length;
		this.height = world.size();
		this.lights = lights;
		this.map = new Tile[this.width][this.height];
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				final int id = Integer.parseInt(world.get(y).split(" ")[x]);
				switch (id){
					case 0:
						this.map[x][y] = new Tile(x, y);
						break;
					case 1:
						this.map[x][y] = new Mirror(x, y, false);
						break;
					case 2:
						this.map[x][y] = new Mirror(x, y, true);
						break;
					case 3:
						this.map[x][y] = new Splitter(x, y, false);
						break;
					case 4:
						this.map[x][y] = new Splitter(x, y, true);
						break;
					case 5:
						this.map[x][y] = new Wall(x, y);
						break;
					case 6:
						this.map[x][y] = new Checkpoint(x, y);
						break;
					case 7:
						this.map[x][y] = new SingleMirror(x, y);
						break;
					case 8:
						this.map[x][y] = new BlockTile(x, y);
						break;
				}

				// This is a pre-placed tile
				this.map[x][y].setPrePlaced(true);
				this.map[x][y].setId(id);
			}
		}
	}

	public void render(GraphicsContext gc){
		for (Light light : this.lights){
			light.render(gc);
		}

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

	public ArrayList<Light> getLights(){
		return this.lights;
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