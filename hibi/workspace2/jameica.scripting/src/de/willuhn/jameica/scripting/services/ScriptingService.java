/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.scripting/src/de/willuhn/jameica/scripting/services/ScriptingService.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/07/29 23:43:38 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.scripting.services;

import java.util.List;

import javax.script.ScriptEngine;

import de.willuhn.datasource.Service;

/**
 * Interface fuer den Scripting-Service.
 */
public interface ScriptingService extends Service
{
  /**
   * Liefert die Script-Engine.
   * @return die Script-Engine.
   */
  public ScriptEngine getEngine();
  
  /**
   * Liefert die Namen der auszufuehrenden Javascript-Funktionen fuer das Event.
   * @param event das Event.
   * @return die auszufuehrenden JS-Funktion oder NULL, wenn keine definiert sind.
   */
  public List<String> getFunction(String event);
}



/**********************************************************************
 * $Log: ScriptingService.java,v $
 * Revision 1.2  2010/07/29 23:43:38  willuhn
 * @N Script-Mapping. Damit ueberschreiben sich die Script-Funktionen nicht mehr gegenseitig, wenn mehrere Scripts registriert wurden
 *
 * Revision 1.1  2010-07-23 12:58:34  willuhn
 * @N initial import
 *
 **********************************************************************/