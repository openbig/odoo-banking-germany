/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/SelectConfigDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/10/11 20:58:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Ein Dialog zur Auswahl der zu verwendenden DDV-Config.
 */
public class SelectConfigDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private DDVConfig selected = null;
  private String text        = null;

  /**
   * @param position
   */
  public SelectConfigDialog(int position)
  {
    super(position);
    setTitle(i18n.tr("Auswahl der Kartenleser-Konfiguration"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Konfiguration"));
    group.addText(text == null ? i18n.tr("Bitte w�hlen Sie die zu verwendende Kartenleser-Konfiguration aus") : text,true);
    
    final TablePart table = new TablePart(DDVConfigFactory.getConfigs(), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof DDVConfig))
          return;
        selected = (DDVConfig) context;
        close();
      }
    });
    table.addColumn(i18n.tr("Alias-Name"),"name");
    table.addColumn(i18n.tr("Kartenleser"),"readerPreset");
    table.addColumn(i18n.tr("Index des HBCI-Zugangs"),"entryIndex");
    table.setMulti(false);
    table.setSummary(false);
    table.paint(parent);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("�bernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        selected = (DDVConfig) table.getSelection();
        if (selected == null)
          return;
        close();
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
    buttons.paint(parent);
  }

  /**
   * Legt den Text des Dialogs fest.
   * @param text
   */
  public void setText(String text)
  {
    this.text = text;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return selected;
  }

}


/*********************************************************************
 * $Log: SelectConfigDialog.java,v $
 * Revision 1.2  2010/10/11 20:58:52  willuhn
 * @N BUGZILLA 927
 *
 * Revision 1.1  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 **********************************************************************/