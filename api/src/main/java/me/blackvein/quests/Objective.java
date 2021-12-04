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

package me.blackvein.quests;

import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.enums.ObjectiveType;

public class Objective {
    private final ObjectiveType type;
    private final int progress;
    private final int goal;
    private final ItemStack progressStack;
    private final ItemStack goalStack;
    
    
    public Objective(final ObjectiveType type, final int progress, final int goal) {
        this.type = type;
        this.progress = progress;
        this.goal = goal;
        this.progressStack = null;
        this.goalStack = null;
    }
    
    public Objective(final ObjectiveType type, final ItemStack progress, final ItemStack goal) {
        this.type = type;
        this.progress = progress.getAmount();
        this.goal = goal.getAmount();
        this.progressStack = progress;
        this.goalStack = goal;
    }
    
    public ObjectiveType getType() {
        return type;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public int getGoal() {
        return goal;
    }
    
    public ItemStack getItemProgress() {
        return progressStack;
    }
    
    public ItemStack getItemGoal() {
        return goalStack;
    }
}
