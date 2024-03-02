package com.orangomango.reflectiongame;

import javafx.geometry.Point2D;

public class Util{
	public static final int DIRECTION_N = 0;
	public static final int DIRECTION_E = 1;
	public static final int DIRECTION_S = 2;
	public static final int DIRECTION_W = 3;

	public static void playSound(String name){
		AssetLoader.getInstance().getAudio(name).play();
	}

	public static void schedule(Runnable r, int delay){
		new Thread(() -> {
			try {
				Thread.sleep(delay);
				r.run();
			} catch (InterruptedException ex){
				ex.printStackTrace();
			}
		}).start();
	}

	public static Point2D reducePoint(Point2D point, int dir){
		switch (dir){
			case DIRECTION_N:
				return new Point2D(point.getX(), point.getY()+0.5);
			case DIRECTION_E:
				return new Point2D(point.getX()-0.5, point.getY());
			case DIRECTION_S:
				return new Point2D(point.getX(), point.getY()-0.5);
			case DIRECTION_W:
				return new Point2D(point.getX()+0.5, point.getY());
			default:
				return null;
		}
	}

	public static int[] getDirection(int d){
		switch (d){
			case DIRECTION_N:
				return new int[]{0, -1};
			case DIRECTION_E:
				return new int[]{1, 0};
			case DIRECTION_S:
				return new int[]{0, 1};
			case DIRECTION_W:
				return new int[]{-1, 0};
			default:
				return null;
		}
	}
}