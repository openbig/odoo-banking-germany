-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "ueberweisung" "typ"
-- fuer den Textschluessel
-- ----------------------------------------------------------------------

alter table ueberweisung add typ varchar(2) NULL;

-- ----------------------------------------------------------------------
-- $Log: update0008.sql,v $
-- Revision 1.1  2008/08/01 11:05:14  willuhn
-- @N BUGZILLA 587
--
-- ----------------------------------------------------------------------
