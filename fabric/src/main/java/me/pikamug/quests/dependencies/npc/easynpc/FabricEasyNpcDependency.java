package me.pikamug.quests.dependencies.npc.easynpc;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.dependencies.npc.FabricEntityNpcDependency;
import me.pikamug.quests.listeners.npc.FabricEasyNpcListener;

public class FabricEasyNpcDependency extends FabricEntityNpcDependency {

    public FabricEasyNpcDependency(final FabricQuestsPlugin plugin) {
        super(plugin, "BOs-Easy-NPC",
                "de.markusbordihn.easynpc.entity.easynpc.EasyNPC",
                "de.markusbordihn.easynpc.entity.EasyNPCBaseEntity",
                "de.markusbordihn.easynpc.entity.easy.EasyNPCEntity");
        new FabricEasyNpcListener(plugin, this);
    }
}
