package com.orangomango.reflectiongame.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import javafx.geometry.Rectangle2D;

import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.*;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;
import com.orangomango.reflectiongame.core.*;
import com.orangomango.reflectiongame.core.inventory.Inventory;

public class PlayScreen extends GameScreen{
	private World currentWorld;
	private Laser currentLaser;
	private double mouseX, mouseY;
	private int selectedItem = -1;
	private double spaceX, spaceY;
	private ArrayList<Pair<World, Laser>> worlds = new ArrayList<>();
	private int currentLevel = 0;
	private boolean levelCompleted, inputAllowed = true, justShowedArrows;

	private static final Font FONT = Font.loadFont(PlayScreen.class.getResourceAsStream("/misc/font.ttf"), 40);

	public PlayScreen(int w, int h, HashMap<KeyCode, Boolean> keys, int startLevel){
		super(w, h, keys);

		// Load all the levels
		loadLevels();
		this.currentLevel = startLevel;
		loadWorld(this.currentLevel);
	}

	private void loadLevels(){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/misc/levels.data")));
			String line;
			World cWorld = null;
			Laser cLaser = null;
			Inventory cInv = null;
			ArrayList<Light> lights = new ArrayList<>();
			ArrayList<String> matrix = new ArrayList<>();
			this.worlds.clear();
			while ((line = reader.readLine()) != null){
				if (!line.startsWith("[") && !line.isBlank()){
					if (line.equals("end")){
						this.worlds.add(new Pair<World, Laser>(cWorld, cLaser));
						matrix.clear();
						lights.clear();
					} else {
						if (line.startsWith(". ")){
							// Pretty useless
							int lx = Integer.parseInt(line.substring(2).split(" ")[0]);
							int ly = Integer.parseInt(line.substring(2).split(" ")[1]);
							lights.add(new Light(lx, ly));
						} else if (line.startsWith("- ")){
							cWorld = new World(matrix, new ArrayList<>(lights), cInv);
							if (line.length() > 2){
								int lx = Integer.parseInt(line.substring(2).split(" ")[0]);
								int ly = Integer.parseInt(line.substring(2).split(" ")[1]);
								int ld = Integer.parseInt(line.substring(2).split(" ")[3]);
								Tile tile = cWorld.getTileAt(lx, ly);
								cLaser = new Laser(null, cWorld, lx, ly, ld);
								((Rotatable)tile).setRotationDisabled(line.substring(2).split(" ")[2].equals("noRot"));
								for (int i = 0; i < ld; i++){
									((Rotatable)tile).rotate90();
								}
							} else {
								cLaser = null;
							}
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
							} else if (tile instanceof Flippable){
								((Flippable)tile).setFlippingDisabled(line.substring(2).split(" ")[2].equals("noFlip"));
								if (line.substring(2).split(" ")[3].equals("flip")){
									((Flippable)tile).flip();
								}
							} else {
								throw new RuntimeException("Tile can't be rotated/flipped");
							}
						} else if (line.startsWith("#")){
							cInv = new Inventory(line);
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
	}

	private void loadWorld(int index){
		this.currentWorld = this.worlds.get(index).getKey();
		this.currentLaser = this.worlds.get(index).getValue();
		this.spaceX = 65;
		this.spaceY = (this.height-this.currentWorld.getHeight()*Tile.SIZE)/2;
		updateWorld();
	}

	@Override
	public void handleMouseMovement(MouseEvent e, double scale, double offsetX){
		if (!this.inputAllowed) return;
		this.mouseX = (e.getX()-offsetX)/scale;
		this.mouseY = e.getY()/scale;
		int px = (int)((this.mouseX-this.spaceX)/Tile.SIZE);
		int py = (int)((this.mouseY-this.spaceY)/Tile.SIZE);
		Tile tile = this.currentWorld.getTileAt(px, py);
		for (int x = 0; x < this.currentWorld.getWidth(); x++){
			for (int y = 0; y < this.currentWorld.getHeight(); y++){
				this.currentWorld.getTileAt(x, y).setShowArrow(false);
			}
		}
		if (tile != null){
			if (tile instanceof Flippable){
				tile.setShowArrow(!((Flippable)tile).isFlippingDisabled());
			} else if (tile instanceof Rotatable){
				tile.setShowArrow(!((Rotatable)tile).isRotationDisabled());
			}
		}
	}

	@Override
	public void handleMouseInput(MouseEvent e, double scale, double offsetX){
		if (!this.inputAllowed) return;
		int px = (int)(((e.getX()-offsetX)/scale-this.spaceX)/Tile.SIZE);
		int py = (int)((e.getY()/scale-this.spaceY)/Tile.SIZE);
		if (e.getButton() == MouseButton.PRIMARY){
			Tile after = null;
			final int idx = this.currentWorld.getInventory().mapIndexToType(this.selectedItem);
			switch (idx){
				case -1:
					after = new Tile(px, py);
					break;
				case 0:
					after = new SingleMirror(px, py);
					break;
				case 1:
					after = new Splitter(px, py, false);
					break;
				case 2:
					after = new Mirror(px, py, false);
					break;
				case 3:
					after = new Checkpoint(px, py);
					break;
				case 5: // TODO: Change 5 to 4
					after = new LaserTile(px, py);
					break;
			}
			Tile before = this.currentWorld.setTileAt(after);
			if (before != null){
				if (before.isPrePlaced() && before.getId() != 0 || this.currentWorld.getInventory().getItems().getOrDefault(idx, -1) == 0){
					this.currentWorld.setTileAt(before);
					Util.playSound("invalid.wav");
				} else {
					if (after instanceof LaserTile){
						for (int x = 0; x < this.currentWorld.getWidth(); x++){
							for (int y = 0; y < this.currentWorld.getHeight(); y++){
								Tile tile = this.currentWorld.getTileAt(x, y);
								if (tile != after && tile instanceof LaserTile){
									this.currentWorld.setTileAt(new Tile(x, y));
								}
							}
						}
						this.currentLaser = new Laser(null, this.currentWorld, after.getX(), after.getY(), 0);
					} else if (after instanceof SingleMirror){
						this.currentWorld.getLights().add(new Light(after.getX(), after.getY()));
					}

					// Remove any lights that are on the same tile
					if (!(after instanceof SingleMirror)){
						for (int i = 0; i < this.currentWorld.getLights().size(); i++){
							Light light = this.currentWorld.getLights().get(i);
							if (light.getX() == after.getX() && light.getY() == after.getY()){
								this.currentWorld.getLights().remove(i);
								i--;
							}
						}
					}

					boolean laserTile = false;
					for (int x = 0; x < this.currentWorld.getWidth(); x++){
						for (int y = 0; y < this.currentWorld.getHeight(); y++){
							if (this.currentWorld.getTileAt(x, y) instanceof LaserTile){
								laserTile = true;
							}
						}
					}
					if (!laserTile) this.currentLaser = null;

					updateInventory(before.getId(), 1);
					updateInventory(after.getId(), -1);
					this.selectedItem = -1;
					Util.playSound("tile_placed.wav");
					updateWorld();
				}
			} else {
				this.selectedItem = -1;
				for (int i = 0; i < this.currentWorld.getInventory().getItems().size(); i++){
					Rectangle2D rect = new Rectangle2D(this.width-120, 30+i*90, 80, 80); // TODO: Scale
					if (rect.contains((e.getX()-offsetX)/scale, e.getY()/scale)){
						this.selectedItem = i;
						Util.playSound("button_click.wav");
						break;
					}
				}
			}
		} else if (e.getButton() == MouseButton.SECONDARY){
			Tile tile = this.currentWorld.getTileAt(px, py);
			if (tile instanceof Flippable){
				if (!((Flippable)tile).isFlippingDisabled()){
					((Flippable)tile).flip();
					Util.playSound("tile_used.wav");
					updateWorld();
				}
			} else if (tile instanceof Rotatable){
				if (!((Rotatable)tile).isRotationDisabled()){
					((Rotatable)tile).rotate90();
					if (tile instanceof LaserTile){
						this.currentLaser.rotate90();
					}
					Util.playSound("tile_used.wav");
					updateWorld();
				}
			}
		}
	}

	private void updateInventory(int id, int inc){
		switch (id){
			case 1:
				this.currentWorld.getInventory().getItems().put(2, this.currentWorld.getInventory().getItems().get(2)+inc);
				break; // Mirror
			case 2:
				this.currentWorld.getInventory().getItems().put(1, this.currentWorld.getInventory().getItems().get(1)+inc);
				break; // Splitter
			case 3:
				this.currentWorld.getInventory().getItems().put(3, this.currentWorld.getInventory().getItems().get(3)+inc);
				break; // Checkpoint
			case 4:
				this.currentWorld.getInventory().getItems().put(0, this.currentWorld.getInventory().getItems().get(0)+inc);
				break; // SingleMirror
			case 6:
				this.currentWorld.getInventory().getItems().put(5, this.currentWorld.getInventory().getItems().get(5)+inc);
				break; // Laser
		}
	}

	private void updateWorld(){
		for (Light light : this.currentWorld.getLights()){
			light.setOn(false);
		}
		for (int x = 0; x < this.currentWorld.getWidth(); x++){
			for (int y = 0; y < this.currentWorld.getHeight(); y++){
				Tile tile = this.currentWorld.getTileAt(x, y);
				tile.hasLaser = false;
				if (tile instanceof Checkpoint){
					((Checkpoint)tile).setActivated(false);
				}
			}
		}
		if (this.currentLaser != null){
			this.currentLaser.update();
			if (this.currentLaser.getCheckpointsPassed() == this.currentWorld.getCheckpoints()){
				long onLights = this.currentWorld.getLights().stream().filter(l -> l.isOn()).count();
				boolean allLaser = true;
				for (int x = 0; x < this.currentWorld.getWidth(); x++){
					for (int y = 0; y < this.currentWorld.getHeight(); y++){
						Tile tile = this.currentWorld.getTileAt(x, y);
						// Tiles excluded: empty tile, laser tile and block tile
						if (tile.getId() != 5 && tile.getId() != 0 && tile.getId() != 6 && !tile.hasLaser){
							allLaser = false;
						}
					}
				}

				if (allLaser && onLights >= this.currentWorld.getInventory().getTargets() && !this.levelCompleted && this.currentWorld.getInventory().isEmpty()){
					this.inputAllowed = false;
					Util.schedule(() -> {
						this.levelCompleted = true;
						Util.playSound("level_completed.wav");
					}, 1000);
				}
			}
		}
	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		for (int i = 1; i <= this.currentWorld.getInventory().getItems().size(); i++){
			KeyCode keyCode = KeyCode.valueOf("DIGIT"+i);
			if (this.keys.getOrDefault(keyCode, false)){
				this.selectedItem = this.selectedItem == i-1 ? -1 : i-1;
				this.keys.put(keyCode, false);
			}
		}

		// Skip/restart the level
		if (this.inputAllowed){
			if (this.keys.getOrDefault(KeyCode.N, false)){
				this.levelCompleted = true;
				Util.playSound("invalid.wav");
				this.keys.put(KeyCode.N, false);
			} else if (this.keys.getOrDefault(KeyCode.R, false)){
				loadLevels();
				loadWorld(this.currentLevel);
				this.keys.put(KeyCode.R, false);
			}
		}

		if (this.keys.getOrDefault(KeyCode.S, false) && this.inputAllowed){
			for (int x = 0; x < this.currentWorld.getWidth(); x++){
				for (int y = 0; y < this.currentWorld.getHeight(); y++){
					Tile tile = this.currentWorld.getTileAt(x, y);
					if (tile instanceof Flippable){
						tile.setShowArrow(!((Flippable)tile).isFlippingDisabled());
					} else if (tile instanceof Rotatable){
						tile.setShowArrow(!((Rotatable)tile).isRotationDisabled());
					}
				}
			}
			this.justShowedArrows = true;
		} else if (this.justShowedArrows || !this.inputAllowed){
			for (int x = 0; x < this.currentWorld.getWidth(); x++){
				for (int y = 0; y < this.currentWorld.getHeight(); y++){
					this.currentWorld.getTileAt(x, y).setShowArrow(false);
				}
			}
			this.justShowedArrows = false;
		}

		if (this.keys.getOrDefault(KeyCode.Q, false)){
			File file = new File(System.getProperty("user.home"), "reflection_level-"+this.currentLevel+".png");
			if (file != null){
				try {
					Canvas miniCanvas = new Canvas(Tile.SIZE*this.currentWorld.getWidth(), Tile.SIZE*this.currentWorld.getHeight());
					this.currentWorld.render(miniCanvas.getGraphicsContext2D());
					if (this.currentLaser != null) this.currentLaser.render(miniCanvas.getGraphicsContext2D());
					WritableImage wi = new WritableImage((int)miniCanvas.getWidth(), (int)miniCanvas.getHeight());
					miniCanvas.snapshot(null, wi);
					RenderedImage ri = SwingFXUtils.fromFXImage(wi, null);
					ImageIO.write(ri, "png", file);
					System.out.println("Screenshot saved");
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		}

		gc.save();
		gc.scale(scale, scale);
		gc.drawImage(AssetLoader.getInstance().getImage("background.jpg"), 0, 0, this.width, this.height);
		gc.translate(this.spaceX, this.spaceY);
		this.currentWorld.render(gc);
		if (this.currentLaser != null) this.currentLaser.render(gc);
		gc.translate(-this.spaceX, -this.spaceY);

		for (int i = 0; i < this.currentWorld.getInventory().getItems().size(); i++){
			int index = this.currentWorld.getInventory().mapIndexToType(i);
			gc.drawImage(AssetLoader.getInstance().getImage("items.png"), 1+index*34, 1, 32, 32, this.width-120, 30+i*90, 80, 80);
			gc.setFont(FONT);
			gc.setTextAlign(TextAlignment.LEFT);
			gc.setFill(Color.BLACK);
			gc.fillText(Integer.toString(this.currentWorld.getInventory().getItems().get(index)), this.width-120-25, 30+i*90+40);
		}

		gc.setFont(FONT);
		gc.setFill(Color.BLACK);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText(this.currentLevel+". Number of targets: "+this.currentWorld.getLights().stream().filter(l -> l.isOn()).count()+"/"+this.currentWorld.getInventory().getTargets(), this.width/2, 50);

		// Render the selected tool
		if (this.selectedItem != -1){
			gc.save();
			gc.setGlobalAlpha(0.6);
			final int index = this.currentWorld.getInventory().mapIndexToType(this.selectedItem);
			gc.drawImage(AssetLoader.getInstance().getImage("items.png"), 1+index*34, 1, 32, 32, this.mouseX-Tile.SIZE/2, this.mouseY-Tile.SIZE/2, Tile.SIZE, Tile.SIZE);
			gc.restore();
		}

		if (this.levelCompleted){
			if (this.keys.getOrDefault(KeyCode.SPACE, false)){
				this.currentLevel++;
				if (this.currentLevel >= this.worlds.size()){
					SCREEN_SWITCHER.accept(new HomeScreen(this.width, this.height, this.keys));
				} else {
					loadWorld(this.currentLevel);
					this.levelCompleted = false;
					this.inputAllowed = true;
					this.selectedItem = -1;
				}
				this.keys.put(KeyCode.SPACE, false);
			}

			gc.save();
			gc.setGlobalAlpha(0.8);
			gc.setFill(Color.BLACK);
			gc.fillRect(0, 0, this.width, this.height);
			gc.setFill(Color.WHITE);
			gc.setFont(FONT);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.fillText("Level completed!\nPress space to continue", this.width/2.0, this.height-80);
			gc.restore();
		}

		gc.restore();
	}
}