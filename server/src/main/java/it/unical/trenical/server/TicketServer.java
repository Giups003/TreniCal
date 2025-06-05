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

            // Configura e avvia il server gRPC
            Server server = ServerBuilder.forPort(SERVER_PORT)
                    .addService(ticketService) // Aggiunge il servizio biglietti
                    .addService(trainService)  // Aggiunge il servizio treni
                    .addService(promotionService) // Aggiunge il servizio promozioni
                    .build();

            System.out.println("Il server gRPC è avviato sulla porta " + SERVER_PORT + "...");
            System.out.println("Servizi disponibili: TicketService, TrainService, PromotionService");

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

