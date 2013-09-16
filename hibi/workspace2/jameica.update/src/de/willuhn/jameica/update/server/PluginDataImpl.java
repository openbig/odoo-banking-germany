/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/server/PluginDataImpl.java,v $
 * $Revision: 1.18 $
 * $Date: 2011/06/01 13:45:40 $
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
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.rmi.PluginData;
import de.willuhn.jameica.update.rmi.PluginGroup;
import de.willuhn.jameica.update.server.transport.Transport;
import de.willuhn.jameica.update.server.transport.TransportRegistry;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Plugin-Daten-Containers.
 */
public class PluginDataImpl extends UnicastRemoteObject implements PluginData
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  private PluginGroup group = null;
  private Manifest manifest = null;

  /**
   * @param url URL zum Plugin.
   * @throws Exception
   */
  protected PluginDataImpl(PluginGroup group, URL url) throws Exception
  {
    super();

    this.group = group;

    String s = url.toString();
    if (!s.endsWith("/")) s += "/";

    URL plugin = new URL(s + "plugin.xml");
    Logger.info("reading " + plugin);

    Transport t = TransportRegistry.getTransport(plugin);
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      t.get(bos,null);
      this.manifest = new Manifest(new ByteArrayInputStream(bos.toByteArray()));
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error while downloading " + plugin,e);
      throw new OperationCanceledException(plugin + " not found");
    }
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#getName()
   */
  public String getName() throws RemoteException
  {
    return this.manifest.getName();
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#getSize()
   */
  public long getSize() throws RemoteException
  {
    Transport t = TransportRegistry.getTransport(this.getDownloadUrl());
    return t.getSize();
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#getPluginGroup()
   */
  public PluginGroup getPluginGroup() throws RemoteException
  {
    return this.group;
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#getDescription()
   */
  public String getDescription() throws RemoteException
  {
    return this.manifest.getDescription();
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#getDownloadUrl()
   */
  public URL getDownloadUrl() throws RemoteException
  {
    try
    {
      return new URL(this.manifest.getURL());
    }
    catch (Exception e)
    {
      throw new RemoteException(i18n.tr("Ungültige Download-URL in Plugin-Definition"),e);
    }
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#getSignatureUrl()
   */
  public URL getSignatureUrl() throws RemoteException
  {
    try
    {
      return new URL(this.manifest.getURL() + ".sha1");
    }
    catch (Exception e)
    {
      throw new RemoteException(i18n.tr("Ungültige Signatur-URL in Plugin-Definition"),e);
    }
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#getAvailableVersion()
   */
  public Version getAvailableVersion() throws RemoteException
  {
    return this.manifest.getVersion();
  }
  
  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#isInstalledVersion()
   */
  public boolean isInstalledVersion() throws RemoteException
  {
    Version installed = this.getInstalledVersion();
    if (installed == null)
      return false;
    
    return this.getAvailableVersion().equals(installed);
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#getInstalledVersion()
   */
  public Version getInstalledVersion() throws RemoteException
  {
    Manifest mf = findInstalledVersion();
    return mf == null ? null : mf.getVersion();
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#isInstallable()
   */
  public boolean isInstallable() throws RemoteException
  {
    try
    {
      this.manifest.canDeploy();
      return true;
    }
    catch (ApplicationException ae)
    {
      return false;
    }
  }
  
  /**
   * Prueft, ob das Plugin bereits installiert ist.
   * @return das Manifest oder NULL.
   * @throws RemoteException
   */
  private Manifest findInstalledVersion() throws RemoteException
  {
    String name = this.getName();
    if (name == null)
      return null;
    List<Manifest> list = Application.getPluginLoader().getInstalledManifests();
    for (Manifest m:list)
    {
      if (m.getName().equals(name))
        return m;
    }
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null || !(other instanceof PluginData))
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
    if ("description".equals(name))
      return getDescription();
    if ("availableVersion".equals(name))
      return getAvailableVersion();
    if ("installedVersion".equals(name))
      return getInstalledVersion();
    if ("size".equals(name))
      return getSize();
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[]{"name","description","availableVersion","installedVersion"};
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return this.getName() + this.getDownloadUrl().toString();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name";
  }

  /**
   * @see de.willuhn.jameica.update.rmi.PluginData#getDependencies()
   */
  public Dependency[] getDependencies() throws RemoteException
  {
    List<Dependency> deps = new ArrayList<Dependency>();
    Dependency jd = this.manifest.getJameicaDependency();
    if (jd != null)
      deps.add(jd);
    
    Dependency[] dl = manifest.getDependencies();
    if (dl != null && dl.length > 0)
    {
      for (Dependency d:dl)
      {
        if (d != null)
          deps.add(d);
      }
    }
    return deps.toArray(new Dependency[deps.size()]);
  }
}


/**********************************************************************
 * $
 **********************************************************************/
