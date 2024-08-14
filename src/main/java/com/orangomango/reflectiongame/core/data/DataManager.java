package com.orangomango.reflectiongame.core.data;

import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.orangomango.reflectiongame.core.*;
import com.orangomango.reflectiongame.core.inventory.Inventory;

public class DataManager{
	private File gameDataFolder;
	private int currentWorldId;

	public DataManager(String folderName){
		File dir = new File(System.getProperty("user.home"), folderName);
		this.gameDataFolder = dir;
		if (dir.exists()){
			int maxId = 0;
			for (File file : dir.listFiles()){
				if (file.getName().endsWith(".world")){
					int wId = Integer.parseInt(file.getName().split("\\.world")[0].split("-")[1]);
					if (wId > maxId){
						maxId = wId;
					}
				}
			}
			this.currentWorldId = maxId;
		} else {
			dir.mkdir();
		}
	}

	public void saveWorld(World world){
		String worldName = "custom_level-"+this.currentWorldId;

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.gameDataFolder, worldName+".world")));
			writer.write("["+worldName+"]");
			writer.newLine();
			writer.write(buildInventory(world, world.getInventory().getTargets()));
			writer.newLine();
			writer.write(world.toString());
			writer.newLine();
			writer.write("end");
			writer.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}

		this.currentWorldId++;
	}

	private String buildInventory(World world, int targets){
		StringBuilder builder = new StringBuilder();
		HashMap<Integer, Integer> items = new HashMap<>();
		builder.append("#"+targets+" ");

		for (int x = 0; x < world.getWidth(); x++){
			for (int y = 0; y < world.getHeight(); y++){
				Tile tile = world.getTileAt(x, y);
				if (tile.getId() != 0 && tile.isMarked()){
					switch (tile.getId()){
						case 1:
							items.put(2, items.getOrDefault(2, 0)+1);
							break;
						case 2:
							items.put(1, items.getOrDefault(1, 0)+1);
							break;
						case 3:
							items.put(3, items.getOrDefault(3, 0)+1);
							break;
						case 4:
							items.put(0, items.getOrDefault(0, 0)+1);
							break;
						case 5:
							items.put(4, items.getOrDefault(4, 0)+1);
							break;
						case 6:
							items.put(5, items.getOrDefault(5, 0)+1);
							break;
					}
				}
			}
		}

		for (Map.Entry<Integer, Integer> item : items.entrySet()){
			builder.append(item.getKey()+";"+item.getValue()+" ");
		}

		return builder.toString();
	}

	public static ArrayList<Pair<World, Laser>> loadWorlds(InputStream inputStream){
		ArrayList<Pair<World, Laser>> worlds = new ArrayList<>();
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			World cWorld = null;
			Laser cLaser = null;
			Inventory cInv = null;
			ArrayList<Light> lights = new ArrayList<>();
			ArrayList<String> matrix = new ArrayList<>();
			while ((line = reader.readLine()) != null){
				if (!line.startsWith("[") && !line.isBlank()){
					if (line.equals("end")){
						worlds.add(new Pair<World, Laser>(cWorld, cLaser));
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
								throw new RuntimeException("Tile can't be rotated/flipped: "+tx+" "+ty);
							}
						} else if (line.startsWith("#")){
							cInv = new Inventory(line);
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

		return worlds;
	}

	public File getGameDataFolder(){
		return this.gameDataFolder;
	}
}