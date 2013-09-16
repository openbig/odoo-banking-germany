/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIAuslandsUeberweisungJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen AuslandsUeberweisung.
 */
public class HBCISynchronizeJobSepaUeberweisung extends SynchronizeJobSepaUeberweisung implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCIAuslandsUeberweisungJob((AuslandsUeberweisung) this.getContext(CTX_ENTITY))};
  }

}
