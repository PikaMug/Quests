/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.util.Calendar;
import java.util.TimeZone;

public class Planner {
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
        cal.set(Integer.valueOf(s[2]), Integer.valueOf(s[1]), Integer.valueOf(s[0]),
                Integer.valueOf(s[3]), Integer.valueOf(s[4]), Integer.valueOf(s[5]));
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
        cal.set(Integer.valueOf(s[2]), Integer.valueOf(s[1]), Integer.valueOf(s[0]),
                Integer.valueOf(s[3]), Integer.valueOf(s[4]), Integer.valueOf(s[5]));
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
