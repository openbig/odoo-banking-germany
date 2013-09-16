/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/AuslandsUeberweisungList.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/10/20 16:20:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.parts.columns.AusgefuehrtColumn;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Auslandsueberweisungen.
 */
public class AuslandsUeberweisungList extends AbstractTransferList
{

  /**
   * @param action
   * @throws RemoteException
   */
  public AuslandsUeberweisungList(Action action) throws RemoteException
  {
    super(action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.AuslandsUeberweisungList());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractTransferList#initColums()
   */
  protected void initColums()
  {
    addColumn(new KontoColumn());
    addColumn(i18n.tr("Gegenkonto Inhaber"),"empfaenger_name");
    addColumn(i18n.tr("Gegenkonto BIC"),"empfaenger_bic");
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.DATEFORMAT),false,Column.ALIGN_RIGHT);
    addColumn(new AusgefuehrtColumn());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractTransferList#getObjectType()
   */
  protected Class getObjectType()
  {
    return AuslandsUeberweisung.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#getList(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date, java.util.Date, java.lang.String)
   */
  protected DBIterator getList(Object konto, Date from, Date to, String text) throws RemoteException
  {
    DBIterator list = super.getList(konto, from, to, text);
    if (text != null && text.length() > 0)
    {
      String s = "%" + text.toLowerCase() + "%";
      list.addFilter("(LOWER(empfaenger_konto) like ? or LOWER(empfaenger_name) like ? or LOWER(zweck) like ?)", new Object[]{s,s,s});
    }
    
    return list;
  }
}
