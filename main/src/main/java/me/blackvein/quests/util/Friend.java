package me.blackvein.quests.util;

import me.blackvein.quests.Quester;

public class Friend {
	private Quester quester;
	private FriendType type;
	
	public Friend(Quester quester, FriendType type) {
		this.quester = quester;
		this.type = type;
	}
	
	public Quester getQuester() {
		return quester;
	}
	
	public void setQuester(Quester quester) {
		this.quester = quester;
	}
	
	public FriendType getType() {
		return type;
	}
	
	public void setType(FriendType type) {
		this.type = type;
	}
	
	public enum FriendType {
		DUNGEONXL, PARTIES;
	}
}
