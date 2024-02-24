package com.orangomango.indiedev3.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

import java.util.ArrayList;

import com.orangomango.indiedev3.Util;

public class Laser{
	private int x, y;
	private int direction;
	private ArrayList<Point2D> points = new ArrayList<>();
	private ArrayList<Laser> generatedLasers = new ArrayList<>();
	private World world;

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
				dir = tile.updateDirection(dir);
			}
		} while (this.world.containsPoint(cx, cy));
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
		gc.fillOval(this.x*Tile.SIZE+Tile.SIZE/3, this.y*Tile.SIZE+Tile.SIZE/3, Tile.SIZE/3, Tile.SIZE/3);

		// Render the laser
		renderLaser(gc);
	}
}