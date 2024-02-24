package com.orangomango.indiedev3.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.util.HashMap;

import com.orangomango.indiedev3.AssetLoader;
import com.orangomango.indiedev3.Util;
import com.orangomango.indiedev3.core.*;

public class PlayScreen extends GameScreen{
	private World currentWorld;
	private Laser currentLaser;
	private double mouseX, mouseY;
	private int selectedItem = -1;
	private double spaceX, spaceY;

	public PlayScreen(int w, int h, HashMap<KeyCode, Boolean> keys){
		super(w, h, keys);
		loadWorld();
	}

	private void loadWorld(){
		this.currentWorld = new World(7, 7);
		this.currentLaser = new Laser(this.currentWorld, 7, 0, Util.DIRECTION_W);
		this.spaceX = 100;
		this.spaceY = (this.height-this.currentWorld.getHeight()*Tile.SIZE)/2;
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
			switch (this.selectedItem){
				case 0:
					before = this.currentWorld.setTileAt(new Tile(px, py));
					break;
				case 1:
					before = this.currentWorld.setTileAt(new Mirror(px, py, false));
					break;
				case 2:
					before = this.currentWorld.setTileAt(new Splitter(px, py, false));
					break;
			}

			this.selectedItem = -1;
			this.currentLaser.update();
		} else if (e.getButton() == MouseButton.SECONDARY){
			Tile tile = this.currentWorld.getTileAt(px, py);
			if (tile instanceof Flippable){
				((Flippable)tile).flip();
				this.currentLaser.update();
			}
		}
	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		if (this.keys.getOrDefault(KeyCode.DIGIT1, false)){
			this.selectedItem = this.selectedItem == 0 ? -1 : 0;
			this.keys.put(KeyCode.DIGIT1, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT2, false)){
			this.selectedItem = this.selectedItem == 1 ? -1 : 1;
			this.keys.put(KeyCode.DIGIT2, false);
		} else if (this.keys.getOrDefault(KeyCode.DIGIT3, false)){
			this.selectedItem = this.selectedItem == 1 ? -1 : 2;
			this.keys.put(KeyCode.DIGIT3, false);
		}

		gc.save();
		gc.scale(scale, scale);
		gc.setFill(Color.GREEN);
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
			switch (this.selectedItem){
				case 0:
					image = AssetLoader.getInstance().getImage("tile.png");
					break;
				case 1:
					image = AssetLoader.getInstance().getImage("tile.png");
					break;
				case 2:
					image = AssetLoader.getInstance().getImage("tile.png");
					break;
			}
			gc.drawImage(image, this.mouseX, this.mouseY, Tile.SIZE, Tile.SIZE);
			gc.restore();
		}
	}
}