/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/action/RepositoryOpen.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/01/19 00:41:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.gui.action;

import java.net.URL;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.gui.parts.RepositoryList;
import de.willuhn.jameica.update.gui.views.RepositoryDetails;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Oeffnet die Konfiguration eines Repository.
 */
public class RepositoryOpen implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof RepositoryList.UrlObject)
      context = ((RepositoryList.UrlObject)context).getUrl();
    
    if (context instanceof String)
    {
      try
      {
        context = new URL(context.toString());
      }
      catch (Exception e)
      {
        Logger.error("invalid url: " + context);
      }
    }

    if (context == null || !(context instanceof URL))
    {
      I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
      throw new ApplicationException(i18n.tr("Bitte wählen Sie eine URL aus"));
    }
    
    GUI.startView(RepositoryDetails.class,context);
  }

}


/**********************************************************************
 * $
 **********************************************************************/
