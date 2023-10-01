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

public class ActionFormatException extends Exception {

    private static final long serialVersionUID = 6165516939621807530L;
    private final String message;
    private final String actionId;

    public ActionFormatException(final String message, final String actionId) {
        super(message + ", see action of ID " + actionId);
        this.message = message + ", see action of ID " + actionId;
        this.actionId = actionId;
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
     * Get the action ID associated with this exception.
     * 
     * @return The action that an invalid value was set within.
     */
    public String getActionId() {
        return actionId;
    }
}
