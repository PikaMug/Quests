/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public abstract class CustomRequirement {

    private String name = null;
    private String author = null;
    private String display = null;
    private final Map<String, Short> items = new HashMap<String, Short>();
    private final Map<String, Object> data = new HashMap<String, Object>();
    private final Map<String, String> descriptions = new HashMap<String, String>();

    public abstract boolean testRequirement(Player p, Map<String, Object> m);

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
    
    public Map<String, Short> getItems() {
        return items;
    }
    
    public void addItem(final String type, final short durability) {
        this.items.put(type, durability);
    }

    public Map<String, Object> getData() {
        return data;
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

    public Map<String, String> getDescriptions() {
        return descriptions;
    }
}
