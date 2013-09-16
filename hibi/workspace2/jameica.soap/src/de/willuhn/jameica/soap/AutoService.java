/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.soap/src/de/willuhn/jameica/soap/AutoService.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/01/19 00:33:59 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.soap;

import javax.jws.WebService;

/**
 * Marker-Interface fuer Webservices, die automatisch deployed werden sollen.
 * Wenn eine Klasse dieses Interface implementiert und via Classfinder
 * gefunden wird, wird die Implementierung automatisch als Webservice
 * verfuegbar gemacht.
 * 
 * Die Implementierung muss die {@link WebService} Annotation besitzen und
 * das Attribut {@link WebService#name()} muss vorhanden sein.
 */
public interface AutoService
{
}



/**********************************************************************
 * $Log: AutoService.java,v $
 * Revision 1.1  2010/01/19 00:33:59  willuhn
 * @C Publishing der Webservices aus MessageConsumer in dedizierten PublishService verschoben
 * @N Auto-Deployment von Services via AutoService
 *
 **********************************************************************/