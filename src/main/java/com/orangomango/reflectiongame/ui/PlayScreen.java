package com.orangomango.reflectiongame.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.*;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;
import com.orangomango.reflectiongame.core.*;

public class PlayScreen extends GameScreen{
	private World currentWorld;
	private Laser currentLaser;
	private double mouseX, mouseY;
	private int selectedItem = -1;
	private double spaceX, spaceY;
	private ArrayList<Pair<World, Laser>> worlds = new ArrayList<>();

	public PlayScreen(int w, int h, HashMap<KeyCode, Boolean> keys){
		super(w, h, keys);

		// Load all the levels
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/misc/levels.data")));
			String line;
			World cWorld = null;
			Laser cLaser = null;
			ArrayList<Light> lights = new ArrayList<>();
			ArrayList<String> matrix = new ArrayList<>();
			while ((line = reader.readLine()) != null){
				if (!line.startsWith("[") && !line.isBlank()){
					if (line.equals("end")){
						this.worlds.add(new Pair<World, Laser>(cWorld, cLaser));
						matrix.clear();
						lights.clear();
					} else {
						if (line.startsWith(". ")){
							int lx = Integer.parseInt(line.substring(2).split(" ")[0]);
							int ly = Integer.parseInt(line.substring(2).split(" ")[1]);
							lights.add(new Light(lx, ly));
						} else if (line.startsWith("- ")){
							cWorld = new World(matrix, new ArrayList<>(lights));
							int lx = Integer.parseInt(line.substring(2).split(" ")[0]);
							int ly = Integer.parseInt(line.substring(2).split(" ")[1]);
							int ld = Integer.parseInt(line.substring(2).split(" ")[2]);
							cLaser = new Laser(cWorld, lx, ly, ld);
						} else if (line.startsWith(": ")){
							int tx = Integer.parseInt(line.substring(2).split(" ")[0]);
							int ty = Integer.parseInt(line.substring(2).split(" ")[1]);
							Tile tile = cWorld.getTileAt(tx, ty);
							if (tile instanceof Rotatable){
								((Rotatable)tile).setRotationDisabled(line.substring(2).split(" ")[2].equals("noRot"));
								int am = Integer.parseInt(line.substring(2).split(" ")[3]);
								for (int i = 0; i < am; i++){
									((Rotatable)tile).rotate90();
								}
							} else {
								throw new RuntimeException("Tile can't be rotated");
							}
						} else {
							matrix.add(line);
						}
					}
				}
			}
			reader.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}

		loadWorld(0);
	}

	private void loadWorld(int index){
		this.currentWorld = this.worlds.get(index).getKey();
		this.currentLaser = this.worlds.get(index).getValue();
		this.spaceX = 100;
		this.spaceY = (this.height-this.currentWorld.getHeight()*Tile.SIZE)/2;
		updateWorld();
	}

	@Override
	public void handleMouseMovement(MouseEvent e){
		this.mouseX = e.getX();
		this.mouseY = e.getY();
	}

	@Override
	public void handleMouseInput(MouseEvent e){
		int px = (int)((e.getX()-this.spaceX)/Tile.SIZE);
		int py = (int)((e.getY()-this.spaceY)/Tile.SIZE);
		if (e.getButton() == MouseButton.PRIMARY){
			Tile before = null;
			switch (this.selectedItem){ // TODO
				case 0:
					before = this.currentWorld.setTileAt(new Tile(px, py));
					break;
				case 1:
					before = this.currentWorld.setTileAt(new Mirror(px, py, false));
					break;
				case 2:
					before = this.currentWorld.setTileAt(new Splitter(px, py, false));
					break;
				case 3:
					before = this.currentWorld.setTileAt(new Wall(px, py));
					break;
				case 4:
					before = this.currentWorld.setTileAt(new Checkpoint(px, py));
					break;
				case 5:
					before = this.currentWorld.setTileAt(new SingleMirror(px, py));
					break;
			}

			if (before != null){
				if (before.isPrePlaced() && before.getId() != 0){
					this.currentWorld.setTileAt(before);
				} else {
					this.selectedItem = -1;
					updateWorld();
				}
			}
		} else if (e.getButton() == MouseButton.SECONDARY){
			Tile tile = this.currentWorld.getTileAt(px, py);
			if (tile instanceof Flippable){
				((Flippable)tile).flip();
				updateWorld();
			} else if (tile instanceof Rotatable){
				if (!((Rotatable)tile).isRotationDisabled()){
					((Rotatable)tile).rotate90();
					updateWorld();
				}
			}
		}
	}

	private void updateWorld(){
		for (Light light : this.currentWorld.getLights()){
			light.setOn(false);
		}
		this.currentLaser.update();
		if (this.currentLaser.getCheckpointsPassed() == this.currentWorld.getCheckpoints()){
			boolean allOn = this.currentWorld.getLights().stream().map(Light::isOn).filter(b -> !b).findAny().isEmpty();
			if (allOn){
				System.out.println("Done");
			}
		}
	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		for (int i = 1; i <= 6; i++){
			KeyCode keyCode = KeyCode.valueOf("DIGIT"+i);
			if (this.keys.getOrDefault(keyCode, false)){
				this.selectedItem = this.selectedItem == i-1 ? -1 : i-1;
				this.keys.put(keyCode, false);
			}
		}

		gc.save();
		gc.scale(scale, scale);
		gc.setFill(Color.CYAN);
		gc.fillRect(0, 0, this.width, this.height);
		gc.translate(this.spaceX, this.spaceY);
		this.currentWorld.render(gc);
		this.currentLaser.render(gc);
		gc.translate(-this.spaceX, -this.spaceY);
		gc.restore();

		// Render the selected tool
		if (this.selectedItem != -1){
			gc.save();
			gc.setGlobalAlpha(0.6);
			Image image = null;
			switch (this.selectedItem){ // TODO: Improve
				case 0:
					image = AssetLoader.getInstance().getImage("tile.png");
					break;
				case 1:
					image = AssetLoader.getInstance().getImage("tile.png");
					break;
				case 2:
					image = AssetLoader.getInstance().getImage("tile.png");
					break;
				case 3:
					image = AssetLoader.getInstance().getImage("tile.png");
					break;
				case 4:
					image = AssetLoader.getInstance().getImage("tile.png");
					break;
				case 5:
					image = AssetLoader.getInstance().getImage("tile.png");
					break;
			}
			gc.drawImage(image, this.mouseX, this.mouseY, Tile.SIZE, Tile.SIZE);
			gc.restore();
		}
	}
}