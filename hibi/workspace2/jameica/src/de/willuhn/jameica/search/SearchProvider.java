/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/search/SearchProvider.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/08/31 23:07:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.search;

import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.util.ApplicationException;


/**
 * Interface fuer einen Provider, der von der Suchmaschine abgefragt wird.
 * Das Interface muss lediglich implementiert werden, um automatisch
 * in der Suchmaschine registriert zu werden.
 * Die Implementierungen muessen einen parameterlosen Konstruktor mit
 * dem Modifier "public" besitzen, um geladen werden zu koennen
 * (Bean-Spezifikation).
 */
public interface SearchProvider
{
  /**
   * Liefert einen sprechenden Namen fuer den Provider.
   * @return sprechender Name fuer den Provider.
   */
  public String getName();
  
  /**
   * Stellt eine Suchanfrage an den Provider.
   * @param search der Suchbegriff.
   * @return Liste der Ergebnisse als Objekte vom Typ "Result" oder <code>null</code>, wenn nichts gefunden wurde.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public List search(String search) throws RemoteException, ApplicationException;
}


/**********************************************************************
 * $Log: SearchProvider.java,v $
 * Revision 1.2  2008/08/31 23:07:10  willuhn
 * @N Erster GUI-Code fuer die Suche
 *
 * Revision 1.1  2008/08/31 14:08:45  willuhn
 * @N Erster Code fuer eine jameica-interne Suchmaschine
 *
 **********************************************************************/
