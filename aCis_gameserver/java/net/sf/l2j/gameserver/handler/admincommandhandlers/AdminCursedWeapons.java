/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.CursedWeapon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>cw_info : displays cursed weapons status.</li>
 * <li>cw_remove : removes a cursed weapon from the world - item id or name must be provided.</li>
 * <li>cw_add : adds a cursed weapon into the world - item id or name must be provided. Current target will be the owner.</li>
 * <li>cw_goto : teleports GM to the specified cursed weapon (item or player position).</li>
 * <li>cw_reload : reloads instance manager.</li>
 * </ul>>
 */
public class AdminCursedWeapons implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_cw_info",
		"admin_cw_remove",
		"admin_cw_goto",
		"admin_cw_add",
		"admin_cw_info_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_cw_info"))
		{
			if (!command.contains("menu"))
			{
				activeChar.sendMessage("====== Cursed Weapons: ======");
				for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
				{
					activeChar.sendMessage(cw.getName() + " (" + cw.getItemId() + ")");
					if (cw.isActive())
					{
						long milliToStart = cw.getTimeLeft();
						
						double numSecs = (milliToStart / 1000) % 60;
						double countDown = ((milliToStart / 1000) - numSecs) / 60;
						int numMins = (int) Math.floor(countDown % 60);
						countDown = (countDown - numMins) / 60;
						int numHours = (int) Math.floor(countDown % 24);
						int numDays = (int) Math.floor((countDown - numHours) / 24);
						
						if (cw.isActivated())
						{
							final L2PcInstance pl = cw.getPlayer();
							activeChar.sendMessage("  Owner: " + (pl == null ? "null" : pl.getName()));
							activeChar.sendMessage("  Stored values: karma=" + cw.getPlayerKarma() + " PKs=" + cw.getPlayerPkKills());
							activeChar.sendMessage("  Current stage:" + cw.getCurrentStage());
							activeChar.sendMessage("  Overall time: " + numDays + " days " + numHours + " hours " + numMins + " min.");
							activeChar.sendMessage("  Hungry time: " + cw.getHungryTime() + " min.");
							activeChar.sendMessage("  Current kills : " + cw.getNbKills() + " / " + cw.getNumberBeforeNextStage());
						}
						else if (cw.isDropped())
						{
							activeChar.sendMessage("  Lying on the ground.");
							activeChar.sendMessage("  Time remaining: " + numDays + " days " + numHours + " hours " + numMins + " min.");
						}
					}
					else
						activeChar.sendMessage("  Doesn't exist in the world.");
					
					activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
				}
			}
			else
			{
				final StringBuilder sb = new StringBuilder(2000);
				for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
				{
					StringUtil.append(sb, "<table width=280><tr><td>Name:</td><td>", cw.getName(), "</td></tr>");
					
					if (cw.isActive())
					{
						long milliToStart = cw.getTimeLeft();
						
						double numSecs = (milliToStart / 1000) % 60;
						double countDown = ((milliToStart / 1000) - numSecs) / 60;
						int numMins = (int) Math.floor(countDown % 60);
						countDown = (countDown - numMins) / 60;
						int numHours = (int) Math.floor(countDown % 24);
						int numDays = (int) Math.floor((countDown - numHours) / 24);
						
						if (cw.isActivated())
						{
							L2PcInstance pl = cw.getPlayer();
							StringUtil.append(sb, "<tr><td>Owner:</td><td>", ((pl == null) ? "null" : pl.getName()), "</td></tr><tr><td>Stored values:</td><td>Karma=", cw.getPlayerKarma(), " PKs=", cw.getPlayerPkKills(), "</td></tr><tr><td>Current stage:</td><td>", cw.getCurrentStage(), "</td></tr><tr><td>Overall time:</td><td>", numDays, "d. ", numHours, "h. ", numMins, "m.</td></tr><tr><td>Hungry time:</td><td>", cw.getHungryTime(), "m.</td></tr><tr><td>Current kills:</td><td>", cw.getNbKills(), " / ", cw.getNumberBeforeNextStage(), "</td></tr><tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td><button value=\"Go\" action=\"bypass -h admin_cw_goto ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td></tr>");
						}
						else if (cw.isDropped())
							StringUtil.append(sb, "<tr><td>Position:</td><td>Lying on the ground</td></tr><tr><td>Overall time:</td><td>", numDays, "d. ", numHours, "h. ", numMins, "m.</td></tr><tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td><button value=\"Go\" action=\"bypass -h admin_cw_goto ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td></tr>");
					}
					else
						StringUtil.append(sb, "<tr><td>Position:</td><td>Doesn't exist.</td></tr><tr><td><button value=\"Give to Target\" action=\"bypass -h admin_cw_add ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td></td></tr>");
					
					sb.append("</table><br>");
				}
				
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/admin/cwinfo.htm");
				html.replace("%cwinfo%", sb.toString());
				activeChar.sendPacket(html);
			}
		}
		else
		{
			try
			{
				int id = 0;
				
				String parameter = st.nextToken();
				if (parameter.matches("[0-9]*"))
					id = Integer.parseInt(parameter);
				else
				{
					parameter = parameter.replace('_', ' ');
					for (CursedWeapon cwp : CursedWeaponsManager.getInstance().getCursedWeapons())
					{
						if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
						{
							id = cwp.getItemId();
							break;
						}
					}
				}
				
				final CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(id);
				if (cw == null)
				{
					activeChar.sendMessage("Unknown cursed weapon ID.");
					return false;
				}
				
				if (command.startsWith("admin_cw_remove "))
					cw.endOfLife();
				else if (command.startsWith("admin_cw_goto "))
					cw.goTo(activeChar);
				else if (command.startsWith("admin_cw_add"))
				{
					if (cw.isActive())
						activeChar.sendMessage("This cursed weapon is already active.");
					else
					{
						L2Object target = activeChar.getTarget();
						if (target instanceof L2PcInstance)
							((L2PcInstance) target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
						else
							activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
						
						// Start task
						cw.reActivate(true);
					}
				}
				else
					activeChar.sendMessage("Unknown command.");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //cw_remove|//cw_goto|//cw_add <itemid|name>");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}