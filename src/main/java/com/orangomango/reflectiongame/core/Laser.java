package com.orangomango.reflectiongame.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

import java.util.ArrayList;

import com.orangomango.reflectiongame.Util;

public class Laser{
	private int x, y;
	private int direction;
	private ArrayList<Point2D> points = new ArrayList<>();
	private ArrayList<Laser> generatedLasers = new ArrayList<>();
	private World world;
	private int checkpointsPassed;

	public Laser(World world, int x, int y, int d){
		this.world = world;
		this.x = x;
		this.y = y;
		this.direction = d;
		update();
	}

	public void update(){
		int cx = this.x;
		int cy = this.y;
		int dir = this.direction;
		int lastDir = dir;
		this.checkpointsPassed = 0;
		this.points.clear();
		this.generatedLasers.clear();
		this.points.add(new Point2D(cx+0.5, cy+0.5));
		do {
			int[] t = Util.getDirection(dir);
			cx += t[0];
			cy += t[1];
			this.points.add(new Point2D(cx+0.5, cy+0.5));
			Tile tile = this.world.getTileAt(cx, cy);
			if (tile != null){
				Laser gen = tile.generateLaser(this.world, dir);
				if (gen != null) this.generatedLasers.add(gen);
				lastDir = dir;
				dir = tile.updateDirection(dir);

				if (tile instanceof Checkpoint){
					if (((Checkpoint)tile).laserPassed(dir)){
						this.checkpointsPassed++; // TODO: Change checkpoint texture to turned on
					}
				}
			}
		} while (this.world.containsPoint(cx, cy) && dir != -1 && this.points.size() < 25);

		Point2D lastPoint = this.points.get(this.points.size()-1);
		this.points.remove(this.points.size()-1);
		this.points.add(Util.reducePoint(lastPoint, dir == -1 ? lastDir : dir));

		for (Light light : this.world.getLights()){
			if (cx == light.getX() && cy == light.getY()){
				Tile tile = this.world.getTileAt(cx, cy);
				if (tile == null || lastDir == ((Rotatable)tile).getRotation()){
					light.setOn(true);
				}
			}
		}
	}

	private void renderLaser(GraphicsContext gc){
		gc.setStroke(Color.RED);
		gc.setLineWidth(3);
		for (int i = 0; i < this.points.size()-1; i++){
			Point2D a = this.points.get(i);
			Point2D b = this.points.get(i+1);
			gc.strokeLine(a.getX()*Tile.SIZE, a.getY()*Tile.SIZE, b.getX()*Tile.SIZE, b.getY()*Tile.SIZE);
		}
		for (Laser laser : this.generatedLasers){
			laser.renderLaser(gc);
		}
	}

	public void render(GraphicsContext gc){
		gc.setFill(Color.RED);
		final double size = Tile.SIZE/5;
		gc.fillOval(this.x*Tile.SIZE+(Tile.SIZE-size)/2, this.y*Tile.SIZE+(Tile.SIZE-size)/2, size, size);

		// Render the laser
		renderLaser(gc);
	}

	public void rotate90(){
		this.direction = (this.direction+1) % 4;
	}

	public int getCheckpointsPassed(){
		int sum = this.checkpointsPassed;
		for (Laser laser : this.generatedLasers){
			sum += laser.getCheckpointsPassed();
		}
		return sum;
	}
}