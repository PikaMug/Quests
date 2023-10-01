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

public interface Planner {
    String getStart();

    long getStartInMillis();

    boolean hasStart();

    void setStart(final String start);

    String getEnd();

    long getEndInMillis();

    boolean hasEnd();

    void setEnd(final String end);

    long getRepeat();

    boolean hasRepeat();

    void setRepeat(final long repeat);

    long getCooldown();

    boolean hasCooldown();

    void setCooldown(final long cooldown);

    boolean getOverride();

    void setOverride(final boolean override);
}
