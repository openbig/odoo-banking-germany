/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.filter;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;

/**
 * Mit diesem Filter koennen einzelne Konten bei der Suche
 * ausgefiltert werden. Das wird z.Bsp. genutzt, um bei
 * Auslandsueberweisungen nur jene Konten anzuzeigen, die
 * eine IBAN besitzen.
 */
public interface KontoFilter extends Filter<Konto>
{
  /**
   * @see de.willuhn.jameica.hbci.gui.filter.Filter#accept(java.lang.Object)
   */
  public boolean accept(Konto konto) throws RemoteException;
  
  /**
   * Filter, der alle Konten zulaesst.
   */
  public final static KontoFilter ALL = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.KontoFilter#accept(de.willuhn.jameica.hbci.rmi.Konto)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      return true;
    }
  };
  
  /**
   * Filter, der nur HBCI-Konten zulaesst.
   */
  public final static KontoFilter ONLINE = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.KontoFilter#accept(de.willuhn.jameica.hbci.rmi.Konto)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      // Es muss auf jeden Fall erstmal ein aktives Konto sein
      if (!ACTIVE.accept(konto))
        return false;

      // Es darf kein Offline-Konto sein.
      return !konto.hasFlag(Konto.FLAG_OFFLINE);
    }
  };

  /**
   * Filter, der nur aktive Konten zulaesst.
   */
  public final static KontoFilter ACTIVE = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.KontoFilter#accept(de.willuhn.jameica.hbci.rmi.Konto)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      if (konto == null)
        return false;
      
      return !konto.hasFlag(Konto.FLAG_DISABLED);
    }
  };

  /**
   * Filter, der nur aktive Konten zulaesst, die eine IBAN haben.
   */
  public final static KontoFilter FOREIGN = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.AddressFilter#accept(de.willuhn.jameica.hbci.rmi.Address)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      // Muss auf jeden Fall erstmal ein Online-Konto sein.
      if (!ONLINE.accept(konto))
        return false;

      // Jetzt noch checken, ob wir eine IBAN haben
      String iban = konto.getIban();
      return (iban != null && iban.length() > 0 && iban.length() <= HBCIProperties.HBCI_IBAN_MAXLENGTH);
    }
  };
  
  /**
   * Filter, der nur Konten zulaesst, fuer die Synchronisierungsoptionen aktiviert sind oder die prinzipiell
   * synchronisierbar sind.
   */
  public final static KontoFilter SYNCED = new KontoFilter()
  {
    /**
     * @see de.willuhn.jameica.hbci.gui.filter.AddressFilter#accept(de.willuhn.jameica.hbci.rmi.Address)
     */
    public boolean accept(Konto konto) throws RemoteException
    {
      if (konto == null || konto.hasFlag(Konto.FLAG_DISABLED))
        return false;

      // Wenn es ein Online-Konto ist, kann es prinzipiell gesynct werden
      if (!konto.hasFlag(Konto.FLAG_OFFLINE))
        return true;
      
      // Ist zwar ein Offline-Konto. Aber wir schauen mal, ob es
      // synchronisiert werden kann.
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeEngine engine = service.get(SynchronizeEngine.class);
      return engine.supports(SynchronizeJobKontoauszug.class,konto);
    }
  }; 

}
