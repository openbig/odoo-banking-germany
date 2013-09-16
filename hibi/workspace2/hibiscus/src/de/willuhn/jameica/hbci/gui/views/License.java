/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/License.java,v $
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
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.LicenseControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * View fuer die Lizenz-Informationen
 */
public class License extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		GUI.getView().setTitle(i18n.tr("Lizenzinformationen"));
    
		LicenseControl control = new LicenseControl(this);

		Part libs = control.getLibList();
		libs.paint(getParent());
  }
}


/**********************************************************************
 * $Log: License.java,v $
 * Revision 1.9  2011/04/08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.8  2009/01/20 10:51:45  willuhn
 * @N Mehr Icons - fuer Buttons
 **********************************************************************/