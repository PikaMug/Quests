/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests;

import me.blackvein.quests.util.Lang;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Settings {
    
    private final Quests plugin;
    private int acceptTimeout = 20;
    private boolean allowCommands = true;
    private boolean allowCommandsForNpcQuests = false;
    private boolean allowPranks = true;
    private boolean askConfirmation = true;
    private boolean clickablePrompts = true;
    private int consoleLogging = 1;
    private boolean disableCommandFeedback = true;
    private boolean genFilesOnJoin = true;
    private boolean ignoreLockedQuests = false;
    private int killDelay = 0;
    private int maxQuests = 0;
    private boolean npcEffects = true;
    private String effect = "note";
    private String redoEffect = "angry_villager";
    private boolean showQuestReqs = true;
    private boolean showQuestTitles = true;
    private int strictPlayerMovement = 0;
    private boolean trialSave = true;
    private int topLimit = 150;
    private boolean translateNames = false;
    private boolean translateSubCommands = false;
    private boolean updateCheck = true;
    
    public Settings(final Quests plugin) {
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
    public boolean canAskConfirmation() {
        return askConfirmation;
    }
    public void setAskConfirmation(final boolean askConfirmation) {
        this.askConfirmation = askConfirmation;
    }
    public boolean canClickablePrompts() {
        return clickablePrompts;
    }
    public void setClickablePrompts(boolean clickablePrompts) {
        this.clickablePrompts = clickablePrompts;
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
    
    public void init() {
        final FileConfiguration config = plugin.getConfig();
        acceptTimeout = config.getInt("accept-timeout", 20);
        allowCommands = config.getBoolean("allow-command-questing", true);
        allowCommandsForNpcQuests = config.getBoolean("allow-command-quests-with-npcs", false);
        allowPranks = config.getBoolean("allow-pranks", true);
        askConfirmation = config.getBoolean("ask-confirmation", true);
        clickablePrompts = config.getBoolean("clickable-prompts", true);
        consoleLogging = config.getInt("console-logging", 1);
        disableCommandFeedback = config.getBoolean("disable-command-feedback", true);
        genFilesOnJoin = config.getBoolean("generate-files-on-join", true);
        ignoreLockedQuests = config.getBoolean("ignore-locked-quests", false);
        killDelay = config.getInt("kill-delay", 600);
        if (Objects.requireNonNull(config.getString("language")).equalsIgnoreCase("en")) {
            //Legacy
            Lang.setISO("en-US");
        } else {
            Lang.setISO(config.getString("language", "en-US"));
        }
        maxQuests = config.getInt("max-quests", maxQuests);
        npcEffects = config.getBoolean("npc-effects.enabled", true);
        effect = config.getString("npc-effects.new-quest", "note");
        redoEffect = config.getString("npc-effects.redo-quest", "angry_villager");
        showQuestReqs = config.getBoolean("show-requirements", true);
        showQuestTitles = config.getBoolean("show-titles", true);
        strictPlayerMovement = config.getInt("strict-player-movement", 0);
        trialSave = config.getBoolean("trial-save", false);
        topLimit = config.getInt("top-limit", 150);
        translateNames = config.getBoolean("translate-names", true);
        translateSubCommands = config.getBoolean("translate-subcommands", false);
        updateCheck = config.getBoolean("update-check", true);
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
