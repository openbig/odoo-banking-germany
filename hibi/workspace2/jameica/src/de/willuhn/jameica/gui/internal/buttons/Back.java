/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/buttons/Back.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/26 12:01:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.buttons;

import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;

/**
 * Vorkonfigurierter Zurueck-Button.
 */
public class Back extends Button
{
  /**
   * ct.
   * Der Button ist als Default-Button markiert.
   * @deprecated Ist nicht mehr noetig, da Jameica inzwischen einen globalen Back-Button besitzt
   */
  public Back()
  {
    this(true);
  }

  /**
   * ct.
   * @param isDefault true, wenn es der Default-Button sein soll.
   * @deprecated Ist nicht mehr noetig, da Jameica inzwischen einen globalen Back-Button besitzt
   */
  public Back(boolean isDefault)
  {
    super(Application.getI18n().tr("Zur�ck"),new de.willuhn.jameica.gui.internal.action.Back(),null,isDefault,"go-previous.png");
  }
}


/**********************************************************************
 * $Log: Back.java,v $
 * Revision 1.2  2011/04/26 12:01:42  willuhn
 * @D javadoc Fixes
 *
 * Revision 1.1  2009-01-20 10:51:51  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 **********************************************************************/
