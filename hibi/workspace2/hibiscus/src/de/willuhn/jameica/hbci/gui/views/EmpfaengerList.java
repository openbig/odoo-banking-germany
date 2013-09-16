/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/EmpfaengerList.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/04/08 15:19:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.gui.controller.EmpfaengerControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Empfaenger-Adressen an.
 */
public class EmpfaengerList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		GUI.getView().setTitle(i18n.tr("Vorhandene Adressen"));
		
		EmpfaengerControl control = new EmpfaengerControl(this);
		
    control.getEmpfaengerListe().paint(getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Neue Adresse"),new EmpfaengerNew(),null,true,"contact-new.png");
    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: EmpfaengerList.java,v $
 * Revision 1.9  2011/04/08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 **********************************************************************/