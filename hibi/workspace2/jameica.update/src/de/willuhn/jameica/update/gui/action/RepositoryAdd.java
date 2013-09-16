/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/action/RepositoryAdd.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/01/18 00:18:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.gui.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.rmi.RepositoryService;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Aktion zum Hinzufuegen eines neuen Repository.
 */
public class RepositoryAdd implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
    
    String url = null;
    try
    {
      String s = i18n.tr("Bitte geben Sie die URL des Repository ein");
      url = Application.getCallback().askUser(s,i18n.tr("Neue URL"));
      if (url == null || url.length() == 0)
        return;

      URL u = new URL(url);

      RepositoryService service = (RepositoryService) Application.getServiceFactory().lookup(Plugin.class,"repository");

      List<URL> list = service.getRepositories();
      if (list.contains(u))
        throw new ApplicationException(i18n.tr("Repository-URL {0} existiert bereits",url));
      
      service.addRepository(u);
      Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.add").sendMessage(new QueryMessage(u));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Repository-URL hinzugefügt"),StatusBarMessage.TYPE_SUCCESS));

    }
    catch (OperationCanceledException oce)
    {
      // ignore
    }
    catch (MalformedURLException e)
    {
      throw new ApplicationException(i18n.tr("Ungültige URL: {0}",url));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while adding url " + context,e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Hinzufügen der Repository-URL"),StatusBarMessage.TYPE_ERROR));
    }
  }

}


/**********************************************************************
 * $
 **********************************************************************/
