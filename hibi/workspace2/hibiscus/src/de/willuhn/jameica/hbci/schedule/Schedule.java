/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/schedule/Schedule.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/02/20 17:03:50 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.schedule;

import java.util.Date;

import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;

/**
 * Bean-Holder eines einzelnen Schedule.
 * @param <T> der konkrete Typ.
 */
public class Schedule<T extends HibiscusDBObject>
{
  private Date date = null;
  private T context = null;
  private boolean planned = false;
  
  /**
   * ct.
   * @param date der Termin des Auftrages.
   * @param context der Auftrag.
   * @param planned true, wenn der Auftrag noch nicht existiert sondern er lediglich geplant ist.
   */
  public Schedule(Date date, T context, boolean planned)
  {
    this.date    = date;
    this.context = context;
    this.planned = planned;
  }
  
  /**
   * Liefert den Termin.
   * @return der Termin.
   */
  public Date getDate()
  {
    return this.date;
  }
  
  /**
   * Liefert den Auftrag.
   * @return der Auftrag.
   */
  public T getContext()
  {
    return this.context;
  }
  
  /**
   * Liefert true, wenn der Auftrag noch nicht existiert sondern lediglich geplant ist.
   * @return true, wenn er noch nicht existiert sondern lediglich geplant ist.
   */
  public boolean isPlanned()
  {
    return this.planned;
  }
}



/**********************************************************************
 * $Log: Schedule.java,v $
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/