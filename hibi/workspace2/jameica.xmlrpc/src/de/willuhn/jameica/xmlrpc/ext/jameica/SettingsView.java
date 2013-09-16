/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/ext/jameica/SettingsView.java,v $
 * $Revision: 1.14 $
 * $Date: 2011/01/26 23:08:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc.ext.jameica;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.internal.views.Settings;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.xmlrpc.Plugin;
import de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Erweitert die View mit dem System-Einstellungen um die XML-RPC-Optionen.
 * @author willuhn
 */
public class SettingsView implements Extension
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  private CheckboxInput name  = null;
  private TablePart services  = null;

  private MessageConsumer consumer = null;
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  public void extend(Extendable extendable)
  {
    if (extendable == null || !(extendable instanceof Settings))
      return;

    Settings settings = (Settings) extendable;
    
    this.consumer = new MessageConsumer() {
    
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
    Application.getMessagingFactory().registerMessageConsumer(this.consumer);

    try
    {
      TabGroup tab = new TabGroup(settings.getTabFolder(),i18n.tr("XML-RPC"));
      
      // Da wir keine echte View sind, haben wir auch kein unbind zum Aufraeumen.
      // Damit wir unsere GUI-Elemente aber trotzdem disposen koennen, registrieren
      // wir einen Dispose-Listener an der Tabgroup
      tab.getComposite().addDisposeListener(new DisposeListener() {
      
        public void widgetDisposed(DisposeEvent e)
        {
          services = null;
          name     = null;
          Application.getMessagingFactory().unRegisterMessageConsumer(consumer);
        }
      
      });
      tab.addHeadline(i18n.tr("Freigegebene Services"));
      getServices().paint(tab.getComposite());
      tab.addCheckbox(getUseInterfaceNames(), i18n.tr("Java-Interface-Namen als XML-RPC-Namen verwenden"));
    }
    catch (Exception e)
    {
      Logger.error("unable to extend settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen der XML-RPC Einstellungen"), StatusBarMessage.TYPE_ERROR));
    }
    
  }
  
  /**
   * Wird beim Speichern aufgerufen.
   * @throws ApplicationException
   */
  private void handleStore() throws ApplicationException
  {
    de.willuhn.jameica.xmlrpc.Settings.setUseInterfaceNames(((Boolean) getUseInterfaceNames().getValue()).booleanValue());
    
    // Jetzt noch die Freigaben speichern
    try
    {
      List selected = getServices().getItems();
      XmlRpcServiceDescriptor[] all = de.willuhn.jameica.xmlrpc.Settings.getServices();
      for (int i=0;i<all.length;++i)
      {
        all[i].setShared(selected.contains(all[i]));
      }
    }
    catch (RemoteException re)
    {
      Logger.error("unable to apply service settings",re);
      throw new ApplicationException(i18n.tr("Fehler beim Übernehmen der freigegebenen Services"));
    }
  }

  /**
   * Liefert die Checkbox, um die Interface-Namen als Servicenamen zu verwenden.
   * @return die Checkbox.
   */
  private CheckboxInput getUseInterfaceNames()
  {
    if (this.name != null)
      return this.name;
    
    this.name = new CheckboxInput(de.willuhn.jameica.xmlrpc.Settings.getUseInterfaceNames());
    return this.name;
  }

  /**
   * Liefert die Liste der Services.
   * @return Liste der Services.
   * @throw RemoteException
   */
  private TablePart getServices() throws RemoteException
  {
    if (this.services != null)
      return this.services;
    
    XmlRpcServiceDescriptor[] services = de.willuhn.jameica.xmlrpc.Settings.getServices();
    this.services = new TablePart(PseudoIterator.fromArray(services),null);
    this.services.setCheckable(true);
    this.services.setMulti(false);
    this.services.setRememberColWidths(true);
    this.services.setRememberOrder(true);
    this.services.setSummary(false);
    this.services.addColumn(i18n.tr("Plugin"),"pluginname");
    this.services.addColumn(i18n.tr("Service"),"servicename");
    this.services.setFormatter(new TableFormatter() {
    
      public void format(TableItem item)
      {
        if (item == null)
          return;
        XmlRpcServiceDescriptor service = (XmlRpcServiceDescriptor) item.getData();
        try
        {
          item.setChecked(service.isShared());
        }
        catch (RemoteException re)
        {
          Logger.error("unable to enable service",re);
          try
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktivieren des Services {0}", service.getServiceName()), StatusBarMessage.TYPE_ERROR));
          }
          catch (RemoteException re2)
          {
            // useless
            Logger.error("unable to enable service",re);
          }
        }
      }
    });
    
    return this.services;
  }
}


/*********************************************************************
 * $Log: SettingsView.java,v $
 * Revision 1.14  2011/01/26 23:08:36  willuhn
 * @C Neustart des HTTP-Listeners nicht mehr noetig. Aenderungen an den Service-Freigaben werden jetzt live uebernommen
 *
 * Revision 1.13  2010-12-14 16:02:01  willuhn
 * @B Tabelle zeigte keine Scrollbalken an, wenn der Inhalt nicht reinpasst
 *
 * Revision 1.12  2008/04/04 00:17:14  willuhn
 * @N Apache XML-RPC von 3.0 auf 3.1 aktualisiert
 * @N jameica.xmlrpc ist jetzt von jameica.webadmin abhaengig
 * @N jameica.xmlrpc nutzt jetzt keinen eigenen embedded Webserver mehr sondern den Jetty von jameica.webadmin mittels Servlet. Damit kann nun XML-RPC ueber den gleichen TCP-Port (8080) gemacht werden, wo auch die restlichen Webfrontends laufen -> spart einen TCP-Port und skaliert besser wegen Multi-Threading-Support in Jetty
 *
 * Revision 1.11  2007/06/21 18:31:56  willuhn
 * @B ClassCastException
 *
 * Revision 1.10  2007/06/13 14:50:10  willuhn
 * @N Als XML-RPC-Servicenamen koennen nun auch direkt die Interface-Namen verwendet werden. Das ermoeglicht die Verwendung von dynamischen Proxies auf Clientseite.
 *
 * Revision 1.9  2007/04/10 23:27:40  willuhn
 * @N TablePart Redesign (removed dependencies from GenericIterator/GenericObject)
 *
 * Revision 1.8  2007/04/05 12:14:40  willuhn
 * @N Liste der Services im Handler statisch
 * @C XmlRpcService in XmlRpcServiceDescriptor umbenannt
 *
 * Revision 1.7  2006/12/22 13:49:58  willuhn
 * @N server kann an interface gebunden werden
 *
 * Revision 1.6  2006/12/22 09:31:38  willuhn
 * @N bind address
 *
 * Revision 1.5  2006/10/31 23:56:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2006/10/31 17:44:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2006/10/31 17:06:26  willuhn
 * @N GUI to configure xml-rpc
 *
 * Revision 1.2  2006/10/19 16:08:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/10/19 15:27:01  willuhn
 * @N initial checkin
 *
 *********************************************************************/