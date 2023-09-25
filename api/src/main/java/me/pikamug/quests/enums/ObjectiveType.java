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

import java.util.HashMap;
import java.util.Map;

public enum ObjectiveType {
    
    BREAK_BLOCK(1, "BreakBlock"),
    DAMAGE_BLOCK(2, "DamageBlock"),
    PLACE_BLOCK(3, "PlaceBlock"),
    USE_BLOCK(4, "UseBlock"),
    CUT_BLOCK(5, "CutBlock"),
    CRAFT_ITEM(6, "CraftItem"),
    SMELT_ITEM(7, "SmeltItem"),
    ENCHANT_ITEM(8, "EnchantItem"),
    BREW_ITEM(9, "BrewItem"),
    CONSUME_ITEM(10, "ConsumeItem"),
    DELIVER_ITEM(11, "DeliverItem"),
    MILK_COW(12, "MilkCow"),
    CATCH_FISH(13, "CatchFish"),
    KILL_MOB(14, "KillMob"),
    KILL_PLAYER(15, "KillPlayer"),
    TALK_TO_NPC(16, "TalkToNPC"),
    KILL_NPC(17, "KillNPC"),
    TAME_MOB(18, "TameMob"),
    SHEAR_SHEEP(19, "ShearSheep"),
    REACH_LOCATION(20, "ReachLocation"),
    PASSWORD(21, "Password"),
    CUSTOM(127, "Custom");
    
    private final String name;
    private final byte typeId;
    
    private static final Map<String, ObjectiveType> NAME_MAP = new HashMap<>();
    private static final Map<Byte, ObjectiveType> ID_MAP = new HashMap<>();
    
    static {
        for (final ObjectiveType type : values()) {
            if (type.name != null) {
                NAME_MAP.put(type.name.toLowerCase(), type);
            }
            if (type.typeId > 0) {
                ID_MAP.put(type.typeId, type);
            }
        }
    }
    
    private ObjectiveType(final int typeId, final String name) {
        this.typeId = (byte) typeId;
        // Capitalize first letter for legacy reasons
        this.name = name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    public static ObjectiveType fromName(final String name) {
        if (name == null) {
            return null;
        }
        // Return custom for legacy reasons
        if (name.startsWith("custom")) {
            return CUSTOM;
        }
        return NAME_MAP.get(name.toLowerCase());
    }
    
    public static ObjectiveType fromId(final int id) {
        if (id > Byte.MAX_VALUE) {
            return null;
        }
        return ID_MAP.get((byte) id);
    }
}
