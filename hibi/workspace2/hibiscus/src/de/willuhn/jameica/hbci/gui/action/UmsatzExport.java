/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzExport.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/05/11 10:20:28 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Umsaetze exportieren werden koennen.
 * Als Parameter kann eine einzelnes Umsatz-Objekt oder ein Array uebergeben werden.
 */
public class UmsatzExport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> oder <code>Umsatz[]</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie mindestens einen Umsatz aus"));

		if (!(context instanceof Umsatz) && !(context instanceof Umsatz[]))
			throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Ums�tze aus"));

    Umsatz[] u = null;
		try {

			if (context instanceof Umsatz)
			{
				u = new Umsatz[1];
        u[0] = (Umsatz) context;
			}
      else if (context instanceof Umsatz[])
      {
        u = (Umsatz[]) context;
      }

      ExportDialog d = new ExportDialog(u, Umsatz.class);
      d.open();
		}
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while exporting umsaetze",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der Ums�tze"));
		}
  }

}


/**********************************************************************
 * $Log: UmsatzExport.java,v $
 * Revision 1.4  2011/05/11 10:20:28  willuhn
 * @N OCE fangen
 *
 * Revision 1.3  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 * Revision 1.2  2005/07/04 12:41:39  web0
 * @B bug 90
 *
 * Revision 1.1  2005/06/02 22:57:34  web0
 * @N Export von Konto-Umsaetzen
 *
 **********************************************************************/