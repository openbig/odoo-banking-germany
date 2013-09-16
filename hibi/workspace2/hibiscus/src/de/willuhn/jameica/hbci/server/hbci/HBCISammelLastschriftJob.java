/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCISammelLastschriftJob.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/06/19 11:52:15 $
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

import org.kapott.hbci.exceptions.InvalidArgumentException;

import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Sammel-Lastschrift".
 */
public class HBCISammelLastschriftJob extends AbstractHBCISammelTransferJob
{

  /**
	 * ct.
   * @param lastschrift die auszufuehrende Sammel-Lastschrift.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISammelLastschriftJob(SammelLastschrift lastschrift) throws ApplicationException, RemoteException
	{
    super(lastschrift);
    try
    {
      setJobParam("data",Converter.HibiscusSammelLastschrift2DTAUS(lastschrift).toString());
    }
    catch (InvalidArgumentException e)
    {
      // kann in "toString()" von DTAUS geworfen werden. Konkreter Fall: In Namen
      // des eigenen Konto stand bei einem User ein in DTAUS nicht erlaubtes "(" drin. Da das
      // eine RuntimeException ist, flog sie bis hoch zum GUI loop.
      throw new ApplicationException(e.getMessage());
    }
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier() {
    return "MultiLast";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Sammel-Lastschrift {0}",getSammelTransfer().getBezeichnung());
  }

}


/**********************************************************************
 * $Log: HBCISammelLastschriftJob.java,v $
 * Revision 1.5  2006/06/19 11:52:15  willuhn
 * @N Update auf hbci4java 2.5.0rc9
 *
 * Revision 1.4  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.3  2005/11/02 17:33:31  willuhn
 * @B fataler Bug in Sammellastschrift/Sammelueberweisung
 *
 * Revision 1.2  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.1  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 **********************************************************************/