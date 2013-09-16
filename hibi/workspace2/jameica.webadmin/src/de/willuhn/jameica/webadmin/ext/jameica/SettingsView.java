/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/ext/jameica/SettingsView.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/04/04 00:16:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.ext.jameica;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.Service;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.views.Settings;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.Plugin;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Erweitert die View mit dem System-Einstellungen um die XML-RPC-Optionen.
 * @author willuhn
 */
public class SettingsView implements Extension
{
  private IntegerInput port   = null;
  private CheckboxInput ssl   = null;
  private CheckboxInput auth  = null;
  private Input address       = null;

  private I18N i18n = null;
  private MessageConsumer consumer = null;
  
  /**
   * ct.
   */
  public SettingsView()
  {
    this.i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  }
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
      TabGroup tab = new TabGroup(settings.getTabFolder(),i18n.tr("HTTP"));
      
      // Da wir keine echte View sind, haben wir auch kein unbind zum Aufraeumen.
      // Damit wir unsere GUI-Elemente aber trotzdem disposen koennen, registrieren
      // wir einen Dispose-Listener an der Tabgroup
      tab.getComposite().addDisposeListener(new DisposeListener() {
      
        public void widgetDisposed(DisposeEvent e)
        {
          port     = null;
          ssl      = null;
          auth     = null;
          address  = null;
          Application.getMessagingFactory().unRegisterMessageConsumer(consumer);
        }
      
      });
      tab.addLabelPair(i18n.tr("TCP-Port"), getPort());
      tab.addLabelPair(i18n.tr("Server binden an"), getAddress());
      tab.addCheckbox(getUseSSL(), i18n.tr("HTTP-Kommunikation verschlüsseln (HTTPS)"));
      tab.addCheckbox(getUseAuth(), i18n.tr("Benutzerauthentifizierung mittels Jameica Master-Passwort"));
    }
    catch (Exception e)
    {
      Logger.error("unable to extend settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen der HTTP-Connector Einstellungen"), StatusBarMessage.TYPE_ERROR));
    }
    
  }
  
  /**
   * Wird beim Speichern aufgerufen.
   * @throws ApplicationException
   */
  private void handleStore() throws ApplicationException
  {
    Integer in = (Integer) getPort().getValue();
    if (in == null)
      throw new ApplicationException(i18n.tr("Bitte geben Sie eine TCP-Portnummer für XML-RPC ein"));
    de.willuhn.jameica.webadmin.Settings.setPort(in.intValue());
    de.willuhn.jameica.webadmin.Settings.setUseAuth(((Boolean) getUseAuth().getValue()).booleanValue());
    de.willuhn.jameica.webadmin.Settings.setUseSSL(((Boolean) getUseSSL().getValue()).booleanValue());
    de.willuhn.jameica.webadmin.Settings.setAddress(((AddressObject)getAddress().getValue()).ia);
    
    try
    {
      Logger.info("restart http listener");
      Service listener = Application.getServiceFactory().lookup(Plugin.class,"listener.http");
      listener.stop(true);
      listener.start();
    }
    catch (Exception e)
    {
      Logger.error("unable to restart listener",e);
      throw new ApplicationException(i18n.tr("Fehler beim Neustart des Dienstes, bitte starten Sie Jameica neu"));
    }
  }

  /**
   * Liefert die Checkbox zur Aktivierung von SSL.
   * @return die Checkbox.
   */
  private CheckboxInput getUseSSL()
  {
    if (this.ssl != null)
      return this.ssl;
    
    this.ssl = new CheckboxInput(de.willuhn.jameica.webadmin.Settings.getUseSSL());
    this.ssl.addListener(new AuthListener());
    return this.ssl;
  }
  
  /**
   * Liefert die Checkbox zur Aktivierung von Authentifizierung.
   * @return die Checkbox.
   */
  private CheckboxInput getUseAuth()
  {
    if (this.auth != null)
      return this.auth;
    
    this.auth = new CheckboxInput(de.willuhn.jameica.webadmin.Settings.getUseAuth());
    this.auth.addListener(new AuthListener());
    return this.auth;
  }
  
  /**
   * Liefert den zu verwendenden TCP-Port.
   * @return der TCP-Port.
   */
  private IntegerInput getPort()
  {
    if (this.port != null)
      return this.port;
    this.port = new IntegerInput(de.willuhn.jameica.webadmin.Settings.getPort());
    return this.port;
  }
  
  /**
   * Liefert eine Auswahl-Box fuer die Adresse.
   * @return Auswahl-Box.
   */
  private Input getAddress()
  {
    if (this.address != null)
      return this.address;

    try
    {
      ArrayList l = new ArrayList();
      l.add(new AddressObject(null)); // steht fuer "alle Interfaces"
      Enumeration e = NetworkInterface.getNetworkInterfaces();
      while (e.hasMoreElements())
      {
        NetworkInterface i = (NetworkInterface) e.nextElement();
        Enumeration e2 = i.getInetAddresses();
        while (e2.hasMoreElements())
        {
          InetAddress ia = (InetAddress) e2.nextElement();
          if (ia instanceof Inet6Address)
            continue; // IPv6 ignorieren wir - nutzt ja eh keiner ;)
          l.add(new AddressObject(ia));
        }
      }
      Collections.sort(l);
      GenericIterator i = PseudoIterator.fromArray((AddressObject[])l.toArray(new AddressObject[l.size()]));
      this.address = new SelectInput(i, new AddressObject(de.willuhn.jameica.webadmin.Settings.getAddress()));
    }
    catch (Exception e)
    {
      Logger.error("unable to determine addresses",e);
      this.address = new LabelInput(i18n.tr("Keine Interfaces gefunden"));
    }
    return this.address;
  }
  
  /**
   * Hilfsobjekt fuer die Adress-Auswahl.
   */
  private class AddressObject implements GenericObject, Comparable
  {
    private InetAddress ia = null;
    
    /**
     * ct.
     * @param address
     */
    private AddressObject(InetAddress address)
    {
      this.ia = address;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null || !(arg0 instanceof AddressObject))
        return false;

      AddressObject other = (AddressObject) arg0;
      if (this.ia == other.ia)
        return true;
      
      if (this.ia == null)
        return other.ia == null;
      
      return this.ia.equals(other.ia);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      if (this.ia == null)
        return i18n.tr("Alle Netzwerk-Interfaces");
      return this.ia.getHostAddress();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"foo"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.ia == null ? null : this.ia.getHostAddress();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "foo";
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
      if (o == null)
        return -1;
      if (this.ia == null)
        return -1;
      return this.ia.getHostAddress().compareTo(((AddressObject)o).ia.getHostAddress());
    }
  }

  /**
   * Listener zum Pruefen, dass Authentifizierung nur zusammen mit SSL aktiv ist.
   */
  private class AuthListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      // Wir pruefen, ob SSL aktiv ist, damit das Passwort sicher uebertragen wird
      boolean a = ((Boolean) getUseAuth().getValue()).booleanValue();
      boolean s = ((Boolean) getUseSSL().getValue()).booleanValue();
      if (a && !s)
        GUI.getView().setErrorText(i18n.tr("Benutzerauthentifizierung sollte nur zusammen mit Verschlüsselung aktiviert werden"));
      else
        GUI.getView().setSuccessText("");
    }
  }
}


/*********************************************************************
 * $Log: SettingsView.java,v $
 * Revision 1.2  2008/04/04 00:16:58  willuhn
 * @N Apache XML-RPC von 3.0 auf 3.1 aktualisiert
 * @N jameica.xmlrpc ist jetzt von jameica.webadmin abhaengig
 * @N jameica.xmlrpc nutzt jetzt keinen eigenen embedded Webserver mehr sondern den Jetty von jameica.webadmin mittels Servlet. Damit kann nun XML-RPC ueber den gleichen TCP-Port (8080) gemacht werden, wo auch die restlichen Webfrontends laufen -> spart einen TCP-Port und skaliert besser wegen Multi-Threading-Support in Jetty
 *
 *********************************************************************/