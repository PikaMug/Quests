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

public class ConditionFormatException extends Exception {

    private static final long serialVersionUID = 6165516939621807530L;
    private final String message;
    private final String conditionId;

    public ConditionFormatException(final String message, final String conditionId) {
        super(message + ", see condition of ID " + conditionId);
        this.message = message + ", see condition of ID " + conditionId;
        this.conditionId = conditionId;
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
     * Get the condition ID associated with this exception.
     * 
     * @return The condition that an invalid value was set within.
     */
    public String getConditionId() {
        return conditionId;
    }
}
