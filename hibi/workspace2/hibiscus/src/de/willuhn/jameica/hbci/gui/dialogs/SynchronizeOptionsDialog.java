/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/SynchronizeOptionsDialog.java,v $
 * $Revision: 1.11 $
 * $Date: 2011/05/20 16:22:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den die Synchronisierungs-Optionen fuer ein Konto eingestellt werden koennen.
 */
public class SynchronizeOptionsDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private boolean offline            = false;
  private boolean syncAvail          = false;
  private SynchronizeOptions options = null;
  private CheckboxInput syncOffline  = null;
  private CheckboxInput syncSaldo    = null;
  private CheckboxInput syncUmsatz   = null;
  private CheckboxInput syncUeb      = null;
  private CheckboxInput syncLast     = null;
  private CheckboxInput syncDauer    = null;
  private CheckboxInput syncAueb     = null;
  private LabelInput error           = null;
  private Button apply               = null;

  /**
   * ct.
   * @param konto das Konto.
   * @param position
   * @throws RemoteException
   */
  public SynchronizeOptionsDialog(Konto konto, int position) throws RemoteException
  {
    super(position);
    this.setTitle(i18n.tr("Synchronisierungsoptionen"));
    this.options = new SynchronizeOptions(konto);
    this.offline = konto.hasFlag(Konto.FLAG_OFFLINE);
    
    if (this.offline)
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeEngine engine = service.get(SynchronizeEngine.class);
      this.syncAvail = engine.supports(SynchronizeJobKontoauszug.class,konto);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);

    group.addText(i18n.tr("Bitte w�hlen Sie aus, welche Gesch�ftsvorf�lle bei\nder Synchronisierung des Kontos ausgef�hrt werden sollen."),false);

    this.apply = new Button(i18n.tr("�bernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        
        if (!offline || syncAvail) // Entweder bei Online-Konten oder bei welchen mit neuem Scripting-Support
        {
          options.setSyncSaldo(((Boolean)getSyncSaldo().getValue()).booleanValue());
          options.setSyncKontoauszuege(((Boolean)getSyncUmsatz().getValue()).booleanValue());
        }

        if (offline)
        {
          options.setSyncOffline(((Boolean)getSyncOffline().getValue()).booleanValue());
        }
        else
        {
          options.setSyncUeberweisungen(((Boolean)getSyncUeb().getValue()).booleanValue());
          options.setSyncLastschriften(((Boolean)getSyncLast().getValue()).booleanValue());
          options.setSyncDauerauftraege(((Boolean)getSyncDauer().getValue()).booleanValue());
          options.setSyncAuslandsUeberweisungen(((Boolean)getSyncAueb().getValue()).booleanValue());
        }
        close();
      }
    },null,true,"ok.png");
    
    
    if (!offline || syncAvail)
    {
      group.addInput(getSyncSaldo());
      group.addInput(getSyncUmsatz());
    }

    if (offline)
    {
      group.addInput(getSyncOffline());
    }
    else
    {
      group.addInput(getSyncUeb());
      group.addInput(getSyncAueb());
      group.addInput(getSyncLast());
      group.addInput(getSyncDauer());
    }
    
    group.addInput(getErrorLabel());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.apply);
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"process-stop.png");
    
    group.addButtonArea(buttons);
  }
  
  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Salden.
   * @return Checkbox.
   */
  private CheckboxInput getSyncSaldo()
  {
    if (this.syncSaldo == null)
    {
      this.syncSaldo = new CheckboxInput(options.getSyncSaldo());
      this.syncSaldo.setName(i18n.tr("Saldo abrufen"));
    }
    return this.syncSaldo;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Umsaetze.
   * @return Checkbox.
   */
  private CheckboxInput getSyncUmsatz()
  {
    if (this.syncUmsatz == null)
    {
      this.syncUmsatz = new CheckboxInput(options.getSyncKontoauszuege());
      this.syncUmsatz.setName(i18n.tr("Kontoausz�ge (Ums�tze) abrufen"));
      if (this.offline)
        this.syncUmsatz.addListener(new OfflineListener());
    }
    return this.syncUmsatz;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Ueberweisungen.
   * @return Checkbox.
   */
  private CheckboxInput getSyncUeb()
  {
    if (this.syncUeb == null)
    {
      this.syncUeb = new CheckboxInput(options.getSyncUeberweisungen());
      this.syncUeb.setName(i18n.tr("F�llige �berweisungen absenden"));
    }
    return this.syncUeb;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Lastschriften.
   * @return Checkbox.
   */
  private CheckboxInput getSyncLast()
  {
    if (this.syncLast == null)
    {
      this.syncLast = new CheckboxInput(options.getSyncLastschriften());
      this.syncLast.setName(i18n.tr("F�llige Lastschriften einziehen"));
    }
    return this.syncLast;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der Dauerauftraege.
   * @return Checkbox.
   */
  private CheckboxInput getSyncDauer()
  {
    if (this.syncDauer == null)
    {
      this.syncDauer = new CheckboxInput(options.getSyncDauerauftraege());
      this.syncDauer.setName(i18n.tr("Dauerauftr�ge synchronisieren"));
    }
    return this.syncDauer;
  }

  /**
   * Liefert eine Checkbox fuer die Aktivierung der Synchronisierung der SEPA-Ueberweisungen.
   * @return Checkbox.
   */
  private CheckboxInput getSyncAueb()
  {
    if (this.syncAueb == null)
    {
      this.syncAueb = new CheckboxInput(options.getSyncAuslandsUeberweisungen());
      this.syncAueb.setName(i18n.tr("F�llige SEPA-�berweisungen absenden"));
    }
    return this.syncAueb;
  }

  /**
   * Liefert eine Checkbox, mit der die automatische Synchronisierung
   * von Offline-Konten aktiviert werden kann.
   * @return Checkbox.
   */
  private CheckboxInput getSyncOffline()
  {
    if (this.syncOffline == null)
    {
      this.syncOffline = new CheckboxInput(this.options.getSyncOffline());
      this.syncOffline.setName(i18n.tr("Passende Gegenbuchungen automatisch anlegen"));
      this.syncOffline.addListener(new OfflineListener());
    }
    return this.syncOffline;
  }

  /**
   * Liefert ein Label fuer Fehlermeldungen.
   * @return Label.
   */
  private LabelInput getErrorLabel()
  {
    if (this.error == null)
    {
      this.error = new LabelInput("\n");
      this.error.setColor(Color.ERROR);
    }
    return this.error;
  }


  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }
  
  /**
   * Offline und Umsatz-Abruf schliessen sich gegenseitig aus.
   */
  private class OfflineListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      if (offline && syncAvail)
      {
        // Wir checken, ob beides aktiv ist und bringen einen Warnhinweis
        boolean a = ((Boolean)getSyncOffline().getValue()).booleanValue();
        boolean b = ((Boolean)getSyncUmsatz().getValue()).booleanValue();
        if (a && b)
          getErrorLabel().setValue(i18n.tr("Kontoausz�ge und Anlegen von Gegenbuchungen\nk�nnen nicht zusammen aktiviert werden."));
        else
          getErrorLabel().setValue("\n");
        
        apply.setEnabled(!(a && b));
      }
    }
  }
}
