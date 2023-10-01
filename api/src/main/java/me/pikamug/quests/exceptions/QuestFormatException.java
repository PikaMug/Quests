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

public class QuestFormatException extends Exception {

    private static final long serialVersionUID = -5960613170308750149L;
    private final String message;
    private final String questId;

    public QuestFormatException(final String message, final String questId) {
        super(message + ", see quest of ID " + questId);
        this.message = message + ", see quest of ID " + questId;
        this.questId = questId;
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
     * Get the quest ID associated with this exception.
     * 
     * @return The quest that an invalid value was set within.
     */
    public String getQuestId() {
        return questId;
    }
}
