/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.exceptions;

import me.pikamug.quests.quests.Quest;

public class StageFormatException extends Exception {
    
    private static final long serialVersionUID = -8217391053042612896L;
    private final String message;
    private final Quest quest;
    private final int stage;
    
    
    public StageFormatException(final String message, final Quest quest, final int stage) {
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
    public Quest getQuest() {
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
