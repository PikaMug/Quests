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

import me.pikamug.quests.quests.components.Options;

public class BukkitOptions implements Options {
    private boolean allowCommands = true;
    private boolean allowQuitting = true;
    private boolean ignoreSilkTouch = true;
    private String externalPartyPlugin = null;
    private boolean usePartiesPlugin = true;
    private boolean handleOfflinePlayers = false;
    private double shareDistance = 0.0D;
    private int shareProgressLevel = 1;
    private boolean shareSameQuestOnly = true;
    private boolean ignoreBlockReplace = true;
    
    public boolean canAllowCommands() {
        return allowCommands;
    }
    
    public void setAllowCommands(final boolean allowCommands) {
        this.allowCommands = allowCommands;
    }
    
    public boolean canAllowQuitting() {
        return allowQuitting;
    }
    
    public void setAllowQuitting(final boolean allowQuitting) {
        this.allowQuitting = allowQuitting;
    }
    
    public boolean canIgnoreSilkTouch() {
        return ignoreSilkTouch;
    }
    
    public void setIgnoreSilkTouch(final boolean ignoreSilkTouch) {
        this.ignoreSilkTouch = ignoreSilkTouch;
    }

    public String getExternalPartyPlugin() {
        return externalPartyPlugin;
    }

    public void setExternalPartyPlugin(final String externalPartyPlugin) {
        this.externalPartyPlugin = externalPartyPlugin;
    }
    
    public boolean canUsePartiesPlugin() {
        return usePartiesPlugin;
    }
    
    public void setUsePartiesPlugin(final boolean usePartiesPlugin) {
        this.usePartiesPlugin = usePartiesPlugin;
    }
    
    public int getShareProgressLevel() {
        return shareProgressLevel;
    }
    
    public void setShareProgressLevel(final int shareProgressLevel) {
        this.shareProgressLevel = shareProgressLevel;
    }
    
    public boolean canShareSameQuestOnly() {
        return shareSameQuestOnly;
    }
    
    public void setShareSameQuestOnly(final boolean shareSameQuestOnly) {
        this.shareSameQuestOnly = shareSameQuestOnly;
    }
    
    public double getShareDistance() {
        return shareDistance;
    }
    
    public void setShareDistance(final double shareDistance) {
        this.shareDistance = shareDistance;
    }
    
    public boolean canHandleOfflinePlayers() {
        return handleOfflinePlayers;
    }
    
    public void setHandleOfflinePlayers(final boolean handleOfflinePlayers) {
        this.handleOfflinePlayers = handleOfflinePlayers;
    }

    public boolean canIgnoreBlockReplace() {
        return ignoreBlockReplace;
    }

    public void setIgnoreBlockReplace(final boolean ignoreBlockReplace) {
        this.ignoreBlockReplace = ignoreBlockReplace;
    }
}
