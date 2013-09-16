/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.soap/src/de/willuhn/jameica/soap/services/Echo.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/01/19 00:33:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.soap.services;

import javax.jws.WebService;
import javax.jws.WebParam;

import de.willuhn.jameica.soap.AutoService;


/**
 * Interface des Echo-Services.
 */
@WebService
public interface Echo extends AutoService
{
  /**
   * Liefert den uebergebenen Text als Echo zurueck.
   * @param echo Echo-Text.
   * @return Echo.
   */
  public String echo(@WebParam(name="text") String echo);
}


/*********************************************************************
 * $Log: Echo.java,v $
 * Revision 1.2  2010/01/19 00:33:59  willuhn
 * @C Publishing der Webservices aus MessageConsumer in dedizierten PublishService verschoben
 * @N Auto-Deployment von Services via AutoService
 *
 * Revision 1.1  2008/07/09 21:39:40  willuhn
 * @R Axis2 gegen Apache CXF ersetzt. Letzteres ist einfach besser ;)
 *
 * Revision 1.1  2008/07/09 18:24:34  willuhn
 * @N Apache CXF als zweiten SOAP-Provider hinzugefuegt
 *
 **********************************************************************/