package com.orangomango.reflectiongame.core.inventory;

import java.util.HashMap;
import java.util.ArrayList;

public class Inventory{
	private int targets;
	private HashMap<Integer, Integer> items = new HashMap<>();
	private ArrayList<Integer> indices = new ArrayList<>();

	public Inventory(String data){
		String[] parts = data.split(" ");
		this.targets = Integer.parseInt(parts[0].substring(1));
		for (int i = 1; i < parts.length; i++){
			int type = Integer.parseInt(parts[i].split(";")[0]);
			int amount = Integer.parseInt(parts[i].split(";")[1]);
			this.items.put(type, amount);
			this.indices.add(type);
		}
	}

	public int mapIndexToType(int index){
		if (index == -1){
			return -1;
		} else {
			return this.indices.get(index);
		}
	}

	public boolean isEmpty(){
		return this.items.values().stream().filter(i -> i != 0).findAny().isEmpty();
	}

	public int getTargets(){
		return this.targets;
	}

	public HashMap<Integer, Integer> getItems(){
		return this.items;
	}
}