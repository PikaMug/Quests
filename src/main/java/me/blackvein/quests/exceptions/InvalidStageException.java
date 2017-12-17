package me.blackvein.quests.exceptions;

import me.blackvein.quests.Quest;

/**
 * This is the InvalidStageException class, this exception is used to indicate
 * that the new stage of a quest does not exist. This is currently used in the
 * Quest class when advancing to the next stage or manually setting the stage.
 *
 * @author Zino
 * @author Blackvein
 * @version 3
 * @see Quest#nextStage(me.blackvein.quests.Quester)
 * @see Quest#setStage(me.blackvein.quests.Quester, int)
 * @since 1.7.1-SNAPSHOT
 */
public class InvalidStageException extends Exception {

    /**
     * The version id to use when serialising and deserialising this class.
     */
    private static final long serialVersionUID = 1778748295752972651L;


    /**
     * The Quest instance that an invalid stage was set within.
     */
    private final Quest quest;

    /**
     * The invalid stage number that was attempted to be set.
     */
    private final int stage;

    /**
     * Create a new instance of the InvalidStageException class with the given
     * holding Quest and invalid stage number.
     *
     * @param quest The quest that an invalid stage id was set within.
     * @param stage The invalid stage id that was set.
     */
    public InvalidStageException(Quest quest, int stage) {
        this.quest = quest;
        this.stage = stage;
    }

    /**
     * Get the quest instance associated with this exception.
     *
     * @return The quest that an invalid stage id was set within.
     */
    public Quest getQuest() {
        return quest;
    }

    /**
     * Get the invalid stage id that was attempted to be set within the quest
     * class.
     *
     * @return The invalid stage id that was set.
     */
    public int getStage() {
        return stage;
    }
}
