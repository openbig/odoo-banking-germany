/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/views/UpdatesView.java,v $
 * $Revision: 1.2 $
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

import java.util.List;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.gui.parts.PluginList;
import de.willuhn.jameica.update.rmi.PluginData;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste der gefundenen Updates an.
 */
public class UpdatesView extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Verfügbare Updates"));
    
    Container container = new SimpleContainer(this.getParent(),true);
    new PluginList((List<PluginData>)this.getCurrentObject()).paint(container.getComposite());
  }

}


/**********************************************************************
 * $
 **********************************************************************/
