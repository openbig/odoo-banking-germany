/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/KontoList.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/08/05 11:21:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.passports.ddv.server.PassportImpl;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Vorkonfigurierte Tabelle zur Anzeige der zugeordneten und zuordenbaren Konten zu einer DDV-Config.
 */
public class KontoList extends de.willuhn.jameica.hbci.gui.parts.KontoList
{
  private DDVConfig myConfig = null;

  /**
   * ct.
   * @param config die Konfiguration, fuer den die Konten angezeigt werden sollen.
   * @throws RemoteException
   */
  public KontoList(DDVConfig config) throws RemoteException
  {
    super(PseudoIterator.fromArray(new Konto[0]),new KontoNew());
    this.setCheckable(true);
    this.setSummary(false);
    this.myConfig = config;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    // Erst das Parent zeichnen, damit wir anschliessend die
    // Konten checkable machen koennen.
    super.paint(parent);
    
    /////////////////////////////////////////////////////////////////
    // Wir ermitteln die Liste der bereits verlinkten Konten
    ArrayList linked = new ArrayList();
    List<DDVConfig> configs = DDVConfigFactory.getConfigs();
    for (DDVConfig config:configs)
    {
      if (this.myConfig != null && this.myConfig.getId().equals(config.getId()))
        continue; // Das sind wir selbst

      List<Konto> konten = config.getKonten();
      if (konten == null || konten.size() == 0)
        continue;
      for (Konto k:konten)
      {
        linked.add(k);
      }
    }
    GenericIterator exclude = PseudoIterator.fromArray((GenericObject[])linked.toArray(new GenericObject[linked.size()]));
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Liste der existierenden Konten mit DDV ermitteln
    // Davon ziehen wir die bereits verlinkten ab
    List<Konto> konten = new ArrayList<Konto>();
    DBIterator list = de.willuhn.jameica.hbci.Settings.getDBService().createList(Konto.class);
    list.addFilter("passport_class = ?",PassportImpl.class.getName());
    list.setOrder("ORDER BY blz, bezeichnung");
    while (list.hasNext())
    {
      Konto k = (Konto) list.next();
      if (exclude.contains(k) != null)
        continue; // Ist schon mit einer anderen DDV-Config verlinkt
      konten.add(k);
    }
    /////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////
    // Tabelle erzeugen und nur die relevanten markieren

    // Die derzeit markierten
    GenericIterator checked = null;
    if (myConfig != null)
    {
      List<Konto> k = myConfig.getKonten();
      if (k != null && k.size() > 0)
        checked = PseudoIterator.fromArray(k.toArray(new Konto[k.size()]));
    }

    for (Konto k:konten)
    {
      this.addItem(k,checked != null && (checked.contains(k) != null));
    }
    /////////////////////////////////////////////////////////////////
  }

}


/*********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.2  2011/08/05 11:21:58  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 * Revision 1.1  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.1  2007/08/31 09:43:55  willuhn
 * @N Einer PIN/TAN-Config koennen jetzt mehrere Konten zugeordnet werden
 *
 **********************************************************************/