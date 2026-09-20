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
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class QuesterStorageTest {

    @Test
    public void saveQuesterCompletesExceptionallyWhenImplementationFails() throws Exception {
        final IOException failure = new IOException("save failed");
        final QuesterStorage storage = new QuesterStorage(null, new FailingStorage(failure));

        try {
            storage.saveQuester(null).get(5, TimeUnit.SECONDS);
            fail("Expected save failure");
        } catch (final ExecutionException e) {
            assertSame(failure, e.getCause());
        }
    }

    private static class FailingStorage implements QuesterStorageImpl {
        private final IOException failure;

        private FailingStorage(final IOException failure) {
            this.failure = failure;
        }

        @Override
        public Quests getPlugin() {
            return null;
        }

        @Override
        public String getImplementationName() {
            return "test";
        }

        @Override
        public void init() {
        }

        @Override
        public void close() {
        }

        @Override
        public Quester loadQuester(final UUID uniqueId) {
            return null;
        }

        @Override
        public void saveQuester(final Quester quester) throws Exception {
            throw failure;
        }

        @Override
        public void deleteQuester(final UUID uniqueId) {
        }

        @Override
        public String getQuesterLastKnownName(final UUID uniqueId) {
            return null;
        }

        @Override
        public Collection<UUID> getSavedUniqueIds() {
            return Collections.emptyList();
        }
    }
}
