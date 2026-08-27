/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests.components;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FabricPlanner implements Planner {

    private String start;
    private String end;
    private long repeat = 0;
    private long cooldown = 0;
    private boolean override = false;

    @Override public String getStart() { return start; }
    @Override public void setStart(String v) { this.start = v; }
    @Override public String getEnd() { return end; }
    @Override public void setEnd(String v) { this.end = v; }
    @Override public long getRepeat() { return repeat; }
    @Override public void setRepeat(long v) { this.repeat = v; }
    @Override public long getCooldown() { return cooldown; }
    @Override public void setCooldown(long v) { this.cooldown = v; }
    @Override public boolean getOverride() { return override; }
    @Override public void setOverride(boolean v) { this.override = v; }

    @Override
    public long getStartInMillis() {
        return parseDate(start);
    }

    @Override
    public boolean hasStart() {
        return start != null && !start.isEmpty();
    }

    @Override
    public long getEndInMillis() {
        return parseDate(end);
    }

    @Override
    public boolean hasEnd() {
        return end != null && !end.isEmpty();
    }

    @Override
    public boolean hasRepeat() {
        return repeat > 0;
    }

    @Override
    public boolean hasCooldown() {
        return cooldown > 0;
    }

    private long parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return 0;
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            final Date date = sdf.parse(dateStr);
            return date != null ? date.getTime() : 0;
        } catch (final ParseException e) {
            return 0;
        }
    }
}
