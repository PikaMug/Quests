package me.blackvein.quests.exceptions;

import me.blackvein.quests.Quest;

public class InvalidStageException extends Exception {

    private final Quest quest;
    private final int stage;

    public InvalidStageException(Quest quest, int stage) {
        this.quest = quest;
        this.stage = stage;
    }

    public Quest getQuest() {
        return quest;
    }

    public int getStage() {
        return stage;
    }

    private static final long serialVersionUID = 1778748295752972651L;

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }
}
