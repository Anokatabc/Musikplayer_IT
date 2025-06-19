# Musikplayer_Doit - Projekt README
Autor: Julian Voigt
Projektzeitraum: 17.03.2025 – 27.04.2025
 
 
 
 
 1)Applikationsstart
 
 - (Microsoft) OpenJDK 21.0.7 ist womöglich notwendig für einen sauberen Programmstart.
 
 Ich habe unter Mithilfe von KI mein möglichstes versucht, ein Starten des Projekts 
 in verschiedenen Systemumgebungen zu ermöglichen. Ich habe am Ende alle Maven-
 Bibliotheken gelöscht und manuell alle .jar-Dateien vom Maven Repository heruntergeladen
 und im lib\-Ordner eingefügt. 
 
 
 Sie können probieren, die Applikationsstart.bat-Datei zu verwenden. Mit etwas Glück 
 funktioniert sie.
 
 Sie geht per Default von "java" aus, welches in den Windows-eingestellten Umgebungs-
 variablen nach einer Java-Installation sucht. Falls so ein Pfad nicht gesetzt ist, 
 können Sie diesen unter Systemvariablen > Path setzen.
 Eine Java-Installation findet sich z.B. unter: C:\->Vorheriger Pfad(einfügen)\IntelliJ IDEA Community Edition 2024.3.5\jbr\bin
 
 Alternativ können Sie die .bat-Datei bearbeiten und statt "java" (ohne "") den 
 absoluten Pfad (mit "") Ihrer Java-Installation eintragen. 
 Es wird eine Version 9 oder höher vorausgesetzt.
 
! Hinweis: Der Ordner target/ enthält die ausführbare Datei: 
Musikplayer_Doit-1.0-SNAPSHOT.jar
 Diese ist für den Start der Anwendung über das Skript erforderlich. Sie wurde 
 standardmäßig dort erstellt.
 
 
 Sofern Sie das Projekt über IntelliJ starten wollen, müssen Sie unter:
 Run Configurations > modify options > VM Options (und dann im Feld) einfügen:
 --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media
 
 - Unter Name können Sie eintragen:
 Musikplayer
 
 - Main Class:
org.example.musikplayer_doit.App

 - Working Directory:
 $PROJECT_DIR$ 
 oder der absolute Pfad, an dem Sie das Projekt gespeichert haben, endend mit:
 Musikplayer_Doit
 

 
 
 
 
 2) Möglichst nicht JavaFX updaten
 
 Achten Síe darauf, JavaFX nicht zu updaten. Beim Applikationsstart wird darauf hin-
 gewiesen,  dass einzelne JavaFX-Bibliotheken outdated sind.




 3) Anwendungshinweise

 Ich habe einen Musikordner Beispielmusik/ mit Beispiel-Unterordnern mitgegeben. 
 
 Es sind größtenteils kurze Lieder, um die endOfMedia-Events einfacher auszutesten. 
 
 Einzelne längere Lieder sind auch dabei und ein ganz Langes leeres Lied, 
 falls Sie die Labels austesten wollen.

! Achten Sie aber darauf, diesen Musikordner direkt auf eine Festplatte zu ziehen. Mein
 aktueller Tree-Scan hat die Einschränkung, dass er nur unmittelbar auf der Festplatte 
 befindliche MP3-Ordner auflistet. 
 Sobald ein leerer Ordner gefunden wird, wird für den jeweiligen Pfad direkt die 
 Schleife beendet. Dies fiel mir erst in der letzten Woche vor der Abgabe auf, darum 
 konnte ich es nicht mehr ohne Risiko verändern.




 4) Alternativen: Github oder Dropbox
 Sollte mein geziptes Projekt so nicht funktionieren, können Sie meine andere Version 
 von Github oder Dropbox herunterladen.  Der Github-Version fehlen aber ein paar 
 Kommentare von dieser vollständigen Version und sie enthält noch todo-Notizen. 

 Ich habe Github nicht wirklich sauber umgesetzt, und einfach für jeden wesentlichen 
 Projektschritt einen neuen Branch erstellt, ohne jemals zu mergen. Nach anfänglichen 
 Misserfolgen beim Mergen habe ich das ganz gelassen.
 (Vielleicht würden zukünftige Gruppen davon profitieren, vor ihren Do-IT-Phasen ein 
 wenig Github zu lernen)
 
 Der Dropbox-Link ist aber aktuell. Der Download wird wahrscheinlich als verdächtig 
 oder gefährlich markiert, ich habe keine Ahnung, wie ich das verhindere oder absichere.
 Der Dropbox-Download sollte auf dem aktuellen Stand sein.

Github:
 https://github.com/Anokatabc/Musikplayer_IT/branches
Dropbox:
 https://www.dropbox.com/scl/fo/knsvrdt8nqj6kzmhhg93j/AIn6DAB2mOvg2iR0sN8mt3c?rlkey=3z3h639886bbl3tolgqio2omq&st=tqhkfu3x&dl=0
