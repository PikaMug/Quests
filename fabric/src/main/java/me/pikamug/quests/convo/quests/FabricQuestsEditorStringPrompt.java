package me.pikamug.quests.convo.quests;

import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.quests.QuestFactory;
import net.minecraft.ChatFormatting;
import org.browsit.conversations.api.Conversations;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class FabricQuestsEditorStringPrompt extends FabricQuestsStringPrompt {
    private final UUID uuid;
    private QuestFactory factory;
    
    public FabricQuestsEditorStringPrompt(final @NotNull UUID uuid) {
        this.uuid = uuid;
        this.factory = FabricQuestsPlugin.getInstance().getQuestFactory();
    }
    
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
    
    public UUID getUniqueId() {
        return uuid;
    }
    
    public QuestFactory getQuestFactory() {
        return factory;
    }
    
    public abstract String getTitle();
    
    public abstract String getQueryText();
    
    public abstract @NotNull String getPromptText();
    
    public abstract void acceptInput(String input);

    public void start() {
        Conversations.create(uuid).title(getName()).prompt(getPromptText(), String.class, prompt -> prompt
                .attempts(99).conversionFailText(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput"))
                .converter(String::valueOf).fetch((input, sender) -> acceptInput(input))).start();
    }
}
