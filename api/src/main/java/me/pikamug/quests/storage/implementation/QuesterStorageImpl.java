/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.storage.implementation;

import me.pikamug.quests.Quests;
import me.pikamug.quests.player.Quester;

import java.util.Collection;
import java.util.UUID;

public interface QuesterStorageImpl {
    Quests getPlugin();

    String getImplementationName();

    void init() throws Exception;

    void close();
    
    Quester loadQuester(final UUID uniqueId) throws Exception;

    void saveQuester(final Quester quester) throws Exception;

    void deleteQuester(final UUID uniqueId) throws Exception;

    String getQuesterLastKnownName(final UUID uniqueId) throws Exception;
    
    Collection<UUID> getSavedUniqueIds() throws Exception;
}
