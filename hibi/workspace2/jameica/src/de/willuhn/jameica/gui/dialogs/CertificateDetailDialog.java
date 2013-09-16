/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/CertificateDetailDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/05/03 10:13:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.dialogs;

import java.security.cert.X509Certificate;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, der ein X509-Zertifikat lediglich anzeigt.
 */
public class CertificateDetailDialog extends AbstractCertificateDialog
{

  /**
   * ct.
   * @param position
   * @param cert
   */
  public CertificateDetailDialog(int position, X509Certificate cert)
  {
    super(position, cert);
    setTitle(i18n.tr("Zertifikat"));
   
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractCertificateDialog#paintButtons(de.willuhn.jameica.gui.parts.ButtonArea)
   */
  protected void paintButtons(ButtonArea buttons)
  {
    buttons.addButton("   " + i18n.tr("Schlie�en") + "   ", new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"process-stop.png");
  }
}


/**********************************************************************
 * $Log: CertificateDetailDialog.java,v $
 * Revision 1.3  2011/05/03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.2  2008-04-15 16:16:36  willuhn
 * @B BUGZILLA 584
 *
 * Revision 1.1  2005/06/13 12:13:37  web0
 * @N Certificate-Code completed
 *
 **********************************************************************/