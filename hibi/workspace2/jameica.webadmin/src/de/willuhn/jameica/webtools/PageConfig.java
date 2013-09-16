/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webtools/PageConfig.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/10/27 14:32:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webtools;

import net.n3.nanoxml.IXMLElement;
import de.willuhn.logging.Logger;

/**
 * Die Config einer einzelnen Seite.
 */
public class PageConfig
{
  private String pattern     = null;
  private String template    = null;
  private String action      = null;
  private String controller  = null;
  
  /**
   * ct.
   * @param e das zugehoerige XML-Element.
   * @throws Exception wenn das XML-Element Fehler enthaelt.
   */
  PageConfig(IXMLElement e) throws Exception
  {
    this.pattern    = e.getAttribute("pattern",null);
    this.template   = e.getAttribute("template",null);
    this.action     = e.getAttribute("action",null);
    this.controller = e.getAttribute("controller",null);

    Logger.debug("  pattern: " + this.pattern);
    Logger.debug("    template  : " + this.template);
    Logger.debug("    action    : " + this.action);
    Logger.debug("    controller: " + this.controller);

    if (this.pattern == null || this.pattern.length() == 0)
      throw new Exception("page definition contains no pattern");

    if (this.template == null || this.template.length() == 0)
      throw new Exception("page definition contains no template");
  }
  
  /**
   * Liefert den Pattern der Seite.
   * @return Pattern.
   * Die Funktion liefert nie NULL, weil das bereits bei der Erstellung
   * des Objektes geprueft wurde.
   */
  String getPattern()
  {
    return this.pattern;
  }
  
  /**
   * Liefert den Dateinamen des zu verwendenden Velocity-Templates.
   * @return Dateiname des zu verwendenden Velocity-Templates.
   * Die Funktion liefert nie NULL, weil das bereits bei der Erstellung
   * des Objektes geprueft wurde.
   */
  String getTemplate()
  {
    return this.template;
  }
  
  /**
   * Liefert den Namen der optionalen Default-Action.
   * Diese wird ausgefuehrt, wenn im HTTP-Request keine Action angegeben wurde.
   * @return die optionale Default-Action.
   */
  String getAction()
  {
    return this.action;
  }
  
  /**
   * Liefert den Klassennamen des optionalen Controllers.
   * @return Klassenname des optionalen Controllers.
   */
  String getController()
  {
    return this.controller;
  }
}


/**********************************************************************
 * $Log: PageConfig.java,v $
 * Revision 1.1  2010/10/27 14:32:17  willuhn
 * @R jameica.webtools ist jetzt Bestandteil von jameica.webadmin. Das separate webtools-Plugin ist nicht mehr noetig
 *
 * Revision 1.5  2010/02/01 15:13:36  willuhn
 * @N Neues Element "beans" fuer globale Beans in webtools.xml
 * @N Default-Action via Attribut "action"
 *
 * Revision 1.4  2010/01/21 10:05:18  willuhn
 * @N webtools um Default-Action erweitert
 *
 * Revision 1.3  2009/08/19 21:45:11  willuhn
 * @N Fehlerhandling
 *
 * Revision 1.2  2009/08/12 22:58:04  willuhn
 * @N Passende Bean-Properties automatisch aufrufen, wenn in den Request-Parametern entsprechende Werte uebergeben wurden
 *
 * Revision 1.1  2009/08/05 11:04:00  willuhn
 * @C Code cleanup
 * @N Webtools kennt jetzt die Lifecycle-Annotation
 *
 **********************************************************************/