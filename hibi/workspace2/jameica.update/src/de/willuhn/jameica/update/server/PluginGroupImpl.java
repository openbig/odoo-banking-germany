/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/server/PluginGroupImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/10/28 01:20:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import net.n3.nanoxml.IXMLElement;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.security.SSLFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.rmi.PluginData;
import de.willuhn.jameica.update.rmi.PluginGroup;
import de.willuhn.jameica.update.rmi.Repository;
import de.willuhn.jameica.update.server.transport.Transport;
import de.willuhn.jameica.update.server.transport.TransportRegistry;
import de.willuhn.jameica.util.XPathEmu;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementiert eine Plugin-Gruppe.
 */
public class PluginGroupImpl extends UnicastRemoteObject implements PluginGroup
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  private static Settings settings = new Settings(Repository.class);
  private Repository repository    = null;
  private String name              = null;
  private List<PluginData> plugins = new ArrayList<PluginData>();

  /**
   * ct.
   * @param repository das zugehoerige Repository.
   * @param root das XML-Root-Element der Plugin-Gruppe.
   * @throws Exception
   */
  protected PluginGroupImpl(Repository repository, IXMLElement root) throws Exception
  {
    super();
    
    this.repository = repository;
    
    this.name = root.getAttribute("name",null);
    
    String cert = root.getAttribute("certificate",null);
    if (cert != null)
      importCertificate(cert);
    
    XPathEmu xpath = new XPathEmu(root);
    IXMLElement[] list = xpath.getElements("plugin");
    if (list == null || list.length == 0)
    {
      Logger.warn("plugingroup \"" + this.name + "\" contains no plugins");
      return;
    }
    
    for (IXMLElement e:list)
    {
      String pu = e.getAttribute("url",null);
      if (pu == null || pu.length() == 0)
        continue;
      
      try
      {
        this.plugins.add(new PluginDataImpl(this,new URL(pu)));
      }
      catch (OperationCanceledException oce)
      {
        Logger.warn(oce.getMessage() + ", skipping");
      }
      catch (Exception ex)
      {
        Logger.error("unable to load plugin data for url: " + pu + ", skipping",ex);
      }
    }
  }

  /**
   * Importiert das Zertifikat.
   * @param cert das Zertifikat.
   * @throws Exception
   */
  private void importCertificate(String cert) throws Exception
  {
    if (cert == null || cert.length() == 0)
    {
      Logger.warn("no certificate given");
      return;
    }
    
    SSLFactory factory = Application.getSSLFactory();
    
    // Zertifikat vom Server abrufen
    Transport t = TransportRegistry.getTransport(new URL(cert));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    t.get(bos,null);

    final X509Certificate newCert = factory.loadCertificate(new ByteArrayInputStream(bos.toByteArray()));
    if (newCert == null)
      throw new ApplicationException(Application.getI18n().tr("Zertifikat des Repositories nicht lesbar"));
    
    X509Certificate oldCert = getCertificate();
    
    // Wir haben schon das Zertifikat. Wir pruefen, ob es noch stimmt.
    if (oldCert != null)
    {
      // Wir kennen das Zertifikat und es ist noch korrekt.
      if (oldCert.equals(newCert))
      {
        Logger.info("certificate verified");
        return;
      }

      // Zertifikat hat sich geaendert!
      Logger.warn("certificate has changed for repository: " + getKey());
      String q = i18n.tr("Das Zertifikat des Repositories wurde geändert!\n" +
                         "Möchten Sie den Vorgang dennoch fortsetzen und das neue Zertifikat importieren?");

      
      // User vertraut dem neuen Zertifikat nicht. Abbrechen
      if (!Application.getCallback().askUser(q))
        throw new OperationCanceledException(i18n.tr("Vorgang abgebrochen"));
    }

    // Zertifikat importieren
    String alias = factory.addTrustedCertificate(newCert);
    // Und zur Liste der bekannten Zertifikate hinzufuegen
    settings.setAttribute(getKey(),alias);
  }
  
  /**
   * Liefert den Namen, unter dem der Alias des Zertifikats gespeichert ist.
   * @return der Alias.
   * @throws RemoteException
   */
  private String getKey() throws RemoteException
  {
    return this.repository.getUrl().toString() + ":" + this.name;
  }
  
  /**
   * @see de.willuhn.jameica.update.rmi.PluginGroup#getName()
   */
  public String getName() throws RemoteException
  {
    return this.name;
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginGroup#getRepository()
   */
  public Repository getRepository() throws RemoteException
  {
    return this.repository;
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginGroup#getPlugins()
   */
  public List<PluginData> getPlugins() throws RemoteException
  {
    return this.plugins;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getChildren()
   */
  public GenericIterator getChildren() throws RemoteException
  {
    return PseudoIterator.fromArray((GenericObject[]) this.plugins.toArray(new PluginData[this.plugins.size()]));
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getParent()
   */
  public GenericObjectNode getParent() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPath()
   */
  public GenericIterator getPath() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
   */
  public GenericIterator getPossibleParents() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
   */
  public boolean hasChild(GenericObjectNode node) throws RemoteException
  {
    if (node == null)
      return false;
    return this.getChildren().contains(node) != null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null || !(other instanceof PluginGroup))
      return false;
    
    return this.getID().equals(other.getID());
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) throws RemoteException
  {
    if ("name".equals(name))
      return getName();
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[]{"name"};
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return this.name + this.plugins.size();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name";
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginGroup#getCertificate()
   */
  public X509Certificate getCertificate() throws RemoteException
  {
    String alias = settings.getString(getKey(),null);
    if (alias == null)
      return null; // Kein Alias bekannt. Also auch kein Zertifikat
    
    try
    {
      return Application.getSSLFactory().getTrustedCertificate(alias);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to load certificate",e);
    }
  }
}


/**********************************************************************
 * $Log: PluginGroupImpl.java,v $
 * Revision 1.4  2009/10/28 01:20:48  willuhn
 * @N Erster Code fuer automatische Update-Checks
 * @C Code-Cleanup - sauberere Fehlermeldung, wenn Plugins auf dem Server nicht (mehr) gefunden werden
 *
 * Revision 1.3  2009/01/18 13:51:36  willuhn
 * @N Zertifikate pro Plugin-Gruppe konfigurierbar
 *
 * Revision 1.2  2008/12/31 01:07:21  willuhn
 * @N Plugins remote (via RMI) auf einem Jameica-Server downloadbar
 *
 * Revision 1.1  2008/12/31 00:40:30  willuhn
 * @N BUGZILLA 675 Gruppierung von Plugins
 *
 **********************************************************************/
