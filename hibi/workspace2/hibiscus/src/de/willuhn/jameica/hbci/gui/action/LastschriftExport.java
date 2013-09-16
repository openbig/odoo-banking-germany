/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/LastschriftExport.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/07/09 17:08:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.Lastschrift;

/**
 * Exporter fuer Lastschriften.
 */
public class LastschriftExport extends AbstractObjectExport
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractObjectExport#getExportClass()
   */
  Class getExportClass()
  {
    return Lastschrift.class;
  }

}


/*********************************************************************
 * $Log: LastschriftExport.java,v $
 * Revision 1.2  2009/07/09 17:08:02  willuhn
 * @N BUGZILLA #740
 *
 * Revision 1.1  2006/10/16 14:46:30  willuhn
 * @N CSV-Export von Ueberweisungen und Lastschriften
 *
 **********************************************************************/