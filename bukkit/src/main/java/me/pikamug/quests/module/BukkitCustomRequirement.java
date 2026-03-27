/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.module;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class BukkitCustomRequirement implements CustomRequirement {

    private String name = null;
    private String author = null;
    private String display = null;
    private Map.Entry<String, Short> item = new AbstractMap.SimpleEntry<>("BOOK", (short) 0);
    private final Map<String, Object> data = new HashMap<>();
    private final Map<String, String> descriptions = new HashMap<>();

    public String getModuleName() {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName()
                .replace(".jar", "");
    }

    public Map.Entry<String, Short> getModuleItem() {
        return new AbstractMap.SimpleEntry<>("IRON_INGOT", (short) 0);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(final String display) {
        this.display = display;
    }

    public Map.Entry<String, Short> getItem() {
        return item;
    }

    public void setItem(final String type, final short durability) {
        this.item = new AbstractMap.SimpleEntry<>(type, durability);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    /**
     * Add a new prompt<p>
     *
     * Note that the "defaultValue" Object will be cast to a String internally
     *
     * @param title Prompt name
     * @param description Description of expected input
     * @param defaultValue Value to be used if input is not received
     */
    public void addStringPrompt(final String title, final String description, final Object defaultValue) {
        data.put(title, defaultValue);
        descriptions.put(title, description);
    }

    /**
     * Test whether a player has met the requirement
     *
     * @param uuid UUID of player being tested
     * @param data Map of custom requirement data
     * @return true if met
     */
    public abstract boolean testRequirement(UUID uuid, Map<String, Object> data);
}
