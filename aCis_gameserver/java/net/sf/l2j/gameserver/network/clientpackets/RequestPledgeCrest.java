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
package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.CrestCache.CrestType;

import net.sf.l2j.gameserver.network.serverpackets.PledgeCrest;

public final class RequestPledgeCrest extends L2GameClientPacket
{
	  private static Logger _log = Logger.getLogger(RequestPledgeCrest.class.getName());
	private int _crestId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}
	
	@Override
	protected void runImpl()
	    {
	               if (_crestId == 0)
	    {
	               return;
	    }
	       if (Config.DEBUG)
	    {
	               _log.fine("crestid " + _crestId + " requested");
	    }
	    
	 byte[] data = CrestCache.getCrest(CrestType.PLEDGE,_crestId);     
	     
	     if (data != null)
	     {
	                PledgeCrest pc = new PledgeCrest(_crestId, data);
	                sendPacket(pc);
	     }
	     else
	    {
	                if (Config.DEBUG)
	     {
	                        _log.fine("crest is missing:" + _crestId);
	    }
	    }
	     }
		@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
