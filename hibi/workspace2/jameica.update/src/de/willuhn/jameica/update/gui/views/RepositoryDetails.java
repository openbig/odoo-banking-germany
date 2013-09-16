/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/views/RepositoryDetails.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/06/01 11:02:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.gui.controller.RepositoryContol;
import de.willuhn.util.I18N;

/**
 * Detail-Ansicht eines Repositories.
 */
public class RepositoryDetails extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    RepositoryContol control = new RepositoryContol(this);

    GUI.getView().setTitle(i18n.tr("Repository: {0}",control.getRepository().getName()));
    
    
    SimpleContainer group = new SimpleContainer(getParent(),true);
    group.addHeadline(i18n.tr("Eigenschaften"));
    group.addInput(control.getUrl());
    
    group.addHeadline(i18n.tr("Plugins"));
    group.addPart(control.getPlugins());
  }

}


/**********************************************************************
 * $
 **********************************************************************/
