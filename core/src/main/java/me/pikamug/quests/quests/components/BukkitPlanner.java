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

import java.util.Calendar;
import java.util.TimeZone;

public class BukkitPlanner implements Planner {
    public String start = null;
    public String end = null;
    public long repeat = -1;
    public long cooldown = -1;
    public boolean override = false;
    
    public String getStart() {
        return start;
    }
    public long getStartInMillis() {
        if (start == null) {
            return -1;
        }
        final Calendar cal = Calendar.getInstance();
        final String[] s = start.split(":");
        cal.set(Integer.parseInt(s[2]), Integer.parseInt(s[1]), Integer.parseInt(s[0]),
                Integer.parseInt(s[3]), Integer.parseInt(s[4]), Integer.parseInt(s[5]));
        final TimeZone tz = TimeZone.getTimeZone(s[6]);
        cal.setTimeZone(tz);
        return cal.getTimeInMillis();
    }
    public boolean hasStart() {
        return start != null;
    }
    public void setStart(final String start) {
        this.start = start;
    }
    public String getEnd() {
        return end;
    }
    public long getEndInMillis() {
        if (end == null) {
            return -1;
        }
        final Calendar cal = Calendar.getInstance();
        final String[] s = end.split(":");
        cal.set(Integer.parseInt(s[2]), Integer.parseInt(s[1]), Integer.parseInt(s[0]),
                Integer.parseInt(s[3]), Integer.parseInt(s[4]), Integer.parseInt(s[5]));
        final TimeZone tz = TimeZone.getTimeZone(s[6]);
        cal.setTimeZone(tz);
        return cal.getTimeInMillis();
    }
    public boolean hasEnd() {
        return end != null;
    }
    public void setEnd(final String end) {
        this.end = end;
    }
    public long getRepeat() {
        return repeat;
    }
    public boolean hasRepeat() {
        return repeat != -1;
    }
    public void setRepeat(final long repeat) {
        this.repeat = repeat;
    }
    public long getCooldown() {
        return cooldown;
    }
    public boolean hasCooldown() {
        return cooldown != -1;
    }
    public void setCooldown(final long cooldown) {
        this.cooldown = cooldown;
    }
    public boolean getOverride() {
        return override;
    }
    public void setOverride(final boolean override) {
        this.override = override;
    }
}
