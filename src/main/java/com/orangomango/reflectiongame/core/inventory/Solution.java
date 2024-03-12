package com.orangomango.reflectiongame.core.inventory;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import com.orangomango.reflectiongame.core.*;

public class Solution{
	private ArrayList<World> solutions = new ArrayList<>();

	public Solution(){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/misc/solutions.data")));
			String line;
			World cWorld = null;
			boolean firstProp = true;
			ArrayList<String> matrix = new ArrayList<>();
			while ((line = reader.readLine()) != null){
				if (!line.startsWith("[") && !line.isBlank()){
					if (line.equals("end")){
						this.solutions.add(cWorld);
						matrix.clear();
						firstProp = true;
						cWorld = null;
					} else {
						if (line.startsWith("- ")){
							if (firstProp){
								cWorld = new World(matrix, new ArrayList<>(), new Inventory(null));
								firstProp = false;
							}
							int lx = Integer.parseInt(line.substring(2).split(" ")[0]);
							int ly = Integer.parseInt(line.substring(2).split(" ")[1]);
							int mode = Integer.parseInt(line.substring(2).split(" ")[3]);
							if (line.substring(2).split(" ")[2].equals("rot")){
								Rotatable rot = (Rotatable)cWorld.getTileAt(lx, ly);
								for (int i = 0; i < mode; i++){
									rot.rotate90();
								}
							} else if (line.substring(2).split(" ")[2].equals("flip")){
								Flippable flip = (Flippable)cWorld.getTileAt(lx, ly);
								if (mode == 1){
									flip.flip();
								}
							}
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

	public Tile getHint(World currentWorld, int levelNumber){
		World solution = this.solutions.get(levelNumber);
		Tile output = null;
		do {
			output = getRandomTile(solution);
		} while (isCorrect(output, currentWorld));
		return output;
	}

	private Tile getRandomTile(World world){
		Random random = new Random();
		Tile tile = null;
		do {
			int px = random.nextInt(world.getWidth());
			int py = random.nextInt(world.getHeight());
			tile = world.getTileAt(px, py);
		} while (tile.getId() == 0); // Block tiles are not included
		return tile;
	}

	private boolean isCorrect(Tile tile, World world){
		Tile placed = world.getTileAt(tile.getX(), tile.getY());
		if (placed.getId() == tile.getId()){
			if (placed instanceof Rotatable){
				return ((Rotatable)placed).getRotation() == ((Rotatable)tile).getRotation();
			} else if (placed instanceof Flippable){
				return ((Flippable)placed).isFlipped() == ((Flippable)tile).isFlipped();
			}
		}
		return false;
	}
}