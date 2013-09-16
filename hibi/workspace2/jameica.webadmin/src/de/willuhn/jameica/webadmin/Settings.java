/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/Settings.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/11/02 00:56:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Container fuer die Einstellungen.
 */
public class Settings
{
  /**
   * Die Einstellungen des Plugins.
   */
  public final static de.willuhn.jameica.system.Settings SETTINGS = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
  
  private static Wallet wallet = null;

  /**
   * Liefert den TCP-Port fuer den Server.
   * @return der TCP-Port.
   */
  public static int getPort()
  {
    return SETTINGS.getInt("listener.http.port",8080);
  }
  
  /**
   * Speichert den zu verwendenden TCP-Port.
   * @param port der Port.
   * @throws ApplicationException
   */
  public static void setPort(int port) throws ApplicationException
  {
    if (port == getPort())
    {
      // hat sich nicht geaendert
      return;
    }
    
    if (port < 1 || port > 65535)
      throw new ApplicationException(Application.getI18n().tr("TCP-Portnummer für Webadmin ausserhalb des gültigen Bereichs von {0} bis {1}", new String[]{""+1,""+65535}));

    ServerSocket s = null;
    try
    {
      // Wir machen einen Test auf dem Port wenn es nicht der aktuelle ist
      Logger.info("testing TCP port " + port);
      s = new ServerSocket(port);
    }
    catch (BindException e)
    {
      throw new ApplicationException(Application.getI18n().tr("Die angegebene TCP-Portnummer für Webadmin {0} ist bereits belegt",""+port));
    }
    catch (IOException ioe)
    {
      Logger.error("error while opening socket on port " + port);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Testen der TCP-Portnummer für Webadmin {0}. Ist der Port bereits belegt?",""+port));
    }
    finally
    {
      if (s != null)
      {
        try
        {
          s.close();
        }
        catch (Exception e)
        {
          // ignore
        }
      }
    }
    SETTINGS.setAttribute("listener.http.port",port);
  }
  
  /**
   * Liefert die Adresse, an die der Server gebunden werden soll.
   * @return die Adresse, an die der Server gebunden werden soll oder <code>null</code> fuer alle.
   */
  public static InetAddress getAddress()
  {
    String s = SETTINGS.getString("listener.http.address",null);
    if (s == null)
      return null;
    try
    {
      return InetAddress.getByName(s);
    }
    catch (UnknownHostException e)
    {
      Logger.error("unable to resolve address " + s,e);
    }
    return null;
  }
  
  /**
   * Speichert die Adresse, an die der Server gebunden werden soll.
   * @param address die Adresse, an die der Server gebunden werden soll oder <code>null</code> fuer alle.
   */
  public static void setAddress(InetAddress address)
  {
    SETTINGS.setAttribute("listener.http.address",address == null ? null : address.getHostAddress());
  }
  
  /**
   * Liefert true, wenn die Kommunikation SSL-verschluesselt werden soll.
   * @return true, wenn SSL verwendet wird.
   */
  public static boolean getUseSSL()
  {
    return SETTINGS.getBoolean("listener.http.ssl",true);
  }
  
  /**
   * Legt fest, ob SSL verwendet werden soll.
   * @param ssl true, wenn SSL verwendet werden soll.
   */
  public static void setUseSSL(boolean ssl)
  {
    SETTINGS.setAttribute("listener.http.ssl",ssl);
  }
  
  /**
   * Liefert true, wenn das Jameica-Masterpasswort als HTTP-Authorisierung abgefragt werden soll.
   * @return true, wenn das Passwort abgefragt werden soll.
   */
  public static boolean getUseAuth()
  {
    return SETTINGS.getBoolean("listener.http.auth",true);
  }
  
  /**
   * Legt fest, ob das Jameica-Masterpasswort als HTTP-Authorisierung abgefragt werden soll.
   * @param auth true, wenn das Passwort abgefragt werden soll.
   */
  public static void setUseAuth(boolean auth)
  {
    SETTINGS.setAttribute("listener.http.auth",auth);
  }

  /**
   * Liefert das zu verwendende Passwort fuer die Jameica-Instanz.
   * @param jameicaUrl URL der Jameica-Instanz.
   * @return Passwort.
   * @throws Exception
   */
  public static String getServerPassword(String jameicaUrl) throws Exception
  {
    return (String) getWallet().get(jameicaUrl + ".password");
  }
  
  /**
   * Speichert das zu verwendende Passwort fuer die Jameica-Instanz.
   * @param jameicaUrl URL der Jameica-Instanz.
   * @param password das Passwort.
   * @throws Exception
   */
  public static void setServerPassword(String jameicaUrl, String password) throws Exception
  {
    getWallet().set(jameicaUrl + ".password",password);
  }

  /**
   * Liefert ein Wallet zum verschluesselten Speichern der Passwoerter.
   * @return Wallet.
   * @throws Exception
   */
  private static synchronized Wallet getWallet() throws Exception
  {
    if (wallet == null)
      wallet = new Wallet(Settings.class);
    return wallet;
  }
}


/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.4  2010/11/02 00:56:31  willuhn
 * @N Umstellung des Webfrontends auf Velocity/Webtools
 *
 * Revision 1.3  2009/01/06 01:44:14  willuhn
 * @N Code zum Hinzufuegen von Servern erweitert
 *
 * Revision 1.2  2008/07/02 17:43:00  willuhn
 * @N Remote-Administrierbarkeit
 *
 * Revision 1.1  2007/04/12 13:35:17  willuhn
 * @N SSL-Support
 * @N Authentifizierung
 * @N Korrektes Logging
 *
 **********************************************************************/