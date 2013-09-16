/******************************************************************************************************
 * PayPal für die Scripting-Funktionen von Jameica/Hibiscus
/******************************************************************************************************
* Hibiscus-Script for PayPal (Germany)
* Original:
* http://www.wiedenhoeft.net/hibiscus-scripting/paypal
*
* Modification:
* http://hibiscus-scripting.derrichter.de
*
* Creation Date: 2011/03/10
* @author Ben Wiedenhoeft
*
* Modification Date: 2013/04/16
* @Modificator Sebastian Richter
* 
* @version Revision: 2.0.3
*          Date: 2013/08/07
*
* Copyright (c) Sebastian Richter / http://hibiscus-scripting.derrichter.de / All rights reserved
*
* HINWEIS:
* SOLLTE DIESES SCRIPT ODER TEILE DARAUS KOPIERT ODER FÜR EIGENE ZWECKE WEITERVERWENDET WERDEN
* IST DER AUTOR SEBASTIAN RICHTER DARÜBER ZU INFORMIEREN UND SEINE EINWILLIGUNG ZU BESTÄTIGEN!
*
/*****************************************************************************************************/



/** VersionsHistory
*
* ver 2.0.3
*
* - Anzeige der aktuellen Version des Scripts beim Start
* - Neue Version des Kontoanlage-Assistenten (1.3.1): Dieser unterstützt nun auch das Anlegen zusätzlicher Konten
*   INFO: um diesen Assistenten zu starten: rufen Sie über das Hauptmenü die Jameica-Einstellungen auf und klicken dort einfach auf 'Speichern'
*   Nachteil dabei ist dass dieser bei jedem Benutzer dieses Kontos dann auch erscheint.
*   Aber dieser Assistent kann auch dauerhaft deaktiviert werden indem man "Diese Frage nicht mehr anzeigen" aktiviert und zusätzlich 'Nein' wählt
*   (Um dies rückgängig zu machen muss in der Datei "<Jameica-Profilpfad>\cfg\de.willuhn.jameica.system.ApplicationCallback.properties" die entsprechende Zeile gelöscht werden)
* - Erkennung von fehlerhaften Kontoauszügen (falsche Sprache oder enthält HTML-Daten). Somit wird eine entsprechende Fehlermeldung ausgegeben
*   ALLGEMEINER HINWEIS: Es ist ein Problem bekannt welches sich darin zeigt dass bei manchen Benutzern bei dem Versuch einen Kontoauszug im CSV-Format zu laden (wie hier im Script) 
*   diese Anfrage von der Homepage nicht beantwortet wird, diese Benutzer dann aber später eine Nachricht (z. B. E-Mail) erhalten mit dem Hinweis dass dieser Kontoauszug nun als Download
*   in einen Postkorb auf der PayPal-Homepage bereit gestellt wurde. Diesen verarbeitet dieses Script im Moment nicht aus dem einfachen Grund dass dieses Problem bei den Entwicklern nicht besteht.
*   Es wird hier aber ausdrücklich klargestellt dass auch hierfür schon ein Lösungskonzept besteht welches aber logischerweise nicht ohne Zugang, zu einem Account mit solch einem Problem, umgesetzt werden kann.
*   Wer also sich als zuverlässiger Helfer und Tester zu Verfügung stellen kann (z. B. können ja alle Bankverbindungen aus dem Account gelöscht werden) darf sich gerne melden um auch das PayPal-PlugIn nutzen zu können.
* - Änderung des Aufbaus der Information für die neue Synchronisierung (diese zeigt welche Einstellungen aktiv sind und nicht welche aktiv sein müssen)
*
*
*
* ver 2.0.2
*
* - Umstrukturierungen im Quellcode
* - Es werden nun auch sehr lange Verwendungszwecke (bis zu fünf Zeilen) übernommen. Diese werden immer Standardkonform nach 27 Zeichen umgebrochen
*   (hier könnte man alle Umsätze der letzten zwei Jahre löschen und das Kontosaldo zurücksetzten um diese neu einzulesen
*    dabei muss beachtet werden dass z. B. Kategorie-Zuordnungen verloren gehen. Daher immer auf Backups achten!)
* - Bug behoben der verhinderte dass das Kontosaldo aktualisiert wird wenn es dazu Daten im Kontoauszug gibt (wird nun immer richtig gesetzt)
* - Neuer Aufbau für das setzen des Statusbalken im Logmonitor
* - Erweiterung des SecLogout und hierbei entstandene Fehler werden abgefangen um den Ablauf nicht zu unterbrechen
* - Konto wird bei der neuen Synchronisierung nun nur noch gespeichert wenn in den Synchronisierungsoptionen "Saldo aktualisieren" aktiviert ist um das Kontosaldo-Datum eindeutig zu halten
* - Anpassungen an Buttons von Formularen. Fehlermeldungen für diese werden nun an der richtigen Stelle ausgegeben
* - Live-Aktualisierung neuer Umsätze hinzugefügt da diese fehlte und nur bei neuen Umsätzen der Art "PayPal-Gebühren" ausgeführt wurde
* - diverse Textausgaben angepasst
*
*
*
* ver 2.0.1
*
* - Bug bei der Gültigkeitsprüfung der eingegebenen E-Mail-Adresse, beim Kontoanlage-Assistenten
* - Bug beim setzten des Kontosaldos wenn keine neuen Umsätze vorhanden sind
*
*
*
* ver 2.0.0 (aka 1.20)
* ===================
*
* Neuerungen und Features:
* - Neue Synchronisierung (mit Jameica/Hibiscus Nightly-Build): es wurden die derzeit möglichen Funktionen der neuen Scripting-Synchronisierung von Jameica/Hibiscus umgesetzt
*   (Danke hier auch noch mal an Olaf Willuhn für die Zusammenarbeit und das kurzfristige Umsetzen von Anpassungen)
*   > Konto erscheint nun in der Liste "Konten synchronisieren" und wird bei einer Komplett-Synchronisierung aller Konten mit eingeschlossen
*   > Umsätze werden dabei nun gleich in der Liste "Neue Umsätze" angezeigt
*   > Für das Konto kann "Saldo aktualisieren" und "Kontoauszüge (Umsätze abrufen) abrufen" aktiviert/deaktiviert werden 
*     (das Script überspring dies dann, gilt aber nur für den Abruf über die Startseite mit 'Synchronisierung starten',
*     bei Abruf über das Kontextmenü oder die Detailansicht des Konto wird immer beides abgerufen; Ist "Saldo aktualisieren" deaktiviert, berechnet diesen Hibiscus weiterhin)
*   WICHTIG: für eine korrekte Arbeitsweise müssen mindestens diese Versionen mit Stand nicht älter als 09.05.2013 installiert sein:
*	- Jameica 2.5.0 (Nightly-Build)
*	- Hibiscus 2.5.1 (Nightly-Build)
*	- Jameica-Scripting 2.5.0 (Nightly-Build)
* - Proxy-Unterstützung: vollständige Unterstützung der Proxy-Funktionalität von Jameica (gleich der Version von BoS-Script ver. 1.5 (interne Version 1.4.1))
*   ("Systemproxy benutzen" wird dann wegen Java nur funktionieren wenn in den Java-Einstellungen dies auch so konfiguriert ist 
*   und dort kein eigener Proxy eingestellt ist.) Eine eventuell notwendige Proxy-Authentifizierung müsste im Moment im System hinterlegt werden 
*   und/oder von diesen geregelt werden da dies Jameica nicht unterstützt. (z. B. Benutzername/Passwort)
*   (eine Integration einer Authentifizierung mit statischen Benutzer/Passwort erfolgt in einer späteren Version)
* - HTMLUnit Versions-Check: in der Variante vom BoS-Script ver. 1.6 integriert (interne Version 1.3.6)
* - Sicherheits-Logout: wird automatisch bei Fehlern ausgeführt (dieser setzt unter anderem das Passwort zurück)
*
* Änderungen und Anpassungen:
* - Script erkennt nun die (bekannten) Fehlermeldungen von PayPal und gibt diese an den Benutzer weiter (bei jedem Seitenaufruf wird geprüft)
*   (somit sollten die meisten Fehler vom Benutzer selbst erkannt werden um darauf reagieren zu können)
* - Erweiterung der Fehlererkennung- u. verarbeitung auf alle Formularaktionen und Trennung von anderen Fehlermeldungen
* - Der Assistent zur automatischen Konto-Anlage wurde an die erweiterte Version vom BoS-Script ver. 1.6 angepasst (interne Version 1.2.6)  
*   INFO: um diesen Assistenten zu starten: rufen Sie über das Hauptmenü die Jameica-Einstellungen auf und klicken dort einfach auf 'Speichern' (vorausgesetzt kein Konto ist angelegt)
*   (dieser Assistent kann nun auch dauerhaft deaktiviert werden indem man "Diese Frage nicht mehr anzeigen" aktiviert. 
*   Um dies rückgängig zu machen muss in der Datei "<Jameica-Profilpfad>\cfg\de.willuhn.jameica.system.ApplicationCallback.properties" die entsprechende Zeile gelöscht werden)
* - Integration der ChangeLog in die Script-Datei
* - dynamischen LogIdent für gesamtes Script umgesetzt
* - Logging-Ausgaben angepasst (z. B. auch persönliche Daten entfernt, Stichwort Datenschutz. Dies gilt nicht für den Modus DEBUG!)
* - Anpassung der Login-Adresse für die korrekte Fehlerermittlung
* - Script ermittelt nun wie viele neue Umsätze vorhanden sind und gibt die Anzahl aus
* - Es wird nun der Benutzer auch nach dem Abrufdatum gefragt wenn keine Umsätze vorhanden sind aber dennoch ein Konto-Saldodatum existiert
* - erweiterte Änderungen an der Fehlerausgabe um die Kompatibilität mit der alten und der neuen Synchronisierung zu gewährleisten
* - Verwendungszweck wird nun auch Standard-Konform auf 27 Zeichen pro Zeile gekürzt und/oder umgebrochen. Dies soll auch spätere Probleme vermeiden (z. B. bei der Überweisungsfunktion)
* - vollständige Neu-Strukturierung des Quellcodes (Kodierung: ANSI, Tabulatorbreite: 8)
*
* Bugfixes (Fehlerbehebungen und Anpassungen)
* - Anpassungen bezüglich dass PayPal nun einen Umsatzabruf auf die letzten zwei Jahre beschränkt
* - Fehler beim Erstellen der Umsätze behoben der bewirkte dass die eigene E-Mail-Adresse immer als Kontonummer des Gegenkontos gesetzt wurde
*
*
*
*
*/



/*******************************************************************************
 * Importieren der Standard-Javapackete für das Hibiscus Banking Scripting
 *******************************************************************************/
// Import der Packete aus Jameica für Logging, System, HBCI, und Sync-Funktionen
importPackage(Packages.de.willuhn.util);
importPackage(Packages.de.willuhn.logging);
importPackage(Packages.de.willuhn.jameica.messaging);
importPackage(Packages.de.willuhn.jameica.system);
importPackage(Packages.de.willuhn.jameica.hbci);
importPackage(Packages.de.willuhn.jameica.hbci.rmi);
importPackage(Packages.de.willuhn.jameica.hbci.messaging);
importPackage(Packages.de.willuhn.jameica.hbci.synchronize.jobs);

// Import der Packete aus HTMLUnit für den Webseiten-Abruf
importPackage(Packages.com.gargoylesoftware.htmlunit);
importPackage(Packages.com.gargoylesoftware.htmlunit.html);
importPackage(Packages.com.gargoylesoftware.htmlunit.util);
/*******************************************************************************/



/*******************************************************************************
 * Konfiguration und Cache
 *******************************************************************************/
// Versions-Nummer setzen
var PayPal_Script_Version = "2.0.3";
 
// wichtig für neuen Sync, wir setzten die Variable für die Abfrage
var PayPal_NewSyncActive = false;

// Diese BLZ muss im Hibiscus-Konto hinterlegt sein; als Angabe noch zusätzlich die offizelle Konto-Nr. von PayPal (für Überweisungen)
var HibiscusScripting_PayPal_BLZ = "50110800";
var HibiscusScripting_PayPal_Konto = "6161604670";

// Cache für bekannte Umsaetze
var HibiscusScripting_PayPal_hibiscusUmsaetze = [];

// Cache für Passwort
var PayPal_LoginEmail = "";
var HibiscusScripting_PayPal_aPass = [];

// aktuelle URL-Adresse für den Login
var HibiscusScripting_PayPal_LoginURL = "https://www.paypal.com/de/cgi-bin/webscr?cmd=_login-run";

// aktuelle URL-Adresse für den Logout
var HibiscusScripting_PayPal_LogoutURL = "https://www.paypal.com/de/cgi-bin/webscr?cmd=_logout";

// Ident für Log-Einträge und für Exceptions (Aufbau LogIdent/ExcIdent ist identisch mit anderen Scripts um Code einfach mehrfach zu verwenden)
var LogIdent = "";
var LogIdentPayPal = "PayPal-Script: ";
var ExcIdent = "";
var ExcIdentPayPal = "[PayPal] Fehler: ";

// String für Infofenster-Überschrift "Fehlermeldung der Bank" in Dialogen
var PayPalErrorTitle = new java.lang.String("\nFehlermeldung von PayPal:\n\n\n");
/*******************************************************************************/



/*******************************************************************************
 * Initialisierung
 *******************************************************************************/
// setzten des LogIdent für die Initalisierung
LogIdent = LogIdentPayPal;
ExcIdent = ExcIdentPayPal;

// Start der Initalisierung ins Log screiben
Logger.info(LogIdent+"Initalisierung des Scripts ...");

// Die Registrierung auf das Event "hibiscus.sync.function". Damit kann Hibiscus ermitteln, ob und über welche Javascript-Funktion das Konto synchronisiert werden kann
events.add("hibiscus.sync.function", "HibiscusScripting_PayPal_sync_function");
// Registrierung auf Konto-Sync-Event
events.add("hibiscus.konto.sync", "HibiscusScripting_PayPal_kontoSync");

// Wir schauen bei der Initialisierung, ob es ein Konto gibt, das PayPal-fähig ist
if (! HibiscusScripting_PayPal_isAccountDefined()) {
/*******************************************************************************
* Kontoanlage Assistent ver. 1.3.1
*******************************************************************************/

	// Falls nicht, bieten wir dem Benutzer an eines zu erzeugen
	Logger.info(LogIdent+"Kein PayPal-Konto gefunden, Nachfrage ob Anlage erw\u00FCnscht");
	
	var AssiInfoString = new java.lang.String("\n\n\nDas PayPal-Script f\u00fcr Hibiscus ist installiert ...\n\nZur Zeit ist noch kein Konto f\u00fcr PayPal in Hibiscus angelegt\n"
						+ "Wollen Sie jetzt den Assistenten zur automatischen Kontoanlage ausf\u00fchren?\n\n"
						+ "[ INFO: Um diesen Assistenten erneut zu starten, rufen Sie einfach die Jameica-Einstellungen auf\n"
						+ "und klicken auf 'Speichern' ]\n"
						+ "(vorausgesetzt Sie aktivieren nicht den folgenden Haken und w\u00e4hlen zus\u00e4tzlich 'Nein'!)");
								 
	var infoArray = java.lang.reflect.Array.newInstance(java.lang.String, 1);
	infoArray[0] = AssiInfoString;
	// Abfragefenster beim Benutzer mit Info-Text, einstellbar dass dieses nicht mehr erscheint
	var StartCreateAccount = Application.getCallback().askUser("PayPal - Kontoanlage Assistent {0}", infoArray);
	
	
	
	if (StartCreateAccount == true) {
		HibiscusScripting_PayPal_CreateAccountAssistent();
	
	} else {
		Logger.info(LogIdent+"Anlegen des Kontos wurde vom Benutzer mit NEIN beantwortet oder der Assistent ist deaktiviert");
	}

} else {
	var db;
  	try { 
		db = Application.getServiceFactory().lookup(HBCI,"database"); 
		
	} catch(err) {
		db = false;
	}

	if (db != false) {
		Logger.info(LogIdent+"OK: Mindestens ein Offline-Konto f\u00fcr PayPal ist bereits angelegt");

		var AssiInfoNewString = new java.lang.String("\n\n\nZur Zeit ist schon mindestens ein Konto f\u00fcr PayPal in Hibiscus angelegt\n"
							   + "Wollen Sie jetzt den Assistenten zur automatischen Kontoanlage ausf\u00fchren\n"
							   + "um ein zus\u00e4tzliches Konto f\u00fcr PayPal anzulegen?\n\n"
							   + "[ INFO: Um diesen Assistenten erneut zu starten, rufen Sie einfach die Jameica-Einstellungen auf\n"
							   + "und klicken auf 'Speichern' ]\n"
							   + "(vorausgesetzt Sie aktivieren nicht den folgenden Haken und w\u00e4hlen zus\u00e4tzlich 'Nein'!)\n\n"
							   + "Soll diese Frage zum Anlegen eines zus\u00e4tzlichen Kontos nicht mehr erscheinen,\n"
							   + "aktivieren Sie einfach den folgenden Haken und w\u00e4hlen 'Nein'\n"
							   + "(Um diesen Assisten wieder verf\u00fcbar zu machen folgen Sie der Anleitung unter FAQ auf der Projektseite)");
									 
		var infoNewArray = java.lang.reflect.Array.newInstance(java.lang.String, 1);
		infoNewArray[0] = AssiInfoNewString;
		// Abfragefenster beim Benutzer mit Info-Text, einstellbar dass dieses nicht mehr erscheint
		var StartCreateNewAccount = Application.getCallback().askUser("PayPal - Kontoanlage Assistent (Zusatzkonto){0}", infoNewArray);

			
		if (StartCreateNewAccount == true) {
			HibiscusScripting_PayPal_CreateAccountAssistent();
			
		} else {
			Logger.info(LogIdent+"Anlegen eines zus\u00e4tzlichen Kontos wurde vom Benutzer mit NEIN beantwortet oder der Assistent ist deaktiviert");
		}		
	}
	
}
/*******************************************************************************/



// Ende der Initalisierung ins Log schreiben
Logger.info(LogIdent+"Initalisierung des Scripts beendet");
/*******************************************************************************/





function HibiscusScripting_PayPal_CreateAccountAssistent() {
/*******************************************************************************
* Bentutzerführung des Kontoanlage-Assistenten für ein PayPal-Konto
*******************************************************************************/

	try {
		// die notwendigen Variablen für das Anlegen des Kontos
		var PayPal_eMail = "";
		var CustomerFullName = "";
		var CancelMsg = "Kontoanlage ";
	
		do {
			var eMailString = false;
			CancelMsg = "Eingabe der E-Mail-Adresse ";
			PayPal_eMail = Application.getCallback().askUser("Bitte geben Sie Ihre PayPal-Login E-Mail-Adresse ein\n\n"
								       , "eMail-Adresse:");
			
			// Es wird geprüft ob die E-Mail-Adresse einem logischem Aufbau entspricht
			if ((PayPal_eMail.indexOf("@") != -1) && (PayPal_eMail.lastIndexOf('.') > 4) && (PayPal_eMail.indexOf(" ") == -1)) {
				eMailString = true;
			
			} else {
				Application.getCallback().notifyUser("\nGeben Sie bitte eine gültige E-Mail-Adresse ein  \n\n");
			}
			
		} while(eMailString == false);

		do {
			CancelMsg = "Eingabe des Namens ";
			CustomerFullName = Application.getCallback().askUser("Bitte geben Sie den Namen des Kontoinhabers ein\n" 
									   + "(Vorname Nachname)\n\n"
									   , "Kontoinhaber:");
			      
		} while(CustomerFullName.length() <= 3);
	    
	} catch(err) {
		Logger.warn(LogIdent + CancelMsg + "wurde vom Benuzter abgebrochen");
		// die letzte Variable leeren um keine falsche Fehlermeldung zu setzten
		CustomerFullName = "";

	} finally {	
		try {
			if (PayPal_eMail == "" || CustomerFullName == "") {
				Logger.warn(LogIdent+"Kontodaten nicht vollst\u00E4ndig. Anlegen des Kontos wurde abgebrochen");
				Application.getCallback().notifyUser("Es wurde kein PayPal-Konto erstellt ...\n\n"
								   + "Die Kontodaten sind nicht vollst\u00E4ndig!\n"
								   + "("+CancelMsg+"wurde vom Benutzer abgebrochen)\n\n"
								   + "[ INFO: Um diesen Assistenten erneut zu starten, rufen Sie einfach die Jameica-Einstellungen auf\n"
								   + "und klicken auf 'Speichern' ]\n");
								   
			} else {
				// Hibiscus-Konto wird nun angelegt
				HibiscusScripting_PayPal_createHibiscusAccount(PayPal_eMail, CustomerFullName);
				Logger.info(LogIdent+"Hibiscus-Offline-Konto f\u00fcr PayPal wurde automatisch erzeugt");
				Application.getCallback().notifyUser("\nHibiscus-Offline-Konto f\u00fcr PayPal wurde automatisch erzeugt  \n\n"
								   + "F\u00FCr ein weiteres Konto von PayPal, rufen Sie einfach die Jameica-Einstellungen auf\n"
								   + "und klicken auf 'Speichern'\n"
								   + "oder lesen Sie bitte die Anleitung unter FAQ auf der Projektseite");
			}
			
		} catch(err) {
			Logger.error(LogIdent+"Fehler beim automatischen anlegen des Kontos: Log-Meldung: " + err);
			Application.getCallback().notifyUser("Fehler beim automatischen anlegen des Kontos\n\n" 
							   + "(siehe Meldung im Log)\n\n"
							   + "[ INFO: Um diesen Assistenten erneut zu starten, rufen Sie einfach die Jameica-Einstellungen auf\n"
							   + "und klicken auf 'Speichern' ]\n");

		}
	}

}





function HibiscusScripting_PayPal_isAccountDefined() {
/*******************************************************************************
* Schaut nach, ob bereits ein Konto für den PayPal-Abruf existiert
*******************************************************************************/
	var db;
	
	try {
		db = Application.getServiceFactory().lookup(HBCI,"database"); 
	} catch(e) {
		// Beim Start von Jameica steht zum Init-Zeitpunkt noch kein DB-Zugriff zur Verfügung
		Logger.debug(LogIdent+"Noch kein Datenbankzugriff f\u00fcr das PayPal-Script m\u00F6glich. Check auf existierendes PayPal-Konto wird abgebrochen");
		return true;
	}

	var list = db.createList(Konto);

	while (list.hasNext()) {
		var k = list.next();
		if (k.getBLZ() == HibiscusScripting_PayPal_BLZ && String (k.getKundennummer()).indexOf("@") != -1)
		return true; // Das Konto wuerde gesynct werden.
	}

	return false;
}





function HibiscusScripting_PayPal_createHibiscusAccount(PayPal_eMail, CustomerFullName) {
/*******************************************************************************
* Erzeugen eines Offline-Kontos mit den Einstellungen für PayPal
*******************************************************************************/

	  var db = Application.getServiceFactory().lookup(HBCI,"database"); 
	  
	  var k = db.createObject(Konto, null);
	  k.setBezeichnung("PayPal");
	  k.setKundennummer(PayPal_eMail);                    	// In der Kundennummer muss die E-Mail stehen
	  k.setKontonummer(HibiscusScripting_PayPal_Konto);     // Ist die offizelle Konto-Nummer von PayPal
	  k.setBLZ(HibiscusScripting_PayPal_BLZ); 		// BLZ von PayPal
	  k.setKommentar("Automatisch erzeugtes Konto \n\n(dieser Infotext und die Bezeichnung des Kontos k\u00f6nnen nat\u00fcrlich ge\u00e4ndert werden)");
	  k.setName(CustomerFullName);				// Ist der vollständige Name den der Benutzer eingegeben hat
	  k.setFlags(Konto.FLAG_OFFLINE);              		// Muss ein Offline-Konto sein
	  k.store();
	  
	  // Keine automatischen Gegenbuchungen
	  var sync = new SynchronizeOptions(k);
	  sync.setSyncOffline(false);
	  
}





function HibiscusScripting_PayPal_sync_function(konto, type) {
/*******************************************************************************
 * Sie wird aufgerufen, wenn Hibiscus herauszufinden versucht, ob das Script
 * den angegebenen Geschäftsvorfall für das Konto unterstützt. Falls ja,
 * muss das Script den Namen der Javascript-Funktion zurückliefern, die
 * ausgeführt werden soll, um diesen Geschäftsvorfall auszuführen.
 * Andernfalls NULL.
 * @param konto das betreffende Konto, für welches der Support geprüft werden soll.
 * @param type Name der Klasse des gesuchten Jobs.
 * @return der Name der auszuführenden Javascript-Funktion oder NULL.
 *******************************************************************************/

	// PayPal hat ein Bankkonto unter der BLZ 50110800. Wenn diese BLZ im Hibiscus-
	// Konto hinterlegt wurde und die Kundennummer den Aufbau einer eMail-Adresse 
	// hat, synchronisieren wir...
	// (sollte immer an erster Stelle der Sync-Funktion stehen!)
	if (konto.getBLZ() != HibiscusScripting_PayPal_BLZ) return;
	// Hat die Kundennummer ein @?
	if (konto.getKundennummer().indexOf("@") == -1) return;



	LogIdent = LogIdentPayPal;
	ExcIdent = ExcIdentPayPal;
  


	// Liste aller möglichen Klassen (Gross-Kleinschreibung beachten!):
	// - SynchronizeJobKontoauszug
	// - SynchronizeJobDauerauftragDelete
	// - SynchronizeJobDauerauftragList
	// - SynchronizeJobDauerauftragStore
	// - SynchronizeJobLastschrift
	// - SynchronizeJobSammelLastschrift
	// - SynchronizeJobSammelUeberweisung
	// - SynchronizeJobSepaUeberweisung
	// - SynchronizeJobUeberweisung

	// ACHTUNG: Hibiscus unterstützt beim Scripting-Support derzeit
	// nur "SynchronizeJobKontoauszug". Die anderen könnten in
	// Zukunft jedoch noch folgen. Irgendwann wird also prinzipiell
	// jeder Geschäftsvorfall auch via Scripting möglich sein.

	// Wir liefern hier zurück, dass wir nur den Abruf von Kontoauszügen
	// unterstützen.
	if (type.equals(SynchronizeJobKontoauszug)) return "HibiscusScripting_PayPal_Kontoauszug";

	return null;
	
}





function HibiscusScripting_PayPal_Kontoauszug(job, session) {
/*******************************************************************************
* PayPal neuer Sync Kontoauszug (registrierte Hauptfunktion die den Sync startet)
*******************************************************************************/

	var konto = job.getKonto();
	var monitor = session.getProgressMonitor();

	var options = new SynchronizeOptions(konto);

	var forceSaldo  = job.getContext(SynchronizeJobKontoauszug.CTX_FORCE_SALDO);
	Logger.debug(LogIdent+"HibiscusScripting_PayPal_Kontoauszug: forceSaldo: " + forceSaldo);
	var forceUmsatz = job.getContext(SynchronizeJobKontoauszug.CTX_FORCE_UMSATZ);
	Logger.debug(LogIdent+"HibiscusScripting_PayPal_Kontoauszug: forceUmsatz: " + forceUmsatz);
	
	// Wenn "fetchSaldo" true ist, sollte (wenn möglich) der Saldo des Kontos aktualisiert werden
	PayPal_fetchSaldo  = options.getSyncSaldo() || (forceSaldo != null && forceSaldo.booleanValue());
	// Wenn "fetchUmsatz" true ist, sollten neue Umsätze abgerufen werden
	PayPal_fetchUmsatz = options.getSyncKontoauszuege() || (forceUmsatz != null && forceUmsatz.booleanValue());

	// Über aktiven neuen Sync im Log informieren
	Logger.info(LogIdent+"Neue Synchronisierung wurde erkannt, mit folgenden Einstellungen: ");
	Logger.info(LogIdent+" - Saldo aktualisieren: " + PayPal_fetchSaldo);
	Logger.info(LogIdent+" - Kontoausz\u00fcge (Ums\u00e4tze) abrufen: " + PayPal_fetchUmsatz);

	// wichtig für neuen Sync, wir setzten die Variable dass dieser aktiv ist
	PayPal_NewSyncActive = true;
	
	// Wir leiten den Aufruf einfach an die existierende Funktion weiter
	try {
		HibiscusScripting_PayPal_kontoSync(konto, monitor);
		
	} catch (err) {
		PayPal_fetchSaldo = "";
		PayPal_fetchUmsatz = "";
		return new ApplicationException(ExcIdent + err);
	
	}
	
}





function HibiscusScripting_PayPal_kontoSync(konto, monitor) {
/*******************************************************************************
* PayPal Sync (registrierte Hauptfunktion die den Sync startet)
*******************************************************************************/

	// PayPal hat ein Bankkonto unter der BLZ 50110800. Wenn diese BLZ im Hibiscus-
	// Konto hinterlegt wurde und die Kundennummer den Aufbau einer eMail-Adresse 
	// hat, synchronisieren wir...
	// (sollte immer an erster Stelle der Sync-Funktion stehen!)
	if (konto.getBLZ() != HibiscusScripting_PayPal_BLZ) return;
	// Hat die Kundennummer ein @?
	if (konto.getKundennummer().indexOf("@") == -1) return;

	
	
	LogIdent = LogIdentPayPal;
	ExcIdent = ExcIdentPayPal;

	
	
	monitor.setPercentComplete(0);

	
	
	// Ausgabe der Versionsnummer des Scripts, ist oben unter Konfiguration einzustellen
	Logger.info(LogIdent + "Version " + PayPal_Script_Version + " wurde gestartet ...");
	monitor.log(LogIdent + "Version " + PayPal_Script_Version + " wurde gestartet ...");



	// Setzen des Login für den aktuellen Abruf
	PayPal_LoginEmail = konto.getKundennummer();

  
    
	try {

		if(PayPal_NewSyncActive == false) {
			monitor.log("Synchronisiere Konto: " + konto.getLongName());
		}
		
		
		
		// Abfrage ob neuer Sync aktiv und welche Einstellungen dort aktiv sind
		if (PayPal_NewSyncActive == true) { 
			if(PayPal_fetchSaldo == true) { var Log_fetchSaldo = " - 'Saldo aktualisieren' ist aktiv"; } else { var Log_fetchSaldo = " - 'Saldo aktualisieren' ist deaktiviert"; }
			if(PayPal_fetchUmsatz == true) { var Log_fetchUmsatz = " - 'Kontoausz\u00fcge (Ums\u00e4tze) abrufen' ist aktiv"; } else { var Log_fetchUmsatz = " - 'Kontoausz\u00fcge (Ums\u00e4tze) abrufen' ist deaktiviert"; }
			monitor.log("****************************************************************************************************\n"
				  + "\t\t\t\t\t\t\t\t\t*  Neue Synchronisierung ist aktiv - mit folgenden Einstellungen:\n"
				  + "\t\t\t\t\t\t\t\t\t*   "+Log_fetchSaldo+"\n"
				  + "\t\t\t\t\t\t\t\t\t*   "+Log_fetchUmsatz+"\n"
				  + "\t\t\t\t\t\t\t\t\t*  (verwenden Sie am besten Nightly-Builds ab dem 09.05.2013)\n"
				  + "\t\t\t\t\t\t\t\t\t****************************************************************************************************");
		}		
		
		// hier wird gleich mal geprüft ob nicht beide Optionen deaktiviert sind, dann können wir gleich wieder abbrechen (übernimmt aber auch Jameica)
		if ((PayPal_NewSyncActive == true) && (PayPal_fetchSaldo == false) && (PayPal_fetchUmsatz == false)) {
			Logger.warn(LogIdent+"Neuer Sync wird nicht ausgef\u00fcrt da die Option 'Saldo aktualisieren' und 'Kontoausz\u00fcge (Ums\u00e4tze) abrufen' deaktiviert sind. Nichts zu tun");
			monitor.log("Neuer Sync wird nicht ausgef\u00fcrt da die Option 'Saldo aktualisieren' und 'Kontoausz\u00fcge (Ums\u00e4tze) abrufen' deaktiviert sind. Nichts zu tun");
		}
		
		  
		/*******************************************************************************
		* HTMLUnit Versions-Check ver. 1.3.6
		* (Prüft ob HTMLUnit installiert ist, checkt die Version und gibt diese aus)
		*******************************************************************************/
		monitor.log("\u00dcberpr\u00FCfe HTMLUnit Version ...");
		Logger.info(LogIdent+"\u00dcberpr\u00FCfe HTMLUnit Version ...");
		var minHTMLUnitVer = new java.lang.String("2.9"); // hier stellt man die gewünschte Mindestversion der installierten HTMLUnit ein (Format 2.9 oder 2.10)
		
		try {
			var HTMLUnitVer = new java.lang.String(com.gargoylesoftware.htmlunit.Version.getProductVersion()); // ermittelt die installierte HTMLUnit Version
			
		} catch (err) {
			if(PayPal_NewSyncActive == false) {
				Logger.error(LogIdent+"HTMLUnit Version konnte nicht ermittelt werden. Stellen Sie sicher dass HTMLUnit mindestens in der Version "+minHTMLUnitVer+" installiert ist");
				return new java.lang.Exception("HTMLUnit Version konnte nicht ermittelt werden. Stellen Sie sicher dass HTMLUnit mindestens in der Version "+minHTMLUnitVer+" installiert ist");
			
			} else {
				throw ("HTMLUnit Version konnte nicht ermittelt werden. Stellen Sie sicher dass HTMLUnit mindestens in der Version "+minHTMLUnitVer+" installiert ist");
			}
		}
		
		Logger.debug(LogIdent+"minHTMLUnitVer: " + minHTMLUnitVer);
		Logger.debug(LogIdent+"HTMLUnitVer: " + HTMLUnitVer);
		
		// die Werte werden zum geteilt da Nachkommastellen von JavaScript rein matematisch genutzt und somit gekürzt werden (x.10 wird zu x.1)
		var minHTMLUnitArray = minHTMLUnitVer.split("\\.");
		Logger.debug(LogIdent+"minHTMLUnitArray[0]: " + minHTMLUnitArray[0] + " / in parseFloat: " +parseFloat(minHTMLUnitArray[0]) + "    und    minHTMLUnitArray[1]: " + minHTMLUnitArray[1] + " / in parseFloat: " +parseFloat(minHTMLUnitArray[1]));
		var HTMLUnitVerArray = HTMLUnitVer.split("\\.");
		Logger.debug(LogIdent+"HTMLUnitVerArray[0]: " + HTMLUnitVerArray[0] + " / in parseFloat: " +parseFloat(HTMLUnitVerArray[0]) + "    und    HTMLUnitVerArray[1]: " + HTMLUnitVerArray[1] + " / in parseFloat: " +parseFloat(HTMLUnitVerArray[1]));
		
		// nun starten wir einen logischen Vergleich als Nummern statt String
		if ((parseFloat(HTMLUnitVerArray[0]) < parseFloat(minHTMLUnitArray[0])) || 
		   ((parseFloat(HTMLUnitVerArray[0]) == parseFloat(minHTMLUnitArray[0])) && (parseFloat(HTMLUnitVerArray[1]) < parseFloat(minHTMLUnitArray[1])))) {
			Logger.error(LogIdent+"HTMLUnit Version zu niedrig. Mindestes Version "+minHTMLUnitVer+" wird ben\u00f6tigt. (Ihre Version ist "+ com.gargoylesoftware.htmlunit.Version.getProductVersion() +")");
			if(PayPal_NewSyncActive == false) {
				Logger.error(LogIdent+"HTMUnit Version zu niedrig. Mindestes Version "+minHTMLUnitVer+" wird ben\u00f6tigt. (Ihre Version ist "+ com.gargoylesoftware.htmlunit.Version.getProductVersion() +")");
				return new java.lang.Exception("HTMUnit Version zu niedrig. Mindestes Version "+minHTMLUnitVer+" wird ben\u00f6tigt. (Ihre Version ist "+ com.gargoylesoftware.htmlunit.Version.getProductVersion() +")");
			
			} else {
				throw ("HTMUnit Version zu niedrig. Mindestes Version "+minHTMLUnitVer+" wird ben\u00f6tigt. (Ihre Version ist "+ com.gargoylesoftware.htmlunit.Version.getProductVersion() +")");
			} 

		} else {
			monitor.log("OK: HTMLUnit Version " + com.gargoylesoftware.htmlunit.Version.getProductVersion() + " installiert und aktiv");
			Logger.info(LogIdent+"HTMLUnit Version " + com.gargoylesoftware.htmlunit.Version.getProductVersion() + " installiert und aktiv");
		}
		/******************************************************************************/


		
		monitor.setPercentComplete(3); 


		
		//*******************************************************************************
		// Initialisieren des webClients
		//*******************************************************************************
		webClient = new WebClient();
		
		try {
			HibiscusScripting_PayPal_prepareClient(monitor);
			
		} catch (err) {
			Logger.error(LogIdent +err);
			if(PayPal_NewSyncActive == false) {
				Logger.error(LogIdent+ err);
				return new java.lang.Exception(err);

			} else {
				throw err;
			}
			
		}
		//*******************************************************************************

	  
	  
		//*******************************************************************************
		// Login, inkl. Passwortabfrage beim User
		//*******************************************************************************
		var PostLoginContent = "";

		do {
			monitor.setPercentComplete(8);
			
			monitor.log("PayPal-Login mit " + PayPal_LoginEmail +" ...");
	
			var PayPal_Passwort = HibiscusScripting_PayPal_aPass[PayPal_LoginEmail];
			
			try {
				if (! PayPal_Passwort) {
					PayPal_Passwort = Application.getCallback().askPassword("Bitte geben Sie das PayPal-Passwort zu " + PayPal_LoginEmail + " ein:");
					Logger.info(LogIdent+"Passwort f\u00fcr "+PayPal_LoginEmail+" wird abgefragt ...");
				}

			} catch(err) {
				if(PayPal_NewSyncActive == false) {
					Logger.error(LogIdent+"Login fehlgeschlagen! Passwort-Eingabe vom Benuzter abgebrochen");
					return java.lang.Exception("Login fehlgeschlagen! Passwort-Eingabe vom Benuzter abgebrochen");
				
				} else {
					throw ("Login fehlgeschlagen! Passwort-Eingabe vom Benuzter abgebrochen");
				}
			}
			

			
			//*********************************************************************************
			// Login: Funktionsaufruf mit Benutzername und Passwort
			//*********************************************************************************
			try {
				PostLoginContent = HibiscusScripting_PayPal_HttpsLogin(PayPal_LoginEmail, PayPal_Passwort, webClient);

			} catch(err) {
				try {
					HibiscusScripting_PayPal_SecLogout("noLogin", monitor);
				} catch(secerr) {
					Logger.debug(LogIdent+"SecLogout konnte nicht ohne Fehler durchgef\u00fchrt werden. Fehler: " +secerr);
				}
				
				if(PayPal_NewSyncActive == false) {
					Logger.error(LogIdent + "Login fehlgeschlagen! " +err);
					return new java.lang.Exception("Login fehlgeschlagen! " +err);
				} else {
					throw ("Login fehlgeschlagen! " +err);
				}
			}
			//*********************************************************************************
			
			
			
			//*********************************************************************************
			// Nochmaliger Check zur Sicherheit ob der Login denn funktioniert hat oder der Login noch da ist
			//*********************************************************************************
			if (PostLoginContent.asXml().contains("<form method=\"post\" name=\"login_form\"")) {
				PostLoginContent = ""; 
				PayPal_LoginEmail = "";
				HibiscusScripting_PayPal_aPass[PayPal_LoginEmail] = "";
				throw "Die Loginseite wird trotz keinem bekannten Fehler noch immer angezeigt. Informieren Sie bitte den Script-Entwickler";
				
			} else {
				HibiscusScripting_PayPal_aPass[PayPal_LoginEmail] = PayPal_Passwort;
			}
			//*********************************************************************************

			
			
		}while(! PostLoginContent);
		//*******************************************************************************


		
		//*********************************************************************************
		// Setzen der Variable für die SessionID
		//*********************************************************************************			
		try {
			var PayPal_SessionID = PostLoginContent.getFormByName("searchForm").getInputByName("auth").getValueAttribute();
			Logger.debug(LogIdent+"HibiscusScripting_PayPal_kontoSync: PayPal_SessionID: " +PayPal_SessionID);
			
		} catch(err) {
			try {
				HibiscusScripting_PayPal_SecLogout("Login", monitor);
			} catch(secerr) {
				Logger.debug(LogIdent+"SecLogout konnte nicht ohne Fehler durchgef\u00fchrt werden. Fehler: " +secerr);
			}
	
			if(PayPal_NewSyncActive == false) {
				Logger.error(LogIdent + "Fehler beim setzen der SessionID (siehe Log - Bitte den Entwickler informieren)\nLog-Eintrag: " +err);
				return new java.lang.Exception("Fehler beim setzen der SessionID (siehe Log - Bitte den Entwickler informieren)\nLog-Eintrag: " +err);
			
			} else {
				throw ("Fehler beim setzen der SessionID (siehe Log - Bitte den Entwickler informieren)\nLog-Eintrag: " +err);
			}			
			
		}	
		//*********************************************************************************
		
		
		
		monitor.setPercentComplete(15); 

		Logger.info(LogIdent+"PayPal-Login war erfolgreich");
		monitor.log("Login war erfolgreich");

		
		
		//*********************************************************************************
		// Kontoauszug: Funktionsaufruf für den Datenabruf im CSV-Format
		//*********************************************************************************			
		monitor.log("Starte Abruf des Kontoauszuges ...");
		Logger.info(LogIdent+"Starte Abruf des Kontoauszuges ...");
		try {
			var ResponseDataCSV = HibiscusScripting_PayPal_getCSVExport(PayPal_SessionID, konto, webClient);

		} catch(err) {
			try {
				HibiscusScripting_PayPal_SecLogout("Login", monitor);
			} catch(secerr) {
				Logger.debug(LogIdent+"SecLogout konnte nicht ohne Fehler durchgef\u00fchrt werden. Fehler: " +secerr);
			}
	
			if(PayPal_NewSyncActive == false) {
				Logger.error(LogIdent + "CVS-DownloadError (Kontoauszug fehlerhaft): " + err);	
				return new java.lang.Exception("Kontoauszug fehlerhaft! " +err);
				
			} else {
				throw ("Kontoauszug fehlerhaft! " +err);
			}	
		}
		
		// Überprüfen ob es sich womöglich nicht um einen richtigen Kontoauszug handelt
		//if (ResponseDataCSV.contains("Date") && ResponseDataCSV.contains("Time") && ResponseDataCSV.contains("Currency")) throw ("Kontoauszug hat falsche Sprache: Stellen Sie bitte in Ihrem PayPal-Account unter Profil die Sprache auf Deutsch");
		if (ResponseDataCSV.contains("<html") || ResponseDataCSV.contains("<head")) throw ("Kontoauszug abholen fehlgeschlagen! Beinhaltet falsche Daten. Bitte neu versuchen oder \u00fcberpr\u00fchfen Sie dies mit einem manuellen Download auf PayPal.de");
		//*********************************************************************************			

		
		
		monitor.setPercentComplete(50);
		
		Logger.info(LogIdent+"Kontoauszug erfolgreich. Importiere Daten ...");
		monitor.log("Kontoauszug erfolgreich. Importiere Daten ...");
		

		
		//*********************************************************************************
		// Kontoauszug verarbeiten: Funktionsaufruf für die Datenverarbeitung der CSV
		//*********************************************************************************
		try {
			HibiscusScripting_PayPal_syncDataAndAccount(ResponseDataCSV, konto, monitor);
			
		} catch (err) {
			try {
				HibiscusScripting_PayPal_SecLogout("Login", monitor);
			} catch(secerr) {
				Logger.debug(LogIdent+"SecLogout konnte nicht ohne Fehler durchgef\u00fchrt werden. Fehler: " +secerr);
			}
	
			if(PayPal_NewSyncActive == false) {	
				Logger.error(LogIdent+"Umsatzverarbeitung: " +err);
				return new java.lang.Exception("Umsatzverarbeitung: " +err);
			
			} else {
				throw ("Umsatzverarbeitung: " +err);
			}
		}
		//*********************************************************************************
		
		
		
		monitor.addPercentComplete(99);
		
		// Logout von der Homepage
		Logger.info(LogIdent+"PayPal-Logout ...");
		monitor.log("PayPal-Logout ...");
		try {
			HibiscusScripting_PayPal_SecLogout("Logout", monitor);
		} catch(secerr) {
			Logger.debug(LogIdent+"SecLogout konnte nicht ohne Fehler durchgef\u00fchrt werden. Fehler: " +secerr);
		}
		
		monitor.addPercentComplete(100); 
		
		
		
	} catch (err) {
		throw err;
		
	} finally {
		webClient = null;
	}
	 
}





function HibiscusScripting_PayPal_prepareClient(monitor) {
/*******************************************************************************
* bereitet den WebClient von HTMLUnit vor, richtet diesen ein
*******************************************************************************/
    
    try {
		//webClient.setUseInsecureSSL(false);
		webClient.setJavaScriptEnabled(false); // ist schneller und JS wird nicht benötigt
		//webClient.setThrowExceptionOnFailingStatusCode(false);
		webClient.setCssEnabled(false);
		//webClient.setThrowExceptionOnScriptError(false);
		//webClient.setRedirectEnabled(true);

		
		/****************************************************************************************
		* ProxyCheckSet ver. 1.4.1
		* (Prüfen ob ein Proxy, wie in Jameica eingestellt, benötigt wird und stellt diesen ein)
		*****************************************************************************************/
		Logger.debug(LogIdent+"es wird auf eine Proxy-Konfiguration gepr\u00FCft ...");
		var JameicaSysProxyUse = Application.getConfig().getUseSystemProxy();
		var JameicaProxyHost = Application.getConfig().getProxyHost();
		var JameicaProxyPort = Application.getConfig().getProxyPort();
		var JameicaHttpsProxyHost = Application.getConfig().getHttpsProxyHost();
		var JameicaHttpsProxyPort = Application.getConfig().getHttpsProxyPort();
		var ProxyConfigSet;

		if ((JameicaSysProxyUse == true) || 
			((JameicaProxyHost != null) && (JameicaProxyPort != -1) && (JameicaSysProxyUse == false)) || 
			((JameicaHttpsProxyHost != null) && (JameicaHttpsProxyPort != -1) && (JameicaSysProxyUse == false))) {

			Logger.info(LogIdent+"Proxy Einstellungen setzten ...");
			monitor.log("Proxy Einstellungen setzten ...");
			
			Logger.debug(LogIdent+"Jameica nutzt den System-Proxy: " +JameicaSysProxyUse);
			Logger.debug(LogIdent+"HTTP-Proxy Host von Jameica ist: " +JameicaProxyHost);
			Logger.debug(LogIdent+"HTTP-Proxy Port von Jameica ist: " +JameicaProxyPort);
			Logger.debug(LogIdent+"HTTPS-Proxy Host von Jameica ist: " +JameicaHttpsProxyHost);
			Logger.debug(LogIdent+"HTTPS-Proxy Port von Jameica ist: " +JameicaHttpsProxyPort);

			if (JameicaSysProxyUse == true) {
				// den Systemproxy in Java übernehmen (dies muss in den Java-Einstellungen auch so konfiguriert sein und kein eigener Proxy! sonst müsste dies hier noch weiter ausgebaut werden ...)
				new java.lang.System.setProperty("java.net.useSystemProxies", "true");
				// Werte der System-Proxykonfiguration welche Java nun verwendet
				var SysProxyInfoHTTP = new java.lang.String(new java.net.ProxySelector.getDefault().select(new java.net.URI("http://www.java.de")).get(0));
				var SysProxyInfoHTTPS = new java.lang.String(new java.net.ProxySelector.getDefault().select(new java.net.URI("https://www.mydrive.ch")).get(0)); 
				var SysProxyFehler = 0;
				Logger.debug(LogIdent+"HTTP Proxy-Einstellung des Systems ist f\u00FCr Java: " +SysProxyInfoHTTP);
				Logger.debug(LogIdent+"HTTPS Proxy-Einstellung des Systems ist f\u00FCr Java: " +SysProxyInfoHTTPS);
				
				if ((SysProxyInfoHTTP == "DIRECT") && (SysProxyInfoHTTPS == "DIRECT"))
				{
						monitor.log("Info-Warnung: Systemproxy-Einstellungen verwenden ist in Jameica eingestellt, es ist aber kein Proxy im System eingetragen!");
						Logger.info(LogIdent+"Systemproxy-Einstellungen verwenden ist in Jameica eingestellt, es ist aber kein Proxy im System eingetragen!");
						ProxyConfigSet = new ProxyConfig();
						
				} else {

					if (SysProxyInfoHTTP != "DIRECT") {
						// HTTP-Proxywerte lesen (so ist es am sichersten,da wir nicht die Einstellung von Java selbst wollen, sondern was Java als Systemproxy ausgelesen hat)
						var SysProxyValuesString = SysProxyInfoHTTP.split(" @ "); //eg.: HTTP @ 10.96.3.105:8080
						Logger.debug(LogIdent+"SysProxyValuesString: " +SysProxyValuesString);
						var SysProxyProtokol = SysProxyValuesString[0];
						Logger.debug(LogIdent+"SysProxyProtokol: " +SysProxyProtokol);
						var SysProxySetting = SysProxyValuesString[1];
						Logger.debug(LogIdent+"SysProxySetting: " +SysProxySetting);
						var SysProxyString = SysProxySetting.split(":"); //eg.: 10.96.3.105:8080
						Logger.debug(LogIdent+"SysProxyString: " +SysProxyString);
						var SysProxyHost = SysProxyString[0];
						Logger.debug(LogIdent+"HTTP-Proxy Host des Systems ist f\u00FCr Java: " +SysProxyHost);
						var SysProxyPort = SysProxyString[1];
						Logger.debug(LogIdent+"HTTP-Proxy Port des Systems ist f\u00FCr Java: " +SysProxyPort);
					} else { 
						monitor.log("Warnung: Systemproxy-Einstellungen verwenden ist in Jameica eingestellt, es ist aber kein Proxy f\u00FCr HTTP im System eingetragen!");
						Logger.warn(LogIdent+"Systemproxy-Einstellungen verwenden ist in Jameica eingestellt, es ist aber kein Proxy f\u00FCr HTTP im System eingetragen!");
						var SysProxyFehler = SysProxyFehler + 1;
					}
					
					if (SysProxyInfoHTTPS != "DIRECT") {
						// HTTPS-Proxywerte lesen (so ist es am sichersten,da wir nicht die Einstellung von Java selbst wollen, sondern was Java als Systemproxy ausgelesen hat)
						var SysHttpsProxyValuesString = SysProxyInfoHTTPS.split(" @ "); //eg.: HTTP @ 10.96.3.107:7070
						Logger.debug(LogIdent+"SysHttpsProxyValuesString: " +SysHttpsProxyValuesString);
						var SysHttpsProxyProtokol = SysHttpsProxyValuesString[0];
						Logger.debug(LogIdent+"SysHttpsProxyProtokol: " +SysHttpsProxyProtokol); // HTTP ist hier auch für HTTPS normal (es ist ja das HTTP-Protokoll)
						var SysHttpsProxySetting = SysHttpsProxyValuesString[1];
						Logger.debug(LogIdent+"SysHttpsProxySetting: " +SysHttpsProxySetting);
						var SysHttpsProxyString = SysHttpsProxySetting.split(":"); //eg.: 10.96.3.105:8080
						Logger.debug(LogIdent+"SysHttpsProxyString: " +SysHttpsProxyString);
						var SysHttpsProxyHost = SysHttpsProxyString[0];
						Logger.debug(LogIdent+"HTTPS-Proxy Host des Systems ist f\u00FCr Java: " +SysHttpsProxyHost);
						var SysHttpsProxyPort = SysHttpsProxyString[1];
						Logger.debug(LogIdent+"HTTPS-Proxy Port des Systems ist f\u00FCr Java: " +SysHttpsProxyPort);
					} else { 
						monitor.log("Warnung: Systemproxy-Einstellungen verwenden ist in Jameica eingestellt, es ist aber kein Proxy f\u00FCr HTTPS im System eingetragen!");
						Logger.warn(LogIdent+"Systemproxy-Einstellungen verwenden ist in Jameica eingestellt, es ist aber kein Proxy f\u00FCr HTTPS im System eingetragen!");
						var SysProxyFehler = SysProxyFehler + 1;
					}
					
					if ((SysHttpsProxyHost != null) && (SysHttpsProxyPort != -1)) {
						ProxyConfigSet = new ProxyConfig(SysHttpsProxyHost, SysHttpsProxyPort, false);
						monitor.log("OK: Es wird der HTTPS-Proxy vom System benutzt");
						Logger.info(LogIdent+"Es wird der HTTPS-Proxy vom System benutzt");
					}else if ((SysProxyHost != null) && (SysProxyPort != -1)) {
						ProxyConfigSet = new ProxyConfig(SysProxyHost, SysProxyPort, false);
						monitor.log("Warnung: Es wird der HTTP-Proxy vom System benutzt. Sollte dieser kein HTTPS unterst\u00FCzen gibt es Fehler!");
						Logger.warn(LogIdent+"Es wird der HTTP-Proxy vom System benutzt. Sollte dieser kein HTTPS unterst\u00FCzen gibt es Fehler!");
					}else { 
						var SysProxyFehler = SysProxyFehler + 1;
						if (SysProxyFehler == 3) {
							monitor.log("Warnungs-INFO: Es sieht so aus als w\u00FCrden Sie eigentlich keinen Proxy verwenden ...");
							monitor.log("Warnungs-INFO: ... entfernen Sie daher wom\u00f6glich einfach den Hacken der Proxykonfiguration ...");
							monitor.log("Warnungs-INFO: ... 'System-Einstellungen verwenden' in den Jameica Einstellungen, um diesen Fehler zu beheben");
						}
						throw "Systemproxy-Einstellungen verwenden ist gew\u00E4hlt: aber bei diesen fehlt offensichtlich ein Eintrag!";
					}
					
				}
				
			}else if ((JameicaHttpsProxyHost != null) && (JameicaHttpsProxyPort != -1) && (JameicaSysProxyUse == false)) {
				ProxyConfigSet = new ProxyConfig(JameicaHttpsProxyHost, JameicaHttpsProxyPort, false);
				monitor.log("OK: Es wird der HTTPS-Proxy von Jameica benutzt");
				Logger.info(LogIdent+"Es wird der HTTPS-Proxy von Jameica benutzt");
			}else if ((JameicaProxyHost != null) && (JameicaProxyPort != -1) && (JameicaSysProxyUse == false)) {
				ProxyConfigSet = new ProxyConfig(JameicaProxyHost, JameicaProxyPort, false);
				monitor.log("Warnung: Es wird der HTTP-Proxy von Jameica benutzt. Sollte dieser kein HTTPS unterst\u00FCzen gibt es Fehler!");
				Logger.warn(LogIdent+"Es wird der HTTP-Proxy von Jameica benutzt. Sollte dieser kein HTTPS unterst\u00FCzen gibt es Fehler!");
			}
			
			
			
			// WebClient mit den den Proxy-Einstellungen anlegen
			webClient.setProxyConfig(ProxyConfigSet);
			Logger.debug(LogIdent+"ProxyConfigSet-Configstring ergibt: " + ProxyConfigSet);
			Logger.debug(LogIdent+"WebClient-Proxy-Configstring ergibt: " + webClient.getProxyConfig());
			
			
			
		} else if ((JameicaProxyHost != null) && (JameicaProxyPort == -1) && (JameicaSysProxyUse == false)) {
			throw "Es ist ein HTTP-Proxy eingetragen aber die Port-Einstellung fehlt!";
				
		} else if ((JameicaProxyHost == null) && (JameicaProxyPort != -1) && (JameicaSysProxyUse == false)) {
			throw "Es ist ein HTTP-Proxy-Port eingetragen aber die Host-Einstellung fehlt!"
		
		} else if ((JameicaHttpsProxyHost != null) && (JameicaHttpsProxyPort == -1) && (JameicaSysProxyUse == false)) {
			throw "Es ist ein HTTPS-Proxy eingetragen aber die Port-Einstellung fehlt!"
		
		} else if ((JameicaHttpsProxyHost == null) && (JameicaHttpsProxyPort != -1) && (JameicaSysProxyUse == false)) {
			throw "Es ist ein HTTPS-Proxy-Port eingetragen aber die Host-Einstellung fehlt!"
		
		} else {
			Logger.debug(LogIdent+"... es ist auf keine aktive Weise ein Proxy eingestellt");
		}
			
		Logger.info(LogIdent+"Verbindung vorbereitet");
		/***************************************************************************************/	
		
	} catch (err) {
		monitor.log("Warnung: " +err);
		Logger.warn(LogIdent+err);
		throw "Setzen der webClient Verbindungsparameter fehlgeschlagen! (siehe Warnungen im Log)";
	}
}





function HibiscusScripting_PayPal_HttpsLogin(ResponseLoginEmail, ResponsePasswort, webClient) {
/*******************************************************************************
* Login PayPal, liefert die Ergebnisseite oder dessen Fehlermeldung zurück
*******************************************************************************/

	// Variable für spezifische Error-Meldungen
	var InputError = 0;

	
	
	try {
		//*********************************************************************************
		// Login-Seite aufrufen, liefert die Ergebnisseite oder dessen Fehlermeldung zurück
		//*********************************************************************************
		Logger.info(LogIdent+"PayPal-Login aufrufen ... (GET " +HibiscusScripting_PayPal_LoginURL+")");
		try {		
			var pageLogin = webClient.getPage(HibiscusScripting_PayPal_LoginURL);
			Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: pageLogin: " +pageLogin);
			
		} catch(err) {
			InputError = 2;
			throw "Die Homepage antwortet nicht oder es existiert keine Internertverbindung mit Jameica (siehe Log)\nLog-Eintrag: " +err;
		}
		var LoginPageTitle = pageLogin.getTitleText();
		var LoginPageResponse = pageLogin.getWebResponse().getContentAsString();
		//Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: LoginPageResponse: \n" +LoginPageResponse); // gibt die ganze Seite aus, also ganz schön viel
		
		// Es wird noch der Response auf Fehlernachrichten überprüft
		try {
			HibiscusScripting_PayPal_checkResponse(LoginPageTitle, LoginPageResponse);
			
		} catch(err) {
			InputError = 2;
			throw "Fehlermeldung von PayPal: " +err;
		}
		if (! pageLogin) throw "Die Login-Seite konnte nicht aufgerufen werden!";
		//*********************************************************************************
		
		
		
		//*********************************************************************************
		// Setzen des Formulars für den Login und Werte eintragen
		//*********************************************************************************			
		try {
			var formLogin = pageLogin.getFormByName("login_form");
			Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: formLogin: " +formLogin);
			formLogin.getInputByName("login_email").setValueAttribute(ResponseLoginEmail);
			formLogin.getInputByName("login_password").setValueAttribute(ResponsePasswort);
			var submitLogin = formLogin.getInputByName("submit.x");
			
		} catch(err) {
			throw "Fehler beim setzen des Login-Formulars oder der Felder (siehe Log - Bitte den Entwickler informieren)\nLog-Eintrag: " +err;
			
		}	
		//*********************************************************************************
	
	
	
		//*********************************************************************************
		// Login abschicken, liefert die Ergebnisseite oder dessen Fehlermeldung zurück
		//*********************************************************************************
		Logger.info(LogIdent+"Login-Form wird abgesendet ...");
		try {		
			var PostLoginPage = submitLogin.click();
			Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: PostLoginPage: " +PostLoginPage);
			
		} catch(err) {
			InputError = 2;
			throw "Die Homepage antwortet nicht oder es existiert keine Internertverbindung mit Jameica (siehe Log)\nLog-Eintrag: " +err;
		}
		var PostLoginPageTitle = PostLoginPage.getTitleText();
		var PostLoginPageXML = PostLoginPage.asXml();
		var PostLoginPageResponse = PostLoginPage.getWebResponse().getContentAsString();
		//Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: PostLoginPageResponse: \n" +PostLoginPageResponse); // gibt die ganze Seite aus, also ganz schön viel
		
		// Es wird noch der Response auf Fehlernachrichten überprüft
		try {
			HibiscusScripting_PayPal_checkResponse(PostLoginPageTitle, PostLoginPageResponse);
			
		} catch(err) {
			InputError = 2;
			throw "Fehlermeldung von PayPal: " +err;
		}
		if (! PostLoginPage) throw "Die Login-Folgeseite konnte nicht aufgerufen werden!";
		//*********************************************************************************


		
		// Handelt es sich um einen SMS-Login?
		if (PostLoginPageXML.contains("id=\"sendbtn\" name=\"send_sms\"") && PostLoginPageXML.contains("name=\"security_form\"")) {
			
			//*********************************************************************************
			// Setzen des Formulars für die SMS-Anforderung und Werte eintragen
			//*********************************************************************************			
			Logger.info(LogIdent+"SMS f\u00fcr Login wird angefordert ...");
			try {
				var form = PostLoginPage.getFormByName("security_form");
				Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: form (Pre-SMS): " +form);
				var sms = form.getInputByName("send_sms");
				
			} catch(err) {
				throw "Fehler beim setzen des SMS-Anforderungsformulars oder des Button (siehe Log - Bitte den Entwickler informieren)\nLog-Eintrag: " +err;
				
			}	
			//*********************************************************************************
			
			
			
			//*********************************************************************************
			// SMS-Anforderung abschicken, liefert die Ergebnisseite oder dessen Fehlermeldung zurück
			//*********************************************************************************
			Logger.info(LogIdent+"SMS-Anforderung Form wird abgesendet ...");
			try {		
				PostLoginPage = sms.click();
				Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: PostLoginPage (Pre-SMS): " +PostLoginPage);
				
			} catch(err) {
				InputError = 2;
				throw "(Pre-SMS) Die Homepage antwortet nicht oder es existiert keine Internertverbindung mit Jameica (siehe Log)\nLog-Eintrag: " +err;
			}
			var PostLoginPageTitle = PostLoginPage.getTitleText();
			var PostLoginPageResponse = PostLoginPage.getWebResponse().getContentAsString();
			//Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: PostLoginPageResponse (Pre-SMS): \n" +PostLoginPageResponse); // gibt die ganze Seite aus, also ganz schön viel
			
			// Es wird noch der Response auf Fehlernachrichten überprüft
			try {
				HibiscusScripting_PayPal_checkResponse(PostLoginPageTitle, PostLoginPageResponse);
				
			} catch(err) {
				InputError = 2;
				throw "Fehlermeldung von PayPal: " +err;
			}
			if (! PostLoginPage) throw "Die SMS-Anforderung Folgeseite konnte nicht aufgerufen werden!";
			//*********************************************************************************

			
			
			// Eingabeaufforderung für den SMS-Anmeldecode
			var enterCode = false;
			do {
				try {
					var SmsSec = Application.getCallback().askPassword("Bitte geben Sie den 6-stelligen Code ein, der Ihnen per SMS gesendet wurde:\n");
					if (false == SmsSec.isEmpty()) enterCode = true;
					
				} catch (err) {
					InputError = 2;
					throw "Konto ist gesperrt und Entsperrung ist fehlgeschlagen! SMS-Code Eingabe vom Benuzter abgebrochen ...";

				}
			
			} while(!enterCode || String(SmsSec).length != 6);
			
			
			
			//*********************************************************************************
			// Setzen des Formulars für den SMS-Login und Werte eintragen
			//*********************************************************************************			
			Logger.info(LogIdent+"SMS Login-Form wird gesetzt ...");
			try {
				var formotp = PostLoginPage.getFormByName("security_form");
				Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: formotp (Post-SMS): " +formotp);
				formotp.getInputByName("otp").setValueAttribute(SmsSec);
				var submitSMS = formotp.getInputByName("submit.x");
				
			} catch(err) {
				throw "Fehler beim setzen des SMS-Loginformulars oder des Button (siehe Log - Bitte den Entwickler informieren)\nLog-Eintrag: " +err;
				
			}	
			//*********************************************************************************
			
			
			
			//*********************************************************************************
			// SMS-Login abschicken, liefert die Ergebnisseite oder dessen Fehlermeldung zurück
			//*********************************************************************************
			Logger.info(LogIdent+"SMS-Anforderung Form wird abgesendet ...");
			try {		
				PostLoginPage = submitSMS.click();
				Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: PostLoginPage (Post-SMS): " +PostLoginPage);
				
			} catch(err) {
				InputError = 2;
				throw "(Post-SMS) Die Homepage antwortet nicht oder es existiert keine Internertverbindung mit Jameica (siehe Log)\nLog-Eintrag: " +err;
			}
			var PostLoginPageTitle = PostLoginPage.getTitleText();
			var PostLoginPageResponse = PostLoginPage.getWebResponse().getContentAsString();
			//Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: PostLoginPageResponse (Post-SMS): \n" +PostLoginPageResponse); // gibt die ganze Seite aus, also ganz schön viel
			
			// Es wird noch der Response auf Fehlernachrichten überprüft
			try {
				HibiscusScripting_PayPal_checkResponse(PostLoginPageTitle, PostLoginPageResponse);
				
			} catch(err) {
				InputError = 2;
				throw "Fehlermeldung von PayPal: " +err;
			}
			if (! PostLoginPage) throw "Die SMS-Login Folgeseite konnte nicht aufgerufen werden!";
			//*********************************************************************************

		}

		
		
		// Wenn ein SecurityKey abgefragt wird, reichen wir das nochmal per Schnell-Login (Passwort + SecKey) an HttpsLogin weiter
		if (PostLoginPageXML.contains("<input type=\"hidden\" name=\"token\" value=\"") && PostLoginPageXML.contains("name=\"security_form\"")) {
		
			//*********************************************************************************
			// Setzen des Formulars für den Key-Login und Werte eintragen
			//*********************************************************************************			
			Logger.info(LogIdent+"Key Login-Form wird gesetzt ...");
			try {
				var form = PostLoginPage.getFormByName("security_form");
				Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: form (Key): " +formotp);
				
			} catch(err) {
				throw "Fehler beim setzen des Key-Loginformulars (siehe Log - Bitte den Entwickler informieren)\nLog-Eintrag: " +err;
				
			}	
			//*********************************************************************************
			
			
			
			// Eingabeaufforderung für den Security-Key
			Logger.info(LogIdent+"PayPal-Login, Nachfrage nach Security-Key f\u00fcr Token " + form.getInputByName("token").getValueAttribute());
			var enterKey = false;
			do {
				try {
					var KeySec = Application.getCallback().askPassword("Bitte geben Sie den Code des Sicherheitsschl\u00fcssels " + form.getInputByName("token").getValueAttribute() + " ein:");
					if (false == KeySec.isEmpty()) enterKey = true;
					
				} catch (err) {
					InputError = 2;
					throw "Konto ist gesperrt und Entsperrung ist fehlgeschlagen! SMS-Code Eingabe vom Benuzter abgebrochen ...";

				}
			
			} while(!enterKey || String(KeySec).length != 6);
			
						
			
			// Neustart der Funktion für den Login zusätzlich mit dem Key zum Passwort
			PostLoginPage = HibiscusScripting_PayPal_HttpsLogin(ResponseLoginEmail, ResponsePasswort+KeySec, webClient);
			
		}

		
		
		return PostLoginPage;
		
	} catch(err) {

		if (InputError == 2) {
			throw err;
			
		} else {
			throw "Fehlermeldung von Jameica: " +err;
		
		}
	}
	
}





function HibiscusScripting_PayPal_SecLogout(func, monitor) {
/*******************************************************************************
* Logout PayPal: wird auch zur Sicherheit bei auftreten eines Fehler ausgeführt
*******************************************************************************/

	if (func != "Logout") {
		Logger.info(LogIdent + "f\u00fcr die Sicherheit wird noch der Logout durchgef\u00fcrt und das Passwort zur\u00fcckgesetzt ...");
		monitor.log("Pre-Fehler: f\u00fcr die Sicherheit wird noch der Logout durchgef\u00fcrt und das Passwort zur\u00fcckgesetzt ...");
	}
		
	if (func != "noLogin") {
		try {
			var PostLogoutPage = webClient.getPage(HibiscusScripting_PayPal_LogoutURL);
			Logger.debug(LogIdent+"PostLogoutPage: " +PostLogoutPage);

		} catch(err) {
			throw "Die Homepage antwortet nicht oder es existiert keine Internertverbindung mit Jameica (siehe Log)\nLog-Eintrag: " +err;
		}
		var PostLogoutPageXML = PostLogoutPage.asXml();
		//Logger.debug(LogIdent+"PostLogoutPageXML: \n" +PostLogoutPageXML); //gibt die ganze Logout-Seite im Log aus

	}
	
	// alle Fenster schließen
	webClient.closeAllWindows();
	
	// sensible Daten löschen
	if (func != "Logout") HibiscusScripting_PayPal_aPass[PayPal_LoginEmail] = ""; // zur Sicherheit wird hier nun das Passwort auf Null gesetzt	
	PayPal_LoginEmail = "";
	
	// und noch wichtig: ein paar Variablen zurück setzen!
	PayPal_fetchSaldo = "";
	PayPal_fetchUmsatz = "";

}





function HibiscusScripting_PayPal_getCSVExport(SessionID_CSV, konto, webClient) {
/*******************************************************************************
* Ruft den Kontoauszug als CSV (Tabgetrennt) ab
*******************************************************************************/

	// Variable für spezifische Error-Meldungen
	var InputError = 0;
	
	try {
		var DateNow = new Date();  	// Aktuelles Datum
		var fromDate = new Date(); 	// Abrufdatum

		var lastCall = konto.getSaldoDatum(); // letzte kontoaktualisierung
		var umsaetze = konto.getUmsaetze();
		//Logger.debug(LogIdent+"Jetziger Saldo des Kontos: " + konto.getSaldo());
		var UmsatzZahl = umsaetze.size();
		


		// fallst noch nie, fragen wir nach, ab wann Umsätze geholt werden sollen
		if (!lastCall || (UmsatzZahl == 0)) {
			Logger.info(LogIdent+"Sie benutzen dieses PayPal-Konto das erste Mal in Hibiscus oder das Saldo und Datum wurde zur\u00fcckgesetzt, frage Benutzer nach gew\u00fcnschtem Ab-Datum");
			var datum = [];
			
			try {
				do {
					datum = String(Application.getCallback().askUser("Sie benutzen dieses PayPal-Konto das erste mal in Hibiscus\n"
										       + "oder das Saldo und Datum wurde zur\u00fcckgesetzt ...\n\n"
										       + "Ab welchem Datum sollen Ums\u00e4tze u. Saldo geholt werden?\n"
										       + "(dieses darf nicht mehr als zwei Jahre in der Vergangenheit liegen!)\n\n"
										       , "Datum (TT.MM.JJJJ)")).split(".");
										       
					Logger.info(LogIdent+"Benutzer w\u00fcnscht Abruf ab "+datum[0]+"."+datum[1]+"."+datum[2]);
				} while(datum.length != 3)
			
			} catch(err) {
				
				throw "Eingabe des Abrufdatum wurde vom Benutzer abgebrochen";
			}	
			
				
				fromDate = new Date(datum[2], (datum[1] - 1), datum[0]); 
				
		} else {
			Logger.debug(LogIdent+"Letztes Abrufdatum: " + lastCall.getDate()+"."+(lastCall.getMonth() + 1)+"."+(lastCall.getYear()+1900));

			fromDate = new Date((lastCall.getTime()-1209600000)) // 14 Tage mehr abrufen, Überschneidungen findet der Doppel-Check
			Logger.debug(LogIdent+"Rufe Ums\u00e4tze ab "+fromDate.getDate()+"."+(fromDate.getMonth() + 1)+"."+(fromDate.getYear()+1900)+" ab ...");
		}

		// Guthaben-relevante Zahlungen (Tabulator getrennt)
		// Umsatz-Cache aktualisieren fuer den späteren Abgleich, zur Sicherheit ein paar Tage mehr als nötig...
		HibiscusScripting_PayPal_refreshHibiscusUmsaetze(konto, (parseInt(((DateNow - fromDate) / 86400000)) + 10))
		var adr = "https://history.paypal.com/de/cgi-bin/webscr?cmd=_history-download-submit&history_cache=&type=custom_date_range&from_b="+fromDate.getDate()+"&from_a="+(fromDate.getMonth() + 1)+"&from_c="+(fromDate.getYear()+1900)+"&to_b="+DateNow.getDate()+"&to_a="+(DateNow.getMonth() + 1)+"&to_c="+(DateNow.getYear()+1900)+"&custom_file_type=tabdelim_balaffecting&latest_completed_file_type=&submit.x=Kontoauszug+herunterladen&auth="+SessionID_CSV+"&form_charset=UTF-8";

		

		//*********************************************************************************
		// Kontoauszug(CSV) abrufen, liefert die Ergebnisseite oder dessen Fehlermeldung zurück
		//*********************************************************************************
		Logger.info(LogIdent+"CSV-Download ... (GET "+adr+")");
		try {		
			var csvdl = webClient.getPage(adr);
			Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: csvdl: " +csvdl);
			
		} catch(err) {
			InputError = 2;
			throw "Die Homepage antwortet nicht oder es existiert keine Internertverbindung mit Jameica (siehe Log)\nLog-Eintrag: " +err;
		}
		var CSVMessageTitle = csvdl.getWebResponse().getContentAsString();
		var CSVMessage = csvdl.getWebResponse().getContentAsString();
		//Logger.debug(LogIdent+"HibiscusScripting_PayPal_HttpsLogin: CSVMessage: \n" +CSVMessage); // gibt die ganze Seite aus, also ganz schön viel
		
		// Es wird noch der Response auf Fehlernachrichten überprüft
		try {
			HibiscusScripting_PayPal_checkResponse(CSVMessageTitle, CSVMessage);
			
		} catch(err) {
			InputError = 2;
			throw "Fehlermeldung von PayPal: " +err;
		}
		if (! csvdl) throw "Kontoauszug(CSV) konnte nicht aufgerufen werden!";
		//*********************************************************************************
				
		

		return CSVMessage;

	} catch(err) {

		if (InputError == 2) {
			throw err;

		} else {
			throw "Fehlermeldung von Jameica: " +err;

		}
	}

}





function HibiscusScripting_PayPal_syncDataAndAccount(csv, konto, monitor) {
/*******************************************************************************
* Erzeugen von Umsätzen aus dem CSV-Kontoauszug, Prüfung und Speicherung
*******************************************************************************/

	// CSV-Ergebnisliste in ein Array umwandeln
	var CSVDataRow = HibiscusScripting_PayPal_CSV2Array(csv, "	");
	//Logger.debug(LogIdent+"CSVDataRow csv2Array: " +CSVDataRow);

	try {
		HibiscusScripting_PayPal_getPosInCSV ( /W.hrung$/, CSVDataRow[0] )
		
	} catch(err) {
		// Charset vermurkst? Kommt manchmal vor, aber nicht immer...
		Logger.debug(LogIdent+"Kein W\u00e4hrungsfeld gefunden, versuche Charset zu fixen ...");
		csv = new java.lang.String(csv);
		csv = new java.lang.String(csv.getBytes("ISO-8859-1"),"UTF8");
		CSVDataRow = HibiscusScripting_PayPal_CSV2Array(csv, "	");
	}

	// Datenbank-Verbindung holen
	var db = Application.getServiceFactory().lookup(HBCI,"database");
	// und Variable für Saldo erstellen
	var PayPal_givenSaldo = 0;
	var UmsatzInData = false;

	// wird gemerkt ob ein Eintrag Valide war, also gespeichert wird da er als neu angesehen wird
	var UmsatzAnzahl = 0;
	
	// Auswertung Daten, umgekehrte Reihenfolge, da PayPal die aktuellsten Datensätze oben liefert
	for(var i = (CSVDataRow.length -1); i > 0; i--) {

		if (CSVDataRow[i].length == 1) continue; // Das überspringen wir komplett, ist nichtmal nen Hinweis wert

		Logger.debug(LogIdent+"CSV-Zeile "+i+" enth\u00e4lt " + CSVDataRow[i].length + " Spalten");
		var umsatzdatum = CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Datum$/, CSVDataRow[0] )].split(".");
		var betrag = CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Brutto$/, CSVDataRow[0] )];
		var gebuehr = CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Geb.hr$/, CSVDataRow[0] )];
		var saldo = CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Guthaben$/, CSVDataRow[0] )];
		betrag = betrag.replace(/\./, ""); gebuehr = gebuehr.replace(/\./, ""); saldo = saldo.replace(/\./, "");
		betrag = betrag.replace(/,/, "."); gebuehr = gebuehr.replace(/,/, "."); saldo = saldo.replace(/,/, ".");
		betrag = parseFloat(betrag);       gebuehr = parseFloat(gebuehr);       saldo = parseFloat(saldo);

		// Fortschrittsanzeige auf 50% (Stand nach Kontoabruf) bis max. 99%; Zeilen ohne Umsätze werden abgezogen
		monitor.setPercentComplete(parseInt((50 + (49/(CSVDataRow.length - 1) * ((CSVDataRow.length - 1) - i)))));

		if (umsatzdatum[0] < 1 || umsatzdatum[0] > 31 || umsatzdatum[1] < 1 || umsatzdatum[1] > 12) {
			Logger.debug(LogIdent+"CSV-Zeile "+i+" enth\u00e4lt ung\u00fcltiges Datum, Umsatzerzeugung wird abgebrochen: " + CSVDataRow[i]);
			continue;
		}
		
		if (betrag == 0) {
			Logger.debug(LogIdent+"CSV-Zeile "+i+" enth\u00e4lt eine 0-Buchung, Umsatzerzeugung wird abgebrochen: " + CSVDataRow[i]);
			continue;
		}

		var umsatz = db.createObject(Umsatz,null);
		umsatz.setKonto(konto);
		umsatz.setDatum(new Date(umsatzdatum[2],(umsatzdatum[1] - 1),umsatzdatum[0]));
		umsatz.setValuta(new Date(umsatzdatum[2],(umsatzdatum[1] - 1),umsatzdatum[0]));
		umsatz.setBetrag(betrag);

		// E-Mail in Kontonummer, aber nicht die eigene
		var ggktonr = "";
		var email_von = CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Von E-Mail-Adresse$/, CSVDataRow[0] )];
		var email_an  = CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /An E-Mail-Adresse$/, CSVDataRow[0] )];
		if (email_von && email_von != PayPal_LoginEmail) ggktonr = email_von;
		if ((! ggktonr) && (email_an && email_an != PayPal_LoginEmail)) ggktonr = email_an;

		umsatz.setGegenkontoNummer(ggktonr.substr(0,40));
		umsatz.setGegenkontoName(CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Name$/, CSVDataRow[0] )]); 

		umsatz.setArt(CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Art$/, CSVDataRow[0] )]);
		umsatz.setPrimanota(CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Transaktionscode$/, CSVDataRow[0] )]);  // Transaktionscode als PN
		umsatz.setCustomerRef(CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Status$/, CSVDataRow[0] )]); // Status als KRef

		// 27 Zeichen maximal pro Verwendungszweck und SZ gg. Doppel-S austauschen
		var verwendungszweck = CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Verwendungszweck$/, CSVDataRow[0] )];
		if (verwendungszweck != undefined) {
			verwendungszweck = escape(verwendungszweck);verwendungszweck = verwendungszweck.replace(/%DF/g, "ss");verwendungszweck = unescape(verwendungszweck);
			umsatz.setZweck(String(verwendungszweck.substr(0,27)));
			if (verwendungszweck.length > 27) umsatz.setZweck2(String(verwendungszweck.substr(27,27)));
			// weitere Zeilen
			var purplines = new Array();
			if (verwendungszweck.length > 54) purplines.push(String(verwendungszweck.substr(54,27)));
			if (verwendungszweck.length > 81) purplines.push(String(verwendungszweck.substr(81,27)));
			if (verwendungszweck.length > 108) purplines.push(String(verwendungszweck.substr(108,27)));
			umsatz.setWeitereVerwendungszwecke(purplines);

		}

		var waehrung = CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /W.hrung$/, CSVDataRow[0] )];
		if (waehrung != "EUR") umsatz.setKommentar(waehrung);

		// Saldo nach der Buchung
		umsatz.setSaldo(saldo - gebuehr);

		if (i == 1) {
			// erster Umsatz im CSV, Saldo nach Umsatz wird auch Saldo des Kontos
			Logger.info(LogIdent+"Letzte Buchung des Kontoauszuges. Setze Saldo des Kontos auf Saldo der letzten Buchung ...");
			PayPal_givenSaldo = saldo;
			UmsatzInData = true;
		
		}

		if (HibiscusScripting_PayPal_hibiscusUmsaetze && HibiscusScripting_PayPal_hibiscusUmsaetze.contains(umsatz) != null) {
			Logger.debug(LogIdent+"Umsatz aus der "+i+". Zeile des CSV-Kontoauszuges ist bereits gepeichert");
			
		} else {
		    
			// Abfrage ob neuer Sync aktiv und ob dort "Kontoauszüge (Umsätze) abrufen" aktiviert ist wegen Kontoauszugsverarbeitung
			if((PayPal_NewSyncActive == false) || ((PayPal_NewSyncActive == true) && (PayPal_fetchUmsatz == true))) {

				// Umsatz speichern
				Logger.info(LogIdent+"Speichere Umsatz aus der "+i+". Zeile des CSV-Kontoauszuges ...");
				umsatz.store();

				// Live-Aktualisierung der Umsatz-Liste
				if((PayPal_NewSyncActive == true) && (PayPal_fetchUmsatz == true)) Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));

				//Zählen der neuen Umsätze
				UmsatzAnzahl = UmsatzAnzahl + 1;
			
			}
		}

		
		// Abfrage ob neuer Sync aktiv und ob dort "Kontoauszüge (Umsätze) abrufen" aktiviert ist wegen Kontoauszugsverarbeitung
		if((PayPal_NewSyncActive == false) || ((PayPal_NewSyncActive == true) && (PayPal_fetchUmsatz == true))) {

			// Gebühren von PayPal separat buchen
			if (gebuehr != 0) {
				var umsatz = db.createObject(Umsatz,null);
				umsatz.setKonto(konto);
				umsatz.setDatum(new Date(umsatzdatum[2],(umsatzdatum[1] - 1),umsatzdatum[0]));
				umsatz.setValuta(new Date(umsatzdatum[2],(umsatzdatum[1] - 1),umsatzdatum[0]));
				umsatz.setBetrag(betrag);

				umsatz.setPrimanota(CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Transaktionscode$/, CSVDataRow[0] )]);  // Transaktionscode als PN
				umsatz.setCustomerRef(CSVDataRow[i][HibiscusScripting_PayPal_getPosInCSV ( /Status$/, CSVDataRow[0] )]); // Status als KRef

				umsatz.setBetrag(gebuehr);
				umsatz.setZweck("PayPal-Geb\u00fchren");
				umsatz.setZweck2("");
				umsatz.setSaldo(saldo);
				umsatz.setArt("PayPal-Geb\u00fchren");
				
				if (HibiscusScripting_PayPal_hibiscusUmsaetze && HibiscusScripting_PayPal_hibiscusUmsaetze.contains(umsatz) != null) {
					Logger.debug(LogIdent+"Umsatz-Geb\u00fchr aus der "+i+". Zeile des CSV-Kontoauszuges ist bereits gepeichert");
					
				} else {
					// Umsatz speichern
					Logger.info(LogIdent+"Speichere PayPal-Geb\u00fchren aus der "+i+". Zeile des CSV-Kontoauszuges ...");
					umsatz.store();
					
					// Live-Aktualisierung der Umsatz-Liste
					if((PayPal_NewSyncActive == true) && (PayPal_fetchUmsatz == true)) Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));
					
					//Zählen der neuen Umsätze
					UmsatzAnzahl = UmsatzAnzahl + 1;
				}
			}
		}
		
	} // Ende der For-Schleife für Buchungsverarbeitung
	
	
			
	// Abfrage ob neuer Sync aktiv und ob dort "Kontoauszüge (Umsätze) abrufen" aktiviert ist wegen Kontoauszugsverarbeitung
	if((PayPal_NewSyncActive == false) || ((PayPal_NewSyncActive == true) && (PayPal_fetchUmsatz == true))) {

		// Sollte UmsatzAnzahl == 0 sein ist also kein Umsatz als neu eingestuft worden
		if (UmsatzAnzahl == 0) {
			monitor.log("Ergebnis des Sync: Keine neuen Ums\u00E4tze vorhanden"); 
			Logger.info(LogIdent+"Keine neuen Ums\u00E4tze vorhanden");
			
		} else {
			monitor.log("Ergebnis des Sync: Es wurden ' "+UmsatzAnzahl+" ' neue Ums\u00E4tze \u00FCbernommen"); 
			Logger.info(LogIdent+"Es wurden ' "+UmsatzAnzahl+" ' neue Ums\u00E4tze \u00FCbernommen.");
			
		}
		
	}
		

		
	// Abfrage ob neuer Sync aktiv und ob dort "Saldo aktualisieren" aktiviert ist wegen Speicherung des Konto-Saldos
	if((PayPal_NewSyncActive == false) || ((PayPal_NewSyncActive == true) && (PayPal_fetchSaldo == true))) {

		if ((UmsatzAnzahl == 0) && (UmsatzInData == false))  {
			// kein Umsatz im CSV (setzten eines kontoSaldos für den Store)
			PayPal_givenSaldo = konto.getSaldo();
			Logger.debug(LogIdent+"Keine Daten im CSV ... Saldo wird daher auf Kontosaldo gesetzt für den Store: " +PayPal_givenSaldo);
		
		} else {
			Logger.info(LogIdent+"Setze Saldo des Kontos gleich dem Saldo vom Kontoauszug");				
		}

		Logger.debug(LogIdent+"Konto-Saldo das als letztes gesetzt wird: " +PayPal_givenSaldo);
		konto.setSaldo(PayPal_givenSaldo);

		// Abschließendes Konto-Speichern
		konto.store();
			
		// Live-Aktualisierung des Konto-Saldos
		Application.getMessagingFactory().sendMessage(new SaldoMessage(konto));
		monitor.log("Saldo aktualisiert von Konto: " + konto.getBezeichnung());
		
	}
	
}



function HibiscusScripting_PayPal_getPosInCSV(a, header) {
/*******************************************************************************
* Es werden unterschiedliche CSV-Files je nach User geschickt,
* Diese Funktion sucht mit einem RegEx nach Feldnamen und gibt die Position
* zurueck. a = regex, header = array aus den kopfspalten
*******************************************************************************/

	Logger.debug(LogIdent+"Suche im Header ("+header+") nach "+a);

	for (var pom = 0; pom < 3; pom++) {
		for (var i = 0; i < header.length; ++i)
		{
			if (header[i] == a) {
				Logger.debug(LogIdent+"Gefunden an Position "+i);
				return i;
			}
			if (header[i].match(a)){
				Logger.debug(LogIdent+"Gefunden an Position "+i);
				return i;
			}
		}
		if (a=="/Data$/") a = /Date$/;
		if (a=="/Datum$/") a = /Data$/;
		
		if (a=="/Brutto$/") a = /Gross$/;
		
		if (a==" Op³ata") a = /Fee$/;
		if (a=="/Geb.hr$/") a = " Op³ata";
		
		if (a=="/Name$/") a = " Imiê i nazwisko (nazwa)";
		
		if (a=="/Saldo$/") a = /Balance$/;
		if (a=="/Guthaben$/") a = /Saldo$/;
		
		if (a=="/Z adresu e-mail$/") a = /From Email Address$/;
		if (a=="/Von E-Mail-Adresse$/") a = /Z adresu e-mail$/;
		
		if (a=="/Na adres e-mail$/") a = /To Email Address$/;
		if (a=="/An E-Mail-Adresse$/") a = /Na adres e-mail$/;
		
		if (a=="/Typ$/") a = /Type$/;
		if (a=="/Art$/") a = /Typ$/;
		
		if (a=="/Numer identyfikacyjny transakcji$/") a = /Transaction ID$/;
		if (a=="/Transaktionscode$/") a = /Numer identyfikacyjny transakcji$/;
		
		if (a=="/Nazwa przedmiotu$/") a = /Item Title$/;
		if (a=="/Verwendungszweck$/") a = /Nazwa przedmiotu$/;
		
		if (a=="/Waluta$/") a = /Currency$/;
		if (a=="/Währung$/") a = /Waluta$/;
		
		if (a=="/Waluta$/") a = /Currency$/;
		if (a=="/W.hrung$/") a = /Waluta$/;
		
	}

	throw("Keine Spalte " + a + " im CSV-Kontoauszug gefunden")
	
}





function HibiscusScripting_PayPal_refreshHibiscusUmsaetze(konto, Tage) {
/*******************************************************************************
* Aktualisiert den Cache der bekannten Umsätze
*******************************************************************************/

	Logger.debug(LogIdent+"Ums\u00e4tze der letzten "+Tage+" Tage von Hibiscus f\u00fcr Doppelbuchung-Checks holen");
	HibiscusScripting_PayPal_hibiscusUmsaetze = konto.getUmsaetze(Tage);
	
}





function HibiscusScripting_PayPal_CSV2Array(strData, strDelimiter) {
/*******************************************************************************
* http://www.bennadel.com/blog/1504-Ask-Ben-Parsing-CSV-Strings-With-Javascript-Exec-Regular-Expression-Command.htm
*******************************************************************************/
	strDelimiter = (strDelimiter || ",");
	var objPattern = new RegExp(
		(
		"(\\" + strDelimiter + "|\\r?\\n|\\r|^)" +
		"(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|" +
		"([^\"\\" + strDelimiter + "\\r\\n]*))"
		), "gi"); 
	var arrData = [[]];
	var arrMatches = null;
	while (arrMatches = objPattern.exec( strData )) {
		var strMatchedDelimiter = arrMatches[ 1 ];
		if (strMatchedDelimiter.length && (strMatchedDelimiter != strDelimiter) ) {
			arrData.push( [] );
		}
		if (arrMatches[ 2 ]) {
			var strMatchedValue = arrMatches[ 2 ].replace( new RegExp( "\"\"", "g" ), "\"" );
		} else {
			var strMatchedValue = arrMatches[ 3 ];
		}
		arrData[ arrData.length - 1 ].push( strMatchedValue );
	}
	return( arrData );
}





function HibiscusScripting_PayPal_checkResponse(ResponseTitle, ResponseContent) {
/*******************************************************************************
* Prüfen ob der Response bekannte Fehlermarker oder Nachrichten enthält
*******************************************************************************/

		//Logger.debug(LogIdent+"Funktion checkResponse wurde aufgerufen mit dem Title: " +ResponseTitle); // gibt den Titel der du prüfenden Seite aus (bei CSV ganz schön viel)
		//Logger.debug(LogIdent+"Funktion checkResponse wurde aufgerufen mit dem Content: " +ResponseContent); // sollte die ganze HTML-Seite ausgeben also sehr viel
		
		if (ResponseContent.contains("Fehlermeldung")) {
			var LongErrorMessage = HibiscusScripting_PayPal_formErrorMessage(ResponseContent);
			var ShortErrorMessage = HibiscusScripting_PayPal_getErrorString(ResponseContent);
			
			// Hier wird nun also die perfekt formatierte Fehlernachricht(en) in einem Infofenster ausgegeben
			Application.getCallback().notifyUser(PayPalErrorTitle +LongErrorMessage);
			
			throw ShortErrorMessage; // und die kurze Version wird als Exception weiter gegeben
			
		} else if (ResponseContent.contains("s.prop14=")) {
			var ShortErrorMessage = HibiscusScripting_PayPal_getErrorString(ResponseContent);
			
			// Hier wird Fehlernachricht in einem Infofenster ausgegeben
			Application.getCallback().notifyUser(PayPalErrorTitle +ShortErrorMessage);
			
			throw ShortErrorMessage; // und als Exception weiter gegeben
		
		} else if (ResponseTitle.contains("Fehler")) {
			throw "Im Titel gibt es einen Fehlerhinweis, aber es wurde kein Textabruf hierzu implementiert. Informieren Sie bitte den Entwickler";
		
		} else {
			return;
		}
}





function HibiscusScripting_PayPal_getErrorString(MessagePage) {
/*******************************************************************************
* ermittelt in der Seite den kurzen Fehlertext und bereitet diesen auf
*******************************************************************************/

	// Jetzt wird die kurze Fehlernachricht gelesen
	var shortErrorIDXstart = MessagePage.indexOf("s.prop14="); // Ermittle die Position (Index) des Anfangs der kurzen Fehlernachricht
	Logger.debug(LogIdent+"shortErrorIDXstart: " +shortErrorIDXstart);
	
	if (shortErrorIDXstart == -1) {
		return "NULL";
	
	} else {
		shortErrorIDXstart = shortErrorIDXstart +10;
		var shortErrorIDXend = MessagePage.indexOf("\";", shortErrorIDXstart); // Ermittle die Position (Index) des Endes der kurzen Fehlernachricht
		Logger.debug(LogIdent+"shortErrorIDXend: " +shortErrorIDXend);
		var shortErrorText = MessagePage.substring(shortErrorIDXstart, shortErrorIDXend); // Hole den String vom Index1 bis Index2
		//Logger.debug(LogIdent+"shortErrorText (unformatiert): " +shortErrorText);
		// Nun wird nochmal die Fehlernachricht in einen Char-String umgewandelt (d. h. die ASCII-HEX-Codes sollen richtig dargestellt werden)
		var shortErrorTextString = HibiscusScripting_PayPal_HEXtoChar(shortErrorText);
		Logger.debug(LogIdent+"shortErrorTextString (formatiert): " +shortErrorTextString);
		
		return shortErrorTextString;
	}
	
}		





function HibiscusScripting_PayPal_HEXtoChar(StringWithHEX) {
/*******************************************************************************
* ermittelt HEX-Codes wie '\x20' in einem String und ersetzt Sie mit dem Char
*******************************************************************************/

	// Konvertieren von diesen Zeichen:
	// Leerzeichen ! - , .
	var StringfromHEX = StringWithHEX.split("\\\\").join("")
					 .split("x20").join(" ")
					 .split("x21").join("!")
					 .split("x2c").join(",")
					 .split("x2d").join("-")
					 .split("x2e").join(".");

	return StringfromHEX;
	
}





function HibiscusScripting_PayPal_formErrorMessage(MessagePage) {
/*******************************************************************************
* ermittelt in der Seite den langen Fehlertext und bereitet diesen auf
*******************************************************************************/

	//Logger.debug(LogIdent+"MessagePage: " +MessagePage); //gibt die ganze Seite aus, sollte daher auskommentiert sein!
	// Jetzt wird die lange Fehlernachricht gelesen
	var longErrorIDXstart = MessagePage.indexOf("Fehlermeldung"); // Ermittle die Position (Index) des Anfangs der langen Fehlermeldung
	Logger.debug(LogIdent+"longErrorIDXstart: " +longErrorIDXstart);

	if (longErrorIDXstart == -1) {
		return "NULL";
	
	} else {
		longErrorIDXstart = longErrorIDXstart +13;
		var longErrorIDXend = MessagePage.indexOf("</p>", longErrorIDXstart); // Ermittle die Position (Index) des Endes der langen Fehlermeldung
		Logger.debug(LogIdent+"longErrorIDXend: " +longErrorIDXend);
		var longErrorText = MessagePage.substring(longErrorIDXstart, longErrorIDXend); // Hole den String vom Index1 bis Index2
		//Logger.debug(LogIdent+"longErrorText: " +longErrorText);
		//Logger.debug(LogIdent+"longErrorText asText: " +longErrorText.DomNode().asText());

		// Um die lange Fehlernachricht schön mit einem Infofenster ausgeben zu können werden nun die HTML-Tags entfernt und der String wird nach jedem Punkt umgebrochen
		longErrorText = longErrorText
					.split("<b>").join("")
					.split("</b>").join("")
					.split("</h2>").join("")
					.split("<p>").join("");
					//.split("  ").join("");
		//longErrorText = longErrorText.replace(/(\r\n|\n|\r)/gm,""); // noch die vorhandenen Zeilenumbrüche entfernen
		// und Textänderungen
		longErrorText = longErrorText.split("die folgenden Tipps zur Fehlerbehebung").join("die Tipps zur Fehlerbehebung unter der PayPal-Homepage\noder warten Sie zwei Stunden");
		//Logger.debug(LogIdent+"longErrorText nach replace: " +longErrorText);
		
		// Der Textstring wird für das Infofenster nach jedem Zeilen-Punkt umgebrochen
		var longErrorTextArray = longErrorText.split(". ");
		var errorFenster = "";
		var longErrArrNr = 0;
		for (var x = longErrorTextArray.length-1; x >= 0 ; x--) {
			errorFenster = errorFenster +longErrorTextArray[longErrArrNr];
			errorFenster = errorFenster +"\n";
			longErrArrNr = longErrArrNr + 1;
		}
		
		return errorFenster;
	}
	
}		




