/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0038.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/01/02 22:32:20 $
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
import de.willuhn.jameica.hbci.server.DBSupportPostgreSQLImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Fuegt bei Sammel-Auftraegen das Speichern von Warnungen hinzu.
 */
public class update0042 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0042()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "ALTER TABLE slastschrift         ADD warnungen int(1) NULL;\n" +
        "ALTER TABLE sueberweisung        ADD warnungen int(1) NULL;\n" +
        "ALTER TABLE slastbuchung         ADD warnung varchar(255);\n" +
        "ALTER TABLE sueberweisungbuchung ADD warnung varchar(255);\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE slastschrift         ADD warnungen int(1);\n" +
        "ALTER TABLE sueberweisung        ADD warnungen int(1);\n" +
        "ALTER TABLE slastbuchung         ADD warnung varchar(255);\n" +
        "ALTER TABLE sueberweisungbuchung ADD warnung varchar(255);\n");

    // Update fuer PostGres
    statements.put(DBSupportPostgreSQLImpl.class.getName(),
        "ALTER TABLE slastschrift         ADD warnungen integer NULL;\n" +
        "ALTER TABLE sueberweisung        ADD warnungen integer NULL;\n" +
        "ALTER TABLE slastbuchung         ADD warnung varchar(255);\n" +
        "ALTER TABLE sueberweisungbuchung ADD warnung varchar(255);\n");
  }

  /**
   * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
   */
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

    String driver = HBCIDBService.SETTINGS.getString("database.driver",DBSupportH2Impl.class.getName());
    String sql = (String) statements.get(driver);
    if (sql == null)
      throw new ApplicationException(i18n.tr("Datenbank {0} nicht wird unterst�tzt",driver));
    
    try
    {
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle aktualisiert"));
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
    return "Datenbank-Update f�r Warnungen in Sammel-Auftr�gen";
  }

}
