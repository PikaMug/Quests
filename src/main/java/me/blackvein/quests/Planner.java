package me.blackvein.quests;

public class Planner {
	public String start = null;
	public String end = null;
	public long repeat = -1;
	public long cooldown = -1;
	
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public long getRepeat() {
		return repeat;
	}
	public void setRepeat(long repeat) {
		this.repeat = repeat;
	}
	public long getCooldown() {
		return cooldown;
	}
	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
}