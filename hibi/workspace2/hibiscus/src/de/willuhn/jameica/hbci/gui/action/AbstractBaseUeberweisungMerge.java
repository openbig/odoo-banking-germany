/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AbstractBaseUeberweisungMerge.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/01/14 23:09:14 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.TransferMergeDialog;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Action, ueber Einzel-Auftraege zu einem Sammel-Auftrag zusammenzufassen.
 */
public abstract class AbstractBaseUeberweisungMerge implements Action
{

  /**
   * Erzeugt den Sammelauftrag basierend auf dem Context, speichert alles in der Datenbank
   * und liefert ihn zurueck.
   * @param context der Context aus handleAction.
   * @return der erzeugte und bereits gespeicherte Sammel-Auftrag.
   * @throws ApplicationException
   */
  SammelTransfer create(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Auftr�ge aus"));

    if (!(context instanceof BaseUeberweisung) && !(context instanceof BaseUeberweisung[]))
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Auftr�ge aus"));

    BaseUeberweisung[] transfers = null;
    
    if (context instanceof BaseUeberweisung)
      transfers = new BaseUeberweisung[]{(BaseUeberweisung) context};
    else
      transfers = (BaseUeberweisung[]) context;
    
    if (transfers.length == 0)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen oder mehrere Auftr�ge aus"));
      
    SammelTransfer t = null;
		try
    {
		  t = (SammelTransfer) Settings.getDBService().createObject(getTransferClass(),null);
      // Wenn der Sammel-Transfer noch kein Konto hat, nehmen wir das erste
      // der Einzel-Auftraege
      if (t.getKonto() == null)
        t.setKonto(transfers[0].getKonto());
      TransferMergeDialog d = new TransferMergeDialog(t,TransferMergeDialog.POSITION_CENTER);
      SammelTransfer existing = (SammelTransfer) d.open();
      if (!existing.isNewObject()) // Das ist ein bereits existierender
        t = existing;
      
      boolean delete = d.getDelete();
      
      // OK, wir starten die Erzeugung des Auftrages
      t.transactionBegin();
      if (t.isNewObject())
        t.store(); // nur noetig, wenn ein neuer Sammelauftrag erzeugt wird
      
      Class bClass = getBuchungClass();
      for (int i=0;i<transfers.length;++i)
      {
        SammelTransferBuchung buchung = (SammelTransferBuchung) Settings.getDBService().createObject(bClass,null);
        buchung.setSammelTransfer(t);
        buchung.setBetrag(transfers[i].getBetrag());
        buchung.setGegenkontoBLZ(transfers[i].getGegenkontoBLZ());
        buchung.setGegenkontoName(transfers[i].getGegenkontoName());
        buchung.setGegenkontoNummer(transfers[i].getGegenkontoNummer());
        buchung.setZweck(transfers[i].getZweck());
        buchung.setZweck2(transfers[i].getZweck2());
        buchung.setWeitereVerwendungszwecke(transfers[i].getWeitereVerwendungszwecke());
        buchung.setTextSchluessel(transfers[i].getTextSchluessel());
        buchung.store();
        
        if (delete)
          transfers[i].delete();
      }
      t.transactionCommit();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Sammelauftrag erzeugt"), StatusBarMessage.TYPE_SUCCESS));
      return t;
		}
		catch (ApplicationException ae)
		{
      if (t != null) {
        try {
          t.transactionRollback();
        } catch (Exception e) {Logger.error("unable to rollback transaction",e);}
      }
			throw ae;
		}
    catch (OperationCanceledException oce)
    {
      if (t != null) {
        try {
          t.transactionRollback();
        } catch (Exception e) {Logger.error("unable to rollback transaction",e);}
      }
      throw oce;
    }
		catch (Exception e)
		{
      if (t != null) {
        try {
          t.transactionRollback();
        } catch (Exception ex) {Logger.error("unable to rollback transaction",ex);}
      }
			Logger.error("error while exporting transfers",e);
			throw new ApplicationException(i18n.tr("Fehler beim Erzeugen des Sammel-Auftrages"));
		}
  }

  /**
   * Muss von abgeleieteten Klassen implementiert werden, um das Interface
   * des Sammel-Auftrages zurueckzuliefern.
   * @return Interface des Sammelauftrages.
   * @throws RemoteException
   */
  abstract Class getTransferClass() throws RemoteException;
  
  /**
   * Muss von abgeleieteten Klassen implementiert werden, um das Interface
   * einer Buchung des Sammel-Auftrages zurueckzuliefern.
   * @return Interface einer Buchung des Sammel-Auftrages.
   * @throws RemoteException
   */
  abstract Class getBuchungClass() throws RemoteException;
}


/**********************************************************************
 * $Log: AbstractBaseUeberweisungMerge.java,v $
 * Revision 1.1  2010/01/14 23:09:14  willuhn
 * @B Beim Mergen einer Einzel-Lastschrift in eine Sammel-Lastschrift wurde der Textschluessel nicht mitkopiert (siehe Mail von Ralf vom 14.01.2010)
 *
 * Revision 1.3  2009/11/26 13:25:30  willuhn
 * @N Einzel-Auftraege in existierende Sammel-Auftraege uebernehmen
 *
 * Revision 1.2  2008/12/04 21:30:06  willuhn
 * @N BUGZILLA 188
 *
 * Revision 1.1  2007/10/25 15:47:21  willuhn
 * @N Einzelauftraege zu Sammel-Auftraegen zusammenfassen (BUGZILLA 402)
 *
 **********************************************************************/