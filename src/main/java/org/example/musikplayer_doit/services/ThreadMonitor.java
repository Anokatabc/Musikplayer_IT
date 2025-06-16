package org.example.musikplayer_doit.services;

import java.util.Map;

public class ThreadMonitor {

    public static void startMonitoring() {
        Thread monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Wartezeit zwischen den Updates (z. B. 5 Sekunden)
                    Thread.sleep(5000);

                    // Alle aktiven Threads abrufen
                    Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();
                    int activeThreadCount = 0;

                    System.out.println("----- Thread-Monitor-Update -----");
                    for (Thread thread : allThreads.keySet()) {
                        if (thread.getName().startsWith("pool-2-thread")) {
                            activeThreadCount++;
                            System.out.println("Thread: " + thread.getName() + ", State: " + thread.getState());
                        }
                    }
                    System.out.println("Aktive Threads: " + activeThreadCount);
                    System.out.println("---------------------------------");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Thread-Monitor wurde unterbrochen.");
                }
            }
        });

        monitorThread.setDaemon(true); // Sicherstellen, dass der Monitor-Thread beendet wird, wenn das Hauptprogramm endet
        monitorThread.start();
    }
}