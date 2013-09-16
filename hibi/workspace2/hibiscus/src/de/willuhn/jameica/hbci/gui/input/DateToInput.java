/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/DateToInput.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/12/18 23:20:20 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.Calendar;
import java.util.Date;

import de.willuhn.jameica.util.DateUtil;

/**
 * Vorkonfigueriertes Eingabefeld fuer ein End-Datum.
 */
public class DateToInput extends AbstractDateInput
{
  /**
   * ct.
   */
  public DateToInput()
  {
    this(null);
  }
  
  /**
   * ct.
   * @param date
   */
  public DateToInput(Date date)
  {
    this(date,"transferlist.filter.to");
  }
  
  /**
   * ct.
   * @param date
   * @param parameter Schluessel-Name, unter dem die Eingabe in der Session gecached werden soll.
   */
  public DateToInput(Date date, String parameter)
  {
    super(date,parameter);
    this.setName(i18n.tr("End-Datum"));
    this.setComment(i18n.tr("Sp�testes Datum"));
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.input.AbstractDateInput#getDefault()
   */
  Date getDefault()
  {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH,Calendar.DECEMBER);
    cal.set(Calendar.DATE,31);

    return DateUtil.endOfDay(cal.getTime());
  }
}



/**********************************************************************
 * $Log: DateToInput.java,v $
 * Revision 1.4  2011/12/18 23:20:20  willuhn
 * @N GUI-Politur
 *
 * Revision 1.3  2011-08-05 12:02:11  willuhn
 * @B Konstruktor falsch
 *
 * Revision 1.2  2011-08-05 11:34:39  willuhn
 * @N Gemeinsame Basis-Klasse
 *
 * Revision 1.1  2011-08-05 11:21:59  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 **********************************************************************/