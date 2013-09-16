/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCISaldoJob.java,v $
 * $Revision: 1.32 $
 * $Date: 2012/03/01 22:19:15 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;

import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.GV_Result.GVRSaldoReq.Info;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Salden-Abfrage".
 */
public class HBCISaldoJob extends AbstractHBCIJob {

	private Konto konto = null;

  /**
	 * ct.
   * @param konto konto, fuer das der Saldo ermittelt werden soll.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISaldoJob(Konto konto) throws ApplicationException, RemoteException
	{
		try
		{
			if (konto == null)
				throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Konto aus")); 

			if (konto.isNewObject())
				konto.store();

			this.konto = konto;

			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));

      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;
      setJobParam("my.curr",curr);
    }
		catch (RemoteException e)
		{
			throw e;
		}
		catch (ApplicationException e2)
		{
			throw e2;
		}
		catch (Throwable t)
		{
			Logger.error("error while executing job " + getIdentifier(),t);
			throw new ApplicationException(i18n.tr("Fehler beim Erstellen des Auftrags. Fehlermeldung: {0}",t.getMessage()),t);
		}
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier()
  {
    return "SaldoReq";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Saldo-Abruf {0}",konto.getLongName());
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
    GVRSaldoReq result = (GVRSaldoReq) getJobResult();
    konto.addToProtokoll(i18n.tr("Saldo abgerufen"),Protokoll.TYP_SUCCESS);

    // Jetzt speichern wir noch den neuen Saldo.
    Info[] info = result.getEntries();
    if (info == null || info.length == 0)
      throw new ApplicationException(i18n.tr("Keine Saldo-Informationen erhalten"));
    
    Saldo saldo = info[0].ready;
    Value avail = info[0].available;
    konto.setSaldo(saldo.value.getDoubleValue());
    if (avail != null)
      konto.setSaldoAvailable(avail.getDoubleValue());

    konto.store();
    Application.getMessagingFactory().sendMessage(new SaldoMessage(konto));
    Logger.info("saldo fetched successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Abrufen das Saldos: {0}",error);
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}
