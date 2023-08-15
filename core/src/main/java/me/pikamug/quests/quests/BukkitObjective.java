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

package me.pikamug.quests.quests;

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
