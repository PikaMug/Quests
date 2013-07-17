package me.blackvein.quests;

import java.util.HashMap;
import java.util.Scanner;

public class ItemData {

	public HashMap<String, Data> map = new HashMap<String, Data>();
	private static ItemData instance = null;
	
	public static ItemData getInstance() {
		if (instance != null) {
			return instance;
		} else {
			instance = new ItemData();
			return instance;
		}
	}
	
	private ItemData() {
		Scanner scanner = new Scanner(this.getClass().getClassLoader().getResourceAsStream("items.txt"));
		
		while(scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("=");
			if (line.length > 1) {
				String[] name = line[0].split(",");
				Data data;
				
				String[] str = line[1].split(",");
				
				int id = -1;
				byte mdata = 0;
				try {
					id = Integer.parseInt(str[0]);
				} catch (Exception e) {
				}
				if (str.length > 1) {
					mdata = Byte.parseByte(str[1]);
				}
				
				for (String n : name) {
					data = new Data(n, id, mdata);
					map.put(n, data);
				}
			}
		}
	}
	public Data getItem(String input) {
		if (map.containsKey(input)) {
			return map.get(input);
		} else {
			return null;
		}
	}
	
	public class Data {
		private String name_;
		private int id_;
		private byte data_;
		
		public Data(String name, int id, byte mdata) {
			this.name_ = name;
			this.id_ = id;
			this.data_ = mdata;
		}

		public int getId() {
			return id_;
		}
		
		public byte getData() {
			return data_;
		}
	}
}
