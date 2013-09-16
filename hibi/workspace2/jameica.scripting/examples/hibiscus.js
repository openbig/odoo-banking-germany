/*******************************************************************************
 * Beispiel, welches die Scripting-Funktionen von Hibiscus zeigt
 *******************************************************************************/
 
// Wir importieren ein paar Java-Packages. Das "Packages." muss
// immer mit davor geschrieben werden
importPackage(Packages.de.willuhn.logging);
importPackage(Packages.de.willuhn.jameica.system);
importPackage(Packages.de.willuhn.jameica.hbci);
importPackage(Packages.de.willuhn.jameica.hbci.rmi);


// Code, der nicht in einer Funktion steht, wird immer dann aufgerufen,
// wenn das Script neu geladen wird. Das passiert beim Start von Jameica,
// beim Klick auf den Button "Speichern" in Datei->Einstellungen und
// automatisch nach dem Speichern von Aenderungen am Script (binnen
// max. 10 Sekunden)
Logger.info("Test-Script initialisiert");

// Wir mappen die Hibiscus-Events auf unsere Funktionsnamen
// Das muss gemacht werden, sonst werden unsere Funktionen
// nicht aufgerufen.
// Bitte achte darauf, deine Funktionen hinreichend eindeutig zu
// benennen. Sollte der User mehrere Scripts in Jameica registriert haben,
// die gleichnamige Funktionen besitzen, ueberschreiben sich diese.
events.add("hibiscus.konto.saldo.changed","test_hibiscus_kontoSaldoChanged");
events.add("hibiscus.konto.sync",         "test_hibiscus_kontoSync");

/**
 * Wird aufgerufen, wenn der Saldo des Kontos abgerufen wurde.
 * @param konto das betreffende Konto.
 */
function test_hibiscus_kontoSaldoChanged(konto)
{
  Logger.info("Saldo aktualisiert von Konto: " + konto.getID());
}

/**
 * Wird aufgerufen, wenn der Button "via Scripting synchronisieren"
 * im Konto angeklickt wurde. Der Button wird nur bei Offline-Konten
 * angezeigt. Und auch nur dann, wenn das Plugin "jameica.scripting"
 * installiert ist.
 * @param konto das betreffende Konto.
 */
function test_hibiscus_kontoSync(konto, monitor)
{
  monitor.log("Synchronisiere Konto " + konto.getLongName());

  //////////////////////////////////////////////////////////////////////////////
  // Beispiele fuer direkte Benutzerinteraktion

  // Sicherheitsabfrage
  // if (!Application.getCallback().askUser("Sind Sie sicher?"))
  //  return;

  // Benutzereingabe
  // var text = Application.getCallback().askUser("Bitte geben Sie das Passwort ein","Passwort");
  // monitor.log("Passwort: " + text);
  
  // Hinweis-Dialog
  // Application.getCallback().notifyUser("Alles in Ordnung.");
  //////////////////////////////////////////////////////////////////////////////
  
  
  // Saldo aktualisieren
  konto.setSaldo(0);
  konto.store(); // Speichern nicht vergessen
  
  monitor.addPercentComplete(10);

  // Datenbank-Verbindung holen
  var db = Application.getServiceFactory().lookup(HBCI,"database");
  
  monitor.addPercentComplete(10);
  
  // neuen Umsatz anlegen. Das kann natuerlich auch in einer
  // Schleife mehrmals ausgefuehrt werden, wenn mehrere Umsatze
  // angelegt werden sollen.
  monitor.log("Lege neuen Umsatz an");
  var umsatz = db.createObject(Umsatz,null);
  
  //////////////////////////////////////////////////////////////////////////////
  // Pflichtangaben
  umsatz.setKonto(konto);
  umsatz.setDatum(new Date(2010,6,25)); // Achtung, der Monat beginnt bei 0
  umsatz.setValuta(new Date());
  umsatz.setBetrag(1.99);
  //////////////////////////////////////////////////////////////////////////////
  
  //////////////////////////////////////////////////////////////////////////////
  // Optionale Angaben
  umsatz.setGegenkontoNummer("1234567890");
  umsatz.setGegenkontoBLZ("12345678");
  umsatz.setGegenkontoName("Max Mustermann");
  
  umsatz.setZweck("Verwendungszweck 1");
  umsatz.setZweck2("Verwendungszweck 2");
  
  // weitere Zeilen
  var lines = new Array();
  lines.push("Zeile 3");
  umsatz.setWeitereVerwendungszwecke(lines);
  
  umsatz.setKommentar("Das ist ein Kommentar");
  umsatz.setPrimanota("1234");
  umsatz.setArt("Ueberweisung");
  umsatz.setCustomerRef("Kundenreferenz");
  
  // Umsatz speichern
  umsatz.store();

  monitor.addPercentComplete(10);
}

