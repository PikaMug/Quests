/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.storage;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.storage.implementation.StorageImplementation;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Storage {
    private final Quests plugin;
    private final StorageImplementation implementation;

    public Storage(final Quests plugin, final StorageImplementation implementation) {
        this.plugin = plugin;
        this.implementation = implementation;
    }

    public StorageImplementation getImplementation() {
        return implementation;
    }

    public Collection<StorageImplementation> getImplementations() {
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

    private CompletableFuture<Void> makeFuture(final Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (final Exception e) {
                throw (RuntimeException) e;
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
            plugin.getLogger().severe("Failed to initialize storage implementation");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            implementation.close();
        } catch (final Exception e) {
            plugin.getLogger().severe("Failed to close storage implementation");
            e.printStackTrace();
        }
    }
    
    public CompletableFuture<Quester> loadQuester(final UUID uniqueId) {
        return makeFuture(() -> implementation.loadQuester(uniqueId));
    }

    public CompletableFuture<Void> saveQuester(final Quester quester) {
        return makeFuture(() -> {
            try {
                implementation.saveQuester(quester);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> saveOfflineQuesters() {
        return makeFuture(() -> {
            try {
                for (Quester quester : plugin.getOfflineQuesters()) {
                    implementation.saveQuester(quester);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> deleteQuester(final UUID uniqueId) {
        return makeFuture(() -> {
            try {
                implementation.deleteQuester(uniqueId);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<String> getQuesterLastKnownName(final UUID uniqueId) {
        return makeFuture(() -> implementation.getQuesterLastKnownName(uniqueId));
    }
}
