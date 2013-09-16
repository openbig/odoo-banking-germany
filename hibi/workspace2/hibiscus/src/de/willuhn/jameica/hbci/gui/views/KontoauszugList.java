/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/KontoauszugList.java,v $
 * $Revision: 1.12 $
 * $Date: 2011/09/12 15:28:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.print.PrintSupportUmsatzList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt Kontoausz�ge an und gibt gibt sie in eine PDF-Datei aus.
 */
public class KontoauszugList extends AbstractView
{
  private I18N i18n = null;

  /**
   * ct.
   */
  public KontoauszugList()
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Kontoausz�ge"));

    final de.willuhn.jameica.hbci.gui.parts.KontoauszugList list = new de.willuhn.jameica.hbci.gui.parts.KontoauszugList();
    final PanelButtonPrint print = new PanelButtonPrint(new PrintSupportUmsatzList(list));
    list.addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        print.setEnabled(list.getSelection() != null);
      }
    });

    GUI.getView().addPanelButton(print);
    
    list.paint(getParent());
    print.setEnabled(list.getSelection() != null); // einmal initial ausloesen
  }

}

/*******************************************************************************
 * $Log: KontoauszugList.java,v $
 * Revision 1.12  2011/09/12 15:28:00  willuhn
 * @N Enabled-State live uebernehmen - nicht erst beim Mouse-Over
 *
 * Revision 1.11  2011-04-13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.10  2008-04-06 23:21:43  willuhn
 * @C Bug 575
 * @N Der Vereinheitlichung wegen alle Buttons in den Auswertungen nach oben verschoben. Sie sind dann naeher an den Filter-Controls -> ergonomischer
 *
 * Revision 1.9  2007/05/02 12:40:18  willuhn
 * @C UmsatzTree*-Exporter nur fuer Objekte des Typs "UmsatzTree" anbieten
 * @C Start- und End-Datum in Kontoauszug speichern und an PDF-Export via Session uebergeben
 *
 * Revision 1.8  2007/04/27 15:30:44  willuhn
 * @N Kontoauszug-Liste in TablePart verschoben
 *
 * Revision 1.7  2007/04/26 15:02:19  willuhn
 * @N Zusaetzliche Suche nach Gegenkonto
 *
 * Revision 1.6  2007/03/21 16:56:56  willuhn
 * @N Online-Hilfe aktualisiert
 * @N Bug 337 (Stichtag in Sparquote)
 * @C Refactoring in Sparquote
 *
 * Revision 1.5  2007/03/21 15:37:46  willuhn
 * @N Vorschau der Umsaetze in Auswertung "Kontoauszug"
 *
 * Revision 1.4  2006/07/03 23:04:32  willuhn
 * @N PDF-Reportwriter in IO-API gepresst, damit er auch an anderen Stellen (z.Bsp. in der Umsatzliste) mitverwendet werden kann.
 *
 * Revision 1.3  2006/06/19 16:20:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/05/15 20:14:51  jost
 * Ausgabe -> PDF-Ausgabe
 * Revision 1.1 2006/05/14 19:53:09 jost
 * Prerelease Kontoauszug-Report Revision 1.4 2006/01/18 00:51:00
 ******************************************************************************/
