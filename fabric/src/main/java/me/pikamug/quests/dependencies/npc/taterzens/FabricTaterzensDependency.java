package me.pikamug.quests.dependencies.npc.taterzens;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.dependencies.npc.FabricEntityNpcDependency;
import me.pikamug.quests.listeners.npc.FabricTaterzensListener;

public class FabricTaterzensDependency extends FabricEntityNpcDependency {

    public FabricTaterzensDependency(final FabricQuestsPlugin plugin) {
        super(plugin, "Taterzens",
                "org.samo_lego.taterzens.npc.TaterzenNPC",
                "org.policymc.taterzens.npc.TaterzenNPC");
        new FabricTaterzensListener(plugin, this);
    }
}
