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
