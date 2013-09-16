/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/DauerauftragDelete.java,v $
 * $Revision: 1.19 $
 * $Date: 2011/03/07 10:40:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.util.Arrays;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.DauerauftragDeleteDialog;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobDauerauftragDelete;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen eines Dauerauftrages.
 * Existiert der Auftrag auch bei der Bank, wird er dort ebenfalls geloescht.
 */
public class DauerauftragDelete implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>Dauerauftrag</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null || !(context instanceof Dauerauftrag))
			throw new ApplicationException(i18n.tr("Kein Dauerauftrag ausgew�hlt"));

    final Dauerauftrag da = (Dauerauftrag) context;

		try
		{
	    final CheckboxInput check = new CheckboxInput(true);
	    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER)
	    {
	      // BUGZILLA #999
	      protected void extend(Container container) throws Exception
	      {
          // Nur bei aktiven Dauerauftraegen anzeigen
	        if (da.isActive()) {
	          final LabelInput warn = new LabelInput("");
	          warn.setColor(Color.COMMENT);
            check.addListener(new Listener() {
              public void handleEvent(Event event)
              {
                // Warnhinweis anzeigen, dass der Auftrag nur lokal geloescht wird
                Boolean b = (Boolean) check.getValue();
                if (b.booleanValue())
                  warn.setValue("");
                else
                  warn.setValue(i18n.tr("Auftrag wird nur lokal gel�scht, bei der Bank bleibt er erhalten."));
              }
            });
            container.addCheckbox(check,i18n.tr("Auftrag auch bei der Bank l�schen."));
            container.addLabelPair("",warn);
	        }
	        super.extend(container);
	      }
	    };
	    d.setTitle(i18n.tr("Dauerauftrag l�schen"));
	    d.setText(i18n.tr("Wollen Sie diesen Dauerauftrag wirklich l�schen?"));
	    d.setSize(350,SWT.DEFAULT);

	    Boolean choice = (Boolean) d.open();
	    if (!choice.booleanValue())
	      return;

	    // Nur bei der Bank loeschen, wenn er aktiv ist und der User das will
      // BUGZILLA #15
		  if (da.isActive() && (Boolean) check.getValue())
	    {

	      DauerauftragDeleteDialog d2 = new DauerauftragDeleteDialog(DauerauftragDeleteDialog.POSITION_CENTER);
	      Date date = (Date) d2.open();
	      
	      Konto konto = da.getKonto();
	      Class<SynchronizeJobDauerauftragDelete> type = SynchronizeJobDauerauftragDelete.class;

	      BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
	      SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
	      SynchronizeBackend backend = engine.getBackend(type,konto);
	      SynchronizeJob job         = backend.create(type,konto);
	      
	      job.setContext(SynchronizeJob.CTX_ENTITY,da);
	      job.setContext(SynchronizeJobDauerauftragDelete.CTX_DATE,date);
	      
        // Das Loeschen der Entity uebernimmt der HBCIDauerauftragDeleteJob selbst in "markExecuted"
	      backend.execute(Arrays.asList(job));
	    }
	    else
	    {
	      // nur lokal loeschen
	      da.delete();
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Dauerauftrag lokal gel�scht."),StatusBarMessage.TYPE_SUCCESS));
	    }
		}
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting",e);
      throw new ApplicationException(i18n.tr("Fehler beim L�schen des Auftrages: {0}",e.getMessage()));
    }
  }
}
