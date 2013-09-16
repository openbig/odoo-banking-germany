/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/controller/RepositoryContol.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/12/31 00:40:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.gui.controller;

import java.net.URL;
import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.gui.parts.PluginList;
import de.willuhn.jameica.update.rmi.Repository;
import de.willuhn.jameica.update.rmi.RepositoryService;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Repository-Details.
 */
public class RepositoryContol extends AbstractControl
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  private Repository repo    = null;
  private Input url          = null;
  private PluginList plugins = null;

  /**
   * ct.
   * @param view
   */
  public RepositoryContol(AbstractView view)
  {
    super(view);
  }
  
  /**
   * Liefert dasa Repository zur URL.
   * @return Repository.
   * @throws RemoteException
   */
  public synchronized Repository getRepository() throws RemoteException
  {
    if (this.repo == null)
    {
      try
      {
        RepositoryService service = (RepositoryService) Application.getServiceFactory().lookup(Plugin.class,"repository");
        this.repo = service.open((URL) getCurrentObject());
      }
      catch (RemoteException re)
      {
        throw re;
      }
      catch (Exception e)
      {
        throw new RemoteException("unable to open repository service",e);
      }
    }
    return this.repo;
  }
  
  /**
   * Liefert ein Label mit der URL
   * @return Label mit der URL.
   * @throws RemoteException
   */
  public Input getUrl() throws RemoteException
  {
    if (this.url == null)
    {
      Repository repo = this.getRepository();
      this.url = new LabelInput(repo.getUrl().toString());
      this.url.setComment(repo.getName());
      this.url.setName(i18n.tr("URL"));
    }
    return this.url;
  }
  
  /**
   * Liefert eine Liste mit den Plugins aus dem Repository.
   * @return Liste mit den Plugins aus dem Repository.
   * @throws RemoteException
   */
  public PluginList getPlugins() throws RemoteException
  {
    if (this.plugins == null)
      this.plugins = new PluginList(this.getRepository());
    return this.plugins;
  }

}


/**********************************************************************
 * $
 **********************************************************************/
