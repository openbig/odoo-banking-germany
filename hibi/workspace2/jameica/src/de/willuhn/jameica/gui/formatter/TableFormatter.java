/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/formatter/TableFormatter.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/12 19:15:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.formatter;

import org.eclipse.swt.widgets.TableItem;

/**
 * Klasse, welche fuer die Formatierung von Tabellen verwendet werden kann.
 */
public interface TableFormatter {
	
	/**
	 * Formatiert ein TableItem.
   * @param item das zu formatierende Item.
   */
  public void format(TableItem item); 
}


/**********************************************************************
 * $Log: TableFormatter.java,v $
 * Revision 1.1  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.1  2004/04/05 23:29:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/