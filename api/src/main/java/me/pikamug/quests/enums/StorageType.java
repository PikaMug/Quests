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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum StorageType {

    // Local text file
    YAML("YAML", "yaml", "yml"),

    // Remote database
    MYSQL("MySQL", "mysql"),

    // Local archive
    JAR("JAR", "jar"),

    // Custom
    CUSTOM("Custom", "custom");

    private final String name;

    private final List<String> identifiers;

    StorageType(final String name, final String... identifiers) {
        this.name = name;
        this.identifiers = Collections.unmodifiableList(Arrays.asList(identifiers));
    }

    public static StorageType parse(final String name, final StorageType def) {
        for (final StorageType t : values()) {
            for (final String id : t.getIdentifiers()) {
                if (id.equalsIgnoreCase(name)) {
                    return t;
                }
            }
        }
        return def;
    }

    public String getName() {
        return name;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }
}
