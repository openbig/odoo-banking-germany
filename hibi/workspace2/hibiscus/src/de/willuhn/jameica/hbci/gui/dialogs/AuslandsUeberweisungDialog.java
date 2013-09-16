/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/AuslandsUeberweisungDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/05/11 10:05:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

/**
 * Sicherheitsabfrage beim Ausfuehren eines Auftrages.
 */
public class AuslandsUeberweisungDialog extends AbstractExecuteDialog
{
	private AuslandsUeberweisung ueb;

  /**
   * ct.
   * @param u die anzuzeigende Auslandsueberweisung.
   * @param position
   */
  public AuslandsUeberweisungDialog(AuslandsUeberweisung u, int position)
  {
    super(position);
    this.ueb = u;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    group.addHeadline(i18n.tr("Details der SEPA-�berweisung"));
			
		Input kto = new LabelInput(ueb.getKonto().getKontonummer());
		kto.setComment(ueb.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

		Input empfName = new LabelInput(ueb.getGegenkontoName());
		group.addLabelPair(i18n.tr("Name des Empf�nger"),empfName);

		Input empfKto = new LabelInput(ueb.getGegenkontoNummer());
		group.addLabelPair(i18n.tr("IBAN des Empf�ngers"),empfKto);

    Input empfBic = new LabelInput(ueb.getGegenkontoBLZ());
    group.addLabelPair(i18n.tr("BIC des Empf�ngers"),empfBic);


    LabelInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(ueb.getBetrag()) + " " + ueb.getKonto().getWaehrung());
    betrag.setColor(Color.ERROR);
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    group.addHeadline(i18n.tr("Verwendungszweck"));
    group.addText(VerwendungszweckUtil.toString(ueb,"\n"),false);
    
    group.addText(i18n.tr("Sind Sie sicher, da� Sie den Auftrag jetzt ausf�hren wollen?") + "\n",true);

    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }
}


/**********************************************************************
 * $Log: AuslandsUeberweisungDialog.java,v $
 * Revision 1.3  2011/05/11 10:05:23  willuhn
 * @N Bestaetigungsdialoge ueberarbeitet (Buttons mit Icons, Verwendungszweck-Anzeige via VerwendungszweckUtil, keine Labelgroups mehr, gemeinsame Basis-Klasse)
 *
 * Revision 1.2  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/