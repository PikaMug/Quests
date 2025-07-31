/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.util.stack;

import me.pikamug.quests.util.stack.impl.LegacyBlockItemStack;
import me.pikamug.quests.util.stack.impl.ModernBlockItemStack;

public final class BlockItemStacks {

    private BlockItemStacks() {
    }

    private static BlockItemStackFactory factory;

    public static BlockItemStackFactory getFactory() {
        return factory;
    }

    private static void setFactory(final BlockItemStackFactory factory) {
        if (BlockItemStacks.factory != null) {
            throw new IllegalStateException("Factory is already set");
        }

        BlockItemStacks.factory = factory;
    }

    public static void init(final boolean modern) {
        if (modern) {
            setFactory(ModernBlockItemStack.FACTORY);
        } else {
            setFactory(LegacyBlockItemStack.FACTORY);
        }
    }
}
