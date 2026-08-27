/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests.components;

public class FabricOptions implements Options {

    private boolean allowCommands = true;
    private boolean allowQuitting = true;
    private boolean ignoreSilkTouch = false;
    private String externalPartyPlugin;
    private boolean usePartiesPlugin = false;
    private int shareProgressLevel = 0;
    private boolean shareSameQuestOnly = false;
    private double shareDistance = 0;
    private boolean handleOfflinePlayers = false;
    private boolean ignoreBlockReplace = false;
    private boolean giveGloballyAtLogin = false;
    private boolean allowStackingGlobal = false;
    private boolean informOnStart = true;
    private boolean overrideMaxQuests = false;

    @Override public boolean canAllowCommands() { return allowCommands; }
    @Override public void setAllowCommands(boolean v) { this.allowCommands = v; }
    @Override public boolean canAllowQuitting() { return allowQuitting; }
    @Override public void setAllowQuitting(boolean v) { this.allowQuitting = v; }
    @Override public boolean canIgnoreSilkTouch() { return ignoreSilkTouch; }
    @Override public void setIgnoreSilkTouch(boolean v) { this.ignoreSilkTouch = v; }
    @Override public String getExternalPartyPlugin() { return externalPartyPlugin; }
    @Override public void setExternalPartyPlugin(String v) { this.externalPartyPlugin = v; }
    @Override public boolean canUsePartiesPlugin() { return usePartiesPlugin; }
    @Override public void setUsePartiesPlugin(boolean v) { this.usePartiesPlugin = v; }
    @Override public int getShareProgressLevel() { return shareProgressLevel; }
    @Override public void setShareProgressLevel(int v) { this.shareProgressLevel = v; }
    @Override public boolean canShareSameQuestOnly() { return shareSameQuestOnly; }
    @Override public void setShareSameQuestOnly(boolean v) { this.shareSameQuestOnly = v; }
    @Override public double getShareDistance() { return shareDistance; }
    @Override public void setShareDistance(double v) { this.shareDistance = v; }
    @Override public boolean canHandleOfflinePlayers() { return handleOfflinePlayers; }
    @Override public void setHandleOfflinePlayers(boolean v) { this.handleOfflinePlayers = v; }
    @Override public boolean canIgnoreBlockReplace() { return ignoreBlockReplace; }
    @Override public void setIgnoreBlockReplace(boolean v) { this.ignoreBlockReplace = v; }
    @Override public boolean canGiveGloballyAtLogin() { return giveGloballyAtLogin; }
    @Override public void setGiveGloballyAtLogin(boolean v) { this.giveGloballyAtLogin = v; }
    @Override public boolean canAllowStackingGlobal() { return allowStackingGlobal; }
    @Override public void setAllowStackingGlobal(boolean v) { this.allowStackingGlobal = v; }
    @Override public boolean canInformOnStart() { return informOnStart; }
    @Override public void setInformOnStart(boolean v) { this.informOnStart = v; }
    @Override public boolean canOverrideMaxQuests() { return overrideMaxQuests; }
    @Override public void setOverrideMaxQuests(boolean v) { this.overrideMaxQuests = v; }
}
