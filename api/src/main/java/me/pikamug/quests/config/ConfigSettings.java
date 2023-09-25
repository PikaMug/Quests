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

public interface ConfigSettings {
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
    String getLanguage();
    void setLanguage(final String language);
    boolean canLanguageOverrideClient();
    void setLanguageOverrideClient(final boolean languageOverrideClient);
    int getMaxQuests();
    void setMaxQuests(final int maxQuests);
    boolean canNpcEffects();
    void setNpcEffects(final boolean npcEffects);
    String getEffect();
    void setEffect(final String effect);
    String getRedoEffect();
    void setRedoEffect(final String redoEffect);
    boolean canShowCompletedObjs();
    void setShowCompletedObjs(final boolean showCompletedObjs);
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
