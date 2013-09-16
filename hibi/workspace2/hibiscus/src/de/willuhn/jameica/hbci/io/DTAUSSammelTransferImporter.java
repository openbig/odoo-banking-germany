/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/DTAUSSammelTransferImporter.java,v $
 * $Revision: 1.12 $
 * $Date: 2009/06/15 08:51:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;

import de.jost_net.OBanToo.Dtaus.ASatz;
import de.jost_net.OBanToo.Dtaus.CSatz;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * DTAUS-Importer fuer komplette Sammel-Ueberweisungen und Sammel-Lastschriften.
 */
public class DTAUSSammelTransferImporter extends AbstractDTAUSImporter
{
  private Hashtable transferCache = null;

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(Object context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    this.transferCache = new Hashtable();
    super.doImport(context, format, is, monitor);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSImporter#create(de.willuhn.datasource.rmi.DBObject, java.lang.Object, de.jost_net.OBanToo.Dtaus.CSatz, de.jost_net.OBanToo.Dtaus.ASatz)
   */
  void create(DBObject skel, Object context, CSatz csatz, ASatz asatz)
    throws RemoteException, ApplicationException
  {
    SammelTransfer t = (SammelTransfer) this.transferCache.get(asatz);
    if (t == null)
    {
      t = (SammelTransfer) skel;
      
      try
      {
        String et = asatz.getGutschriftLastschrift();
        if ((t instanceof SammelUeberweisung) && "LK".equals(et))
        {
          if (!Application.getCallback().askUser(i18n.tr("Sie versuchen, eine DTAUS-Datei mit\nLastschriften als �berweisungen zu importieren.\nSind Sie sicher?")))
            throw new OperationCanceledException("operation cancelled by user");
        }
        else if ((t instanceof SammelLastschrift) && "GK".equals(et))
        {
          if (!Application.getCallback().askUser(i18n.tr("Sie versuchen, eine DTAUS-Datei mit\n�berweisungen als Lastschriften zu importieren.\nSind Sie sicher?")))
            throw new OperationCanceledException("operation cancelled by user");
        }
      }
      catch (OperationCanceledException oce)
      {
        throw oce;
      }
      catch (Exception e)
      {
        Logger.error("unable to ask user",e);
        // Dann importieren wir halt
      }
      

      this.transferCache.put(asatz,t);

      // Konto suchen
      String kontonummer = Long.toString(asatz.getKonto());
      String blz         = Long.toString(asatz.getBlz());

      t.setKonto(findKonto(kontonummer,blz));
      t.setTermin(asatz.getAusfuehrungsdatum());
      t.setBezeichnung(asatz.getKundenname());
      t.store();
      try
      {
        Application.getMessagingFactory().sendMessage(new ImportMessage(t));
      }
      catch (Exception ex)
      {
        Logger.error("error while sending import message",ex);
      }
    }

    final SammelTransferBuchung b = t.createBuchung();
    b.setBetrag(csatz.getBetragInEuro());
    b.setGegenkontoBLZ(Long.toString(csatz.getBlzEndbeguenstigt()));
    b.setGegenkontoName(csatz.getNameEmpfaenger());
    b.setGegenkontoNummer(Long.toString(csatz.getKontonummer()));
    b.setZweck(csatz.getVerwendungszweck(1));
    b.setTextSchluessel(mapDtausToTextschluessel(b,csatz.getTextschluessel()));
    
    int z = csatz.getAnzahlVerwendungszwecke();
    if (z > 1)
      b.setZweck2(csatz.getVerwendungszweck(2));

    // Erweiterte Verwendungszwecke?
    if (z > 2)
    {
      ArrayList l = new ArrayList();
      for (int i=3;i<=z;++i)
        l.add(csatz.getVerwendungszweck(i));
      b.setWeitereVerwendungszwecke((String[])l.toArray(new String[l.size()]));
    }

    b.store();

    // Das muessen wir hier uebernehmen, da AbstractDTAUSImporter nichts
    // von den einzelnen Buchungen weiss.
    try
    {
      Application.getMessagingFactory().sendMessage(new ImportMessage(b));
    }
    catch (Exception ex)
    {
      Logger.error("error while sending import message",ex);
    }
  }


  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSIO#getSupportedObjectTypes()
   */
  Class[] getSupportedObjectTypes()
  {
    return new Class[]
      {
        SammelLastschrift.class,
        SammelUeberweisung.class
      };
  }
}


/*********************************************************************
 * $Log: DTAUSSammelTransferImporter.java,v $
 * Revision 1.12  2009/06/15 08:51:16  willuhn
 * @N BUGZILLA 736
 *
 * Revision 1.11  2008/12/17 23:24:23  willuhn
 * @N Korrektes Mapping der Textschluessel beim Export/Import von Sammelauftraegen von/nach DTAUS
 *
 * Revision 1.10  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.9  2008/06/30 13:04:10  willuhn
 * @N Von-Bis-Filter auch in Sammel-Auftraegen
 *
 * Revision 1.8  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.7  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.6  2007/03/05 15:38:43  willuhn
 * @B Bug 365
 *
 * Revision 1.5  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.4  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.3  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 **********************************************************************/