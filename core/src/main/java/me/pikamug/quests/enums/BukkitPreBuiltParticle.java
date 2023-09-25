/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.enums;

import org.bukkit.util.Vector;

public enum BukkitPreBuiltParticle {

    ENCHANT("enchant", 0, 1, 0, 1, 10),
    CRIT("crit", 0, 0, 0, .35f, 3),
    SPELL("spell", 0, 0, 0, 1, 3),
    MAGIC_CRIT("magiccrit", 0, 0, 0, .35f, 3),
    MOB_SPELL("mobspell", 0, 0, 0, 1, 3),
    NOTE("note", 0, 0, 0, 1, 1, new Vector(0, .5, 0)),
    PORTAL("portal", 0, 0, 0, 1, 5),
    DUST("dust", 0, 0, 0, 1, 1, new Vector(0, .5, 0)),
    WITCH("witch", 0, 0, 0, 1, 3),
    SNOWBALL("snowball", 0, 0, 0, 1, 3),
    SPLASH("splash", 0, 0, 0, 1, 4, new Vector(0, .5, 0)),
    SMOKE("smoke", 0, 1, 0, 1, 20);

    private final String identifier;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float speed;
    private final int count;
    private Vector vector;

    BukkitPreBuiltParticle(final String identifier, final float offsetX, final float offsetY, final float offsetZ, final float speed, final int count) {
        this.identifier = identifier;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.count = count;
    }

    BukkitPreBuiltParticle(final String identifier, final float offsetX, final float offsetY, final float offsetZ, final float speed, final int count,
                           final Vector vector) {
        this(identifier, offsetX, offsetY, offsetZ, speed, count);
        this.vector = vector;
    }

    public String getIdentifier() {
        return identifier;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public float getSpeed() {
        return speed;
    }

    public int getCount() {
        return count;
    }

    /**
     * Returns the vector applied to the default spawn location or null if the location isn't modified.
     *
     * @return the vector applied to the default spawn location or null if the location isn't modified
     */
    public Vector getVector() {
        return vector;
    }

    /**
     * Returns the PreBuiltParticle represented by the specified identifier.
     *
     * @param identifier the identifier
     * @return the PreBuiltParticle represented by the specified identifier
     */
    public static BukkitPreBuiltParticle fromIdentifier(final String identifier) {
        try {
            return valueOf(identifier);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
