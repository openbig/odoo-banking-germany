/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataSaldoVerlauf.java,v $
 * $Revision: 1.21 $
 * $Date: 2012/04/05 21:27:41 $
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
import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.forecast.ForecastCreator;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Datensatzes fuer die Darstellung der Saldo-Prognose.
 */
public class ChartDataSaldoForecast extends AbstractChartDataSaldo
{
  private Konto konto      = null;
  private Date end         = null;
  private List<Value> data = null;
  
  /**
   * ct.
   * @param k das Konto, fuer das das Diagramm gemalt werden soll.
   * @param end End-Datum. Die Prognose beginnt heute und reicht bis zum angegeben End-Datum.
   */
  public ChartDataSaldoForecast(Konto k, Date end)
  {
    this.konto = k;
    this.end   = end;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
   */
  public List getData() throws RemoteException
  {
    if (this.data != null)
      return this.data;
    
    this.data = ForecastCreator.create(this.konto,new Date(),this.end);
    return this.data;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
   */
  public String getLabel() throws RemoteException
  {
    if (this.konto != null)
      return this.konto.getBezeichnung();
    return i18n.tr("Alle Konten");
  }
}
