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

public class FabricConfigSettings implements ConfigSettings {

    private final FabricQuestsPlugin plugin;
    private int acceptTimeout = 20;
    private boolean allowCommands = true;
    private boolean allowCommandsForNpcQuests = false;
    private boolean allowPranks = true;
    private boolean clickablePrompts = true;
    private int conditionInterval = 8;
    private boolean confirmAbandon = true;
    private boolean confirmAccept = true;
    private int consoleLogging = 1;
    private boolean disableCommandFeedback = true;
    private boolean genFilesOnJoin = true;
    private boolean giveJournalItem = false;
    private boolean ignoreLockedQuests = false;
    private int killDelay = 600;
    private String language = "en-US";
    private boolean languageOverrideClient = false;
    private int maxQuests = 0;
    private boolean npcEffects = true;
    private String effect = "note";
    private String redoEffect = "angry_villager";
    private boolean preventExploit = true;
    private boolean showCompletedObjs = true;
    private boolean showQuestReqs = true;
    private boolean showQuestTitles = true;
    private int strictPlayerMovement = 0;
    private boolean trialSave = false;
    private int topLimit = 150;
    private boolean translateNames = true;
    private boolean translateSubCommands = false;
    private boolean updateCheck = true;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private JsonObject config;

    public FabricConfigSettings(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public int getAcceptTimeout() {
        return acceptTimeout;
    }
    public void setAcceptTimeout(final int acceptTimeout) {
        this.acceptTimeout = acceptTimeout;
    }
    public boolean canAllowCommands() {
        return allowCommands;
    }
    public void setAllowCommands(final boolean allowCommands) {
        this.allowCommands = allowCommands;
    }
    public boolean canAllowCommandsForNpcQuests() {
        return allowCommandsForNpcQuests;
    }
    public void setAllowCommandsForNpcQuests(final boolean allowCommandsForNpcQuests) {
        this.allowCommandsForNpcQuests = allowCommandsForNpcQuests;
    }
    public boolean canAllowPranks() {
        return allowPranks;
    }
    public void setAllowPranks(final boolean allowPranks) {
        this.allowPranks = allowPranks;
    }
    public boolean canClickablePrompts() {
        return clickablePrompts;
    }
    public void setClickablePrompts(boolean clickablePrompts) {
        this.clickablePrompts = clickablePrompts;
    }
    public int getConditionInterval() {
        return conditionInterval;
    }
    public void setConditionInterval(final int conditionInterval) {
        this.conditionInterval = conditionInterval;
    }
    public boolean canConfirmAbandon() {
        return confirmAbandon;
    }
    public void setConfirmAbandon(final boolean confirmAbandon) {
        this.confirmAbandon = confirmAbandon;
    }
    public boolean canConfirmAccept() {
        return confirmAccept;
    }
    public void setConfirmAccept(final boolean confirmAccept) {
        this.confirmAccept = confirmAccept;
    }
    public int getConsoleLogging() {
        return consoleLogging;
    }
    public void setConsoleLogging(final int consoleLogging) {
        this.consoleLogging = consoleLogging;
    }
    public boolean canDisableCommandFeedback() {
        return disableCommandFeedback;
    }
    public void setDisableCommandFeedback(final boolean disableCommandFeedback) {
        this.disableCommandFeedback = disableCommandFeedback;
    }
    public boolean canGenFilesOnJoin() {
        return genFilesOnJoin;
    }
    public void setGenFilesOnJoin(final boolean genFilesOnJoin) {
        this.genFilesOnJoin = genFilesOnJoin;
    }
    public boolean canGiveJournalItem() {
        return giveJournalItem;
    }
    public void setGiveJournalItem(final boolean giveJournalItem) {
        this.giveJournalItem = giveJournalItem;
    }
    public boolean canIgnoreLockedQuests() {
        return ignoreLockedQuests;
    }
    public void setIgnoreLockedQuests(final boolean ignoreLockedQuests) {
        this.ignoreLockedQuests = ignoreLockedQuests;
    }
    public int getKillDelay() {
        return killDelay;
    }
    public void setKillDelay(final int killDelay) {
        this.killDelay = killDelay;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(final String language) {
        this.language = language;
    }
    public boolean canLanguageOverrideClient() {
        return languageOverrideClient;
    }
    public void setLanguageOverrideClient(final boolean languageOverrideClient) {
        this.languageOverrideClient = languageOverrideClient;
    }
    public int getMaxQuests() {
        return maxQuests;
    }
    public void setMaxQuests(final int maxQuests) {
        this.maxQuests = maxQuests;
    }
    public boolean canNpcEffects() {
        return npcEffects;
    }
    public void setNpcEffects(final boolean npcEffects) {
        this.npcEffects = npcEffects;
    }
    public String getEffect() {
        return effect;
    }
    public void setEffect(final String effect) {
        this.effect = effect;
    }
    public String getRedoEffect() {
        return redoEffect;
    }
    public void setRedoEffect(final String redoEffect) {
        this.redoEffect = redoEffect;
    }
    public boolean canPreventExploit() {
        return preventExploit;
    }
    public void setPreventExploit(final boolean preventExploit) {
        this.preventExploit = preventExploit;
    }
    public boolean canShowCompletedObjs() {
        return showCompletedObjs;
    }
    public void setShowCompletedObjs(final boolean showCompletedObjs) {
        this.showCompletedObjs = showCompletedObjs;
    }
    public boolean canShowQuestReqs() {
        return showQuestReqs;
    }
    public void setShowQuestReqs(final boolean showQuestReqs) {
        this.showQuestReqs = showQuestReqs;
    }
    public boolean canShowQuestTitles() {
        return showQuestTitles;
    }
    public void setShowQuestTitles(final boolean showQuestTitles) {
        this.showQuestTitles = showQuestTitles;
    }
    public int getStrictPlayerMovement() {
        return strictPlayerMovement;
    }
    public void setStrictPlayerMovement(final int strictPlayerMovement) {
        this.strictPlayerMovement = strictPlayerMovement;
    }
    public boolean canTrialSave() {
        return trialSave;
    }
    public void setTrialSave(final boolean trialSave) {
        this.trialSave = trialSave;
    }
    public int getTopLimit() {
        return topLimit;
    }
    public void setTopLimit(final int topLimit) {
        this.topLimit = topLimit;
    }
    public boolean canTranslateNames() {
        return translateNames;
    }
    public void setTranslateNames(final boolean translateItems) {
        this.translateNames = translateItems;
    }
    public boolean canTranslateSubCommands() {
        return translateSubCommands;
    }
    public void setTranslateSubCommands(final boolean translateSubCommands) {
        this.translateSubCommands = translateSubCommands;
    }
    public boolean canUpdateCheck() {
        return updateCheck;
    }
    public void setUpdateCheck(final boolean updateCheck) {
        this.updateCheck = updateCheck;
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
        if (config == null) return;
        acceptTimeout = getInt("acceptTimeout", acceptTimeout);
        allowCommands = getBool("allowCommands", allowCommands);
        allowCommandsForNpcQuests = getBool("allowCommandsForNpcQuests", allowCommandsForNpcQuests);
        allowPranks = getBool("allowPranks", allowPranks);
        clickablePrompts = getBool("clickablePrompts", clickablePrompts);
        conditionInterval = getInt("conditionInterval", conditionInterval);
        confirmAbandon = getBool("confirmAbandon", confirmAbandon);
        confirmAccept = getBool("confirmAccept", confirmAccept);
        if (conditionInterval < 3 || conditionInterval > 180) {
            plugin.getPluginLogger().warn("Condition interval out of range, reverting to default of 8 seconds");
            conditionInterval = 8;
        }
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
        try (final Writer writer = Files.newBufferedWriter(configFile.toPath())) {
            gson.toJson(defaults, writer);
        } catch (final IOException e) {
            plugin.getPluginLogger().error("Failed to create default config.json", e);
        }
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
}
