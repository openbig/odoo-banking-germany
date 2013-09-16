/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/LogExport.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/08/18 16:38:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.io.FileCopy;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Exportieren des Logs.
 */
public class LogExport implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      DateFormat format = new SimpleDateFormat("yyyyMMdd");
      FileDialog dialog = new FileDialog(GUI.getShell(), SWT.SAVE);
      dialog.setText(Application.getI18n().tr("Bitte w�hlen Sie Verzeichnis und Datei aus, in dem die Log-Datei gespeichert werden soll."));
      dialog.setFilterPath(System.getProperty("user.home"));
      dialog.setFileName("jameica-" + format.format(new Date()) + ".log");
      dialog.setOverwrite(true);
      
      String file = dialog.open();
      if (file == null || file.length() == 0)
        return;
      
      FileCopy.copy(new File(Application.getConfig().getLogFile()),new File(file),true);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Log-Datei gespeichert."),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("unable to export log file",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Speichern der Log-Datei"));
    }
  }

}



/**********************************************************************
 * $Log: LogExport.java,v $
 * Revision 1.1  2011/08/18 16:38:08  willuhn
 * @B Minimize-Button nur einmal hinzufuegen
 * @N Speichern-Button im Syslog via neuem Panel-Button
 *
 **********************************************************************/