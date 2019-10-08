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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public abstract class CustomReward {

    private String name = null;
    private String author = null;
    private String rewardName = null;
    private Map<String, Object> data = new HashMap<String, Object>();
    private Map<String, String> descriptions = new HashMap<String, String>();

    public abstract void giveReward(Player p, Map<String, Object> m);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
    public void addStringPrompt(String title, String description, Object defaultValue) {
        data.put(title, defaultValue);
        descriptions.put(title, description);
    }
    
    /**
     * Set the title of a prompt
     * 
     * @param name Prompt title
     * @deprecated use addPrompt(name, description)
     */
    public void addData(String name) {
        data.put(name, null);
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    /**
     * Set the description for the specified prompt
     * 
     * @param name Prompt title
     * @param description Description of expected input
     * @deprecated use addTaskPrompt(name, description)
     */
    public void addDescription(String name, String description) {
        descriptions.put(name, description);
    }
    
    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String name) {
        rewardName = name;
    }
}
