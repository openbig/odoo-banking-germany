/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/ext/SettingsView.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/10/28 17:00:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.ext;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.internal.views.Settings;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.gui.parts.RepositoryList;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Erweitert die View mit dem System-Einstellungen um die Update-Optionen.
 */
public class SettingsView implements Extension
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  private CheckboxInput updateCheck   = null;
  private SpinnerInput updateInterval = null;
  private SelectInput updateInstall   = null;
  
  private MessageConsumer mc = null;
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  public void extend(Extendable extendable)
  {
    if (extendable == null || !(extendable instanceof Settings))
      return;

    this.mc = new MessageConsumer() {

      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
       */
      public void handleMessage(Message message) throws Exception
      {
        handleStore();
      }

      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
       */
      public Class[] getExpectedMessageTypes()
      {
        return new Class[]{SettingsChangedMessage.class};
      }

      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
       */
      public boolean autoRegister()
      {
        return false;
      }
    };
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

    
    Settings settings = (Settings) extendable;
    
    try
    {
      TabGroup tab = new TabGroup(settings.getTabFolder(),i18n.tr("Updates"),true);
      
      // Da wir keine echte View sind, haben wir auch kein unbind zum Aufraeumen.
      // Damit wir unsere GUI-Elemente aber trotzdem disposen koennen, registrieren
      // wir einen Dispose-Listener an der Tabgroup
      tab.getComposite().addDisposeListener(new DisposeListener() {
      
        public void widgetDisposed(DisposeEvent e)
        {
          updateCheck = null;
          updateInstall = null;
          updateInterval = null;
          Application.getMessagingFactory().unRegisterMessageConsumer(mc);
        }
      
      });
      tab.addHeadline(i18n.tr("Update-Repositories"));
      tab.addPart(new RepositoryList());
      
      tab.addHeadline(i18n.tr("Einstellungen"));
      tab.addInput(this.getUpdateCheck());
      tab.addInput(this.getUpdateInterval());
      tab.addInput(this.getUpdateInstall());
    }
    catch (Exception e)
    {
      Logger.error("unable to extend settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen der Update-Einstellungen"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Liefert eine Checkbox mit der Auswahl, ob automatisch nach Updates gesucht werden soll.
   * @return Checkbox.
   */
  private CheckboxInput getUpdateCheck()
  {
    if (this.updateCheck == null)
    {
      Listener l = new Listener() {
        /**
         * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
         */
        public void handleEvent(Event event)
        {
          Boolean b = (Boolean) getUpdateCheck().getValue();
          getUpdateInstall().setEnabled(b.booleanValue());
          getUpdateInterval().setEnabled(b.booleanValue());
        }
      };

      this.updateCheck = new CheckboxInput(de.willuhn.jameica.update.Settings.getUpdateCheck());
      this.updateCheck.setName(i18n.tr("Automatisch nach Updates von installierten Plugins suchen"));
      this.updateCheck.addListener(l);
      
      // einmal manuell auslesen fuer den initialen Status
      l.handleEvent(null);
    }
    return this.updateCheck;
  }
  
  /**
   * Liefert eine Auswahlbox mit den moeglichen Intervallen.
   * @return Auswahlbox.
   */
  private SpinnerInput getUpdateInterval()
  {
    if (this.updateInterval == null)
    {
      this.updateInterval = new SpinnerInput(1,365,de.willuhn.jameica.update.Settings.getUpdateInterval());
      this.updateInterval.setComment(i18n.tr("Tage"));
      this.updateInterval.setMandatory(true);
      this.updateInterval.setName(i18n.tr("Nach Updates suchen alle"));
    }
    return this.updateInterval;
  }
  
  /**
   * Liefert eine Auswahlbox fuer die moeglichen Aktionen bei Vorhandensein von Updates.
   * @return Auswahlbox.
   */
  private SelectInput getUpdateInstall()
  {
    if (this.updateInstall == null)
    {
      List<Option> values = new ArrayList<Option>();
      values.add(new Option(false,i18n.tr("Nur benachrichtigen")));
      values.add(new Option(true,i18n.tr("Automatisch herunterladen und installieren")));
      
      this.updateInstall = new SelectInput(values,new Option(de.willuhn.jameica.update.Settings.getUpdateInstall(),null));
      this.updateInstall.setName(i18n.tr("Wenn Updates vorhanden sind"));
      this.updateInstall.setComment("");
      this.updateInstall.setMandatory(true);
    }
    return this.updateInstall;
  }
  
  /**
   * Speichert die Einstellungen.
   */
  private void handleStore()
  {
    de.willuhn.jameica.update.Settings.setUpdateCheck(((Boolean)this.getUpdateCheck().getValue()).booleanValue());
    de.willuhn.jameica.update.Settings.setUpdateInterval(((Integer)this.getUpdateInterval().getValue()).intValue());
    
    Option action = (Option) this.getUpdateInstall().getValue();
    de.willuhn.jameica.update.Settings.setUpdateInstall(action.value);
  }
  
  /**
   * Hilfsklasse fuer die Auswahl der Aktionen.
   */
  private class Option
  {
    private boolean value;
    private String text;
    
    /**
     * ct.
     * @param value Wert der Option.
     * @param text Bezeichnung der Option.
     */
    private Option(boolean value,String text)
    {
      this.value = value;
      this.text = text;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return this.text;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
      if (obj == null || !(obj instanceof Option))
        return false;
      return this.value == ((Option)obj).value;
    }
  }
}


/*********************************************************************
 * $Log: SettingsView.java,v $
 * Revision 1.3  2009/10/28 17:00:58  willuhn
 * @N Automatischer Check nach Updates mit der Wahlmoeglichkeit, nur zu benachrichtigen oder gleich zu installieren
 *
 * Revision 1.2  2008/12/16 10:46:08  willuhn
 * @N Funktionalitaet zum Hinzufuegen und Loeschen von Repository-URLs
 *
 * Revision 1.1  2008/12/16 09:38:52  willuhn
 * @C Config-Dialog in "Einstellungen" verschoben
 *
 *********************************************************************/