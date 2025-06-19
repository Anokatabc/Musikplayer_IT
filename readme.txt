# JavaFX Musikplayer
============================
----------------------------

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
⋅⋅*
⋅⋅*
