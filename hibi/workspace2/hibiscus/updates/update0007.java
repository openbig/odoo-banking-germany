/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0007.java,v $
 * $Revision: 1.4 $
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
 * Korrigiertes Datenbank-Update fuer neue Tabelle "property".
 * Die Spalte "content" war u.U. zu kurz.
 */
public class update0007 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0007()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "DROP TABLE property;\n" +
        "CREATE TABLE property (" +
        "    id IDENTITY," +
        "    name varchar(255) NOT NULL," +
        "    content varchar(1000) NULL," +
        "    UNIQUE (id)," +
        "    UNIQUE (name)," +
        "    PRIMARY KEY (id)" +
        ");\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE property CHANGE content content text null;");
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
      throw new ApplicationException(i18n.tr("Datenbank {0} wird nicht unterst�tzt",driver));
    
    try
    {
      Logger.info("create sql table for update0007");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'property' aktualisiert"));
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
    return "Korrigiertes Datenbank-Update f�r neue Tabelle \"property\"";
  }

}


/*********************************************************************
 * $Log: update0007.java,v $
 * Revision 1.4  2011/10/24 14:24:22  willuhn
 * @B Parameter "database.driver" darf inzwischen NULL sein - in dem Fall H2 als Default verwenden
 *
 * Revision 1.3  2010-11-02 12:02:20  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.2  2008/10/12 22:10:20  willuhn
 * @B Typo in den Updates
 * @B Spalten-Sortierung und -breite fuer in den Positionen von Sammelauftraegen nicht gespeichert
 *
 * Revision 1.1  2008/06/15 21:55:51  willuhn
 * @N update007 - Spalte "content" vergroessert
 * @B Fix in update002 - verursachte Fehler auf alten MySQL-Versionen
 *
 **********************************************************************/