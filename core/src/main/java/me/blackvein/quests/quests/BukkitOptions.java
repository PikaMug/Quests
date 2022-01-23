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

package me.blackvein.quests.quests;

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
}
