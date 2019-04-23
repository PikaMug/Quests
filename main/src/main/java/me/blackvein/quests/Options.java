package me.blackvein.quests;

public class Options {
	 private boolean useDungeonsXLPlugin = false;
	 private boolean usePartiesPlugin = true;
	 
	 public boolean getUseDungeonsXLPlugin() {
		 return useDungeonsXLPlugin;
	 }

	 public void setUseDungeonsXLPlugin(boolean useDungeonsXLPlugin) {
		 this.useDungeonsXLPlugin = useDungeonsXLPlugin;
	 }
	 
	 public boolean getUsePartiesPlugin() {
		 return usePartiesPlugin;
	 }

	 public void setUsePartiesPlugin(boolean usePartiesPlugin) {
		 this.usePartiesPlugin = usePartiesPlugin;
	 }
}