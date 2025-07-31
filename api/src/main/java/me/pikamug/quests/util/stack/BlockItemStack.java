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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface BlockItemStack {

    static BlockItemStack of(final ItemStack original) {
        if (original == null) {
            return null;
        }

        return of(original.getType(), original.getAmount(), original.getDurability());
    }

    static BlockItemStack of(final Block block) {
        return BlockItemStacks.getFactory().of(block);
    }

    static BlockItemStack of(final Material type, final int amount, final short durability) {
        return BlockItemStacks.getFactory().of(type, amount, durability);
    }

    static BlockItemStack clone(final BlockItemStack original, final int amount) {
        return BlockItemStacks.getFactory().clone(original, amount);
    }

    static BlockItemStack clone(final BlockItemStack original, final int amount, final short durability) {
        return BlockItemStacks.getFactory().clone(original, amount, durability);
    }

    Material getType();
    int getAmount();
    short getDurability();

    boolean matches(BlockItemStack other);

}
