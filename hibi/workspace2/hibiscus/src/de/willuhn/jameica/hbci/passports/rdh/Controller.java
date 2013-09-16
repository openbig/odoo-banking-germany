/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/Controller.java,v $
 * $Revision: 1.8 $
 * $Date: 2011/08/08 15:46:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.exceptions.NeedKeyAckException;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.gui.action.PassportTest;
import de.willuhn.jameica.hbci.gui.dialogs.NewKeysDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PassportPropertyDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.HBCIVersionInput;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.passports.rdh.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller, der die Eingaben zur Konfiguration des Passports handelt.
 */
public class Controller extends AbstractControl {

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  // Liste aller Schluessel
  private TablePart keyList           = null;

  // Benutzerdaten
  private Input benutzerkennung       = null;
  private Input kundenkennung         = null;
  private Input blz                   = null;
  
  // Verbindungsdaten
  private Input hbciUrl               = null;
  private Input hbciPort              = null;
  private Input hbciVersion           = null;
  
  // Erweiterte Einstellungen
  private Input alias                 = null;
  private Input path                  = null;

  // Fach-Daten
  private RDHKey key                  = null;
  private HBCIPassport passport       = null;

  // BUGZILLA 173
  // Liste der zugeordneten Kunden
  private TablePart kontoList         = null;

  /**
   * ct.
   * @param view
   */
  public Controller(AbstractView view) {
    super(view);
  }

  /**
   * Liefert den Schluessel.
   * @return Schluessel.
   */
  public RDHKey getKey()
  {
    if (this.key != null)
      return this.key;
    Object o = getCurrentObject();
    if (o instanceof RDHKey)
      this.key = (RDHKey) o;
    return this.key;
  }
  
  /**
   * Liefert den HBCI-Passport.
   * @return Passport.
   * @throws RemoteException
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  public HBCIPassport getHBCIPassport() throws RemoteException, ApplicationException, OperationCanceledException
  {
    if (this.passport != null)
      return this.passport;
    RDHKey key = getKey();
    if (key == null)
      throw new ApplicationException(i18n.tr("Kein Schl�ssel ausgew�hlt"));
    this.passport = key.load();
    return this.passport;
  }

  /**
   * Liefert ein Anzeigefeld fuer die Benutzerkennung.
   * @return Anzeigefeld.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getBenutzerkennung() throws RemoteException, ApplicationException
  {
    if (this.benutzerkennung != null)
      return this.benutzerkennung;
    this.benutzerkennung = new TextInput(getHBCIPassport().getUserId());
    this.benutzerkennung.setEnabled(false);
    this.benutzerkennung.setName(i18n.tr("Benutzerkennung"));
    return this.benutzerkennung;
  }
  
  /**
   * Liefert ein Anzeigefeld fuer die Kundenkennung.
   * @return Anzeigefeld.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getKundenkennung() throws RemoteException, ApplicationException
  {
    if (this.kundenkennung != null)
      return this.kundenkennung;
    this.kundenkennung = new TextInput(getHBCIPassport().getCustomerId());
    this.kundenkennung.setEnabled(false);
    this.kundenkennung.setName(i18n.tr("Kundenkennung"));
    return this.kundenkennung;
  }
  
  /**
   * Liefert ein Anzeigefeld fuer die BLZ.
   * @return Anzeigefeld.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getBLZ() throws RemoteException, ApplicationException
  {
    if (this.blz != null)
      return this.blz;
    this.blz = new BLZInput(getHBCIPassport().getBLZ());
    this.blz.setEnabled(false);
    this.blz.setName(i18n.tr("Bankleitzahl"));
    return this.blz;
  }
  
  
  /**
   * Liefert einen zusaetzlichen Alias-Namen, an dem der User mehrere Schluessel
   * unterscheiden kan.
   * @return Alias-Name.
   * @throws RemoteException
   */
  public Input getAlias() throws RemoteException
  {
    if (this.alias != null)
      return this.alias;
    this.alias = new TextInput(getKey().getAlias());
    this.alias.setName(i18n.tr("Alias-Name des Schl�ssels"));
    return this.alias;
  }

  /**
   * BUGZILLA 173
   * BUGZILLA 314
   * Liefert eine Tabelle mit festzuordenbaren Konten.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public TablePart getKontoAuswahl() throws RemoteException
  {
    if (kontoList == null)
      this.kontoList = new KontoList(this.getKey());
    return kontoList;
  }
  
  
  /**
   * Liefert ein Eingabe-Feld zur Eingabe des Pfads zum Schluessel.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getPath() throws RemoteException
  {
    if (this.path == null)
    {
      // Das Aendern des Pfad macht ueberhaupt keine Sinn mehr.
      // Da kann man den Schluessel auch einfach loeschen
      // und neu importieren.
      this.path = new FileInput(getKey().getFilename());
      this.path.setEnabled(false);
      this.path.setName(i18n.tr("Pfad zu Schl�sseldatei"));
    }
    return this.path;
  }
  
  /**
   * Liefert eine Auswahl-Box fuer die HBCI-Version.
   * @return Auswahl-Box.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Input getHBCIVersion() throws RemoteException, ApplicationException
  {
    if (this.hbciVersion != null)
      return this.hbciVersion;
    this.hbciVersion = new HBCIVersionInput(getHBCIPassport(),getKey().getHBCIVersion());
    this.hbciVersion.setName(i18n.tr("HBCI-Version"));
    return this.hbciVersion;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer die URL.
   * @return Eingabe-Feld.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public Input getHBCIUrl() throws ApplicationException, RemoteException
  {
    if (this.hbciUrl != null)
      return this.hbciUrl;

    this.hbciUrl = new TextInput(getHBCIPassport().getHost());
    this.hbciUrl.setName(i18n.tr("Hostname des Bankservers"));
    return this.hbciUrl;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer den TCP-Port.
   * @return Eingabe-Feld.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public Input getHBCIPort() throws ApplicationException, RemoteException
  {
    if (this.hbciPort != null)
      return this.hbciPort;

    Integer i = getHBCIPassport().getPort();
    this.hbciPort = new IntegerInput(i != null ? i.intValue() : 3000);
    this.hbciPort.setName(i18n.tr("TCP-Port des Bankservers"));
    this.hbciPort.setComment(i18n.tr("meist \"3000\""));
    return this.hbciPort;
  }

  /**
   * Liefert eine Liste mit den importierten Schluesseln.
   * @return Liste der Schluessel.
   * @throws RemoteException
   */
  public TablePart getKeyList() throws RemoteException
  {
    if (keyList != null)
      return keyList;

    keyList = new TablePart(RDHKeyFactory.getKeys(),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        GUI.startView(Detail.class,context);
      }
    });


    // Spalte Datei
    keyList.addColumn(i18n.tr("Schl�sseldatei"),"file");
    keyList.addColumn(i18n.tr("Alias-Name"),"alias");
    keyList.addColumn(i18n.tr("Format"),"format");



    ContextMenu ctx = new ContextMenu();

    // Kontext: Details.
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("�ffnen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null)
          return;
        try
        {
          GUI.startView(Detail.class,context);
        }
        catch (Exception e)
        {
          Logger.error("error while loading rdh key",e);
        }
      }
    },"document-open.png"));

    ctx.addItem(new ContextMenuItem(i18n.tr("Neuer Schl�ssel..."),new Action() {
      public void handleAction(Object context) throws ApplicationException {startCreate();}
    },"document-new.png"));
    ctx.addItem(new ContextMenuItem(i18n.tr("Schl�ssel importieren..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        startImport();
      }
    },"stock_keyring.png"));

    // Kontext: Aktivieren/Deaktivieren
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new ActivateKey(true));
    ctx.addItem(new ActivateKey(false));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("L�schen..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          String q = i18n.tr("Wollen Sie diesen Schl�ssel wirklich l�schen?\n" +
                             "Hierbei wird nur die Verkn�pfung aus Hibiscus " +
                             "entfernt. Die Schl�sseldatei selbst bleibt erhalten.");
        
          if (!Application.getCallback().askUser(q))
            return;

          RDHKey k = (RDHKey) context;
          RDHKeyFactory.removeKey(k);
          GUI.startView(View.class,null);
        }
        catch (OperationCanceledException e)
        {
          Logger.info("operation cancelled");
          return;
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e2)
        {
          Logger.error("unable to delete key",e2);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim L�schen des Schl�ssels"),StatusBarMessage.TYPE_ERROR));
        }
      }
    },"user-trash-full.png"));

    keyList.setContextMenu(ctx);
    


    // Format fuer aktiv/inaktiv
    keyList.setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        try
        {
          RDHKey key = (RDHKey) item.getData();
          if (!key.isEnabled())
            item.setForeground(Color.COMMENT.getSWTColor());
        }
        catch (Exception e)
        {
          Logger.error("error while formatting key",e);
        }
      }
    });

    keyList.setMulti(false);
    keyList.setRememberColWidths(true);
    keyList.setRememberOrder(true);
    keyList.setSummary(false);
    return keyList;
  }

  /**
   * Startet die Erzeugung eines INI-Briefs.
   */
  public synchronized void startIniLetter()
  {
    RDHKey key = getKey();
    if (key == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte w�hlen Sie eine Schl�sseldatei aus"),StatusBarMessage.TYPE_ERROR));
      return;
    }
    
    HBCIPassport passport = null;
    try
    {
      passport = key.load();
      NewKeysDialog d = new NewKeysDialog(passport);
      d.open();
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled: " + oce.getMessage());
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr(ae.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while creating ini letter",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Erzeugen des INI-Briefs: {0}",e.getMessage()));
    }
    finally
    {
      if (passport != null)
      {
        try
        {
          passport.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close passport",e);
        }
      }
    }
  }

  /**
   * Aendert das Passwort der Schluesseldatei.
   */
  public synchronized void changePassword()
  {
    HBCIPassport passport = null;
    HBCICallback callback = null;
    
    try
    {
      passport = getHBCIPassport();
      
      // muessen wir zwingend machen, weil sonst der Callback nicht bei uns landet
      // Das wuerde bewirken, dass Hibiscus ein zufaelliges neues Passwort erzeugt,
      // welches der User aber nicht mehr kennt.
      AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
      callback = ((HBCI)plugin).getHBCICallback();
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(new PassportHandleImpl());
      
      passport.changePassphrase();
      
      // Passwort-Cache leeren
      DialogFactory.clearPINCache(passport);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Passwort ge�ndert"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("unable to change password",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim �ndern des Passwortes: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    finally
    {
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(null);
    }
  }
  
  /**
   * Synchronisiert die Signatur-ID.
   */
  public synchronized void syncSigId()
  {
    RDHKey key = getKey();
    if (key == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte w�hlen Sie eine Schl�sseldatei aus"),StatusBarMessage.TYPE_ERROR));
      return;
    }
    
    HBCIPassport passport = null;
    HBCIHandler handler = null;
    try
    {
      String s = i18n.tr("Sind Sie sicher?");
      if (!Application.getCallback().askUser(s))
        return;

      passport = key.load();
      passport.syncSigId();

      QueryMessage msg = new QueryMessage(passport);
      Application.getMessagingFactory().getMessagingQueue("hibiscus.passport.rdh.hbciversion").sendSyncMessage(msg);
      Object data = msg.getData();
      if (data == null || !(data instanceof String))
        throw new ApplicationException(i18n.tr("HBCI-Version nicht ermittelbar"));
      
      String version = (String)msg.getData();
      Logger.info("using hbci version: " + version);
      
      handler = new HBCIHandler(version,passport);
      handler.close();
      handler = null;
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Signatur-ID erfolgreich synchronisiert"), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Synchronisieren der Signatur: {0}",ae.getMessage()), StatusBarMessage.TYPE_ERROR));
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
    }
    catch (Exception e)
    {
      Throwable current = e;
      for (int i=0;i<10;++i)
      {
        if (current == null)
          break;
        if (current instanceof NeedKeyAckException)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Schl�ssel noch nicht freigegeben"), StatusBarMessage.TYPE_ERROR));
          return;
        }
        current = current.getCause();
      }
      Logger.error("unable to sync key ",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Synchronisieren der Signatur: {0}",e.getMessage()), StatusBarMessage.TYPE_ERROR));
    }
    finally
    {
      try
      {
        if (handler != null)
          handler.close();
      }
      catch (Throwable t)
      {
        Logger.error("error while closing handler",t);
      }
    }
  }

  /**
   * Startet einen Schluessel-Import.
   */
  public synchronized void startImport()
  {
    FileDialog fd = new FileDialog(GUI.getShell(),SWT.OPEN);
    fd.setText(i18n.tr("Bitte w�hlen Sie die zu importierende Schl�sseldatei"));

    String importFile = fd.open();
    if (importFile == null || importFile.length() == 0)
      return;
    RDHKeyFactory.importKey(new File(importFile));
    GUI.startView(View.class,null); // Reload
  }

  /**
   * Testet die Konfiguration.
   */
  public synchronized void handleTest()
  {

    // Speichern, damit sicher ist, dass wir vernuenftige Daten fuer den
    // Test haben und die auch gespeichert sind
    if (!handleStore())
      return;

    try
    {
      new PassportTest().handleAction(new PassportHandleImpl(getKey()));
    }
    catch (ApplicationException ae)
    {
      GUI.getStatusBar().setErrorText(ae.getMessage());
    }
    catch (RemoteException e)
    {
      Logger.error("error while testing passport",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Testen der Konfiguration. Bitte pr�fen Sie das Protokoll. ") + e.getMessage());
    }
  }


  /**
   * Speichert die Einstellungen fuer den aktuellen Schluessel.
   * @return true, wenn das Speichern erfolgreich war.
   */
  public synchronized boolean handleStore()
  {
    RDHKey key = getKey();
    if (key == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte w�hlen Sie eine Schl�sseldatei aus"),StatusBarMessage.TYPE_ERROR));
      return false;
    }

    try
    {
      Konto[] konten = null;
      List checked = getKontoAuswahl().getItems();
      if (checked != null && checked.size() > 0)
        konten = (Konto[]) checked.toArray(new Konto[checked.size()]);
      key.setKonten(konten);
      
      key.setHBCIVersion((String)getHBCIVersion().getValue());
      key.setAlias((String)getAlias().getValue());

      
      HBCIPassport p = getHBCIPassport();
      if (p != null)
      {
        String s = (String) getHBCIUrl().getValue();
        if (s != null && s.length() > 0)
          p.setHost(s);
        Integer i = (Integer) getHBCIPort().getValue();
        if (i != null)
          p.setPort(i);
        p.saveChanges();
      }
      
      GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert"));
      return true;
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled: " + oce.getMessage());
    }
    catch (ApplicationException e2)
    {
      Logger.error("error while exporting key",e2);
      GUI.getStatusBar().setErrorText(i18n.tr(e2.getMessage()));
    }
    catch (Exception e)
    {
      Logger.error("error while storing settings",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Einstellungen"));
    }
    return false;
  }
  
  /**
   * Zeigt die BPD/UPD des Passports an.
   */
  public synchronized void handleDisplayProperties()
  {
    try
    {
      new PassportPropertyDialog(PassportPropertyDialog.POSITION_CENTER,this.getHBCIPassport()).open();
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
    }
    catch (ApplicationException e)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr(e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    catch (Throwable t)
    {
      Logger.error("error while displaying BPD/UPD",t);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler Anzeigen der BPD/UPD"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Erstellt einen neuen Schluessel.
   */
  public synchronized void startCreate()
  {
    try
    {
      // Wir fragen den User, wo er den Schluessel hinhaben will.
      FileDialog dialog = new FileDialog(GUI.getShell(), SWT.SAVE);
      dialog.setOverwrite(true);
      dialog.setText(Application.getI18n().tr("Bitte w�hlen einen Pfad und Dateinamen, an dem der Schl�ssel gespeichert werden soll."));
      dialog.setFileName("hibiscus-" + System.currentTimeMillis() + ".rdh");
      dialog.setFilterPath(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath());
      String newFile = dialog.open();
      if (newFile == null || newFile.length() == 0)
        throw new OperationCanceledException("no key file choosen");
      
      File newKey = new File(newFile);
      
      RDHKeyFactory.createKey(newKey);
      GUI.startView(View.class,null);
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled: " + oce.getMessage());
    }
    catch (Throwable t)
    {
      Logger.error("error while exporting key",t);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Export der Schl�sseldatei"),StatusBarMessage.TYPE_ERROR));
    }

  }
  

  /**
   * Hilfsklasse zum Aktivieren des Schluessels.
   */
  private class ActivateKey extends CheckedContextMenuItem
  {

    private boolean activate;

    /**
     * @param activate
     */
    public ActivateKey(final boolean activate)
    {
      super((activate ? i18n.tr("Schl�ssel aktivieren") : i18n.tr("Schl�ssel deaktivieren")),
        new Action()
        {
          public void handleAction(Object context) throws ApplicationException
          {
            if (context == null)
              return;
            try
            {
              ((RDHKey) context).setEnabled(activate);
              GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
            }
            catch (Exception e)
            {
              Logger.error("error while activating rdh key",e);
            }
          }
        },activate ? "network-transmit-receive.png" : "network-offline.png");
      this.activate = activate;
    }

    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null)
        return false;
      try
      {
        boolean active = ((RDHKey) o).isEnabled();
        if (activate)
          return !active;
        return active;
      }
      catch (Exception e)
      {
        Logger.error("error while checking key state",e);
        return false;
      }
    }
  }
}


/**********************************************************************
 * $Log: Controller.java,v $
 * Revision 1.8  2011/08/08 15:46:16  willuhn
 * @B Beim Aendern des Passwortes wurde das CurrentHandle nicht gesetzt - mit dem Effekt, dass der Callback fuer ...PASSPHRASE_SAVE nicht im RDH-PassportHandle landete sondern im HBCICallbackSWT. Dort werden die beiden Callbacks auch behandelt - aber eigentlich nur fuer DDV und PIN/TAN. Ergebnis: Hibiscus aendert das Passwort der Schluesseldiskette auf ein zufaelliges Passwowrt, welches der User aber nicht kennt.
 *
 * Revision 1.7  2011-06-17 08:49:19  willuhn
 * @N Contextmenu im Tree mit den Bank-Zugaengen
 * @N Loeschen von Bank-Zugaengen direkt im Tree
 *
 * Revision 1.6  2011-05-25 10:05:49  willuhn
 * @N Im Fehlerfall nur noch die PINs/Passwoerter der betroffenen Passports aus dem Cache loeschen. Wenn eine PIN falsch ist, muss man jetzt nicht mehr alle neu eingeben
 *
 * Revision 1.5  2011-04-29 11:38:57  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.4  2011-04-28 07:34:43  willuhn
 * @R Summen-Zeile nicht mehr anzeigen - unnuetz
 *
 * Revision 1.3  2010-09-29 23:43:34  willuhn
 * @N Automatisches Abgleichen und Anlegen von Konten aus KontoFetchFromPassport in KontoMerge verschoben
 * @N Konten automatisch (mit Rueckfrage) anlegen, wenn das Testen der HBCI-Konfiguration erfolgreich war
 * @N Config-Test jetzt auch bei Schluesseldatei
 * @B in PassportHandleImpl#getKonten() wurder der Converter-Funktion seit jeher die falsche Passport-Klasse uebergeben. Da gehoerte nicht das Interface hin sondern die Impl
 *
 * Revision 1.2  2010-09-07 15:17:07  willuhn
 * @N GUI-Cleanup
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.46  2010/06/14 22:58:54  willuhn
 * @N Datei-Auswahldialog mit nativem Ueberschreib-Hinweis
 *
 * Revision 1.45  2009/06/16 15:32:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.44  2009/06/16 14:04:30  willuhn
 * @N Dialog zum Anzeigen der BPD/UPD
 *
 * Revision 1.43  2009/03/04 22:49:16  willuhn
 * @C INI-Brief anzeigen/drucken nur noch in Detail-Ansicht
 * @B falsche Button-Anzahl
 *
 * Revision 1.42  2009/03/04 22:37:05  willuhn
 * @N sync sig id (siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=55851#55851)
 *
 * Revision 1.41  2008/07/25 11:06:08  willuhn
 * @N RDH-2 Format
 * @C Haufenweise Code-Cleanup
 *
 * Revision 1.40  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 * Revision 1.39  2008/05/23 08:53:21  willuhn
 * @C Schluesseldateien beim Loeschen nicht mehr physisch loeschen
 *
 * Revision 1.38  2007/05/30 14:48:50  willuhn
 * @N Bug 314
 *
 * Revision 1.37  2007/05/04 13:19:16  willuhn
 * @N Erweiterter Warnhinweis beim Loeschen eines Schluessels
 *
 * Revision 1.36  2007/03/14 11:04:31  willuhn
 * @C bessere Fehleranzeige bei INI-Brieferstellung
 *
 * Revision 1.35  2007/02/21 10:32:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.34  2007/02/21 10:30:01  willuhn
 * @B syntax error
 *
 * Revision 1.33  2007/02/21 10:29:10  willuhn
 * @C Contextmenu ueberarbeitet
 *
 * Revision 1.32  2006/10/23 14:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.31  2006/10/23 14:11:19  willuhn
 * @N Hervorheben alter Schluessel
 *
 * Revision 1.30  2006/01/22 23:42:31  willuhn
 * @B bug 173
 *
 * Revision 1.29  2005/11/16 13:04:34  willuhn
 * @B NPE
 *
 * Revision 1.28  2005/11/14 12:22:31  willuhn
 * @B bug 148
 *
 * Revision 1.27  2005/11/14 11:00:18  willuhn
 * @B bug 148
 *
 * Revision 1.26  2005/08/08 16:05:25  willuhn
 * @B bug 103
 *
 * Revision 1.25  2005/07/25 11:55:45  web0
 * *** empty log message ***
 *
 * Revision 1.24  2005/07/24 14:51:58  web0
 * *** empty log message ***
 *
 * Revision 1.23  2005/07/24 14:44:51  web0
 * *** empty log message ***
 *
 * Revision 1.22  2005/07/12 23:20:36  web0
 * @B NPEs
 *
 * Revision 1.21  2005/07/12 23:14:08  web0
 * @B ClassCastException
 *
 * Revision 1.20  2005/07/11 21:52:40  web0
 * @B NPE
 *
 * Revision 1.19  2005/06/23 21:52:57  web0
 * *** empty log message ***
 *
 * Revision 1.18  2005/06/06 22:57:53  web0
 * @B bug 72
 *
 * Revision 1.17  2005/04/18 09:22:16  web0
 * @B korrekte HBCI-Version konnte nicht ausgewaehlt werden
 *
 * Revision 1.16  2005/04/18 09:08:18  web0
 * @B table refresh after import
 *
 * Revision 1.15  2005/04/05 23:42:02  web0
 * @C moved HBCIVersionInput into Hibiscus source tree
 *
 * Revision 1.14  2005/04/04 11:34:20  web0
 * @B bug 36
 * @B bug 37
 *
 * Revision 1.13  2005/03/23 00:05:55  web0
 * @C RDH fixes
 *
 * Revision 1.12  2005/03/09 01:07:16  web0
 * @D javadoc fixes
 *
 * Revision 1.11  2005/02/28 18:39:57  web0
 * @N list of available hbci version is now read from hbci passport
 *
 * Revision 1.10  2005/02/28 15:08:24  web0
 * @N autodetection of right key
 *
 * Revision 1.9  2005/02/20 19:04:21  willuhn
 * @B Bug 7
 *
 * Revision 1.8  2005/02/08 22:26:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2005/02/08 18:34:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2005/02/07 22:06:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2005/02/02 16:15:35  willuhn
 * @N Erstellung neuer Schluessel
 * @N Schluessel-Import
 * @N Schluessel-Auswahl
 * @N Passport scharfgeschaltet
 *
 * Revision 1.4  2005/01/19 00:15:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/01/09 18:48:27  willuhn
 * @N native lib for sizrdh
 *
 * Revision 1.2  2005/01/07 19:00:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/05 15:32:28  willuhn
 * @N initial import
 *
 **********************************************************************/