/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.storage;

import me.pikamug.quests.Quests;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.storage.implementation.QuesterStorageImpl;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;


public class QuesterStorage {
    private final Quests plugin;
    private final QuesterStorageImpl implementation;

    public QuesterStorage(final Quests plugin, final QuesterStorageImpl implementation) {
        this.plugin = plugin;
        this.implementation = implementation;
    }

    public QuesterStorageImpl getImplementation() {
        return implementation;
    }

    public Collection<QuesterStorageImpl> getImplementations() {
        return Collections.singleton(implementation);
    }

    private <T> CompletableFuture<T> makeFuture(final Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (final Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        });
    }

    public String getName() {
        return implementation.getImplementationName();
    }

    public void init() {
        try {
            implementation.init();
        } catch (final Exception e) {
            // Failed to initialize storage implementation
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            implementation.close();
        } catch (final Exception e) {
            // Failed to close storage implementation
            e.printStackTrace();
        }
    }
    
    public CompletableFuture<Quester> loadQuester(final UUID uniqueId) {
        return makeFuture(() -> implementation.loadQuester(uniqueId));
    }

    public CompletableFuture<Void> saveQuester(final Quester quester) {
        return makeFuture(() -> {
            implementation.saveQuester(quester);
            return null;
        });
    }

    public CompletableFuture<Map<UUID, Quester>> loadOfflineQuesters() {
        final Map<UUID, Quester> tmpQuesters = new ConcurrentHashMap<>();
        try {
            for (final UUID uniqueId : implementation.getSavedUniqueIds()) {
                tmpQuesters.put(uniqueId, implementation.loadQuester(uniqueId));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return makeFuture(() -> tmpQuesters);
    }

    public CompletableFuture<Void> saveOfflineQuesters() {
        return makeFuture(() -> {
            for (final Quester quester : plugin.getOfflineQuesters()) {
                implementation.saveQuester(quester);
            }
            return null;
        });
    }

    public CompletableFuture<Void> deleteQuester(final UUID uniqueId) {
        return makeFuture(() -> {
            implementation.deleteQuester(uniqueId);
            return null;
        });
    }

    public CompletableFuture<String> getQuesterLastKnownName(final UUID uniqueId) {
        return makeFuture(() -> implementation.getQuesterLastKnownName(uniqueId));
    }
}
