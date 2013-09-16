/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/JSONClient.java,v $
 * $Revision: 1.5 $
 * $Date: 2010/11/02 00:56:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONTokener;

import de.willuhn.jameica.messaging.CheckTrustMessage;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Fuehrt JSON-Aufrufe an localhost oder fremde Rechner durch.
 */
public class JSONClient
{
  /**
   * Fragt eine REST-URL ab und liefert das JSON-Response zurueck.
   * @param url die Basis-URL.
   * @param restCommand das REST-Kommando.
   * @return json-Daten.
   * @throws Exception
   */
  public static Object execute(String url, String restCommand) throws Exception
  {
    AutoCertTrust certTrust = null;
    AutoHostTrust hostTrust = null;
    
    try
    {
      // Nur registrieren, wenn wir keine Interaktionsmoeglichkeit via GUI haben
      // und wir uns tatsaechlich mittels HTTPS verbinden
      if (Application.inServerMode() && url.startsWith("https://"))
      {
        certTrust = new AutoCertTrust(url);
        Application.getMessagingFactory().registerMessageConsumer(certTrust);
        hostTrust = new AutoHostTrust(url);
        Application.getMessagingFactory().getMessagingQueue("jameica.trust.hostname").registerMessageConsumer(hostTrust);
      }
      HttpURLConnection connection = (HttpURLConnection) new URL(url + restCommand).openConnection(); 
      connection.setDoOutput(true);
      
      final String password = Settings.getServerPassword(url);
      if (password != null && password.length() > 0)
      {
        Authenticator.setDefault(new Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("admin",password.toCharArray());
          }
        });
      }
  
      // Response
      StringBuffer builder = new StringBuffer(1024);
      char[] buffer = new char[1024];
      Reader reader = new InputStreamReader(connection.getInputStream());
      while (true)
      {
        int bytesRead = reader.read(buffer);
        if (bytesRead < 0)
          break;
        builder.append(buffer, 0, bytesRead);
      }
      reader.close();
      JSONTokener tokener = new JSONTokener(builder.toString());
      return tokener.nextValue();
    }
    catch (JSONException ex)
    {
      Logger.error("unable to execute JSON request",ex);
      throw new IOException("unable to execute JSON request");
    }
    finally
    {
      if (certTrust != null)
        Application.getMessagingFactory().unRegisterMessageConsumer(certTrust);
      if (hostTrust != null)
        Application.getMessagingFactory().getMessagingQueue("jameica.trust.hostname").unRegisterMessageConsumer(certTrust);
    }
  }
  
  /**
   * Gewaehrleistet die automatische Vertrauensstellung von fremden
   * Jameica-Zertifikaten, wenn sich der Admin explizit dorthin verbindet. 
   */
  private static class AutoCertTrust implements MessageConsumer
  {
    private String url = null;
    
    /**
     * ct
     * @param url
     */
    private AutoCertTrust(String url)
    {
      this.url = url;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{CheckTrustMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      CheckTrustMessage msg = (CheckTrustMessage) message;
      msg.setTrusted(true,this.getClass().getName() + " at " + new Date().toString() + " for " + this.url);
    }
  }
  
  /**
   * Gewaehrleistet die automatische Vertrauensstellung von
   * Jameica-Servern, wenn sich der Admin explizit dorthin verbindet. 
   * Auch dann, wenn der Hostname nicht mit dem aus dem Zertifikat
   * uebereinstimmt.
   */
  private static class AutoHostTrust implements MessageConsumer
  {
    private String url = null;
    
    /**
     * ct
     * @param url
     */
    private AutoHostTrust(String url)
    {
      this.url = url;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage msg = (QueryMessage) message;
      msg.setName(this.getClass().getName() + " at " + new Date().toString() + " for " + this.url);
      msg.setData(Boolean.TRUE);
    }
    
  }
}


/**********************************************************************
 * $Log: JSONClient.java,v $
 * Revision 1.5  2010/11/02 00:56:31  willuhn
 * @N Umstellung des Webfrontends auf Velocity/Webtools
 *
 * Revision 1.4  2009/01/07 00:30:20  willuhn
 * @N Hinzufuegen weiterer Jameica-Server
 * @N Auto-Host-Check
 **********************************************************************/