/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/server/RepositoryImpl.java,v $
 * $Revision: 1.20 $
 * $Date: 2011/06/07 11:25:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import de.willuhn.jameica.gui.internal.dialogs.PluginSourceDialog;
import de.willuhn.jameica.messaging.TextMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginSource;
import de.willuhn.jameica.plugin.ZippedPlugin;
import de.willuhn.jameica.services.DeployService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.rmi.PluginData;
import de.willuhn.jameica.update.rmi.PluginGroup;
import de.willuhn.jameica.update.rmi.Repository;
import de.willuhn.jameica.update.server.transport.Transport;
import de.willuhn.jameica.update.server.transport.TransportRegistry;
import de.willuhn.jameica.util.XPathEmu;
import de.willuhn.logging.Logger;
import de.willuhn.security.Signature;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Container fuer ein einzelnes Repository.
 */
public class RepositoryImpl extends UnicastRemoteObject implements Repository
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  private URL url                  = null;
  private String name              = null;
  private List<PluginGroup> groups = new ArrayList<PluginGroup>();
  
  
  /**
   * ct.
   * @param url
   * @throws RemoteException
   */
  protected RepositoryImpl(URL url) throws RemoteException
  {
    super();

    if (url == null)
      throw new RemoteException(i18n.tr("Keine URL angegeben"));

    this.url = url;

    try
    {
      Logger.info("open repository " + this.url);

      String s = this.url.toString();
      if (!s.endsWith("/")) s += "/";
      Transport t = TransportRegistry.getTransport(new URL(s + "repository.xml"));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      t.get(bos,null);

      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
      parser.setReader(new StdXMLReader(new ByteArrayInputStream(bos.toByteArray())));
      
      IXMLElement root = (IXMLElement) parser.parse();
      this.name = root.getAttribute("name",null);
      
      XPathEmu xpath  = new XPathEmu(root);
      IXMLElement[] list = xpath.getElements("plugins");
      if (list == null || list.length == 0)
      {
        Logger.warn("repository " + s + " contains no plugin groups");
        return;
      }
      
      for (IXMLElement e:list)
      {
        try
        {
          this.groups.add(new PluginGroupImpl(this,e));
        }
        catch (Exception ex)
        {
          Logger.error("unable to load plugin group, skipping",ex);
        }
      }
    }
    catch (ApplicationException ae)
    {
      throw new RemoteException(ae.getMessage());
    }
    catch (OperationCanceledException oce)
    {
      throw new RemoteException(oce.getMessage());
    }
    catch (Exception e)
    {
      Logger.error("unable to read from url " + this.url,e);
      throw new RemoteException(i18n.tr("Repository {0} nicht lesbar: {1}",new String[]{this.url.toString(),e.getMessage()}),e);
    }
  }
  
  /**
   * @see de.willuhn.jameica.update.rmi.Repository#getName()
   */
  public String getName() throws RemoteException
  {
    return this.name;
  }

  /**
   * @see de.willuhn.jameica.update.rmi.Repository#getUrl()
   */
  public URL getUrl() throws RemoteException
  {
    return this.url;
  }

  /**
   * @see de.willuhn.jameica.update.rmi.Repository#getPlugins()
   */
  public List<PluginData> getPlugins() throws RemoteException
  {
    List<PluginData> list = new ArrayList<PluginData>();
    for (PluginGroup group:this.getPluginGroups())
      list.addAll(group.getPlugins());
    return list;
  }

  /**
   * @see de.willuhn.jameica.update.rmi.Repository#getPluginGroups()
   */
  public List<PluginGroup> getPluginGroups() throws RemoteException
  {
    return this.groups;
  }

  /**
   * @see de.willuhn.jameica.update.rmi.Repository#download(de.willuhn.jameica.update.rmi.PluginData, boolean)
   */
  public void download(final PluginData data, final boolean interactive) throws RemoteException, ApplicationException
  {
    BackgroundTask t = new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {

        File dir = Application.getConfig().getUpdateDir();
        File archive = null;
        File sig     = null;
        Transport t  = null;
        boolean sigchecked = false;
        boolean update     = false;
        try
        {
          //////////////////////////////////////////////////////////////////////
          // Signatur herunterladen
          Logger.info("checking if plugin is signed");
          t = TransportRegistry.getTransport(data.getSignatureUrl());
          if (t.exists())
          {
            sig = new File(dir,data.getName() + ".zip.sha1");
            t.get(new BufferedOutputStream(new FileOutputStream(sig)),null);
            Logger.info("created signature file " + sig);
          }
          else if (interactive)
          {
            String q = i18n.tr("Plugin wurde vom Herausgeber nicht signiert.\n" +
            		"Möchten Sie es dennoch installieren?");
            if (!Application.getCallback().askUser(q))
              throw new OperationCanceledException(i18n.tr("Vorgang abgebrochen"));
          }
          //////////////////////////////////////////////////////////////////////

          //////////////////////////////////////////////////////////////////////
          //  Datei herunterladen
          t = TransportRegistry.getTransport(data.getDownloadUrl());
          // Wir nehmen hier nicht den Dateinamen der URL sondern generieren selbst einen.
          // Denn die Download-URL kann etwas dynamisches sein, was nicht auf ".zip" endet
          archive = new File(dir,data.getName() + ".zip");
          Logger.info("creating deploy file " + archive);
          t.get(new BufferedOutputStream(new FileOutputStream(archive)),monitor);
          //////////////////////////////////////////////////////////////////////


          //////////////////////////////////////////////////////////////////////
          // Signatur checken
          if (sig != null)
          {
            checkSignature(data.getPluginGroup().getCertificate(),archive,sig);
            sigchecked = true;
          }
          //////////////////////////////////////////////////////////////////////

          //////////////////////////////////////////////////////////////////////
          // Deployen
          ZippedPlugin zp = new ZippedPlugin(archive);
          DeployService service = Application.getBootLoader().getBootable(DeployService.class);
          
          // Checken, ob wir Install oder Update machen muessen
          Manifest mf        = zp.getManifest();
          Manifest installed = Application.getPluginLoader().getManifestByName(mf.getName());
          if (installed != null)
          {
            service.update(installed,zp,monitor);
            update = true;
          }
          else
          {
            // Nach der Plugin-Quelle koennen wir derzeit nur im Desktop-Mode fragen, da wir
            // im Server-Mode noch keinen passenden Callback haben. In dem Fall ist "source" NULL,
            // womit im User-Dir installiert wird.
            PluginSource source = null;
            if (!Application.inServerMode())
            {
              PluginSourceDialog d = new PluginSourceDialog(PluginSourceDialog.POSITION_CENTER,mf);
              source = (PluginSource) d.open();
            }
            service.deploy(zp,source,monitor);
          }
          //////////////////////////////////////////////////////////////////////

          if (interactive)
          {
            String text = sigchecked ? i18n.tr("Digitale Signatur des Plugins korrekt.") :
                                       i18n.tr("Plugin enthielt keine digitale Signatur.");
            
            text += "\n" + i18n.tr("Die Installation erfolgt beim nächsten Neustart von Jameica.");
            TextMessage msg = new TextMessage(i18n.tr("Plugin heruntergeladen"),text);
            Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(msg);
          }
        }
        catch (Exception e)
        {
          if (e instanceof ApplicationException)
            throw (ApplicationException) e;
          if (e instanceof OperationCanceledException)
            throw new ApplicationException(e.getMessage());
          
          Logger.error("error while downloading file",e);
          throw new ApplicationException(i18n.tr("Fehler beim Herunterladen des Plugins: {0}",e.getMessage()));
        }
        finally
        {
          // Kann geloescht werden - wurde ja schon deployed
          // Aber nur, wenn es kein Update ist. Denn da findet das Deployment erst beim Neustart
          // statt. Und dort brauchen wir ja die ZIP-Datei
          if (!update && archive != null && archive.exists())
          {
            Logger.info("deleting " + archive);
            archive.delete();
          }
          
          if (sig != null && sig.exists())
          {
            Logger.info("delete signature " + sig);
            sig.delete();
          }
        }
      }
    
      public boolean isInterrupted() {return false;}
      public void interrupt(){}
    };

    if (interactive)
      Application.getController().start(t);
    else
      t.run(null);
  }
  
  /**
   * Prueft die Signatur.
   * @param cert das Zertifikat.
   * @param archive Datei, dessen Signatur gecheckt werden soll.
   * @param sig die Signatur.
   * @throws Exception
   */
  private void checkSignature(X509Certificate cert, File archive, File sig) throws Exception
  {
    Logger.info("checking signature " + sig + " of file " + archive);

    if (cert == null)
    {
      Logger.warn("plugin signed, but no certificate found");
      return;
    }

    InputStream is1 = null;
    InputStream is2 = null;
    try
    {
      // Signatur einlesen
      is2 = new BufferedInputStream(new FileInputStream(sig));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int read = 0;
      while ((read = is2.read(buf)) != -1)
        bos.write(buf,0,read);
        
      is1 = new BufferedInputStream(new FileInputStream(archive));
      if (Signature.verifiy(is1,cert.getPublicKey(),bos.toByteArray()))
      {
        Logger.info("signature OK");
        return;
      }
      
      // Signatur ungueltig!
      throw new ApplicationException(i18n.tr("Signatur des Plugins ungültig. Installation abgebrochen"));
    }
    finally
    {
      if (is1 != null)
      {
        try
        {
          is1.close();
        } catch (Exception e)
        {
          Logger.error("unable to close inputstream",e);
        }
      }
      if (is2 != null)
      {
        try
        {
          is2.close();
        } catch (Exception e)
        {
          Logger.error("unable to close inputstream",e);
        }
      }
    }
    
  }
}


/**********************************************************************
 * $Log: RepositoryImpl.java,v $
 * Revision 1.20  2011/06/07 11:25:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2011-06-02 13:30:21  willuhn
 * @B Bugxifing
 *
 * Revision 1.18  2011-06-02 13:14:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2011-06-02 13:02:40  willuhn
 * @N Deploy-Service nutzen
 *
 * Revision 1.16  2011-06-02 12:40:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2009/10/28 17:00:58  willuhn
 * @N Automatischer Check nach Updates mit der Wahlmoeglichkeit, nur zu benachrichtigen oder gleich zu installieren
 *
 * Revision 1.14  2009/10/28 01:20:48  willuhn
 * @N Erster Code fuer automatische Update-Checks
 * @C Code-Cleanup - sauberere Fehlermeldung, wenn Plugins auf dem Server nicht (mehr) gefunden werden
 *
 * Revision 1.13  2009/01/18 23:28:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2009/01/18 13:51:36  willuhn
 * @N Zertifikate pro Plugin-Gruppe konfigurierbar
 *
 * Revision 1.11  2009/01/18 01:42:46  willuhn
 * @N Abrufen und Pruefen der Plugin-Signaturen
 *
 * Revision 1.10  2009/01/17 01:09:11  willuhn
 * @B sig loeschen, wenn sie nicht heruntergeladen werden konnte
 *
 * Revision 1.9  2009/01/17 01:08:11  willuhn
 * @N Erster Code fuer das Herunterladen der Signaturen
 *
 * Revision 1.8  2008/12/31 01:07:21  willuhn
 * @N Plugins remote (via RMI) auf einem Jameica-Server downloadbar
 *
 * Revision 1.7  2008/12/31 00:41:08  willuhn
 * @B BUGZILLA 675 - Test-URL entfernt
 *
 * Revision 1.6  2008/12/31 00:40:30  willuhn
 * @N BUGZILLA 675 Gruppierung von Plugins
 *
 * Revision 1.5  2008/12/16 16:16:47  willuhn
 * @N Erste Version mit Download und install
 *
 * Revision 1.4  2008/12/16 14:32:35  willuhn
 * @R RepositoryParser entfernt - unnoetig
 *
 * Revision 1.3  2008/12/16 14:15:13  willuhn
 * @C Command-Pattern entfernt. Brachte keinen wirklichen Mehrwert und erschwerte die Benutzung zusammen mit ProgressMonitor
 *
 * Revision 1.2  2008/12/16 11:43:37  willuhn
 * @N Detail-Dialog fuer Repositories
 *
 * Revision 1.1  2008/12/12 01:13:17  willuhn
 * @N Transport-API
 *
 * Revision 1.1  2008/12/10 00:33:19  willuhn
 * @N initial checkin des Update-Plugins
 *
 **********************************************************************/
