/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.util;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

/**
 * Enables ANSI/VT escape-processing on the Windows console from within the mod, so colored
 * output renders in a plain {@code cmd} window without the user editing console parameters.
 * This is a no-op when stdout is not a console (e.g. output redirected to a file) or on non-Windows.
 */
public final class ConsoleVt {

    private static final long STD_OUTPUT_HANDLE = (long) -11;
    private static final int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004;
    private static boolean applied;

    private ConsoleVt() {}

    /**
     * Attempts to enable virtual terminal processing on the standard output console.
     * Safe to call multiple times.
     */
    public static void enable() {
        if (applied) return;
        applied = true;
        final String os = System.getProperty("os.name", "");
        if (!os.toLowerCase().startsWith("windows")) return;
        try {
            final WinNT.HANDLE out = Kernel32.INSTANCE.GetStdHandle((int) STD_OUTPUT_HANDLE);
            if (out == null || WinNT.INVALID_HANDLE_VALUE.equals(out)) return;
            final IntByReference mode = new IntByReference(0);
            if (!Kernel32.INSTANCE.GetConsoleMode(out, mode)) return;
            if ((mode.getValue() & ENABLE_VIRTUAL_TERMINAL_PROCESSING) == 0) {
                Kernel32.INSTANCE.SetConsoleMode(out, mode.getValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
            }
        } catch (final Throwable ignored) {
            // JNA missing or no console available; colors simply stay off
        }
    }
}