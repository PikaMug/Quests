/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.tasks;

import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tick-based task scheduler for Fabric.
 * Replaces Bukkit's scheduler and FoliaLib's PlatformScheduler.
 */
public final class FabricScheduler {

    private static final List<ScheduledTask> tasks = new CopyOnWriteArrayList<>();
    private static int tickCounter = 0;

    private FabricScheduler() {}

    public static void tick(MinecraftServer server) {
        tickCounter++;
        final var iterator = tasks.iterator();
        while (iterator.hasNext()) {
            final ScheduledTask task = iterator.next();
            if (task.cancelled) {
                tasks.remove(task);
                continue;
            }
            task.currentTick++;
            if (task.currentTick >= task.delayTicks) {
                task.runnable.run();
                if (task.repeating) {
                    task.currentTick = 0;
                } else {
                    tasks.remove(task);
                }
            }
        }
    }

    /**
     * Schedule a task to run after a delay (in ticks).
     */
    public static void runLater(Runnable runnable, long delayTicks) {
        tasks.add(new ScheduledTask(runnable, delayTicks, false));
    }

    /**
     * Schedule a task to run repeatedly at a fixed interval (in ticks).
     */
    public static void runTimer(Runnable runnable, long delayTicks, long periodTicks) {
        final ScheduledTask task = new ScheduledTask(runnable, delayTicks, true);
        task.periodTicks = periodTicks;
        task.repeating = true;
        tasks.add(task);
    }

    /**
     * Schedule a task to run asynchronously (on a new thread).
     */
    public static void runAsync(Runnable runnable) {
        new Thread(runnable, "Quests-Async").start();
    }

    /**
     * Cancel all scheduled tasks.
     */
    public static void cancelAllTasks() {
        tasks.clear();
    }

    private static class ScheduledTask {
        final Runnable runnable;
        final long delayTicks;
        boolean repeating;
        long periodTicks;
        long currentTick = 0;
        boolean cancelled = false;

        ScheduledTask(Runnable runnable, long delayTicks, boolean repeating) {
            this.runnable = runnable;
            this.delayTicks = delayTicks;
            this.repeating = repeating;
        }
    }
}
