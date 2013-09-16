/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.scripting/src/de/willuhn/jameica/scripting/Plugin.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/07/23 12:58:34 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.scripting;

import javax.script.ScriptEngine;

import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.util.ApplicationException;

/**
 * Basis-Klasse des Plugins.
 */
public class Plugin extends AbstractPlugin
{
  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#init()
   */
  public void init() throws ApplicationException
  {
    ScriptEngine engine = Settings.getScriptEngine();
    if (engine == null)
      throw new ApplicationException(getResources().getI18N().tr("Die installierte Java-Version enthält keine JavaScript-Unterstützung (Rhino)"));
    
    super.init();
  }
}
