/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/annotation/Doc.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/05/11 23:21:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation zur Dokumentation.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Doc {
  
  /**
   * Der Text der Dokumentation.
   * @return der Text der Dokumentation.
   */
  String value() default "";
  
  /**
   * Beispiel-Aufruf/-Code.
   * @return Beispiel-Aufruf/-Code.
   */
  String example() default "";
}


/*********************************************************************
 * $Log: Doc.java,v $
 * Revision 1.1  2010/05/11 23:21:44  willuhn
 * @N Automatische Dokumentations-Seite fuer die REST-Beans basierend auf der Annotation "Doc"
 *
 **********************************************************************/