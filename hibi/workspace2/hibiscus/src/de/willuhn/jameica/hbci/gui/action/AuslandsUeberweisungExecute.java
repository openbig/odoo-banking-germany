/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AuslandsUeberweisungExecute.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/11 10:05:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.Arrays;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.AuslandsUeberweisungDialog;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaUeberweisung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung einer Auslandsueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>AuslandsUeberweisung</code> als Context.
 */
public class AuslandsUeberweisungExecute implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
	 * Erwartet ein Objekt vom Typ <code>AuslandsUeberweisung</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null || !(context instanceof AuslandsUeberweisung))
			throw new ApplicationException(i18n.tr("Kein Auftrag angegeben"));

		try
		{
			final AuslandsUeberweisung u = (AuslandsUeberweisung) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("�berweisung wurde bereits ausgef�hrt"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			AuslandsUeberweisungDialog d = new AuslandsUeberweisungDialog(u,AuslandsUeberweisungDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)d.open()).booleanValue())
					return;
			}
      catch (OperationCanceledException oce)
      {
        Logger.info(oce.getMessage());
        return;
      }
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausf�hren des Auftrages"));
				return;
			}

	    Konto konto = u.getKonto();
	    Class<SynchronizeJobSepaUeberweisung> type = SynchronizeJobSepaUeberweisung.class;

	    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
	    SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
	    SynchronizeBackend backend = engine.getBackend(type,konto);
	    SynchronizeJob job         = backend.create(type,konto);
	    
	    job.setContext(SynchronizeJob.CTX_ENTITY,u);
	    
	    backend.execute(Arrays.asList(job));
		}
		catch (RemoteException e)
		{
			Logger.error("error while executing transfer",e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ausf�hren des Auftrages"),StatusBarMessage.TYPE_ERROR));
		}
  }

}
