/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/DauerauftragNew.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/04/22 16:21:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer neuen Dauerauftrag.
 */
public class DauerauftragNew implements Action
{

  /**
   * Als Context kann ein Konto, ein Empfaenger oder ein Dauerauftrag angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in dem Dauerauftrag 
   * vorausgefuellt oder der uebergebene Dauerauftrag geladen.
   * Wenn nichts angegeben ist, wird ein leerer Dauerauftrag erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		Dauerauftrag d = null;

		if (context instanceof Dauerauftrag)
		{
			d = (Dauerauftrag) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				d = (Dauerauftrag) Settings.getDBService().createObject(Dauerauftrag.class,null);
        if ((k.getFlags() & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED && (k.getFlags() & Konto.FLAG_OFFLINE) != Konto.FLAG_OFFLINE)
  				d.setKonto(k);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}
		else if (context instanceof Address)
		{
			try {
				Address e = (Address) context;
				d = (Dauerauftrag) Settings.getDBService().createObject(Dauerauftrag.class,null);
				d.setGegenkonto(e);
			}
			catch (RemoteException e)
			{
				throw new ApplicationException(i18n.tr("Fehler beim Anlegen des Dauerauftrages"));
			}
		}

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.DauerauftragNew.class,d);
  }

}


/**********************************************************************
 * $Log: DauerauftragNew.java,v $
 * Revision 1.8  2010/04/22 16:21:27  willuhn
 * @N HBCI-relevante Buttons und Aktionen fuer Offline-Konten sperren
 *
 * Revision 1.7  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.6  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.5  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.4  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.3  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.2  2004/11/13 17:12:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:03  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.1  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 **********************************************************************/