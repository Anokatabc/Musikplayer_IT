# JavaFX Musikplayer
---------------------------
###### Dieser Player ist das Ergebnis meines ersten größeren eigenständigen Software-Projekts.

Ich habe das JavaFX-Framework für die Erstellung und Gestaltung der Benutzeroberflächeverwendet.
Das Projekt besteht aus folgenden Kernfunktionen:

1. Scannen der Festplatten
⋅⋅⋅ Mit der Methode Files.walkFileTree() wird jede Festplatte vollständig gescannt. Der Scanner 
⋅⋅⋅ folgt der Logik, dass er (aktuell nur) Mp3-Dateien identifiziert und in einer Liste speichert.
⋅⋅⋅ Diese Liste wird rekursiv vervollständigt, geordnet und zu einem TreeView in dem Benutzer-
⋅⋅⋅ Interface hinzugefügt.
2. Darstellung der Musikdateien
⋅⋅⋅ Mit Klick auf einem TreeItem werden Dateiinhalte gescannt, Metadaten ausgelesen und beide zu-
⋅⋅⋅ sammen in der Übersicht in der Mitte dargestellt.
3. Anhören der Musik
⋅⋅⋅ Hierfür habe ich einige Media-Buttons entworfen.
- Play/Pause
- Stop
- Next
- Previous
- "Play Last" Toggle
- "Repeat Queue" Toggle

Mit **Doppelklick** oder **Enter** wird Zieldatei wiedergegeben, und alle Inhalte der angezeigten Liste in die 
Playlist geladen.
⋅⋅⋅ Mit **Strg** lässt sich diese Funktion anpassen, dass die nur die markierten Dateien zur Liste hinzugefügt
⋅⋅⋅ werden. (Ohne Wiedergabe)
⋅⋅⋅ Mit **Alt** lasst lässt sich die Funktion weiter anpassen, dass nur die markierten Dateien zur Liste hinzu-
⋅⋅⋅ gefügt *und* abgespielt werden.
Aktuell lassen sich mit **Entfernen** Musikdateien aus der aktiven Playlist löschen.

Tiefere Dateisystem-Eingriffe werden noch nicht unterstützt.
