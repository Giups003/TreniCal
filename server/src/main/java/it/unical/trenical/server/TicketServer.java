package it.unical.trenical.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * Server gRPC che gestisce i servizi di biglietteria e treni.
 */
public class TicketServer {

    private static final int SERVER_PORT = 9090;

    public static void main(String[] args) {
        try {
            // Crea le implementazioni dei servizi
            TicketServiceImpl ticketService = new TicketServiceImpl();
            TrainServiceImpl trainService = new TrainServiceImpl();
            PromotionServiceImpl promotionService = new PromotionServiceImpl();
            NotificationServiceImpl notificationService = new NotificationServiceImpl();
            // [MODALITÀ DEMO] Avvia un task che aggiorna randomicamente lo stato dei treni e genera notifiche automatiche.
            // Questa funzione è pensata per scopo di test: permette di simulare eventi reali (ritardi, cancellazioni, cambi binario)
            // e di verificare che la pipeline delle notifiche funzioni correttamente lato client/server.
            // In un sistema reale, questi eventi sarebbero generati da operatori o sistemi esterni.
            notificationService.startRandomTrainStatusUpdater();

            // Configura e avvia il server gRPC
            Server server = ServerBuilder.forPort(SERVER_PORT)
                    .addService(ticketService) // Aggiunge il servizio biglietti
                    .addService(trainService)  // Aggiunge il servizio treni
                    .addService(promotionService) // Aggiunge il servizio promozioni
                    .addService(notificationService) // Aggiunge il servizio notifiche
                    .build();

            System.out.println("Il server gRPC è avviato sulla porta " + SERVER_PORT + "...");
            System.out.println("Servizi disponibili: TicketService, TrainService, PromotionService, NotificationService");

            // Avvia il server
            server.start();

            // Registra un hook per lo spegnimento pulito
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Arresto del server in corso...");
                server.shutdown();
                System.out.println("Server arrestato.");
            }));

            // Blocca il thread principale fino alla terminazione del server
            server.awaitTermination();

        } catch (IOException e) {
            System.err.println("Errore durante l'avvio del server gRPC: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Il server gRPC è stato interrotto: " + e.getMessage());
            Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione del thread
        }
    }
}
