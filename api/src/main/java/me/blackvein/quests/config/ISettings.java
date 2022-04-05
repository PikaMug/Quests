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

package me.blackvein.quests.config;

public interface ISettings {
    int getAcceptTimeout();
    void setAcceptTimeout(final int acceptTimeout);
    boolean canAllowCommands();
    void setAllowCommands(final boolean allowCommands);
    boolean canAllowCommandsForNpcQuests();
    void setAllowCommandsForNpcQuests(final boolean allowCommandsForNpcQuests);
    boolean canAllowPranks();
    void setAllowPranks(final boolean allowPranks);
    boolean canClickablePrompts();
    void setClickablePrompts(boolean clickablePrompts);
    int getConditionInterval();
    void setConditionInterval(final int conditionInterval);
    boolean canConfirmAbandon();
    void setConfirmAbandon(final boolean confirmAbandon);
    boolean canConfirmAccept();
    void setConfirmAccept(final boolean confirmAccept);
    int getConsoleLogging();
    void setConsoleLogging(final int consoleLogging);
    boolean canDisableCommandFeedback();
    void setDisableCommandFeedback(final boolean disableCommandFeedback);
    boolean canGenFilesOnJoin();
    void setGenFilesOnJoin(final boolean genFilesOnJoin);
    boolean canGiveJournalItem();
    void setGiveJournalItem(final boolean giveJournalItem);
    boolean canIgnoreLockedQuests();
    void setIgnoreLockedQuests(final boolean ignoreLockedQuests);
    int getKillDelay();
    void setKillDelay(final int killDelay);
    int getMaxQuests();
    void setMaxQuests(final int maxQuests);
    boolean canNpcEffects();
    void setNpcEffects(final boolean npcEffects);
    String getEffect();
    void setEffect(final String effect);
    String getRedoEffect();
    void setRedoEffect(final String redoEffect);
    boolean canShowQuestReqs();
    void setShowQuestReqs(final boolean showQuestReqs);
    boolean canShowQuestTitles();
    void setShowQuestTitles(final boolean showQuestTitles);
    int getStrictPlayerMovement();
    void setStrictPlayerMovement(final int strictPlayerMovement);
    boolean canTrialSave();
    void setTrialSave(final boolean trialSave);
    int getTopLimit();
    void setTopLimit(final int topLimit);
    boolean canTranslateNames();
    void setTranslateNames(final boolean translateItems);
    boolean canTranslateSubCommands();
    void setTranslateSubCommands(final boolean translateSubCommands);
    boolean canUpdateCheck();
    void setUpdateCheck(final boolean updateCheck);
    void init();
}
