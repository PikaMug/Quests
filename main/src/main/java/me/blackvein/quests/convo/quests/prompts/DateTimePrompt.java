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

package me.blackvein.quests.convo.quests.prompts;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class DateTimePrompt extends QuestsEditorNumericPrompt {
    private final Prompt oldPrompt;
    private String source = "";

    public DateTimePrompt(ConversationContext context, Prompt old, String origin) {
        super(context);
        oldPrompt = old;
        source = origin;
    }
    
    private final int size = 9;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("dateTimeTitle");
    }
    
    public String getDataText(ConversationContext context) {
        String dateData = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        if (context.getSessionData("tempDay") == null) {
            context.setSessionData("tempDay", cal.get(Calendar.DAY_OF_MONTH));
        }
        if (context.getSessionData("tempMonth") == null) {
            context.setSessionData("tempMonth", cal.get(Calendar.MONTH));
        }
        if (context.getSessionData("tempYear") == null) {
            context.setSessionData("tempYear", cal.get(Calendar.YEAR));
        }

        if (context.getSessionData("tempHour") == null) {
            context.setSessionData("tempHour", cal.get(Calendar.HOUR_OF_DAY));
        }
        if (context.getSessionData("tempMinute") == null) {
            context.setSessionData("tempMinute", cal.get(Calendar.MINUTE));
        }
        if (context.getSessionData("tempSecond") == null) {
            context.setSessionData("tempSecond", cal.get(Calendar.SECOND));
        }
        cal.set((Integer) context.getSessionData("tempYear"), (Integer) context.getSessionData("tempMonth"), 
                (Integer) context.getSessionData("tempDay"), (Integer) context.getSessionData("tempHour"), 
                (Integer) context.getSessionData("tempMinute"), (Integer) context.getSessionData("tempSecond"));
        dateData += ChatColor.DARK_AQUA + dateFormat.format(cal.getTime()) + " ";
        dateData += ChatColor.AQUA + timeFormat.format(cal.getTime()) + " ";
        
        if (context.getSessionData("tempZone") == null) {
            context.setSessionData("tempZone", cal.getTimeZone().getID());
        }
        TimeZone tz = TimeZone.getTimeZone((String) context.getSessionData("tempZone"));
        cal.setTimeZone(tz);
        String[] iso = Lang.getISO().split("-");
        Locale loc = new Locale(iso[0], iso[1]);
        Double hour = (double) (cal.getTimeZone().getRawOffset() / 60 / 60 / 1000);
        String[] sep = String.valueOf(hour).replace("-", "").split("\\.");
        DecimalFormat zoneFormat = new DecimalFormat("00");
        dateData += ChatColor.LIGHT_PURPLE + "UTC" + (hour < 0 ? "-":"+") + zoneFormat.format(Integer.valueOf(sep[0])) 
                + ":" + zoneFormat.format(Integer.valueOf(sep[1])) + ChatColor.GREEN + " (" 
                + cal.getTimeZone().getDisplayName(loc) + ")";
        return dateData;
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                return ChatColor.BLUE;
            case 8:
                return ChatColor.RED;
            case 9:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("timeDay");
        case 2:
            return ChatColor.YELLOW + Lang.get("timeMonth");
        case 3:
            return ChatColor.YELLOW + Lang.get("timeYear");
        case 4:
            return ChatColor.YELLOW + Lang.get("timeHour");
        case 5:
            return ChatColor.YELLOW + Lang.get("timeMinute");
        case 6:
            return ChatColor.YELLOW + Lang.get("timeSecond");
        case 7:
            return ChatColor.YELLOW + Lang.get("timeZone");
        case 8:
            return ChatColor.RED + Lang.get("cancel");
        case 9:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    public String getAdditionalText(ConversationContext context, int number) {
        switch(number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
        case 9:
            return "";
        default:
            return null;
        }
    }
    
    @Override
    public String getPromptText(ConversationContext context) {
        QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
    
        String text = ChatColor.AQUA + getTitle(context) + "\n" + getDataText(context) + "\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch(input.intValue()) {
        case 1:
            return new DayPrompt();
        case 2:
            return new MonthPrompt();
        case 3:
            return new YearPrompt();
        case 4:
            return new HourPrompt();
        case 5:
            return new MinutePrompt();
        case 6:
            return new SecondPrompt();
        case 7:
            return new OffsetPrompt();
        case 8:
            context.setSessionData("tempDay", null);
            context.setSessionData("tempMonth", null);
            context.setSessionData("tempYear", null);
            context.setSessionData("tempHour", null);
            context.setSessionData("tempMinute", null);
            context.setSessionData("tempSecond", null);
            context.setSessionData("tempZone", null);
            return oldPrompt;
        case 9:
            int day = (Integer) context.getSessionData("tempDay");
            int month = (Integer) context.getSessionData("tempMonth");
            int year = (Integer) context.getSessionData("tempYear");
            int hour = (Integer) context.getSessionData("tempHour");
            int minute = (Integer) context.getSessionData("tempMinute");
            int second = (Integer) context.getSessionData("tempSecond");
            String zone = (String) context.getSessionData("tempZone");
            String date = day + ":" + month + ":" + year + ":"
                    + hour + ":" + minute + ":" + second + ":" + zone;
            if (source != null) {
                if (source.equals("start")) {
                    context.setSessionData(CK.PLN_START_DATE, date);
                } else if (source.equals("end")) {
                    context.setSessionData(CK.PLN_END_DATE, date);
                }
            }
            return oldPrompt;
        default:
            return null;
        }
    }
    
    private class DayPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("dateCreateEnterDay");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                try {
                    int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 31) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "31"));
                        return new DayPrompt();
                    } else {
                        context.setSessionData("tempDay", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new DayPrompt();
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    private class MonthPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("dateCreateEnterMonth");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                try {
                    int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 12) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "12"));
                        return new MonthPrompt();
                    } else {
                        context.setSessionData("tempMonth", Integer.parseInt(input) - 1);
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new MonthPrompt();
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    private class YearPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("dateCreateEnterYear");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                try {
                    int amt = Integer.parseInt(input);
                    if (amt < 1000 || amt > 9999) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "1000").replace("<greatest>", "9999"));
                        return new YearPrompt();
                    } else {
                        context.setSessionData("tempYear", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new YearPrompt();
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    private class HourPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("dateCreateEnterHour");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                try {
                    int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 23) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "23"));
                        return new HourPrompt();
                    } else {
                        context.setSessionData("tempHour", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new HourPrompt();
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    private class MinutePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("dateCreateEnterMinute");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                try {
                    int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59"));
                        return new MinutePrompt();
                    } else {
                        context.setSessionData("tempMinute", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new MinutePrompt();
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    private class SecondPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("dateCreateEnterSecond");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                try {
                    int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59"));
                        return new SecondPrompt();
                    } else {
                        context.setSessionData("tempSecond", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new SecondPrompt();
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    private class OffsetPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            return ChatColor.YELLOW + Lang.get("dateCreateEnterOffset");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                try {
                    double amt = Double.parseDouble(input.replace("UTC", "").replace(":", "."));
                    if (amt < -12.0 || amt > 14.0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                            .replace("<least>", "-12:00").replace("<greatest>", "14:00"));
                        return new OffsetPrompt();
                    } else {
                        String[] t = TimeZone.getAvailableIDs((int) Math.round(amt * 60.0 * 60.0 * 1000.0));
                        if (t.length > 1) {
                            return new ZonePrompt(t);
                        } else if (t.length > 0) {
                            context.setSessionData("tempZone", t[0]);
                        }  else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                        }    
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new OffsetPrompt();
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    private class ZonePrompt extends StringPrompt {
        
        String[] zones;
        
        public ZonePrompt(String[] timezones) {
            zones = timezones;
        }

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = ChatColor.LIGHT_PURPLE + Lang.get("timeZoneTitle") + "\n";
            for (String z : zones) {
                text += ChatColor.GREEN + z + ", ";
            }
            text = text.substring(0, text.length() - 2);
            return text + "\n" + ChatColor.YELLOW + Lang.get("dateCreateEnterZone");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (String z : zones) {
                    if (z.toLowerCase().startsWith(input.toLowerCase())) {
                        context.setSessionData("tempZone", z);
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                return new ZonePrompt(zones);
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
}
