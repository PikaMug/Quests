package me.pikamug.quests.convo.quests;

import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.quests.QuestFactory;
import net.minecraft.ChatFormatting;
import org.browsit.conversations.api.Conversations;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class FabricQuestsEditorIntegerPrompt extends FabricQuestsIntegerPrompt {
    private final UUID uuid;
    private QuestFactory factory;
    
    public FabricQuestsEditorIntegerPrompt(final @NotNull UUID uuid) {
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
    
    public abstract int getSize();
    
    public abstract String getTitle();
    
    public abstract ChatFormatting getNumberColor(int number);
    
    public abstract String getSelectionText(int number);
    
    public abstract String getAdditionalText(int number);
    
    public abstract @NotNull String getPromptText();
    
    public abstract void acceptInput(final Number input);

    public void start() {
        Conversations.create(uuid).title(getName()).prompt(getPromptText(), Integer.class, prompt -> prompt
                .attempts(99).conversionFailText(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput"))
                .converter(Integer::parseInt).fetch((input, sender) -> acceptInput(input))).start();
    }
}
