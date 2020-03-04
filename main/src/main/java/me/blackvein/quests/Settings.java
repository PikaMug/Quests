/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings {
    
    private Quests plugin;
    private int acceptTimeout = 20;
    private boolean allowCommands = true;
    private boolean allowCommandsForNpcQuests = false;
    private boolean askConfirmation = true;
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
    private int topLimit = 150;
    private boolean translateNames = false;
    private boolean translateSubCommands = false;
    private boolean useGPS = true;
    
    public Settings(Quests plugin) {
        this.plugin = plugin;
    }
    
    public int getAcceptTimeout() {
        return acceptTimeout;
    }
    public void setAcceptTimeout(int acceptTimeout) {
        this.acceptTimeout = acceptTimeout;
    }
    public boolean canAllowCommands() {
        return allowCommands;
    }
    public void setAllowCommands(boolean allowCommands) {
        this.allowCommands = allowCommands;
    }
    public boolean canAllowCommandsForNpcQuests() {
        return allowCommandsForNpcQuests;
    }
    public void setAllowCommandsForNpcQuests(boolean allowCommandsForNpcQuests) {
        this.allowCommandsForNpcQuests = allowCommandsForNpcQuests;
    }
    public boolean canAskConfirmation() {
        return askConfirmation;
    }
    public void setAskConfirmation(boolean askConfirmation) {
        this.askConfirmation = askConfirmation;
    }
    public boolean canGenFilesOnJoin() {
        return genFilesOnJoin;
    }
    public void setGenFilesOnJoin(boolean genFilesOnJoin) {
        this.genFilesOnJoin = genFilesOnJoin;
    }
    public boolean canIgnoreLockedQuests() {
        return ignoreLockedQuests;
    }
    public void setIgnoreLockedQuests(boolean ignoreLockedQuests) {
        this.ignoreLockedQuests = ignoreLockedQuests;
    }
    public int getKillDelay() {
        return killDelay;
    }
    public void setKillDelay(int killDelay) {
        this.killDelay = killDelay;
    }
    public int getMaxQuests() {
        return maxQuests;
    }
    public void setMaxQuests(int maxQuests) {
        this.maxQuests = maxQuests;
    }
    public boolean canNpcEffects() {
        return npcEffects;
    }
    public void setNpcEffects(boolean npcEffects) {
        this.npcEffects = npcEffects;
    }
    public String getEffect() {
        return effect;
    }
    public void setEffect(String effect) {
        this.effect = effect;
    }
    public String getRedoEffect() {
        return redoEffect;
    }
    public void setRedoEffect(String redoEffect) {
        this.redoEffect = redoEffect;
    }
    public boolean canShowQuestReqs() {
        return showQuestReqs;
    }
    public void setShowQuestReqs(boolean showQuestReqs) {
        this.showQuestReqs = showQuestReqs;
    }
    public boolean canShowQuestTitles() {
        return showQuestTitles;
    }
    public void setShowQuestTitles(boolean showQuestTitles) {
        this.showQuestTitles = showQuestTitles;
    }
    public int getStrictPlayerMovement() {
        return strictPlayerMovement;
    }
    public void setStrictPlayerMovement(int strictPlayerMovement) {
        this.strictPlayerMovement = strictPlayerMovement;
    }
    public int getTopLimit() {
        return topLimit;
    }
    public void setTopLimit(int topLimit) {
        this.topLimit = topLimit;
    }
    public boolean canTranslateNames() {
        return translateNames;
    }
    public void setTranslateNames(boolean translateItems) {
        this.translateNames = translateItems;
    }
    public boolean canTranslateSubCommands() {
        return translateSubCommands;
    }
    public void setTranslateSubCommands(boolean translateSubCommands) {
        this.translateSubCommands = translateSubCommands;
    }
    public boolean canUseGPS() {
        return useGPS;
    }
    public void setUseGPS(boolean useGPS) {
        this.useGPS = useGPS;
    }
    
    public void init() {
        FileConfiguration config = plugin.getConfig();
        acceptTimeout = config.getInt("accept-timeout", 20);
        allowCommands = config.getBoolean("allow-command-questing", true);
        allowCommandsForNpcQuests = config.getBoolean("allow-command-quests-with-npcs", false);
        askConfirmation = config.getBoolean("ask-confirmation", true);
        genFilesOnJoin = config.getBoolean("generate-files-on-join", true);
        ignoreLockedQuests = config.getBoolean("ignore-locked-quests", false);
        killDelay = config.getInt("kill-delay", 600);
        if (config.getString("language").equalsIgnoreCase("en")) {
            //Legacy
            plugin.getLang().setISO("en-US");
        } else {
            plugin.getLang().setISO(config.getString("language", "en-US"));
        }
        maxQuests = config.getInt("max-quests", maxQuests);
        npcEffects = config.getBoolean("npc-effects.enabled", true);
        effect = config.getString("npc-effects.new-quest", "note");
        redoEffect = config.getString("npc-effects.redo-quest", "angry_villager");
        showQuestReqs = config.getBoolean("show-requirements", true);
        showQuestTitles = config.getBoolean("show-titles", true);
        strictPlayerMovement = config.getInt("strict-player-movement", 0);
        topLimit = config.getInt("top-limit", 150);
        translateNames = config.getBoolean("translate-names", true);
        translateSubCommands = config.getBoolean("translate-subcommands", false);
        useGPS = config.getBoolean("use-gps-plugin", true);
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
