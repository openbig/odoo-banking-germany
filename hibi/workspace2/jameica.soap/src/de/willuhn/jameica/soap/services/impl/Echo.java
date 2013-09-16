/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.soap/src/de/willuhn/jameica/soap/services/impl/Echo.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/01/19 00:33:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.soap.services.impl;

import javax.jws.WebService;

import de.willuhn.logging.Logger;


/**
 * Implementierung des Echo-Services.
 */
@WebService(endpointInterface="de.willuhn.jameica.soap.services.Echo",name="Echo")
public class Echo implements de.willuhn.jameica.soap.services.Echo
{
  /**
   * @see de.willuhn.jameica.soap.services.Echo#echo(java.lang.String)
   */
  public String echo(String echo)
  {
    Logger.info("GOT ECHO: " + echo);
    return "Echo: " + echo;
  }

}


/*********************************************************************
 * $Log: Echo.java,v $
 * Revision 1.4  2010/01/19 00:33:59  willuhn
 * @C Publishing der Webservices aus MessageConsumer in dedizierten PublishService verschoben
 * @N Auto-Deployment von Services via AutoService
 *
 * Revision 1.3  2008/07/11 12:56:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2008/07/09 23:30:53  willuhn
 * @R Nicht benoetigte Jars (gemaess WHICH_JARS) entfernt
 * @N Deployment vereinfacht
 *
 * Revision 1.1  2008/07/09 21:39:40  willuhn
 * @R Axis2 gegen Apache CXF ersetzt. Letzteres ist einfach besser ;)
 *
 * Revision 1.1  2008/07/09 18:24:34  willuhn
 * @N Apache CXF als zweiten SOAP-Provider hinzugefuegt
 *
 **********************************************************************/