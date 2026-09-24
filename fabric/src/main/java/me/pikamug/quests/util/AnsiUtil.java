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

public final class AnsiUtil {

    private static final char SECTION = '\u00A7';
    private static final String RESET = "\u001B[0m";

    private AnsiUtil() {}

    /**
     * Converts Minecraft legacy formatting codes into ANSI escape sequences so that
     * colored output renders on terminals with virtualization terminal processing enabled.
     *
     * @param input a string that may contain legacy {@code §} formatting codes
     * @return the input with formatting codes translated to ANSI escapes (unchanged if none present)
     */
    public static String toAnsi(final String input) {
        if (input == null || input.indexOf(SECTION) == -1) {
            return input;
        }
        final StringBuilder sb = new StringBuilder(input.length() + 32);
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c == SECTION && i + 1 < input.length()) {
                final int codeIndex = i + 1;
                final char code = Character.toLowerCase(input.charAt(codeIndex));
                if (code == 'x') {
                    final String hex = hexAnsi(input, codeIndex);
                    if (hex != null) {
                        sb.append(hex);
                        i = codeIndex + 12;
                        continue;
                    }
                } else {
                    final String ansi = ansiFor(code);
                    if (ansi != null) {
                        sb.append(ansi);
                        i = codeIndex;
                        continue;
                    }
                }
                sb.append(SECTION).append(input.charAt(codeIndex));
                i = codeIndex;
            } else {
                sb.append(c);
            }
        }
        sb.append(RESET);
        return sb.toString();
    }

    /**
     * Legacy hex color: {@code §x§R§R§G§G§B§B}.
     *
     * @param input    the source string
     * @param xIndex   index of the {@code x} following the section sign
     * @return an ANSI 24-bit color sequence, or {@code null} if the pattern is malformed/incomplete
     */
    private static String hexAnsi(final String input, final int xIndex) {
        if (xIndex + 12 >= input.length()) {
            return null;
        }
        final int[] rgb = new int[3];
        for (int part = 0; part < 3; part++) {
            final int off = xIndex + 1 + (part * 4);
            if (input.charAt(off) != SECTION || input.charAt(off + 2) != SECTION) {
                return null;
            }
            final int hi = Character.digit(input.charAt(off + 1), 16);
            final int lo = Character.digit(input.charAt(off + 3), 16);
            if (hi < 0 || lo < 0) {
                return null;
            }
            rgb[part] = (hi << 4) | lo;
        }
        return "\u001B[38;2;" + rgb[0] + ';' + rgb[1] + ';' + rgb[2] + 'm';
    }

    private static String ansiFor(final char code) {
        switch (code) {
            case '0': return "\u001B[30m";
            case '1': return "\u001B[34m";
            case '2': return "\u001B[32m";
            case '3': return "\u001B[36m";
            case '4': return "\u001B[31m";
            case '5': return "\u001B[35m";
            case '6': return "\u001B[33m";
            case '7': return "\u001B[37m";
            case '8': return "\u001B[90m";
            case '9': return "\u001B[94m";
            case 'a': return "\u001B[92m";
            case 'b': return "\u001B[96m";
            case 'c': return "\u001B[91m";
            case 'd': return "\u001B[95m";
            case 'e': return "\u001B[93m";
            case 'f': return "\u001B[97m";
            case 'k': return "\u001B[5m";
            case 'l': return "\u001B[1m";
            case 'm': return "\u001B[9m";
            case 'n': return "\u001B[4m";
            case 'o': return "\u001B[3m";
            case 'r': return RESET;
            default: return null;
        }
    }
}