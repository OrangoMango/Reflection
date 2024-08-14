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
import com.orangomango.reflectiongame.core.inventory.Solution;
import com.orangomango.reflectiongame.core.data.DataManager;

public class PlayScreen extends GameScreen{
	private World currentWorld;
	private Laser currentLaser;
	private double mouseX, mouseY;
	private int selectedItem = -1;
	private double spaceX, spaceY;
	private ArrayList<Pair<World, Laser>> worlds = new ArrayList<>();
	private int currentLevel = 0; // If in editor mode, currentLevel = -1
	private boolean levelCompleted, inputAllowed = true, justShowedArrows;
	private Solution solutions;
	private Tile hintTile;
	private UiButton infoButton, skipButton, clearButton, hintButton;
	private UiButton lessTargetsButton, moreTargetsButton, saveButton, loadButton, markMissingButton;
	private boolean markMode = false;
	private DataManager dataManager;

	private static final Font FONT = Font.loadFont(PlayScreen.class.getResourceAsStream("/misc/font.ttf"), 40);
	private static final Font FONT_SMALL = Font.loadFont(PlayScreen.class.getResourceAsStream("/misc/font.ttf"), 25);

	public PlayScreen(int w, int h, HashMap<KeyCode, Boolean> keys, int startLevel){
		super(w, h, keys);

		this.infoButton = new UiButton("quick_buttons.png", 300, 75, 32, 32, () -> {
			displayInfo();
			Util.schedule(() -> this.justShowedArrows = true, 5000);
		});
		this.skipButton = new UiButton("quick_buttons.png", 350, 75, 32, 32, () -> nextLevel());
		this.clearButton = new UiButton("quick_buttons.png", 400, 75, 32, 32, () -> resetLevel());
		this.hintButton = new UiButton("quick_buttons.png", 450, 75, 32, 32, () -> displayHint());

		this.lessTargetsButton = new UiButton("quick_buttons.png", 250, 75, 32, 32, () -> this.currentWorld.getInventory().modifyTargets(-1));
		this.moreTargetsButton = new UiButton("quick_buttons.png", 300, 75, 32, 32, () -> this.currentWorld.getInventory().modifyTargets(1));
		this.saveButton = new UiButton("quick_buttons.png", 350, 75, 32, 32, () -> saveCustomWorld());
		this.loadButton = new UiButton("quick_buttons.png", 400, 75, 32, 32, () -> System.out.println("4"));
		this.markMissingButton = new UiButton("quick_buttons.png", 450, 75, 32, 32, () -> {
			this.markMode = !this.markMode;
			if (this.markMode){
				this.selectedItem = -1;
			}
		});

		// Load all the levels
		this.worlds = DataManager.loadWorlds(getClass().getResourceAsStream("/misc/levels.data"));
		this.currentLevel = startLevel;
		loadWorld(this.currentLevel);
		this.solutions = new Solution();

		this.dataManager = new DataManager(".reflection");
	}

	private void loadWorld(int index){
		if (index == -1){
			this.currentWorld = World.customWorld();
			this.currentLaser = null;
		} else {
			this.currentWorld = this.worlds.get(index).getKey();
			this.currentLaser = this.worlds.get(index).getValue();
		}
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
		if (!this.inputAllowed || this.markMode) return;	
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
				case 4:
					after = new BlockTile(px, py);
					break;
				case 5:
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
		if (!this.markMode){
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
	}

	@Override
	public void handleMouseInput(MouseEvent e, double scale, double offsetX){
		if (this.inputAllowed){
			double px = (this.mouseX-this.spaceX)/Tile.SIZE;
			double py = (this.mouseY-this.spaceY)/Tile.SIZE;
			double mx = (e.getX()-offsetX)/scale;
			double my = e.getY()/scale;
					
			if (e.getButton() == MouseButton.PRIMARY){
				if (this.currentLevel == -1 && this.markMode){
					if (px > 0 && py > 0){
						Tile tile = this.currentWorld.getTileAt((int)px, (int)py);
						if (tile != null && (tile.getId() == 1 || tile.getId() == 2 || tile.getId() == 3 || tile.getId() == 4 || tile.getId() == 6)){
							if (tile.isMarked()){
								if (tile instanceof Rotatable || tile instanceof Flippable){
									tile.setLocked(true);
									tile.setMarked(false);
								} else {
									tile.setMarked(false);
								}
							} else {
								if (tile.isLocked()){
									tile.setLocked(false);
								} else {
									tile.setMarked(true);
								}
							}
						}
					}
				}

				if (!this.markMode){
					for (int i = 0; i < this.currentWorld.getInventory().getItems().size(); i++){
						Rectangle2D rect = new Rectangle2D(this.width-120, 50+i*90, 80, 80);
						if (rect.contains(mx, my)){
							this.selectedItem = i;
							Util.playSound("button_click.wav");
							break;
						}
					}
				}

				if (this.currentLevel == -1){
					this.lessTargetsButton.click(mx, my);
					this.moreTargetsButton.click(mx, my);
					this.saveButton.click(mx, my);
					this.loadButton.click(mx, my);
					this.markMissingButton.click(mx, my);
				} else {
					this.infoButton.click(mx, my);
					this.skipButton.click(mx, my);
					this.clearButton.click(mx, my);
					this.hintButton.click(mx, my);
				}
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
			case 5:
				this.currentWorld.getInventory().getItems().put(4, this.currentWorld.getInventory().getItems().get(4)+inc);
				break; // BlockTile
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
				if (isLevelCompleted() && !this.levelCompleted){
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

	private void saveCustomWorld(){
		System.out.println("Saving");
		if (isLevelCompleted()){
			this.dataManager.saveWorld(this.currentWorld);
			System.out.println("Done");
		} else {
			System.out.println("Level not completed");
		}
	}

	private boolean isLevelCompleted(){
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

		return allLaser && onLights >= this.currentWorld.getInventory().getTargets() && (this.currentLevel == -1 || this.currentWorld.getInventory().isEmpty());
	}

	private void nextLevel(){
		this.levelCompleted = true;
		this.inputAllowed = false;
		this.hintTile = null;
		Util.playSound("invalid.wav");
	}

	private void resetLevel(){
		this.worlds = DataManager.loadWorlds(getClass().getResourceAsStream("/misc/levels.data"));
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
		if (this.inputAllowed && this.currentLevel >= 0){
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
			File file = new File(this.dataManager.getGameDataFolder(), "reflection_level-"+this.currentLevel+".png");
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

		if (this.currentLevel == -1){
			this.lessTargetsButton.render(gc, 4);
			this.moreTargetsButton.render(gc, 5);
			this.saveButton.render(gc, 6);
			this.loadButton.render(gc, 7);
			this.markMissingButton.render(gc, 8);
		} else {
			this.infoButton.render(gc, 0);
			this.skipButton.render(gc, 1);
			this.clearButton.render(gc, 2);
			this.hintButton.render(gc, 3);
		}

		gc.save();
		gc.setGlobalAlpha(0.6);
		gc.setFill(Color.WHITE);
		gc.fillRect(this.width-175, 35, 160, 90*this.currentWorld.getInventory().getItems().size()+10);
		gc.restore();

		for (int i = 0; i < this.currentWorld.getInventory().getItems().size(); i++){
			int index = this.currentWorld.getInventory().mapIndexToType(i);
			gc.drawImage(AssetLoader.getInstance().getImage("items.png"), 1+index*34, 1, 32, 32, this.width-120, 50+i*90, 80, 80);
			gc.setFont(FONT);
			gc.setTextAlign(TextAlignment.LEFT);
			gc.setFill(Color.BLACK);
			gc.fillText(Integer.toString(this.currentWorld.getInventory().getItems().get(index)), this.width-120-40, 50+i*90+40);
		}

		gc.setFont(FONT);
		gc.setFill(Color.BLACK);
		gc.setTextAlign(TextAlignment.LEFT);
		String levelInfoText = this.currentLevel == -1 ? "Custom level mode (Targets: "+this.currentWorld.getInventory().getTargets()+")" : this.currentLevel+". Number of targets: "+this.currentWorld.getLights().stream().filter(l -> l.isOn()).count()+"/"+this.currentWorld.getInventory().getTargets();
		gc.fillText(levelInfoText, 30, 50);

		if (this.currentLevel >= 0){
			gc.setFont(FONT_SMALL);
			gc.setFill(Color.WHITE);
			gc.setTextAlign(TextAlignment.LEFT);
			gc.fillText("I - Info | N - Skip | R - Restart | H - Hint | Q - Screenshot", 50, this.height-60);
		}

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

		if (this.markMode){
			gc.save();
			gc.setGlobalAlpha(0.5);
			gc.setFill(Color.BLACK);
			gc.fillRect(0, 0, this.width, this.height);
			gc.restore();
		}

		gc.restore();
	}
}