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

public abstract class FabricCustomReward implements CustomReward {

    private String name = null;
    private String author = null;
    private String display = null;
    private Map.Entry<String, Short> item = new AbstractMap.SimpleEntry<>("BOOK", (short) 0);
    private final Map<String, Object> data = new HashMap<>();
    private final Map<String, String> descriptions = new HashMap<>();

    @Override
    public String getModuleName() {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName()
                .replace(".jar", "");
    }

    @Override
    public Map.Entry<String, Short> getModuleItem() {
        return new AbstractMap.SimpleEntry<>("IRON_INGOT", (short) 0);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public void setAuthor(final String author) {
        this.author = author;
    }

    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public void setDisplay(final String display) {
        this.display = display;
    }

    @Override
    public Map.Entry<String, Short> getItem() {
        return item;
    }

    @Override
    public void setItem(final String type, final short durability) {
        this.item = new AbstractMap.SimpleEntry<>(type, durability);
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    @Override
    public void addStringPrompt(final String title, final String description, final Object defaultValue) {
        data.put(title, defaultValue);
        descriptions.put(title, description);
    }

    @Override
    public abstract void giveReward(UUID uuid, Map<String, Object> data);
}