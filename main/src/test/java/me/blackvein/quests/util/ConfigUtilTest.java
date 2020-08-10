package me.blackvein.quests.util;

import org.bukkit.ChatColor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigUtilTest {
	@Test
	public void testParseStringColors() {
		// Basic colors
		assertEquals(
				"test " + ChatColor.BLACK.toString() + " message " + ChatColor.BLUE.toString() + " with <unknown> some colors",
				ConfigUtil.parseString("test <black> message <blue> with <unknown> some colors")
		);
		
		// Formatting
		assertEquals(
				"test " + ChatColor.BOLD.toString() + " message " + ChatColor.UNDERLINE.toString() + " with some colors",
				ConfigUtil.parseString("test <bold> message <underline> with some colors")
		);
		
		// Special characters
		assertEquals(
				"test \n message",
				ConfigUtil.parseString("test <br> message")
		);
		
		// HEX
		final char CC = ChatColor.COLOR_CHAR;
		assertEquals(
				"test hex " + CC + "x" + CC + "f" + CC + "f" + CC + "0" + CC + "0" + CC + "0" + CC + "0"
				+ ", " + CC + "x" + CC + "1" + CC + "2" + CC + "3" + CC + "4" + CC + "5" + CC + "6"
				+ " and <#unknown>",
				ConfigUtil.parseString("test hex <#ff0000>, <#123456> and <#unknown>")
		);
	}
}
