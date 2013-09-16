/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/menus/PluginListMenu.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/01/19 00:27:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.gui.menus;

import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.gui.action.PluginDownload;
import de.willuhn.util.I18N;

/**
 * Context-Menu fuer die Plugin-Liste.
 */
public class PluginListMenu extends ContextMenu
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  /**
   * ct.
   */
  public PluginListMenu()
  {
    addItem(new CheckedContextMenuItem(i18n.tr("Herunterladen und installieren..."),new PluginDownload(),"document-save.png"));
  }
}


/**********************************************************************
 * $
 **********************************************************************/
