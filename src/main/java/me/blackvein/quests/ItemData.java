package me.blackvein.quests;

import org.bukkit.Material;

public class ItemData {

	public static Material getMaterial(String name) {
		Material material = Material.matchMaterial(name);
		if (material == null) {
			name = name.toUpperCase().replace(" ", "_");
			for (Material mat : Material.values()) {
				if (mat.toString().contains(name)) {
					return mat;
				}
			}
		}
		return material;
	}
}
