package me.pikamug.quests.module;

import org.bukkit.entity.Player;

import java.util.Map;

public interface CustomRequirement {

    String getModuleName();

    @SuppressWarnings("unused")
    Map.Entry<String, Short> getModuleItem();

    String getName();

    void setName(final String name);

    String getAuthor();

    void setAuthor(final String author);

    String getDisplay();

    void setDisplay(final String display);

    Map.Entry<String, Short> getItem();

    void setItem(final String type, final short durability);

    Map<String, Object> getData();

    /**
     * Add a new prompt<p>
     *
     * Note that the "defaultValue" Object will be cast to a String internally
     *
     * @param title Prompt name
     * @param description Description of expected input
     * @param defaultValue Value to be used if input is not received
     */
    void addStringPrompt(final String title, final String description, final Object defaultValue);

    Map<String, String> getDescriptions();

    boolean testRequirement(Player p, Map<String, Object> m);
}
