/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/beans/RestMethodDoc.java,v $
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

/**
 * Bean, welche die Dokumentation einer einzelnen REST-Funktion enthaelt.
 */
public class RestMethodDoc implements Serializable
{
  private String path    = null;
  private String text    = null;
  private String example = null;
  private String method  = null;
  
  /**
   * Liefert das URI-Pattern.
   * @return URI-Pattern.
   */
  public String getPath()
  {
    return path;
  }
  
  /**
   * Speichert das URI-Pattern.
   * @param path das URI-Pattern.
   */
  public void setPath(String path)
  {
    this.path = path;
  }
  
  /**
   * Liefert den Beschreibungstext.
   * @return der Beschreibungstext.
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
   * Liefert einen Beispiel-Aufruf.
   * @return Beispiel-Aufruf.
   */
  public String getExample()
  {
    return example;
  }
  
  /**
   * Speichert den Beispiel-Aufruf.
   * @param example der Beispiel-Aufruf.
   */
  public void setExample(String example)
  {
    this.example = example;
  }
  
  /**
   * Liefert den Namen der Funktion.
   * @return Name der Funktion.
   */
  public String getMethod()
  {
    return method;
  }
  
  /**
   * Speichert den Namen der Funktion.
   * @param method Name der Funktion.
   */
  public void setMethod(String method)
  {
    this.method = method;
  }
}


/**********************************************************************
 * $
 **********************************************************************/
