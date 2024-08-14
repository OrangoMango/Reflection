package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

import com.orangomango.reflectiongame.core.inventory.Inventory;
import com.orangomango.reflectiongame.AssetLoader;

public class World{
	private int width, height;
	private Tile[][] map;
	private ArrayList<Light> lights;
	private int checkpoints;
	private Laser laser;
	private Inventory inventory;

	public World(ArrayList<String> world, ArrayList<Light> lights, Inventory inv){
		this.width = world.get(0).split(" ").length;
		this.height = world.size();
		this.lights = lights;
		this.inventory = inv;
		this.map = new Tile[this.width][this.height];
		this.checkpoints = inv.getItems().getOrDefault(3, 0);
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
						this.map[x][y] = new Mirror(x, y, true); // Useless
						break;
					case 3:
						this.map[x][y] = new Splitter(x, y, false);
						break;
					case 4:
						this.map[x][y] = new Splitter(x, y, true); // Useless
						break;
					case 5:
						this.map[x][y] = new Checkpoint(x, y);
						this.checkpoints++;
						break;
					case 6:
						this.map[x][y] = new SingleMirror(x, y);
						this.lights.add(new Light(x, y));
						break;
					case 7:
						this.map[x][y] = new BlockTile(x, y);
						break;
					case 8:
						this.map[x][y] = new LaserTile(x, y);
						break;
				}

				// This is a pre-placed tile
				this.map[x][y].setPrePlaced(true);
			}
		}
	}

	public static World customWorld(){
		Inventory inv = new Inventory("#3 0;25 1;25 2;25 3;25 4;25 5;1");
		ArrayList<String> data = new ArrayList<>();
		for (int i = 0; i < 5; i++){
			data.add("0 0 0 0 0");
		}
		World world = new World(data, new ArrayList<>(), inv);
		return world;
	}

	public void render(GraphicsContext gc){
		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){
				Tile tile = this.map[x][y];
				tile.render(gc);
				
				if (tile.isShowArrow()){
					gc.save();
					gc.setGlobalAlpha(0.9);
					gc.drawImage(AssetLoader.getInstance().getImage("arrow.png"), tile.getX()*Tile.SIZE, tile.getY()*Tile.SIZE, Tile.SIZE, Tile.SIZE);
					gc.restore();
				}
				
				if (tile.isMarked()){
					gc.save();
					gc.setGlobalAlpha(0.9);
					gc.drawImage(AssetLoader.getInstance().getImage("marked.png"), tile.getX()*Tile.SIZE, tile.getY()*Tile.SIZE, Tile.SIZE, Tile.SIZE);
					gc.restore();
				}

				if (tile.isLocked()){
					gc.save();
					gc.setGlobalAlpha(0.9);
					gc.drawImage(AssetLoader.getInstance().getImage("lock.png"), tile.getX()*Tile.SIZE, tile.getY()*Tile.SIZE, Tile.SIZE, Tile.SIZE);
					gc.restore();
				}
			}
		}

		for (Light light : this.lights){
			Tile tile = getTileAt(light.getX(), light.getY());
			light.render(gc, tile == null ? null : (Rotatable)tile);
		}
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		ArrayList<String> information = new ArrayList<>();
		String laserInfo = "";

		for (int y = 0; y < this.height; y++){
			for (int x = 0; x < this.width; x++){
				Tile tile = this.map[x][y];
				if (tile.isMarked()){
					builder.append("0");
				} else {
					switch (tile.getId()){
						case 0:
							builder.append("0");
							break;
						case 1:
							builder.append("1");
							break;
						case 2:
							builder.append("3");
							break;
						case 3:
							builder.append("5");
							break;
						case 4:
							builder.append("6");
							break;
						case 5:
							builder.append("7");
							break;
						case 6:
							builder.append("8");
							break;
					}

					if (tile.getId() == 6){ // Laser tile
						laserInfo = tile.getX()+" "+tile.getY()+" "+(tile.isLocked() ? "noRot" : "rot")+" "+((Rotatable)tile).getRotation();
					} else {
						if (tile instanceof Rotatable){
							Rotatable rot = (Rotatable)tile;
							information.add(tile.getX()+" "+tile.getY()+" "+(tile.isLocked() ? "noRot" : "rot")+" "+rot.getRotation());
						} else if (tile instanceof Flippable){
							Flippable flip = (Flippable)tile;
							information.add(tile.getX()+" "+tile.getY()+" "+(tile.isLocked() ? "noFlip" : "flip")+" "+(flip.isFlipped() ? "flip" : "-"));
						}
					}
				}

				if (x < this.width-1) builder.append(" ");
			}

			if (y < this.height-1) builder.append("\n");
		}

		builder.append("\n");
		builder.append("- "+laserInfo);
		for (String info : information){
			builder.append("\n");
			builder.append(": "+info);
		}

		return builder.toString();
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

	public Laser getLaser(){
		return this.laser;
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

	public int getCheckpoints(){
		return this.checkpoints;
	}

	public Inventory getInventory(){
		return this.inventory;
	}
}