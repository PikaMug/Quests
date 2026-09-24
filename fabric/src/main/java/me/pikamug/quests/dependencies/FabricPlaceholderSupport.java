/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.dependencies;

import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import net.minecraft.server.level.ServerPlayer;

/**
 * Resolves placeholders through Text placeholder API. Only referenced once the
 * mod is confirmed present, so its API classes are never linked otherwise.
 */
public final class FabricPlaceholderSupport {

    private FabricPlaceholderSupport() {
    }

    public static String parse(ServerPlayer player, String identifier) {
        return Placeholders.SERVER_PLACEHOLDER_PARSER
                .parseComponent(identifier, ServerPlaceholderContext.of(player).asParserContext()).getString();
    }
}