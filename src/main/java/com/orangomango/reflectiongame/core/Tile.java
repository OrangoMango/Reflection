package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;

import com.orangomango.reflectiongame.AssetLoader;

public class Tile{
	protected int x, y;
	protected int id;
	private boolean prePlaced;
	private boolean showArrow, marked, locked;
	public boolean hasLaser;

	public static final double SIZE = 64;

	public Tile(int x, int y){
		this.x = x;
		this.y = y;
	}

	public int updateDirection(int dir){
		return dir;
	}

	public Laser generateLaser(Laser parent, World world, int dir){
		return null;
	}

	public void render(GraphicsContext gc){
		gc.drawImage(AssetLoader.getInstance().getImage("tile.png"), this.x*SIZE, this.y*SIZE, SIZE, SIZE);
	}

	public int getId(){
		return this.id;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	public void setPrePlaced(boolean value){
		this.prePlaced = value;
	}

	public boolean isPrePlaced(){
		return this.prePlaced;
	}

	public void setShowArrow(boolean value){
		this.showArrow = value;
	}

	public boolean isShowArrow(){
		return this.showArrow;
	}

	public void setMarked(boolean value){
		this.marked = value;
	}

	public boolean isMarked(){
		return this.marked;
	}

	public void setLocked(boolean value){
		this.locked = value;
	}

	public boolean isLocked(){
		return this.locked;
	}
}