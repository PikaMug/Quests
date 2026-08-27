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

import me.pikamug.quests.enums.ObjectiveType;

import org.jetbrains.annotations.NotNull;

public class FabricObjective implements Objective {

    private final ObjectiveType type;
    private final String message;
    private final int progress;
    private final int goal;
    private final Object progressObject;
    private final Object goalObject;

    public FabricObjective(ObjectiveType type, String message, int progress, int goal) {
        this(type, message, progress, goal, null, null);
    }

    public FabricObjective(ObjectiveType type, String message, int progress, int goal,
                           Object progressObject, Object goalObject) {
        this.type = type;
        this.message = message;
        this.progress = progress;
        this.goal = goal;
        this.progressObject = progressObject;
        this.goalObject = goalObject;
    }

    @Override public ObjectiveType getType() { return type; }
    @Override public String getMessage() { return message; }
    @Override public int getProgress() { return progress; }
    @Override public int getGoal() { return goal; }
    @Override public @NotNull Object getProgressObject() { return progressObject != null ? progressObject : progress; }
    @Override public @NotNull Object getGoalObject() { return goalObject != null ? goalObject : goal; }
}
