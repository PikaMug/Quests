/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.pikamug.quests.FabricQuestsPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfigSettings implements ConfigSettings {

    private final FabricQuestsPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private JsonObject config;

    private int acceptTimeout = 60;
    private boolean allowCommands = true;
    private boolean allowCommandsForNpcQuests = true;
    private boolean allowPranks = false;
    private boolean clickablePrompts = true;
    private int conditionInterval = 1;
    private boolean confirmAbandon = true;
    private boolean confirmAccept = true;
    private int consoleLogging = 2;
    private boolean disableCommandFeedback = true;
    private boolean genFilesOnJoin = true;
    private boolean giveJournalItem = true;
    private boolean ignoreLockedQuests = false;
    private int killDelay = 30;
    private String language = "en-US";
    private boolean languageOverrideClient = false;
    private int maxQuests = 10;
    private boolean npcEffects = true;
    private String effect = "ENCHANTMENT_TABLE";
    private String redoEffect = "FLAME";
    private boolean preventExploit = true;
    private boolean showCompletedObjs = true;
    private boolean showQuestReqs = true;
    private boolean showQuestTitles = true;
    private int strictPlayerMovement = 0;
    private boolean trialSave = false;
    private int topLimit = 10;
    private boolean translateNames = true;
    private boolean translateSubCommands = true;
    private boolean updateCheck = true;

    public FabricConfigSettings(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        final File configFile = new File(plugin.getPluginDataFolder(), "config.json");
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }
        try (Reader reader = Files.newBufferedReader(configFile.toPath())) {
            config = gson.fromJson(reader, JsonObject.class);
        } catch (final IOException e) {
            plugin.getPluginLogger().error("Failed to load config.json", e);
            config = new JsonObject();
        }
        loadValues();
    }

    private void createDefaultConfig(final File configFile) {
        final JsonObject defaults = new JsonObject();
        defaults.addProperty("acceptTimeout", acceptTimeout);
        defaults.addProperty("allowCommands", allowCommands);
        defaults.addProperty("allowCommandsForNpcQuests", allowCommandsForNpcQuests);
        defaults.addProperty("allowPranks", allowPranks);
        defaults.addProperty("clickablePrompts", clickablePrompts);
        defaults.addProperty("conditionInterval", conditionInterval);
        defaults.addProperty("confirmAbandon", confirmAbandon);
        defaults.addProperty("confirmAccept", confirmAccept);
        defaults.addProperty("consoleLogging", consoleLogging);
        defaults.addProperty("disableCommandFeedback", disableCommandFeedback);
        defaults.addProperty("genFilesOnJoin", genFilesOnJoin);
        defaults.addProperty("giveJournalItem", giveJournalItem);
        defaults.addProperty("ignoreLockedQuests", ignoreLockedQuests);
        defaults.addProperty("killDelay", killDelay);
        defaults.addProperty("language", language);
        defaults.addProperty("languageOverrideClient", languageOverrideClient);
        defaults.addProperty("maxQuests", maxQuests);
        defaults.addProperty("npcEffects", npcEffects);
        defaults.addProperty("effect", effect);
        defaults.addProperty("redoEffect", redoEffect);
        defaults.addProperty("preventExploit", preventExploit);
        defaults.addProperty("showCompletedObjs", showCompletedObjs);
        defaults.addProperty("showQuestReqs", showQuestReqs);
        defaults.addProperty("showQuestTitles", showQuestTitles);
        defaults.addProperty("strictPlayerMovement", strictPlayerMovement);
        defaults.addProperty("trialSave", trialSave);
        defaults.addProperty("topLimit", topLimit);
        defaults.addProperty("translateNames", translateNames);
        defaults.addProperty("translateSubCommands", translateSubCommands);
        defaults.addProperty("updateCheck", updateCheck);
        try (Writer writer = Files.newBufferedWriter(configFile.toPath())) {
            gson.toJson(defaults, writer);
        } catch (final IOException e) {
            plugin.getPluginLogger().error("Failed to create default config.json", e);
        }
    }

    private void loadValues() {
        if (config == null) return;
        acceptTimeout = getInt("acceptTimeout", acceptTimeout);
        allowCommands = getBool("allowCommands", allowCommands);
        allowCommandsForNpcQuests = getBool("allowCommandsForNpcQuests", allowCommandsForNpcQuests);
        allowPranks = getBool("allowPranks", allowPranks);
        clickablePrompts = getBool("clickablePrompts", clickablePrompts);
        conditionInterval = getInt("conditionInterval", conditionInterval);
        confirmAbandon = getBool("confirmAbandon", confirmAbandon);
        confirmAccept = getBool("confirmAccept", confirmAccept);
        consoleLogging = getInt("consoleLogging", consoleLogging);
        disableCommandFeedback = getBool("disableCommandFeedback", disableCommandFeedback);
        genFilesOnJoin = getBool("genFilesOnJoin", genFilesOnJoin);
        giveJournalItem = getBool("giveJournalItem", giveJournalItem);
        ignoreLockedQuests = getBool("ignoreLockedQuests", ignoreLockedQuests);
        killDelay = getInt("killDelay", killDelay);
        language = getString("language", language);
        languageOverrideClient = getBool("languageOverrideClient", languageOverrideClient);
        maxQuests = getInt("maxQuests", maxQuests);
        npcEffects = getBool("npcEffects", npcEffects);
        effect = getString("effect", effect);
        redoEffect = getString("redoEffect", redoEffect);
        preventExploit = getBool("preventExploit", preventExploit);
        showCompletedObjs = getBool("showCompletedObjs", showCompletedObjs);
        showQuestReqs = getBool("showQuestReqs", showQuestReqs);
        showQuestTitles = getBool("showQuestTitles", showQuestTitles);
        strictPlayerMovement = getInt("strictPlayerMovement", strictPlayerMovement);
        trialSave = getBool("trialSave", trialSave);
        topLimit = getInt("topLimit", topLimit);
        translateNames = getBool("translateNames", translateNames);
        translateSubCommands = getBool("translateSubCommands", translateSubCommands);
        updateCheck = getBool("updateCheck", updateCheck);
    }

    private int getInt(String key, int def) {
        return config.has(key) && config.get(key).isJsonPrimitive() ? config.get(key).getAsInt() : def;
    }

    private boolean getBool(String key, boolean def) {
        return config.has(key) && config.get(key).isJsonPrimitive() ? config.get(key).getAsBoolean() : def;
    }

    private String getString(String key, String def) {
        return config.has(key) && config.get(key).isJsonPrimitive() ? config.get(key).getAsString() : def;
    }

    private void setAndSave(String key, Object value) {
        config.add(key, gson.toJsonTree(value));
        final File configFile = new File(plugin.getPluginDataFolder(), "config.json");
        try (Writer writer = Files.newBufferedWriter(configFile.toPath())) {
            gson.toJson(config, writer);
        } catch (final IOException e) {
            plugin.getPluginLogger().error("Failed to save config.json", e);
        }
    }

    @Override public int getAcceptTimeout() { return acceptTimeout; }
    @Override public void setAcceptTimeout(int v) { acceptTimeout = v; setAndSave("acceptTimeout", v); }
    @Override public boolean canAllowCommands() { return allowCommands; }
    @Override public void setAllowCommands(boolean v) { allowCommands = v; setAndSave("allowCommands", v); }
    @Override public boolean canAllowCommandsForNpcQuests() { return allowCommandsForNpcQuests; }
    @Override public void setAllowCommandsForNpcQuests(boolean v) { allowCommandsForNpcQuests = v; setAndSave("allowCommandsForNpcQuests", v); }
    @Override public boolean canAllowPranks() { return allowPranks; }
    @Override public void setAllowPranks(boolean v) { allowPranks = v; setAndSave("allowPranks", v); }
    @Override public boolean canClickablePrompts() { return clickablePrompts; }
    @Override public void setClickablePrompts(boolean v) { clickablePrompts = v; setAndSave("clickablePrompts", v); }
    @Override public int getConditionInterval() { return conditionInterval; }
    @Override public void setConditionInterval(int v) { conditionInterval = v; setAndSave("conditionInterval", v); }
    @Override public boolean canConfirmAbandon() { return confirmAbandon; }
    @Override public void setConfirmAbandon(boolean v) { confirmAbandon = v; setAndSave("confirmAbandon", v); }
    @Override public boolean canConfirmAccept() { return confirmAccept; }
    @Override public void setConfirmAccept(boolean v) { confirmAccept = v; setAndSave("confirmAccept", v); }
    @Override public int getConsoleLogging() { return consoleLogging; }
    @Override public void setConsoleLogging(int v) { consoleLogging = v; setAndSave("consoleLogging", v); }
    @Override public boolean canDisableCommandFeedback() { return disableCommandFeedback; }
    @Override public void setDisableCommandFeedback(boolean v) { disableCommandFeedback = v; setAndSave("disableCommandFeedback", v); }
    @Override public boolean canGenFilesOnJoin() { return genFilesOnJoin; }
    @Override public void setGenFilesOnJoin(boolean v) { genFilesOnJoin = v; setAndSave("genFilesOnJoin", v); }
    @Override public boolean canGiveJournalItem() { return giveJournalItem; }
    @Override public void setGiveJournalItem(boolean v) { giveJournalItem = v; setAndSave("giveJournalItem", v); }
    @Override public boolean canIgnoreLockedQuests() { return ignoreLockedQuests; }
    @Override public void setIgnoreLockedQuests(boolean v) { ignoreLockedQuests = v; setAndSave("ignoreLockedQuests", v); }
    @Override public int getKillDelay() { return killDelay; }
    @Override public void setKillDelay(int v) { killDelay = v; setAndSave("killDelay", v); }
    @Override public String getLanguage() { return language; }
    @Override public void setLanguage(String v) { language = v; setAndSave("language", v); }
    @Override public boolean canLanguageOverrideClient() { return languageOverrideClient; }
    @Override public void setLanguageOverrideClient(boolean v) { languageOverrideClient = v; setAndSave("languageOverrideClient", v); }
    @Override public int getMaxQuests() { return maxQuests; }
    @Override public void setMaxQuests(int v) { maxQuests = v; setAndSave("maxQuests", v); }
    @Override public boolean canNpcEffects() { return npcEffects; }
    @Override public void setNpcEffects(boolean v) { npcEffects = v; setAndSave("npcEffects", v); }
    @Override public String getEffect() { return effect; }
    @Override public void setEffect(String v) { effect = v; setAndSave("effect", v); }
    @Override public String getRedoEffect() { return redoEffect; }
    @Override public void setRedoEffect(String v) { redoEffect = v; setAndSave("redoEffect", v); }
    @Override public boolean canPreventExploit() { return preventExploit; }
    @Override public void setPreventExploit(boolean v) { preventExploit = v; setAndSave("preventExploit", v); }
    @Override public boolean canShowCompletedObjs() { return showCompletedObjs; }
    @Override public void setShowCompletedObjs(boolean v) { showCompletedObjs = v; setAndSave("showCompletedObjs", v); }
    @Override public boolean canShowQuestReqs() { return showQuestReqs; }
    @Override public void setShowQuestReqs(boolean v) { showQuestReqs = v; setAndSave("showQuestReqs", v); }
    @Override public boolean canShowQuestTitles() { return showQuestTitles; }
    @Override public void setShowQuestTitles(boolean v) { showQuestTitles = v; setAndSave("showQuestTitles", v); }
    @Override public int getStrictPlayerMovement() { return strictPlayerMovement; }
    @Override public void setStrictPlayerMovement(int v) { strictPlayerMovement = v; setAndSave("strictPlayerMovement", v); }
    @Override public boolean canTrialSave() { return trialSave; }
    @Override public void setTrialSave(boolean v) { trialSave = v; setAndSave("trialSave", v); }
    @Override public int getTopLimit() { return topLimit; }
    @Override public void setTopLimit(int v) { topLimit = v; setAndSave("topLimit", v); }
    @Override public boolean canTranslateNames() { return translateNames; }
    @Override public void setTranslateNames(boolean v) { translateNames = v; setAndSave("translateNames", v); }
    @Override public boolean canTranslateSubCommands() { return translateSubCommands; }
    @Override public void setTranslateSubCommands(boolean v) { translateSubCommands = v; setAndSave("translateSubCommands", v); }
    @Override public boolean canUpdateCheck() { return updateCheck; }
    @Override public void setUpdateCheck(boolean v) { updateCheck = v; setAndSave("updateCheck", v); }
}
