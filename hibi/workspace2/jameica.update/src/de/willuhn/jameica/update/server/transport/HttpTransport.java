/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/server/transport/HttpTransport.java,v $
 * $Revision: 1.10 $
 * $Date: 2011/01/21 17:53:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.server.transport;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.server.transport.annotation.TransportUrl;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung des HTTP-Transport.
 */
public class HttpTransport implements Transport
{
  private static List<String> protocols = new ArrayList<String>();
  static
  {
    protocols.add("http");
    protocols.add("https");
    protocols.add("file");
  }
  
  @TransportUrl
  private URL url = null;
  
  /**
   * @see de.willuhn.jameica.update.server.transport.Transport#exists()
   */
  public boolean exists()
  {
    try
    {
      Logger.debug("checking if " + this.url + " exists");
      if (this.url.toString().startsWith("file"))
      {
        File file = new File(this.url.toURI());
        return file.exists();
      }

      HttpURLConnection conn = (HttpURLConnection) this.url.openConnection();
      conn.connect();
      return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
    }
    catch (Exception e)
    {
      Logger.error("unable to check, if url " + this.url + " exists",e);
      return false;
    }
  }

  /**
   * @see de.willuhn.jameica.update.server.transport.Transport#getSize()
   */
  public long getSize()
  {
    try
    {
      Logger.debug("checking download size of " + this.url);
      if (this.url.toString().startsWith("file"))
      {
        File file = new File(this.url.toURI());
        return file.length();
      }

      URLConnection conn = this.url.openConnection();
      conn.connect();
      return conn.getContentLength();
    }
    catch (Exception e)
    {
      Logger.error("unable to determine download size for url " + this.url,e);
      return -1;
    }
  }

  /**
   * @see de.willuhn.jameica.update.server.transport.Transport#get(java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void get(OutputStream os, ProgressMonitor monitor) throws Exception
  {
    Logger.info("downloading " + this.url);
    I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

    if (os == null)
      throw new ApplicationException(i18n.tr("Kein OutputStream angegeben"));
    
    URLConnection conn = this.url.openConnection();
    conn.connect();
    
    if (monitor != null) monitor.setStatusText(i18n.tr("Download von {0}",this.url.toString()));

    int length = conn.getContentLength();
    long start = System.currentTimeMillis();
    long count = 0;
    long last  = 0;

    if (length <= 0)
      length = 5 * 1024 * 1024; // Wenn wir keinen Groesse haben, nehmen wir 5MB als Basis
    double factor = 100d / length;

    InputStream is = null;
    try
    {
      is = conn.getInputStream();
      byte[] buf = new byte[4096];
      int read = 0;
      while ((read = is.read(buf)) != -1)
      {
        os.write(buf,0,read);
        count += read;

        if (monitor != null)
          monitor.setPercentComplete((int)(count * factor));
        ////////////////////////////////////////////////////////////////////
        // stats
        long now = System.currentTimeMillis();
        if (now - last > 5000L)
        {
          long millis = now - start;
          if (millis > 0)
          {
            long kbps = count / millis;
            if (monitor != null)
              monitor.log(i18n.tr("{0} Kb/sek",""+kbps));
          }
          last = now;
        }
        ////////////////////////////////////////////////////////////////////
      }

      long used = (System.currentTimeMillis() - start);
      if (used > 0)
      {
        long kbps = used == 0 ? count : (count / used);
        Logger.info("download finished. " + kbps + " Kb/sek");
      }
      else
        Logger.info("download finished in less than a second");

      if (monitor != null)
      {
        monitor.setPercentComplete(100);
        monitor.setStatus(ProgressMonitor.STATUS_DONE);
        monitor.setStatusText(i18n.tr("Download beendet"));
      }
    }
    catch (Exception e)
    {
      if (monitor != null)
      {
        monitor.setStatus(ProgressMonitor.STATUS_ERROR);
        monitor.log(e.getMessage());
        monitor.setStatusText(i18n.tr("Fehler beim Download: {0}",e.getMessage()));
      }
      throw e;
    }
    finally
    {
      if (is != null)
      {
        try {
          is.close();
        }
        catch (Exception e) {
          Logger.error("unable to close inputstream",e);
        }
      }
      if (os != null)
      {
        try {
          os.close();
        }
        catch (Exception e) {
          Logger.error("unable to close outputstream",e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.update.server.transport.Transport#getProtocols()
   */
  public List<String> getProtocols()
  {
    return protocols;
  }
}


/**********************************************************************
 * $Log: HttpTransport.java,v $
 * Revision 1.10  2011/01/21 17:53:14  willuhn
 * @N Download-Groessen der Plugins mit anzeigen und im Hintergrund laden (dann wird die View schneller angezeigt)
 * @R transport#exists() im Konstruktor von PluginDataImpl entfernt. Spart 50% TCP-Verbindungen beim Aufbau des Trees. Und wenn das Plugin nicht existiert, kann man auch einfach die Exception fangen.
 *
 * Revision 1.9  2009/10/29 18:06:05  willuhn
 * @N Manuelle Suche nach Updates
 * @R Test auf last-modified entfernt - war nicht wirklich deterministisch loesbar ;)
 *
 * Revision 1.8  2009/10/28 01:20:48  willuhn
 * @N Erster Code fuer automatische Update-Checks
 * @C Code-Cleanup - sauberere Fehlermeldung, wenn Plugins auf dem Server nicht (mehr) gefunden werden
 *
 * Revision 1.7  2009/01/18 01:42:46  willuhn
 * @N Abrufen und Pruefen der Plugin-Signaturen
 *
 * Revision 1.6  2008/12/31 00:40:30  willuhn
 * @N BUGZILLA 675 Gruppierung von Plugins
 *
 * Revision 1.5  2008/12/21 23:04:33  willuhn
 * @N Transport "file://"
 *
 * Revision 1.4  2008/12/16 16:16:47  willuhn
 * @N Erste Version mit Download und install
 *
 * Revision 1.3  2008/12/16 14:19:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2008/12/16 14:15:13  willuhn
 * @C Command-Pattern entfernt. Brachte keinen wirklichen Mehrwert und erschwerte die Benutzung zusammen mit ProgressMonitor
 *
 * Revision 1.1  2008/12/12 01:13:17  willuhn
 * @N Transport-API
 *
 **********************************************************************/
