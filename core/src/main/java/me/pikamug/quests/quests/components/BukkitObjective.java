/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests.components;

import me.pikamug.quests.entity.BukkitCountableMob;
import me.pikamug.quests.enums.ObjectiveType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitObjective implements Objective {
    private final ObjectiveType type;
    private final String message;
    private final int progress;
    private final int goal;
    private final Object progressObj;
    private final Object goalObj;
    
    public BukkitObjective(final ObjectiveType type, @Nullable final String message, final int progress,
                           final int goal) {
        this.type = type;
        this.message = message;
        this.progress = progress;
        this.goal = goal;
        this.progressObj = new Object();
        this.goalObj = new Object();
    }
    
    public BukkitObjective(final ObjectiveType type, @Nullable final String message, final @NotNull Object progress,
                           final @NotNull Object goal) {
        this.type = type;
        this.message = message;
        this.progressObj = progress;
        this.goalObj = goal;
        if (progressObj instanceof ItemStack) {
            this.progress = ((ItemStack) progressObj).getAmount();
        } else if (progressObj instanceof BukkitCountableMob) {
            this.progress = ((BukkitCountableMob) progressObj).getCount();
        } else {
            this.progress = 0;
        }
        if (goalObj instanceof ItemStack) {
            this.goal = ((ItemStack) goalObj).getAmount();
        }  else if (goalObj instanceof BukkitCountableMob) {
            this.goal = ((BukkitCountableMob) goalObj).getCount();
        } else {
            this.goal = 0;
        }
    }

    @Override
    public ObjectiveType getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public int getGoal() {
        return goal;
    }

    @Override
    public @NotNull Object getProgressObject() {
        return progressObj;
    }

    @Override
    public @NotNull Object getGoalObject() {
        return goalObj;
    }
    
    public @Nullable ItemStack getProgressAsItem() {
        return progressObj instanceof ItemStack ? (ItemStack) progressObj : null;
    }
    
    public @Nullable ItemStack getGoalAsItem() {
        return goalObj instanceof ItemStack ? (ItemStack) goalObj : null;
    }

    public @Nullable BukkitCountableMob getProgressAsMob() {
        return progressObj instanceof BukkitCountableMob ? (BukkitCountableMob) progressObj : null;
    }

    public @Nullable BukkitCountableMob getGoalAsMob() {
        return goalObj instanceof BukkitCountableMob ? (BukkitCountableMob) goalObj : null;
    }
}
