/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0017.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/10/24 14:24:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.DBSupportMySqlImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Erzeugt die Tabelle "aueberweisung" fuer Auslandsueberweisungen
 */
public class update0017 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0017()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "CREATE TABLE aueberweisung (" +
        "    id IDENTITY," +
        "    konto_id int(4) NOT NULL," +
        "    empfaenger_konto varchar(40) NOT NULL," +
        "    empfaenger_bank varchar(140) NOT NULL," +
        "    empfaenger_name varchar(140) NOT NULL," +
        "    betrag double NOT NULL," +
        "    zweck varchar(140)," +
        "    termin date NOT NULL," +
        "    ausgefuehrt int(1) NOT NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        "  );\n" +
        "ALTER TABLE aueberweisung ADD CONSTRAINT fk_konto8 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "CREATE TABLE aueberweisung (" +
        "    id int(10) AUTO_INCREMENT" +
        "  , konto_id int(10) NOT NULL" +
        "  , empfaenger_konto VARCHAR(40) NOT NULL" +
        "  , empfaenger_bank VARCHAR(140) NOT NULL" +
        "  , empfaenger_name VARCHAR(140) NOT NULL" +
        "  , betrag DOUBLE NOT NULL" +
        "  , zweck VARCHAR(140)" +
        "  , termin DATE NOT NULL" +
        "  , ausgefuehrt int(10) NOT NULL" +
        "  , UNIQUE (id)" +
        "  , PRIMARY KEY (id)" +
        ") ENGINE=InnoDB;\n" +
        "CREATE INDEX idx_aueberweisung_konto ON aueberweisung(konto_id);\n" +
        "ALTER TABLE aueberweisung ADD CONSTRAINT fk_aueberweisung_konto FOREIGN KEY (konto_id) REFERENCES konto (id);\n");
  }

  /**
   * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
   */
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

    // Wenn wir eine Tabelle erstellen wollen, muessen wir wissen, welche
    // SQL-Dialekt wir sprechen
    String driver = HBCIDBService.SETTINGS.getString("database.driver",DBSupportH2Impl.class.getName());
    String sql = (String) statements.get(driver);
    if (sql == null)
      throw new ApplicationException(i18n.tr("Datenbank {0} nicht wird unterst�tzt",driver));
    
    try
    {
      Logger.info("create sql table for update0017");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'aueberweisung' erstellt"));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to execute update",e);
      throw new ApplicationException(i18n.tr("Fehler beim Ausf�hren des Updates"),e);
    }
  }

  /**
   * @see de.willuhn.sql.version.Update#getName()
   */
  public String getName()
  {
    return "Datenbank-Update f�r Tabelle \"aueberweisung\"";
  }

}


/*********************************************************************
 * $Log: update0017.java,v $
 * Revision 1.3  2011/10/24 14:24:22  willuhn
 * @B Parameter "database.driver" darf inzwischen NULL sein - in dem Fall H2 als Default verwenden
 *
 * Revision 1.2  2010-11-02 12:02:20  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/