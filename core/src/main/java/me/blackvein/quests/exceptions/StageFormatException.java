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

package me.blackvein.quests.exceptions;

import me.blackvein.quests.quests.IQuest;

public class StageFormatException extends Exception {
    
    private static final long serialVersionUID = -8217391053042612896L;
    private final String message;
    private final IQuest quest;
    private final int stage;
    
    
    public StageFormatException(final String message, final IQuest quest, final int stage) {
        super(message + ", see quest " + quest.getName() + " stage " + stage);
        this.message = message + ", see quest " + quest.getName() + " stage " + stage;
        this.quest = quest;
        this.stage = stage;
    }
    
    /**
     * Get the message associated with this exception.
     * 
     * @return The message.
     */
    @Override
    public String getMessage() {
        return message;
    }
    
    /**
     * Get the quest instance associated with this exception.
     * 
     * @return The quest that an invalid stage id was set within.
     */
    public IQuest getQuest() {
        return quest;
    }

    /**
     * Get the invalid stage id that was attempted to be set within the quest class.
     * 
     * @return The invalid stage id that was set.
     */
    public int getStage() {
        return stage;
    }
}
