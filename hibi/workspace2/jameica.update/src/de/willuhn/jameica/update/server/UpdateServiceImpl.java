/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/server/UpdateServiceImpl.java,v $
 * $Revision: 1.9 $
 * $Date: 2009/10/30 00:11:38 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.server;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.messaging.TextMessage;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.Settings;
import de.willuhn.jameica.update.rmi.PluginData;
import de.willuhn.jameica.update.rmi.Repository;
import de.willuhn.jameica.update.rmi.RepositoryService;
import de.willuhn.jameica.update.rmi.UpdateService;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung des Update-Services.
 */
public class UpdateServiceImpl implements UpdateService
{
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(UpdateService.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  private Timer timer = null;
  private Worker worker = null;

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "Update-Service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted() && Settings.getUpdateCheck();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.timer != null && this.worker != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }
    
    if (!Settings.getUpdateCheck()){
      Logger.info("update check disabled");
      return;
    }

    Logger.info("update check interval: " + Settings.getUpdateInterval() + " days");

    this.timer = new Timer(getName(),true);
    this.worker = new Worker();

    // Wir checken einmal direkt nach dem Start und dann haeufiger als es im
    // Update-Intervall angegeben ist. Ob tatsaechlich nach Updates gesucht werden
    // soll, entscheidet dann der Worker intern auch anhand des Datums der letzten
    // Pruefung.
    Logger.info("starting updater thread");
    this.timer.schedule(this.worker,10 * 1000L, 24 * 60 * 60 * 1000L); // alle 24h pruefen, ob geprueft werden soll ;)
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }

    if (this.worker != null)
    {
      try
      {
        Logger.info("stopping updater thread");
        this.worker.cancel();
      }
      catch (Exception e) {
        Logger.error("error while stopping updater thread",e);
      }
      finally
      {
        this.worker = null;
      }
    }

    if (this.timer != null)
    {
      try
      {
        Logger.info("stopping timer task");
        this.timer.cancel();
      }
      catch (Exception e) {
        Logger.error("error while stopping timer task",e);
      }
      finally
      {
        this.timer = null;
      }
    }
  }

  /**
   * @see de.willuhn.jameica.update.rmi.UpdateService#findUpdates(de.willuhn.util.ProgressMonitor)
   */
  public List<PluginData> findUpdates(ProgressMonitor monitor) throws ApplicationException
  {
    List<UpdateStatus> states = this.findUpdateStates(monitor);
    List<PluginData> plugins = new ArrayList<PluginData>();

    for (UpdateStatus status:states)
      plugins.add(status.plugin);
    
    return plugins;
  }

  /**
   * Sammelt die Status-Infos zu aktualisierbaren Plugins.
   * @param monitor optionaler Status-Monitor.
   * @return Liste der gefundenen Updates.
   * @throws ApplicationException
   */
  private List<UpdateStatus> findUpdateStates(ProgressMonitor monitor) throws ApplicationException
  {
    // Der Aufruf dieser Funktion soll auch gehen, wenn der Worker-Thread nicht
    // laeuft. Damit kann man manuell nach Updates suchen - auch wenn automatische
    // Updates deaktiviert sind.
    // Wir sammeln alle Updates und fuehren es dann am Stueck durch.

    try
    {
      List<UpdateStatus> updates = new ArrayList<UpdateStatus>();

      Logger.info("checking for updates");
      if (monitor != null) monitor.setStatusText(i18n.tr("Suche nach Updates"));
      RepositoryService service = (RepositoryService) Application.getServiceFactory().lookup(Plugin.class,"repository");
      List<URL> urls = service.getRepositories();
      
      for (URL url:urls)
      {
        Logger.info("checking repository " + url);
        try
        {
          if (monitor != null)
          {
            monitor.log(i18n.tr("Pr�fe {0}",url.toString()));
            monitor.addPercentComplete(10);
          }
          Repository repo = service.open(url);
          List<PluginData> plugins = repo.getPlugins();
          for (PluginData plugin:plugins)
          {
            if (monitor != null) monitor.addPercentComplete(5);
            try
            {
              UpdateStatus status = new UpdateStatus(plugin);
              if (!status.available)
                continue;
              
              Logger.info("update available for plugin " + plugin.getName());
              if (monitor != null)
              {
                monitor.log("  " + i18n.tr("Update gefunden: {0}",plugin.getName()));
                monitor.addPercentComplete(5);
              }
              updates.add(status);
            }
            catch (Exception e)
            {
              if (monitor != null) monitor.log("  " + i18n.tr("Fehler beim Pr�fen des Plugins {0}: {1}",new String[]{plugin.getName(),e.getMessage()}));
              Logger.error("error while checking plugin " + plugin.getName(),e);
            }
          }
        }
        catch (Exception e)
        {
          if (monitor != null) monitor.log(i18n.tr("Fehler beim Pr�fen des Repository: {0}",e.getMessage()));
          Logger.error("error while checking repository " + url,e);
        }
      }
      
      if (monitor != null)
      {
        monitor.setStatus(ProgressMonitor.STATUS_DONE);
        monitor.setPercentComplete(100);
        monitor.setStatusText(i18n.tr("Suche beendet. Gefundene Updates: {0}",String.valueOf(updates.size())));
      }
      return updates;
    }
    catch (Exception e)
    {
      if (e instanceof ApplicationException)
        throw (ApplicationException) e;

      Logger.error("error while searching for updates",e);
      throw new ApplicationException(i18n.tr("Suche nach Updates erfolglos: {0}",e.getMessage()));
    }
  }

  /**
   * Unser Worker.
   */
  private class Worker extends TimerTask
  {
    /**
     * @see java.util.TimerTask#run()
     */
    public void run()
    {
      // Damit das Intervall auch dann eingehalten wird, wenn Jameica haeufig
      // neu gestartet wird (Beispiel: Als Intervall sind 7 Tage eingestellt,
      // die Anwendung wird aber mehrmals taeglich gestartet) checken wir
      // erst, wann die letzte Pruefung stattfand. Wenn das weniger her ist
      // als das angegebene Intervall, dann lassen wir die Pruefung
      long lastRun = settings.getLong("lastrun",0L);
      long nextRun = lastRun + (Settings.getUpdateInterval() * 24 * 60 * 60 * 1000L);
      long now     = System.currentTimeMillis();
      
      Logger.info("check for updates needed?");

      // Wir starten nicht, wenn die letzte Pruefung weniger als x Tage her ist
      if (nextRun > now)
      {
        Logger.info("no, last check: " + new Date(lastRun) + ", next check: " + new Date(nextRun));
        return;
      }
      Logger.info("last check was on " + new Date(lastRun));

      try
      {
        if (!Application.inServerMode())
          GUI.getStatusBar().startProgress();
        
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Suche nach Updates"),StatusBarMessage.TYPE_SUCCESS));

        // Wir sammeln alle Updates und fuehren es dann am Stueck durch.
        List<UpdateStatus> states = findUpdateStates(null);
        
        // Haben wir Updates gefunden?
        if (states.size() == 0)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Keine Updates gefunden."),StatusBarMessage.TYPE_SUCCESS));
          return;
        }

        // Updates installieren
        if (Settings.getUpdateInstall())
        {
          StringBuffer names = new StringBuffer();
          for (UpdateStatus status:states)
          {
            try
            {
              PluginData pd = status.plugin;
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Lade Update f�r Plugin {0}",pd.getName()),StatusBarMessage.TYPE_SUCCESS));
              Repository repo = pd.getPluginGroup().getRepository();
              repo.download(pd,false);
              status.update();
              names.append(pd.getName());
              names.append("\n");
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Update f�r Plugin {0} installiert",pd.getName()),StatusBarMessage.TYPE_SUCCESS));
            }
            catch (Exception e)
            {
              Logger.error("unable to download plugin",e);
            }
          }
          TextMessage qm = new TextMessage(i18n.tr("Updates installiert"),i18n.tr("F�r folgende Plugins wurden Updates installiert:\n\n{0}\nBitte starten Sie Jameica neu.",names.toString()));
          Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(qm);
        }
        else
        {
          // nur benachrichtigen
          StringBuffer names = new StringBuffer();
          int count = 0;
          for (UpdateStatus status:states)
          {
            if (status.notified)
              continue; // ueber das Update haben wir den User schon benachrichtigt
            status.update();
            names.append(status.plugin.getName());
            names.append("\n");
            count++;
          }
          
          // Popup nur anzeigen, wenn seit dem letzten Check neue Updates dazugekommen sind
          if (count > 0)
          {
            TextMessage qm = new TextMessage(i18n.tr("Neue Updates verf�gbar"),i18n.tr("F�r folgende Plugins sind Updates verf�gbar:\n\n{0}",names.toString()));
            Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(qm);
          }
        }
        
        // Auch wenn wir den User schonmal via Popup ueber das Update benachrichtigt haben,
        // schreiben wir es als kleine Erinnerung wenigstens noch in die Statuszeile
        String text = states.size() == 1 ? i18n.tr("1 Update gefunden") : i18n.tr("{0} Updates gefunden",String.valueOf(states.size()));
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_SUCCESS));
      }
      catch (Exception e)
      {
        Logger.error("error while checking for updates",e);
      }
      finally
      {
        // wir speichern das Datum des Check
        Logger.info("save check date: " + new Date(now));
        settings.setAttribute("lastrun",now);

        if (!Application.inServerMode())
          GUI.getStatusBar().stopProgress();
      }
    }
  }
  
  /**
   * Hilfsklasse, um den Update-Status zu pruefen.
   */
  private class UpdateStatus
  {
    private PluginData plugin = null;
    private boolean available = false;
    private boolean notified  = false;
    
    /**
     * ct.
     * @param p das zu checkende Plugin.
     * @throws RemoteException
     */
    private UpdateStatus(PluginData p) throws RemoteException
    {
      this.plugin = p;
      
      Logger.debug("checking plugin " + this.plugin.getName());
      
      // Aktuell installierte Version
      Version vc = this.plugin.getInstalledVersion();

      // Verfuegbare Version
      Version va = plugin.getAvailableVersion();

      // Nicht installiert
      if (vc == null)
      {
        Logger.debug("not installed");
        return;
      }
      
      if (va == null)
      {
        Logger.debug("no version available");
        return;
      }
      
      // wegen fehlenden Abhaengigkeiten ohnehin nicht installierbar
      if (!plugin.isInstallable())
      {
        Logger.debug("not installable");
        return;
      }


      // Verfuegbare Version, die dem User zuletzt gemeldet wurde.
      Version vn = new Version(settings.getString(plugin.getName(),vc.toString()));
      
      Logger.debug("checking version. current: " + vc + ", available: " + va + ", last notified: " + vn);

      // Wenn die verfuegbare Version hoeher als die installierte ist, ist grundsaetzlich ein Update verfuegbar
      this.available = (va.compareTo(vc) > 0);
      
      // Die die zuletzt gepruefte Version groesser oder gleich der verfuegbaren ist, haben wir den User
      // schonmal ueber diese Version benachrichtigt
      this.notified = (vn.compareTo(va) >= 0);

      // Nur fuers Protokoll
      if (this.available)
        Logger.info("found update for plugin " + plugin.getName() + " from " + vc + " to " + va);
    }
    
    /**
     * Setzt den Benachrichtigungsstatus auf die aktuelle Version.
     * @throws RemoteException
     */
    private void update() throws RemoteException
    {
      if (!this.available)
        return; // nicht noetig, da ohnehin keine neue Version verfuegbar war
      Version va = this.plugin.getAvailableVersion();
      if (va != null)
      {
        Logger.info("set notified version from " + settings.getString(this.plugin.getName(),null) + " to " + va);
        settings.setAttribute(this.plugin.getName(),va.toString());
      }
    }
  }
}


/**********************************************************************
 * $
 **********************************************************************/
