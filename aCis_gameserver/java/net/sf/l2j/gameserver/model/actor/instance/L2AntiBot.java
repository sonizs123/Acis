
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.skills.AbnormalEffect;

/**
 *
* @author: Fissban - H5
* @rework: CaFi - Fronze - C6
* @rework: SoultakerNo1 - aCis - C6
*/

public class L2AntiBot
{
   public static final Logger _log = Logger.getLogger(L2AntiBot.class.getName());
   private static L2AntiBot _instance;
   public static ScheduledFuture<?> _antiBotTask;
    
   private int _antibot;
   private static String _code = "";
    
   public L2AntiBot()
   {
   }
    
   public static L2AntiBot getInstance()
   {
      if (_instance == null)
      {
          _instance = new L2AntiBot();
      }
      return _instance;
   }
    
   private static final String[] _IMG =
   {
   "Numeros.Keys0",
   "Numeros.Keys1",
   "Numeros.Keys2",
   "Numeros.Keys3",
   "Numeros.Keys4",
   "Numeros.Keys5",
   "Numeros.Keys6",
   "Numeros.Keys7",
   "Numeros.Keys8",
   "Numeros.Keys9"
   };
    
   public void antibot(L2Character player)
   {
      _antibot++;

      if (!((L2PcInstance) player).isGM())
      if (_antibot >= Config.ANTIBOT_KILL_MOBS)
      {
         _antibot = 0;
         _antiBotTask = ThreadPoolManager.getInstance().scheduleGeneral(new StartAntiBotTask(player), Config.ANTIBOT_TIME_VOTE * 1000);
      }
    }
    
   public static class StartAntiBotTask implements Runnable
   {
    
      L2Character _player_cha;
      NpcHtmlMessage _npcHtmlMessage = new NpcHtmlMessage(0);
        
      protected StartAntiBotTask(L2Character player)
      {
         if (player != null)
         {
            _player_cha = player;
              
            _player_cha.getActingPlayer().setIsParalyzed(true);
            _player_cha.getActingPlayer().setIsInvul(true);
            _player_cha.getActingPlayer().startAbnormalEffect(AbnormalEffect.FLOATING_ROOT);
            _player_cha.getActingPlayer().sendPacket(new ExShowScreenMessage("Você tem " + Config.ANTIBOT_TIME_VOTE + " segundos para confirma a Senha!", 10000));
            _player_cha.getActingPlayer().getActingPlayer().sendPacket(new CreatureSay(0, Say2.BATTLEFIELD, "[AntiBot]", "Você tem  " + Config.ANTIBOT_TIME_VOTE + "  segundos para confirma a Senha!"));
            _npcHtmlMessage.setHtml(ShowHtml_Start(_player_cha));
            _player_cha.getActingPlayer().sendPacket(_npcHtmlMessage);
         }
         else
         {
                return;
         }
      }
        
      @Override
      public void run()
      {
         if (!_player_cha.getActingPlayer().isInJail())
         {
            _player_cha.getActingPlayer().sendPacket(new CreatureSay(0, Say2.TELL, "[AntiBot]", "Seu tempo expirou!"));
            _player_cha.getActingPlayer().increaseAttempt();
              
            if (_player_cha.getActingPlayer().getAttempt() >= 3)
            {
               _player_cha.getActingPlayer().setIsParalyzed(false);
               _player_cha.getActingPlayer().setIsInvul(false);
               _player_cha.getActingPlayer().stopAbnormalEffect(AbnormalEffect.FLOATING_ROOT);
               _player_cha.getActingPlayer().getActingPlayer().setPunishLevel(L2PcInstance.PunishLevel.JAIL, Config.ANTIBOT_TIME_JAIL);
               _player_cha.getActingPlayer().getActingPlayer().sendPacket(new CreatureSay(0, Say2.TELL, "[AntiBot]", "Character " + _player_cha.getActingPlayer().getName() + " jailed for " + Config.ANTIBOT_TIME_JAIL + " minutes!"));
               _log.warning("[AntiBot - L2 poseidon] " + _player_cha.getActingPlayer().getName() + " jail por" + Config.ANTIBOT_TIME_JAIL + " minutos!");
             }
             else
             {
                  _antiBotTask = ThreadPoolManager.getInstance().scheduleGeneral(new StartAntiBotTask(_player_cha), Config.ANTIBOT_TIME_VOTE * 1000);
             }
           }
        }
     }  
    
     public static String ShowHtml_Start(L2Character player)
     {
        String htmltext = "";
        htmltext += "<html>";
        htmltext += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"292\" height=\"350\" background=\"L2UI_CH3.refinewnd_back_Pattern\">";
        htmltext += "<tr><td valign=\"top\" align=\"center\">";
        htmltext += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">";
        htmltext += "</table><br><br>";
        htmltext += "<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br></center>";
        htmltext += "<center><font color=\"00C3FF\">" + player.getName() + "<font color=\"LEVEL\"> enter the captcha</center><br><br>";
        htmltext += "<center><font color=\"LEVEL\">Voce ainda tem chanse " + (3 - player.getActingPlayer().getAttempt()) + " complete a senha</center><br><br>";
        htmltext += "<br>";
        
        htmltext += "<table>";
        htmltext += "<tr>";
        _code = "";
        for (int cont = 1; cont < 5; cont++)
        {
          int number = Rnd.get(_IMG.length - 1);
          _code += String.valueOf(number);
          
          htmltext += "<td><right><img src=\"" + _IMG[number] + "\" width=37 height=31 ></right></td>";
        }
        
        player.getActingPlayer().setCode(_code);
        htmltext += "</tr>";
        htmltext += "</table><br>";
        
        htmltext += "<center><edit type=\"captcha\" var=\"captcha\" width=\"150\"></center>";
        htmltext += "<br>";
        htmltext += "<center><button value=\"Confirm\" action=\"bypass -h antibot $captcha\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></center><br1>";
        htmltext += "<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><br>";
        htmltext += "</html></body>";
        
        return htmltext;
      }
    
     public static String ShowHtml_End(L2Character player)
     {
        String htmltext = "";
        htmltext += "<html>";
        htmltext += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"292\" height=\"350\" background=\"L2UI_CH3.refinewnd_back_Pattern\">";
        htmltext += "<tr><td valign=\"top\" align=\"center\">";
        htmltext += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">";
        htmltext += "</table><br><br>";
        htmltext += "<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br></center>";
        htmltext += "<center><font color=\"00C3FF\">" + player.getName() + "<font color=\"LEVEL\"> Errou a senha</center><br><br>";
        htmltext += "<br><br>";
        htmltext += "<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><br>";
        htmltext += "</html></body>";
        return htmltext;
     }
    
     // bypass
     public void checkCode(L2PcInstance player, String code)
     {
        if (code.equals(player.getCode()))
        {
           stopAntiBotTask();
           player.setCheckCode(true);
           player.resetAttemp();
          
           player.sendPacket(new CreatureSay(0, Say2.TELL, "[AntiBot]", "Anti-bot conferido com sucesso, até aproxima!!"));
           player.setIsParalyzed(false);
           player.setIsInvul(false);
           player.stopAbnormalEffect(AbnormalEffect.FLOATING_ROOT);
        }
        else
        {
           stopAntiBotTask();
           player.setCheckCode(false);
           player.increaseAttempt();
          
           _antiBotTask = ThreadPoolManager.getInstance().scheduleGeneral(new StartAntiBotTask(player), Config.ANTIBOT_TIME_VOTE * 1000);
        }
        
        if (player.getAttempt() >= 3)
        {
           stopAntiBotTask();
           player.resetAttemp();
          
           player.setIsParalyzed(false);
           player.setIsInvul(false);
          player.stopAbnormalEffect(AbnormalEffect.FLOATING_ROOT);
          
           player.getActingPlayer().setPunishLevel(L2PcInstance.PunishLevel.JAIL, Config.ANTIBOT_TIME_JAIL);
           player.getActingPlayer().sendPacket(new CreatureSay(0, Say2.TELL, "[AntiBot]", "Character " + player.getName() + " jail por " + Config.ANTIBOT_TIME_JAIL + " minutos!"));
                 _log.warning("[AntiBot] Character " + player.getName() + " jail por" + Config.ANTIBOT_TIME_JAIL + " minutos!");
                 L2World.getInstance();
                 L2PcInstance allgms = null ;
               //  for (L2PcInstance allgms : L2World.getAllGMs())
                 allgms.sendPacket(new CreatureSay(16, Say2.SHOUT, "[AntiBot] The player ", player.getName() + " risco de boot"));
        }
     }
    
     public static void stopAntiBotTask()
     {
       if (_antiBotTask != null)
        {
           _antiBotTask.cancel(false);
           _antiBotTask = null;
        }
     }
  }