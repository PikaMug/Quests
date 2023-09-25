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

public interface Options {
    boolean canAllowCommands();

    void setAllowCommands(final boolean allowCommands);

    boolean canAllowQuitting();

    void setAllowQuitting(final boolean allowQuitting);

    boolean canIgnoreSilkTouch();

    void setIgnoreSilkTouch(final boolean ignoreSilkTouch);

    String getExternalPartyPlugin();

    void setExternalPartyPlugin(final String externalPartyPlugin);

    boolean canUsePartiesPlugin();

    void setUsePartiesPlugin(final boolean usePartiesPlugin);

    int getShareProgressLevel();

    void setShareProgressLevel(final int shareProgressLevel);

    boolean canShareSameQuestOnly();

    void setShareSameQuestOnly(final boolean shareSameQuestOnly);

    double getShareDistance();

    void setShareDistance(final double shareDistance);

    boolean canHandleOfflinePlayers();

    void setHandleOfflinePlayers(final boolean handleOfflinePlayers);

    boolean canIgnoreBlockReplace();

    void setIgnoreBlockReplace(final boolean ignoreBlockReplace);
}
