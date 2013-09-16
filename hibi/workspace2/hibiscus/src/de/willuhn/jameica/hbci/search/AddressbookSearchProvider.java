/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/search/AddressbookSearchProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/09/03 11:13:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.search;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung eines Searchproviders fuer das Hibiscus-Adressbuch.
 */
public class AddressbookSearchProvider implements SearchProvider
{

  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Adressbuch");
  }

  /**
   * @see de.willuhn.jameica.search.SearchProvider#search(java.lang.String)
   */
  public List search(String search) throws RemoteException,
      ApplicationException
  {
    if (search == null || search.length() == 0)
      return null;

    try
    {
      AddressbookService service = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
      List result = service.findAddresses(search);
      if (result == null)
        return null;
      
      ArrayList al = new ArrayList();
      for (int i=0;i<result.size();++i)
      {
        al.add(new MyResult((Address) result.get(i)));
      }
      return al;
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to search in addressbook",e);
    }
    return null;
  }

  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private Address address = null;
    
    /**
     * ct.
     * @param a
     */
    private MyResult(Address a)
    {
      this.address = a;
    }

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      new EmpfaengerNew().handleAction(this.address);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      try
      {
        String comment = this.address.getKommentar();
        if (comment != null && comment.length() > 0)
          return this.address.getName() + " (" + comment + ")";
        return this.address.getName();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determin result name",re);
        return null;
      }
    }
    
  }

}


/*********************************************************************
 * $Log: AddressbookSearchProvider.java,v $
 * Revision 1.1  2008/09/03 11:13:51  willuhn
 * @N Mehr Suchprovider
 *
 **********************************************************************/