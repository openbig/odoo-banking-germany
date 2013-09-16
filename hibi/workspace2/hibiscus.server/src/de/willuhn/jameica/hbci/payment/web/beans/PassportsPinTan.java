/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus.server/src/de/willuhn/jameica/hbci/payment/web/beans/PassportsPinTan.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/11/12 15:09:59 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.payment.web.beans;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.kapott.hbci.callback.HBCICallback;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.pintan.PinTanConfigFactory;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.passports.pintan.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.passports.pintan.server.PassportImpl;
import de.willuhn.jameica.hbci.passports.pintan.server.PinTanConfigImpl;
import de.willuhn.jameica.hbci.payment.Settings;
import de.willuhn.jameica.hbci.payment.handler.TANHandler;
import de.willuhn.jameica.hbci.payment.handler.TANHandlerRegistry;
import de.willuhn.jameica.hbci.payment.messaging.TrustMessageConsumer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.annotation.Request;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Base64;
import de.willuhn.util.Session;

/**
 * Controller fuer PIN/TAN-Passports.
 */
public class PassportsPinTan extends AbstractPassports
{
  
  @Request
  private HttpServletRequest request = null;
  
  private PinTanConfig config = null;
  
  /**
   * Eine Session, in der wir die Benutzereingaben kurz zwischenspeichern,
   * damit sie vom HBCI4Java-Callback abgefragt werden koennen.
   */
  public final static Session SESSION = new Session(1000l * 60); // 1 Minute
  
  /**
   * @see de.willuhn.jameica.hbci.payment.web.beans.AbstractPassports#getImplementationClass()
   */
  Class<? extends Passport> getImplementationClass()
  {
    return PassportImpl.class;
  }

  /**
   * Action zum Laden der angegebenen PIN/TAN-Config.
   * @throws Exception
   */
  public void load() throws Exception
  {
    String path = request.getParameter("config");
    if (path == null || path.length() == 0)
      return;
    
    File f = new File(path);
    if (!f.exists() || !f.canRead())
    {
      Logger.error("pin/tan config " + f.getAbsolutePath() + " is not readable, skipping");
      return;
    }
    
    this.config = new PinTanConfigImpl(PinTanConfigFactory.load(f),f);
  }
  
  /**
   * Action zum Aktualisieren der zugeordneten Konten zu einer PIN/TAN-Config.
   */
  public void update()
  {
    try
    {
      if (this.config == null)
        throw new ApplicationException(i18n.tr("Keine PIN/TAN-Konfiguration ausgew�hlt"));

      Logger.info("create passport handle");
      PassportHandle handle = new PassportHandleImpl(this.config);

      Logger.info("fetch accounts");
      Konto[] konten = readKonten(handle.open());
      if (konten != null)
        this.config.setKonten(konten); // Konten fest verknuepfen
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Konten aktualisiert."), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      String msg = e.getMessage();
      if (!(e instanceof ApplicationException))
      {
        Logger.error("error while updating accounts for pin/tan config",e);
        msg = i18n.tr("Fehler beim Aktualisieren der Konten: {0}",e.getMessage());
      }
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_ERROR));
    }
    finally
    {
      // Geladene Config loeschen, damit wir nicht in die Detail-View wechseln
      this.config = null;
    }
  }
  
  /**
   * Loescht die aktuelle PIN/TAN-Config.
   * @throws Exception
   */
  public void delete() throws Exception
  {
    try
    {
      if (this.config == null)
        throw new ApplicationException(i18n.tr("Keine zu l�schende PIN/TAN-Konfiguration ausgew�hlt"));

      // Synchronisierung der Konten komplett deaktivieren - wuerde sonst nur Fehler liefern, weil
      // das Sicherheitsmedium nicht mehr da ist
      Konto[] konten = this.config.getKonten();
      if (konten != null)
      {
        for (Konto k:konten)
        {
          Logger.info("disable all synchronize options for account [id: " + k.getID() + "]");
          SynchronizeOptions options = new SynchronizeOptions(k);
          options.setAll(false);
        }
      }
      
      // Konfiguration loeschen
      PinTanConfigFactory.delete(this.config);
      
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("PIN/TAN-Konfiguration gel�scht"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      String msg = e.getMessage();
      if (!(e instanceof ApplicationException))
      {
        Logger.error("error while deleting pin/tan config",e);
        msg = i18n.tr("Fehler beim L�schen der PIN/TAN-Konfiguration: {0}",e.getMessage());
      }
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_ERROR));
    }
    finally
    {
      this.config = null;
    }
  }
  
  /**
   * Action zum Speichern einer PIN/TAN-Config.
   */
  public void store()
  {
    boolean update = this.config != null;
    TrustMessageConsumer tmc = new TrustMessageConsumer();

    try
    {
      /////////////////////////////////////////////////////////////////
      // PIN checken
      String password   = request.getParameter("pin");
      String password2  = request.getParameter("pin2");

      if (!update && (password == null || password.length() == 0))
        throw new ApplicationException(i18n.tr("Keine PIN angegeben"));

      if (!update && (password2 == null || password2.length() == 0))
        throw new ApplicationException(i18n.tr("Bitte gib die PIN zur Kontrolle ein zweites Mal ein"));

      if (password != null && password.length() > 0 &&
          password2 != null && password2.length() > 0 &&
          !password.equals(password2))
        throw new ApplicationException(i18n.tr("PIN-Eingaben stimmen nicht �berein."));
      //
      /////////////////////////////////////////////////////////////////

      /////////////////////////////////////////////////////////////////
      // Benutzereingaben checken
      String bezeichnung     = request.getParameter("bezeichnung");
      String benutzerkennung = request.getParameter("benutzerkennung");
      String kundenkennung   = request.getParameter("kundenkennung");
      String url             = request.getParameter("url");
      String version         = request.getParameter("version");
      String blz             = request.getParameter("blz");
      
      if (bezeichnung == null || bezeichnung.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte gib eine Bezeichnung f�r diese PIN/TAN-Konfiguration ein"));

      if (benutzerkennung == null || benutzerkennung.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte gib eine Benutzerkennung ein"));
      
      if (kundenkennung == null || kundenkennung.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte gib eine Kundenkennung ein"));
      
      if (url == null || url.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte gib eine URL ein"));
      
      if (url.startsWith("https://"))
      {
        Logger.info("removing leading https:// from " + url);
        url = url.substring(8);
      }
      
      if (blz == null || blz.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte gib eine BLZ ein"));

      if (version == null || version.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte w�hle eine HBCI-Version"));
      //
      /////////////////////////////////////////////////////////////////


      
      /////////////////////////////////////////////////////////////////
      // Config-File vorbereiten
      File f = null;
      if (update)
      {
        f = new File(this.config.getFilename());
        Logger.info("updating pin/tan config " + f.getAbsolutePath());
      }
      else
      {
        f = PinTanConfigFactory.createFilename();
        Logger.info("created new pin/tan file " + f.getAbsolutePath());

        Logger.info("creating random passport key");
        byte[] pass = new byte[8];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.nextBytes(pass);
        Settings.setHBCIPassword(f.getName(),Base64.encode(pass));
      }
      //
      /////////////////////////////////////////////////////////////////


      /////////////////////////////////////////////////////////////////
      // Passwort speichern
      if (password != null && password.length() > 0)
      {
        Logger.info("saving pin in wallet");
        Settings.setHBCIPassword(f.getName() + "." + HBCICallback.NEED_PT_PIN,password);
      }
      //
      /////////////////////////////////////////////////////////////////


      Application.getMessagingFactory().registerMessageConsumer(tmc);


      if (!update)
      {
        Logger.info("preparing callback session");
        SESSION.put(new Integer(HBCICallback.NEED_USERID),    benutzerkennung);
        SESSION.put(new Integer(HBCICallback.NEED_CUSTOMERID),kundenkennung);
        SESSION.put(new Integer(HBCICallback.NEED_HOST),      url);
        SESSION.put(new Integer(HBCICallback.NEED_BLZ),       blz);
        SESSION.put(new Integer(HBCICallback.NEED_COUNTRY),   "DE");
        SESSION.put(new Integer(HBCICallback.NEED_PORT),      "443");
        SESSION.put(new Integer(HBCICallback.NEED_FILTER),    "Base64");

        Logger.info("creating pin/tan config");
        config = new PinTanConfigImpl(PinTanConfigFactory.load(f),f);
      }

      // Die werden nicht via Callback abgefragt
      config.setBezeichnung(bezeichnung);
      config.setHBCIVersion(version);
        
      Logger.info("save pin/tan config");
      PinTanConfigFactory.store(config);

      if (!update)
      {
        Logger.info("create passport handle");
        PassportHandle handle = new PassportHandleImpl(config);

        Logger.info("fetch accounts");
        Konto[] konten = readKonten(handle.open());
        if (konten != null)
          config.setKonten(konten); // Konten fest verknuepfen
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Konten angelegt."), StatusBarMessage.TYPE_SUCCESS));
      }

      Logger.info("applying TAN handler");
      String th = request.getParameter("tanhandler");
      if (th != null && th.length() > 0)
      {
        TANHandler tanHandler = this.getCurrentTanHandler();
        if (tanHandler == null)
          tanHandler = TANHandlerRegistry.createTANHandler(th,new Format().escapePath(this.config.getFilename()));

        Iterator keys = request.getParameterMap().keySet().iterator();
        while (keys.hasNext())
        {
          String name = (String) keys.next();
          if (!name.startsWith(th + "."))
            continue;
          String shortname = name.substring(th.length()+1);
          tanHandler.set(shortname,request.getParameter(name));
        }
      }
      
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Einstellungen gespeichert"), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      if (!update && config != null)
      {
        // Im Fehlerfall die Config wieder loeschen. Aber nur bei Neuanlage
        try
        {
          PinTanConfigFactory.delete(config);
        }
        catch (Exception e1)
        {
          Logger.error("unable to delete config",e1);
        }
      }
      this.config = null;
      
      String msg = e.getMessage();
      if (!(e instanceof ApplicationException))
      {
        Logger.error("error while saving pin/tan config",e);
        msg = i18n.tr("Fehler beim Speichern der PIN/TAN-Einstellungen: {0}",e.getMessage());
      }
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_ERROR));
    }
    finally
    {
      Application.getMessagingFactory().unRegisterMessageConsumer(tmc);
    }
  }
  
  /**
   * Liefert die aktuell geladene PIN/TAN-Config oder NULL wenn keine geladen ist.
   * @return die aktuell geladene PIN/TAN-Config oder NULL wenn keine geladen ist.
   */
  public PinTanConfig getCurrentConfig()
  {
    return this.config;
  }

  /**
   * Liefert den TAN-Handler der aktuellen PIN/TAN-Config.
   * @return TAN-Handler der aktuellen PIN/TAN-Config.
   * @throws Exception
   */
  public TANHandler getCurrentTanHandler() throws Exception
  {
    if (this.config == null)
      return null;
    return TANHandlerRegistry.getTANHandler(new Format().escapePath(this.config.getFilename()));
  }
  
  /**
   * Prueft, ob der uebergebene TAN-Handler der aktuelle fuer die geladene Config ist. 
   * @param h zu testender TAN-Handler.
   * @return true, wenn es der aktuelle ist.
   * @throws Exception
   */
  public boolean isCurrentTanHandler(TANHandler h) throws Exception
  {
    if (h == null || this.config == null)
      return false;
    TANHandler test = this.getCurrentTanHandler();
    if (test == null)
      return false;
    
    return test.getClass().getName().equals(h.getClass().getName());
  }

  
  /**
   * Liefert eine Liste der vorhandenen PIN/TAN-Konfigurationen.
   * @return Liste der PIN/TAN-Konfigurationen.
   * @throws Exception
   */
  public List<PinTanConfig> getConfigs() throws Exception
  {
    List<PinTanConfig> list = new ArrayList<PinTanConfig>();

    GenericIterator it = PinTanConfigFactory.getConfigs();
    while (it.hasNext())
    {
      list.add((PinTanConfig)it.next());
    }
    return list;
  }

  /**
   * Liefert eine Liste der verfuegbaren TAN-Handler.
   * @return Liste der verfuegbaren TAN-Handler.
   * @throws Exception
   */
  public List<TANHandler> getTanHandlers() throws Exception
  {
    TANHandler[] list = TANHandlerRegistry.getTANHandler();
    if (this.config != null)
    {
      String path = new Format().escapePath(this.config.getFilename());
      for (TANHandler h:list)
      {
        h.setConfig(path);
      }
    }
    return Arrays.asList(list);
  }
}



/**********************************************************************
 * $Log: PassportsPinTan.java,v $
 * Revision 1.1  2011/11/12 15:09:59  willuhn
 * @N initial import
 *
 * Revision 1.15  2011/10/25 13:57:16  willuhn
 * @R Saemtliche Lizenz-Checks entfernt - ist jetzt Opensource
 *
 * Revision 1.14  2011/05/12 17:37:20  willuhn
 * @C "https://" automatisch entfernen, falls mit eingegeben
 *
 * Revision 1.13  2011/02/09 12:30:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2011/02/07 10:24:36  willuhn
 * @N Aktualisieren der zu einer PIN/TAN-Konfiguration zugeordneten Konten
 *
 * Revision 1.11  2010/12/14 10:48:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2010/11/08 11:36:37  willuhn
 * @B NPE
 *
 * Revision 1.9  2010/10/07 12:20:28  willuhn
 * @N Lizensierungsumfang (Anzahl der zulaessigen Konten) konfigurierbar
 *
 * Revision 1.8  2010/09/08 14:54:03  willuhn
 * @N Umstellung auf Multi-DDV-Support
 *
 * Revision 1.7  2010/03/04 16:13:31  willuhn
 * @N Kartenleser-Konfiguration
 *
 * Revision 1.6  2010/02/26 16:19:43  willuhn
 * @N Konten loeschen
 *
 * Revision 1.5  2010/02/26 15:38:16  willuhn
 * @B
 *
 * Revision 1.4  2010/02/26 15:22:46  willuhn
 * @N Konten in Liste der Schluesseldisketten anzeigen
 * @N Schluesseldisketten loeschen
 * @B kleinere Bugfixes
 *
 * Revision 1.3  2010/02/23 18:21:54  willuhn
 * @N PIN/TAN-Config loeschen
 * @C Styling
 * @B debugging
 *
 * Revision 1.2  2010/02/23 17:22:04  willuhn
 * @B small pin/tan bugs
 *
 * Revision 1.1  2010/02/18 17:13:09  willuhn
 * @N Komplettes Rewrite des Webfrontends auf jameica.webtools-Plattform - endlich keine haesslichen JSPs mehr
 *
 **********************************************************************/