/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.ext;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.io.Exporter;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Erweitert den Export-Dialog um eine zusaetzliche Option mit der ausgewaehlt
 * werden kann, ob der Saldo mit exportiert werden soll.
 */
public class ExportSaldoExtension implements Extension
{
  /**
   * Der Context-Schluessel fuer die Option zum Ausblenden des Saldo im Export.
   */
  public final static String KEY_SALDO_HIDE = "saldo.hide";
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  public void extend(Extendable extendable)
  {
    if (!(extendable instanceof ExportDialog))
      return;
    
    ExportDialog e = (ExportDialog) extendable;
    
    Class type = e.getType();
    if (!type.isAssignableFrom(Umsatz.class))
      return;
    
    // Erstmal per Default nicht ausblenden
    Exporter.SESSION.put(KEY_SALDO_HIDE,false);
    
    final CheckboxInput check = new CheckboxInput(false);
    check.setName(i18n.tr("Spalte \"Saldo\" in Export ausblenden"));
    check.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        Exporter.SESSION.put(KEY_SALDO_HIDE,check.getValue());
      }
    });
    
    final Container c = e.getContainer();
    c.addInput(check);
  }
}


