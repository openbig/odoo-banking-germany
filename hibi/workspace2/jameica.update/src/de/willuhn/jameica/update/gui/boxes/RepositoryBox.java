/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/boxes/RepositoryBox.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/12/17 22:38:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.gui.boxes;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.gui.parts.RepositoryList;
import de.willuhn.util.I18N;

/**
 * Zeigt die Liste der Repositories an, wenn keine
 * weiteren Plugins installiert sind.
 */
public class RepositoryBox extends AbstractBox
{

  /**
   * ct.
   */
  public RepositoryBox()
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
    return i18n.tr("Online-Repositories");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    try
    {
      I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
      SimpleContainer container = new SimpleContainer(parent);
      container.addText(i18n.tr("Bitte wählen Sie ein Online-Repository durch Doppelklick aus, von dem Sie weitere Plugins installieren möchten."),true);
      new RepositoryList().paint(parent);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to display repository list",e);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  public boolean isActive()
  {
    // Nur, wenn keine weiteren Plugins installiert sind
    List l = Application.getPluginLoader().getInstalledManifests();
    return super.isActive() && l.size() == 1;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    // Nur, wenn keine weiteren Plugins installiert sind
    List l = Application.getPluginLoader().getInstalledManifests();
    return l.size() == 1;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    // Das darf der User nicht.
  }

  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  public int getHeight()
  {
    return 200;
  }

}


/**********************************************************************
 * $
 **********************************************************************/
