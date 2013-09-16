/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Standard-Job zum Senden einer Sammel-Lastschrift.
 */
public class SynchronizeJobSammelLastschrift extends AbstractSynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    try
    {
      SammelLastschrift last = (SammelLastschrift) this.getContext(CTX_ENTITY);
      Konto k = last.getKonto();
      return i18n.tr("{0}: ({1}) {2} {3} als Sammel-Lastschrift einziehen",k.getLongName(),last.getBezeichnung(),HBCI.DECIMALFORMAT.format(last.getSumme()),k.getWaehrung());
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine job name",re);
      throw new ApplicationException(i18n.tr("Auftragsbezeichnung nicht ermittelbar: {0}",re.getMessage()));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring()
  {
    return false;
  }

}


