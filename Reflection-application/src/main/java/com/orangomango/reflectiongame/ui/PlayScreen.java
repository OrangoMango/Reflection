package com.orangomango.reflectiongame.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Rectangle2D;

import java.util.HashMap;
import java.util.ArrayList;

import com.orangomango.reflectiongame.AssetLoader;
import com.orangomango.reflectiongame.Util;
import com.orangomango.reflectiongame.core.*;
import com.orangomango.reflectiongame.core.inventory.Inventory;
import com.orangomango.reflectiongame.core.inventory.Solution;

import dev.webfx.platform.resource.Resource;

public class PlayScreen extends GameScreen{
	private static class Pair<K, V>{
		private K key;
		private V value;

		public Pair(K k, V v){
			this.key = k;
			this.value = v;
		}

		public K getKey(){
			return this.key;
		}

		public V getValue(){
			return this.value;
		}
	}

	private World currentWorld;
	private Laser currentLaser;
	private double mouseX, mouseY;
	private int selectedItem = -1;
	private double spaceX, spaceY;
	private ArrayList<Pair<World, Laser>> worlds = new ArrayList<>();
	private int currentLevel = 0;
	private boolean levelCompleted, inputAllowed = true, justShowedArrows;
	private Solution solutions;
	private Tile hintTile;
	private UiButton infoButton, skipButton, clearButton, hintButton;

	private static final Font FONT = Font.loadFont(Resource.toUrl("/misc/font.ttf", PlayScreen.class), 40);
	private static final Font FONT_SMALL = Font.loadFont(Resource.toUrl("/misc/font.ttf", PlayScreen.class), 25);

	public PlayScreen(int w, int h, HashMap<KeyCode, Boolean> keys, int startLevel){
		super(w, h, keys);

		this.infoButton = new UiButton("quick_buttons.png", 300, 75, 32, 32, () -> {
			displayInfo();
			Util.schedule(() -> this.justShowedArrows = true, 5000);
		});
		this.skipButton = new UiButton("quick_buttons.png", 350, 75, 32, 32, () -> nextLevel());
		this.clearButton = new UiButton("quick_buttons.png", 400, 75, 32, 32, () -> resetLevel());
		this.hintButton = new UiButton("quick_buttons.png", 450, 75, 32, 32, () -> displayHint());

		// Load all the levels
		loadLevels();
		this.currentLevel = startLevel;
		loadWorld(this.currentLevel);
		this.solutions = new Solution();
	}

	private void loadLevels(){
		String[] lines = Resource.getText(Resource.toUrl("/misc/levels.data", PlayScreen.class)).split("\n");
		World cWorld = null;
		Laser cLaser = null;
		Inventory cInv = null;
		ArrayList<Light> lights = new ArrayList<>();
		ArrayList<String> matrix = new ArrayList<>();
		this.worlds.clear();
		for (String line : lines){
			if (!line.startsWith("[") && !line.equals("")){
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
	}

	private void loadWorld(int index){
		this.currentWorld = this.worlds.get(index).getKey();
		this.currentLaser = this.worlds.get(index).getValue();
		this.spaceX = 65;
		this.spaceY = (this.height-this.currentWorld.getHeight()*Tile.SIZE)/2;
		updateWorld();
	}

	@Override
	public void handleMouseDragged(MouseEvent e, double scale, double offsetX){
		if (!this.inputAllowed) return;
		this.mouseX = (e.getX()-offsetX)/scale;
		this.mouseY = e.getY()/scale;
	}

	@Override
	public void handleMouseReleased(MouseEvent e, double scale, double offsetX){
		if (!this.inputAllowed) return;	
		if (e.getButton() == MouseButton.PRIMARY){
			double px0 = (this.mouseX-this.spaceX)/Tile.SIZE;
			double py0 = (this.mouseY-this.spaceY)/Tile.SIZE;
			if (px0 < 0 || py0 < 0) return;
			int px = (int)px0;
			int py = (int)py0;
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
					this.selectedItem = -1;
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
			}
		}
	}

	@Override
	public void handleMouseMovement(MouseEvent e, double scale, double offsetX){
		if (!this.inputAllowed) return;
		this.mouseX = (e.getX()-offsetX)/scale;
		this.mouseY = e.getY()/scale;
		double px = (this.mouseX-this.spaceX)/Tile.SIZE;
		double py = (this.mouseY-this.spaceY)/Tile.SIZE;
		Tile tile = this.currentWorld.getTileAt((int)px, (int)py);
		for (int x = 0; x < this.currentWorld.getWidth(); x++){
			for (int y = 0; y < this.currentWorld.getHeight(); y++){
				this.currentWorld.getTileAt(x, y).setShowArrow(false);
			}
		}
		if (tile != null && px > 0 && py > 0){
			if (tile instanceof Flippable){
				tile.setShowArrow(!((Flippable)tile).isFlippingDisabled());
			} else if (tile instanceof Rotatable){
				tile.setShowArrow(!((Rotatable)tile).isRotationDisabled());
			}
		}
	}

	@Override
	public void handleMouseInput(MouseEvent e, double scale, double offsetX){
		if (this.inputAllowed){
			double px = (this.mouseX-this.spaceX)/Tile.SIZE;
			double py = (this.mouseY-this.spaceY)/Tile.SIZE;
			if (e.getButton() == MouseButton.PRIMARY){
				double mx = (e.getX()-offsetX)/scale;
				double my = e.getY()/scale;
				for (int i = 0; i < this.currentWorld.getInventory().getItems().size(); i++){
					Rectangle2D rect = new Rectangle2D(this.width-120, 60+i*90, 80, 80);
					if (rect.contains(mx, my)){
						this.selectedItem = i;
						Util.playSound("button_click.wav");
						break;
					}
				}
		
				this.infoButton.click(mx, my);
				this.skipButton.click(mx, my);
				this.clearButton.click(mx, my);
				this.hintButton.click(mx, my);
			} else if (e.getButton() == MouseButton.SECONDARY){
				if (px > 0 && py > 0){
					Tile tile = this.currentWorld.getTileAt((int)px, (int)py);
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
		} else {
			if (this.levelCompleted){
				completeLevel();
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
					this.hintTile = null;
					Util.schedule(() -> {
						this.levelCompleted = true;
						Util.playSound("level_completed.wav");
					}, 1000);
				}
			}
		}
	}

	private void nextLevel(){
		this.levelCompleted = true;
		this.inputAllowed = false;
		this.hintTile = null;
		Util.playSound("invalid.wav");
	}

	private void resetLevel(){
		loadLevels();
		loadWorld(this.currentLevel);
	}

	private void displayHint(){
		if (this.hintTile == null){
			this.hintTile = this.solutions.getHint(this.currentWorld, this.currentLevel);
			Util.schedule(() -> this.hintTile = null, 1500);
		}
	}

	private void displayInfo(){
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
	}

	private void completeLevel(){
		this.currentLevel++;
		if (this.currentLevel >= this.worlds.size()){
			SCREEN_SWITCHER.accept(new HomeScreen(this.width, this.height, this.keys));
		} else {
			loadWorld(this.currentLevel);
			this.levelCompleted = false;
			this.inputAllowed = true;
			this.selectedItem = -1;
		}
	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		// Skip/restart the level
		if (this.inputAllowed){
			if (this.keys.getOrDefault(KeyCode.N, false)){
				nextLevel();
				this.keys.put(KeyCode.N, false);
			} else if (this.keys.getOrDefault(KeyCode.R, false)){
				resetLevel();
				this.keys.put(KeyCode.R, false);
			} else if (this.keys.getOrDefault(KeyCode.H, false)){
				displayHint();
				this.keys.put(KeyCode.H, false);
			}
		}

		if (this.keys.getOrDefault(KeyCode.I, false) && this.inputAllowed){
			displayInfo();
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
			// Screenshot
			// ...
			this.keys.put(KeyCode.Q, false);
		}

		gc.save();
		gc.scale(scale, scale);
		gc.drawImage(AssetLoader.getInstance().getImage("background.jpg"), 0, 0, this.width, this.height);
		gc.translate(this.spaceX, this.spaceY);
		this.currentWorld.render(gc);
		if (this.currentLaser != null) this.currentLaser.render(gc);
		if (this.hintTile != null){
			gc.save();
			gc.setGlobalAlpha(0.8);
			this.hintTile.render(gc);
			gc.restore();
		}
		gc.translate(-this.spaceX, -this.spaceY);

		this.infoButton.render(gc, 0);
		this.skipButton.render(gc, 1);
		this.clearButton.render(gc, 2);
		this.hintButton.render(gc, 3);

		gc.save();
		gc.setGlobalAlpha(0.6);
		gc.setFill(Color.WHITE);
		gc.fillRect(this.width-165, 50, 150, 90*this.currentWorld.getInventory().getItems().size()+10);
		gc.restore();

		for (int i = 0; i < this.currentWorld.getInventory().getItems().size(); i++){
			int index = this.currentWorld.getInventory().mapIndexToType(i);
			gc.drawImage(AssetLoader.getInstance().getImage("items.png"), 1+index*34, 1, 32, 32, this.width-120, 60+i*90, 80, 80);
			gc.setFont(FONT);
			gc.setTextAlign(TextAlignment.LEFT);
			gc.setFill(Color.BLACK);
			gc.fillText(Integer.toString(this.currentWorld.getInventory().getItems().get(index)), this.width-120-25, 60+i*90+40);
		}

		gc.setFont(FONT);
		gc.setFill(Color.BLACK);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText(this.currentLevel+". Number of targets: "+this.currentWorld.getLights().stream().filter(l -> l.isOn()).count()+"/"+this.currentWorld.getInventory().getTargets(), this.width/2, 50);

		gc.setFont(FONT_SMALL);
		gc.setFill(Color.WHITE);
		gc.setTextAlign(TextAlignment.LEFT);
		gc.fillText("I - Info | N - Skip | R - Restart | H - Hint", 50, this.height-60);

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
				completeLevel();
				this.keys.put(KeyCode.SPACE, false);
			}

			gc.save();
			gc.setGlobalAlpha(0.65);
			gc.setFill(Color.BLACK);
			gc.fillRect(0, 0, this.width, this.height);
			gc.setFill(Color.RED);
			gc.setFont(FONT);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.fillText("Level completed!\nClick/Press space to continue", this.width/2.0, this.height-80);
			gc.restore();
		}

		gc.restore();
	}
}
