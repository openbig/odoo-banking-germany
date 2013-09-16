/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/MenuItem.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/08/26 21:47:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui;

import java.rmi.RemoteException;

import org.eclipse.swt.graphics.Image;


/**
 * Interface fuer ein Menu-Element.
 * @author willuhn
 */
public interface MenuItem extends Item
{
	/**
	 * Tastenkombi fuer Short-Cut.
   * @return Tastenkombi.
   * @throws RemoteException
   */
  public String getShortcut() throws RemoteException;
  
  /**
   * Liefert ein optionales Icon.
   * @return das optionale Icon.
   * @throws RemoteException
   */
  public Image getIcon() throws RemoteException;

}


/*********************************************************************
 * $Log: MenuItem.java,v $
 * Revision 1.3  2010/08/26 21:47:48  willuhn
 * @N Icons auch im Hauptmenu
 *
 * Revision 1.2  2004/10/11 22:41:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 **********************************************************************/