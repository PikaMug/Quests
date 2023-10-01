/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class BukkitQuestsLog4JFilter extends AbstractFilter {

    private static final long serialVersionUID = -5594073755007974514L;

    /**
     * Validates a message and returns the {@link Result} value
     * depending on whether the message contains undesirable data.
     *
     * @param message The message to filter for
     *
     * @return Result of DENY or NUETRAL
     */
    private static Result validateMessage(String message) {
        if (message == null) {
            return Result.NEUTRAL;
        }
        // Hikari tries to nag author about unavoidable logger usage
        if (message.contains("[PikaMug]")) {
            return Result.DENY;
        }
        // Hikari outputs these messages as ERROR which concerns some
        if (message.contains("quests-hikari - Shutdown")) {
            return Result.DENY;
        }
        // Command for 1.19+ clickable text spam
        if (message.contains("quests choice")) {
            return Result.DENY;
        }
        // Paper outputs errors when dependencies for module events are missing
        return (message.contains("Plugin Quests") && message.contains("failed to register events"))
                ? Result.DENY : Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null) {
            return Result.NEUTRAL;
        }

        return validateMessage(event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return validateMessage(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        if (msg == null) {
            return Result.NEUTRAL;
        }

        return validateMessage(msg.toString());
    }
}
