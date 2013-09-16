/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/LineChartData.java,v $
 * $Revision: 1.5 $
 * $Date: 2010/11/24 16:27:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;


/**
 * Interface, welches die zu zeichnenden Datenreihen fuer ein Liniendiagramm enthaelt.
 */
public interface LineChartData extends ChartData
{
  /**
   * Legt fest, ob die Punkte gerade oder zu einer geschwungenen Linie verbunden werden sollen.
   * @return true, wenn die Punkte zu einer geschwungenen Linie verbunden werden sollen.
   * @throws RemoteException
   */
  public boolean getCurve() throws RemoteException;
  
  /**
   * Liefert ein Array mit den Farbwerten (0-255) fuer Ror, Gruen und Blau.
   * @return Array mit den Farbwerten oder null, wenn eine zufaellige Farbe gewaehlt werden soll.
   * @throws RemoteException
   */
  public int[] getColor() throws RemoteException;
  
  /**
   * Legt fest, ob die Flaeche unter der Linie gef�llt ist oder nicht.
   * @return true falls gefuellt.
   * @throws RemoteException
   */
  public boolean isFilled() throws RemoteException;
}
