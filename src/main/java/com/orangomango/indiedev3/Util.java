package com.orangomango.indiedev3;

public class Util{
	public static final int DIRECTION_N = 0;
	public static final int DIRECTION_E = 1;
	public static final int DIRECTION_S = 2;
	public static final int DIRECTION_W = 3;

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