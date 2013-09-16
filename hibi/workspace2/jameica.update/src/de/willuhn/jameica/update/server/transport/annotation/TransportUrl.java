/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/server/transport/annotation/TransportUrl.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/12/12 01:13:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.server.transport.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation fuer die URL eines Transports.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TransportUrl {
}


/**********************************************************************
 * $Log: TransportUrl.java,v $
 * Revision 1.1  2008/12/12 01:13:17  willuhn
 * @N Transport-API
 *
 **********************************************************************/
