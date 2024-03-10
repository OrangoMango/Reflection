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
	private Laser parent;
	private int[][] map;

	public Laser(Laser parent, World world, int x, int y, int d){
		this.parent = parent;
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
		boolean loopFound = false;
		this.checkpointsPassed = 0;
		this.points.clear();
		this.generatedLasers.clear();
		this.points.add(new Point2D(cx+0.5, cy+0.5));
		this.map = new int[this.world.getWidth()][this.world.getHeight()];
		do {
			this.map[cx][cy] |= Util.convertDirection(lastDir);
			int[] t = Util.getDirection(dir);
			cx += t[0];
			cy += t[1];
			this.points.add(new Point2D(cx+0.5, cy+0.5));
			if (loopFound) break;
			Tile tile = this.world.getTileAt(cx, cy);
			if (tile != null){
				tile.hasLaser = true;
				if (getRoot().containsLaser(cx, cy, Util.convertDirection(dir))){
					loopFound = true;
				} else {
					Laser gen = tile.generateLaser(this, this.world, dir);
					if (gen != null){
						this.generatedLasers.add(gen);
					}
				}
				lastDir = dir;
				dir = tile.updateDirection(dir);

				if (tile instanceof Checkpoint){
					if (((Checkpoint)tile).laserPassed(dir)){
						this.checkpointsPassed++;
						((Checkpoint)tile).setActivated(true);
					}
				}
			}
		} while (this.world.containsPoint(cx, cy) && dir != -1);

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

	private Laser getRoot(){
		return this.parent == null ? this : this.parent.getRoot();
	}

	private boolean containsLaser(int lx, int ly, int ld){
		if ((this.map[lx][ly] & ld) == ld){
			System.out.println(lx+" "+ly+" "+this.map[lx][ly]);
			return true;
		} else {
			for (Laser laser : this.generatedLasers){
				if (laser.containsLaser(lx, ly, ld)){
					return true;
				}
			}
			return false;
		}
	}

	private void renderLaser(GraphicsContext gc){
		gc.setStroke(Color.RED);
		gc.setLineWidth(1.5);
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