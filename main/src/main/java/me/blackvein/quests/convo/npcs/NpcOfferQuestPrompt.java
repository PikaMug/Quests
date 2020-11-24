/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.convo.npcs;

import java.text.MessageFormat;
import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.Lang;

public class NpcOfferQuestPrompt extends StringPrompt {

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(final ConversationContext context) {
        final Quests plugin = (Quests)context.getPlugin();
        final Quester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
        final LinkedList<Quest> quests = (LinkedList<Quest>) context.getSessionData("npcQuests");
        final String npc = (String) context.getSessionData("npc");
        final String text = Lang.get("questNPCListTitle").replace("<npc>", npc);
        String menu = text + "\n";
        for (int i = 1; i <= quests.size(); i++) {
            final Quest quest = quests.get(i - 1);
            if (quester.getCompletedQuests().contains(quest)) {
                menu += ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "" + i + ". " + ChatColor.RESET + "" 
                        + ChatColor.GREEN + "" + ChatColor.ITALIC + quest.getName() + ChatColor.RESET + "" 
                        + ChatColor.GREEN + " " + Lang.get("redoCompleted") + "\n";
            } else {
                menu += ChatColor.GOLD + "" + ChatColor.BOLD + "" + i + ". " + ChatColor.RESET + "" + ChatColor.YELLOW 
                        + "" + ChatColor.ITALIC + quest.getName() + "\n";
            }
        }
        menu += ChatColor.GOLD + "" + ChatColor.BOLD + "" + (quests.size() + 1) + ". " + ChatColor.RESET + "" 
                + ChatColor.GRAY + Lang.get("cancel") + "\n";
        menu += ChatColor.WHITE + Lang.get("enterAnOption");
        return menu;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Prompt acceptInput(final ConversationContext context, final String input) {
        final Quests plugin = (Quests)context.getPlugin();
        final Quester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
        final LinkedList<Quest> quests = (LinkedList<Quest>) context.getSessionData("npcQuests");
        int numInput = -1;
        try {
            numInput = Integer.parseInt(input);
        } catch (final NumberFormatException e) {
            // Continue
        }
        if (input.equalsIgnoreCase(Lang.get("cancel")) || numInput == (quests.size() + 1)) {
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("cancelled"));
            return Prompt.END_OF_CONVERSATION;
        } else {
            Quest q = null;
            for (final Quest quest : quests) {
                if (quest.getName().equalsIgnoreCase(input)) {
                    q = quest;
                    break;
                }
            }
            if (q == null) {
                for (final Quest quest : quests) {
                    if (numInput == (quests.indexOf(quest) + 1)) {
                        q = quest;
                        break;
                    }
                }
            }
            if (q == null) {
                for (final Quest quest : quests) {
                    if (quest.getName().toLowerCase().contains(input.toLowerCase())) {
                        q = quest;
                        break;
                    }
                }
            }
            if (q == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new NpcOfferQuestPrompt();
            } else {
                final Player player = quester.getPlayer();
                if (quester.canAcceptOffer(q, true)) {
                    quester.setQuestIdToTake(q.getId());
                    for (final String msg : extracted(plugin, quester).split("<br>")) {
                        player.sendMessage(msg);
                    }
                    if (!plugin.getSettings().canAskConfirmation()) {
                        quester.takeQuest(q, false);
                    } else {
                        plugin.getConversationFactory().buildConversation(player).begin();
                    }
                }
                /*Player player = quester.getPlayer();
                if (!quester.getCompletedQuests().contains(q.getName())) {
                    if (quester.getCurrentQuests().size() < plugin.getSettings().getMaxQuests() 
                            || plugin.getSettings().getMaxQuests() < 1) {
                        if (q.testRequirements(quester)) {
                            quester.setQuestToTake(q.getName());
                            String s = extracted(plugin, quester);
                            for (String msg : s.split("<br>")) {
                                player.sendMessage(msg);
                            }
                            if (!plugin.getSettings().canAskConfirmation()) {
                                quester.takeQuest(q, false);
                            } else {
                                plugin.getConversationFactory().buildConversation((Conversable) player).begin();
                            }
                        } else {
                            for (String msg : q.getRequirements().getDetailsOverride()) {
                                player.sendMessage(msg);
                            }
                        }
                    } else if (quester.getCurrentQuests().containsKey(q) == false) {
                        String msg = Lang.get("questMaxAllowed");
                        msg = msg.replace("<number>", String.valueOf(plugin.getSettings().getMaxQuests()));
                        player.sendMessage(ChatColor.YELLOW + msg);
                    }
                } else if (quester.getCompletedQuests().contains(q.getName())) {
                    if (quester.getCurrentQuests().size() < plugin.getSettings().getMaxQuests() 
                            || plugin.getSettings().getMaxQuests() < 1) {
                        if (quester.getRemainingCooldown(q) > 0 && !q.getPlanner().getOverride()) {
                            String early = Lang.get("questTooEarly");
                            early = early.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW);
                            early = early.replace("<time>", ChatColor.DARK_PURPLE 
                                    + MiscUtil.getTime(quester.getRemainingCooldown(q)) + ChatColor.YELLOW);
                            player.sendMessage(ChatColor.YELLOW + early);
                        } else if (q.getPlanner().getCooldown() < 0) {
                            String completed = Lang.get("questAlreadyCompleted");
                            completed = completed.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW);
                            player.sendMessage(ChatColor.YELLOW + completed);
                        } else {
                            quester.setQuestToTake(q.getName());
                            String s = extracted(plugin, quester);
                            for (String msg : s.split("<br>")) {
                                player.sendMessage(msg);
                            }
                            if (!plugin.getSettings().canAskConfirmation()) {
                                quester.takeQuest(q, false);
                            } else {
                                plugin.getConversationFactory().buildConversation((Conversable) player).begin();
                            }
                        }
                    } else if (quester.getCurrentQuests().containsKey(q) == false) {
                        String msg = Lang.get("questMaxAllowed");
                        msg = msg.replace("<number>", String.valueOf(plugin.getSettings().getMaxQuests()));
                        player.sendMessage(ChatColor.YELLOW + msg);
                    }
                }*/
                return Prompt.END_OF_CONVERSATION;
            }
        }
    }

    private String extracted(final Quests plugin, final Quester quester) {
        final Quest quest = plugin.getQuestById(quester.getQuestIdToTake());
        return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n", ChatColor.GOLD, ChatColor.DARK_PURPLE, 
                quest.getName(), ChatColor.GOLD, ChatColor.RESET, quest.getDescription());
    }
}
