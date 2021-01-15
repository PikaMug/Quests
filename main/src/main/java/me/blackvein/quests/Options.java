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

public class Options {
    private boolean allowCommands = true;
    private boolean allowQuitting = true;
    private boolean ignoreSilkTouch = true;
    private boolean useDungeonsXLPlugin = false;
    private boolean usePartiesPlugin = true;
    private int shareProgressLevel = 1;
    private long shareDistance = 0;
    private boolean shareOnlySameQuest = true;
    
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
    
    public boolean canUseDungeonsXLPlugin() {
        return useDungeonsXLPlugin;
    }
    
    public void setUseDungeonsXLPlugin(final boolean useDungeonsXLPlugin) {
        this.useDungeonsXLPlugin = useDungeonsXLPlugin;
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
    
    public long getShareDistance() {
        return shareDistance;
    }
    
    public void setShareDistance(final long shareDistance) {
        this.shareDistance = shareDistance;
    }
    
    public boolean canShareOnlySameQuest() {
        return shareOnlySameQuest;
    }
    
    public void setShareOnlySameQuest(final boolean shareOnlySameQuest) {
        this.shareOnlySameQuest = shareOnlySameQuest;
    }
}
