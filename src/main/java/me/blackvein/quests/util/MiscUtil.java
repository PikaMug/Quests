/*******************************************************************************************************
 * Continued by FlyingPikachu/HappyPikachu with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.util;

import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;

public class MiscUtil {

	public static String getCapitalized(String s) {
		if (s.isEmpty()) {
			return s;
		}
		s = s.toLowerCase();
		String s2 = s.substring(0, 1);
		s2 = s2.toUpperCase();
		s = s.substring(1, s.length());
		return s2 + s;
	}
	
	public static String getProperEnchantmentName(Enchantment enchantment) {
		String name = enchantment.getName().toLowerCase();
		return name;
	}

	public static String getProperMobName(EntityType type) {
		String name = type.name().toLowerCase();
		name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		while (fixUnderscore(name) != null) {
			name = fixUnderscore(name);
		}
		return name;
	}

	public static EntityType getProperMobType(String properName) {
		properName = properName.replaceAll("_", "").replaceAll(" ", "").toUpperCase();
		for (EntityType et : EntityType.values()) {
			if (et.isAlive() && et.name().replaceAll("_", "").equalsIgnoreCase(properName)) {
				return et;
			}
		}
		return null;
	}

	private static String fixUnderscore(String s) {
		int index = s.indexOf('_');
		if (index == -1) {
			return null;
		}
		s = s.substring(0, (index + 1)) + Character.toUpperCase(s.charAt(index + 1)) + s.substring(index + 2);
		s = s.replaceFirst("_", "");
		return s;
	}

	public static String concatArgArray(String[] args, int startingIndex, int endingIndex, char delimiter) {
		String s = "";
		for (int i = startingIndex; i <= endingIndex; i++) {
			s += args[i] + delimiter;
		}
		s = s.substring(0, s.length());
		return s.trim().equals("") ? null : s.trim();
	}

	public static LinkedList<String> makeLines(String s, String wordDelimiter, int lineLength, ChatColor lineColor) {
		LinkedList<String> toReturn = new LinkedList<String>();
		String[] split = s.split(wordDelimiter);
		String line = "";
		int currentLength = 0;
		for (String piece : split) {
			if ((currentLength + piece.length()) > (lineLength + 1)) {
				if (lineColor != null) {
					toReturn.add(lineColor + line.replaceAll("^" + wordDelimiter, ""));
				} else {
					toReturn.add(line.replaceAll("^" + wordDelimiter, ""));
				}
				line = piece + wordDelimiter;
				currentLength = piece.length() + 1;
			} else {
				line += piece + wordDelimiter;
				currentLength += piece.length() + 1;
			}
		}
		if (line.equals("") == false)
			if (lineColor != null) {
				toReturn.add(lineColor + line);
			} else {
				toReturn.add(line);
			}
		return toReturn;
	}
}