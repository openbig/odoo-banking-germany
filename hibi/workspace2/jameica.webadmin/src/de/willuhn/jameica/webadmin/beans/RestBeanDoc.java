/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/beans/RestBeanDoc.java,v $
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

package de.willuhn.jameica.webadmin.beans;

import java.io.Serializable;
import java.util.List;

/**
 * Bean, welche die Dokumentation einer REST-Bean enthaelt.
 */
public class RestBeanDoc implements Serializable
{
  private Class beanClass             = null;
  private String text                 = null;
  private List<RestMethodDoc> methods = null;
  
  /**
   * Liefert den Namen der Klasse der REST-Bean.
   * @return Name der Klasse der REST-Bean.
   */
  public Class getBeanClass()
  {
    return beanClass;
  }
  
  /**
   * Speichert den Namen der Klasse der REST-Bean.
   * @param beanClass Name der Klasse der REST-Bean.
   */
  public void setBeanClass(Class beanClass)
  {
    this.beanClass = beanClass;
  }
  
  /**
   * Liefert den Beschreibungstext.
   * @return Beschreibungstext.
   */
  public String getText()
  {
    return text;
  }
  
  /**
   * Speichert den Beschreibungstext.
   * @param text der Beschreibungstext.
   */
  public void setText(String text)
  {
    this.text = text;
  }
  
  /**
   * Liefert eine Liste der Bean-Funktionen.
   * @return Liste der Bean-Funktionen.
   */
  public List<RestMethodDoc> getMethods()
  {
    return methods;
  }
  
  /**
   * Speichert die Liste der Bean-Funktionen.
   * @param methods Liste der Bean-Funktionen.
   */
  public void setMethods(List<RestMethodDoc> methods)
  {
    this.methods = methods;
  }
  
  
}


/**********************************************************************
 * $
 **********************************************************************/
