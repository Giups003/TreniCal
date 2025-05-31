package it.unical.trenical.client;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import it.unical.trenical.grpc.ticket.OperationResponse;
import it.unical.trenical.grpc.ticket.PurchaseTicketResponse;

import java.util.Scanner;

/**
 * Classe principale per l'applicazione client.
 * Fornisce un'interfaccia a riga di comando per interagire con i servizi gRPC.
 */
public class ClientMain {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9090;

    private final TicketClient ticketClient;
    // private final TrainClient trainClient; // Sarà implementato in futuro

    /**
     * Costruttore che inizializza i client gRPC.
     *
     * @param host Indirizzo del server
     * @param port Porta del server
     */
    public ClientMain(String host, int port) {
        ticketClient = new TicketClient(host, port);
        // trainClient = new TrainClient(host, port); // Sarà implementato in futuro
    }

    /**
     * Chiude tutte le connessioni ai servizi gRPC.
     */
    public void shutdown() {
        try {
            ticketClient.shutdown();
            // trainClient.shutdown(); // Sarà implementato in futuro
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interruzione durante la chiusura dei client: " + e.getMessage());
        }
    }

    /**
     * Esegue l'interfaccia a riga di comando.
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== TreniCal - Client ===");

        while (running) {
            System.out.println("\nSeleziona un'operazione:");
            System.out.println("1. Acquista biglietto");
            System.out.println("2. Modifica biglietto");
            System.out.println("3. Cerca treni (non implementato)");
            System.out.println("4. Visualizza orari (non implementato)");
            System.out.println("0. Esci");

            System.out.print("\nScelta: ");
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        purchaseTicket(scanner);
                        break;
                    case "2":
                        modifyTicket(scanner);
                        break;
                    case "3":
                        System.out.println("Funzionalità non ancora implementata.");
                        break;
                    case "4":
                        System.out.println("Funzionalità non ancora implementata.");
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Scelta non valida. Riprova.");
                }
            } catch (StatusRuntimeException e) {
                System.err.println("Errore durante la comunicazione con il server: " + e.getStatus());
            } catch (Exception e) {
                System.err.println("Errore: " + e.getMessage());
            }
        }

        System.out.println("Arrivederci!");
        scanner.close();
    }

    /**
     * Gestisce l'acquisto di un biglietto.
     *
     * @param scanner Scanner per l'input utente
     */
    private void purchaseTicket(Scanner scanner) {
        System.out.println("\n=== Acquisto Biglietto ===");

        System.out.print("ID Treno: ");
        int trainId = scanner.nextInt();

        System.out.print("Nome Passeggero: ");
        String passengerName = scanner.nextLine();

        System.out.print("Stazione di Partenza: ");
        String departureStation = scanner.nextLine();

        System.out.print("Stazione di Arrivo: ");
        String arrivalStation = scanner.nextLine();

        System.out.print("Data di Viaggio (DD:MM:YYYY): ");
        String travelDateString = scanner.nextLine();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd:MM:yyyy");
        java.time.LocalDate localDate = java.time.LocalDate.parse(travelDateString, formatter);
        Timestamp travelDate = Timestamp.newBuilder()
                .setSeconds(localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond())
                .build();

        System.out.print("Classe di Servizio (Economy, Standard, Prima Classe, Business): ");
        String serviceClass = scanner.nextLine();

        System.out.print("Codice Promozionale (opzionale): ");
        String promoCode = scanner.nextLine();

        // Acquista il biglietto
        PurchaseTicketResponse response = ticketClient.purchaseTicket(
                trainId,
                passengerName,
                departureStation,
                arrivalStation,
                travelDate,
                serviceClass,
                promoCode.isEmpty() ? null : promoCode
        );

        // Mostra il risultato
        if (response.getSuccess()) {
            System.out.println("\nBiglietto acquistato con successo!");
            System.out.println("ID Biglietto: " + response.getTicketId());
            System.out.println("Prezzo: " + response.getPrice() + " €");

            // Mostra i dettagli del biglietto
            System.out.println("\nDettagli del biglietto:");
            System.out.println("Passeggero: " + response.getTicket().getPassengerName());
            System.out.println("Treno: " + response.getTicket().getTrainId());
            System.out.println("Da: " + response.getTicket().getDepartureStation());
            System.out.println("A: " + response.getTicket().getArrivalStation());
            System.out.println("Data: " + response.getTicket().getTravelDate());
            System.out.println("Classe: " + response.getTicket().getServiceClass());
        } else {
            System.out.println("\nAcquisto biglietto fallito: " + response.getMessage());
        }
    }

    /**
     * Gestisce la modifica di un biglietto.
     *
     * @param scanner Scanner per l'input utente
     */
    private void modifyTicket(Scanner scanner) {
        System.out.println("\n=== Modifica Biglietto ===");

        System.out.print("ID Biglietto: ");
        String ticketId = scanner.nextLine();

        System.out.print("Nuova Data (DD:MM:YYYY, lascia vuoto per non modificare): ");
        String newDateString = scanner.nextLine();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd:MM:yyyy");
        java.time.LocalDate localDate = java.time.LocalDate.parse(newDateString, formatter);
        Timestamp newDate = Timestamp.newBuilder()
                .setSeconds(localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond())
                .build();

        System.out.print("Nuovo Orario (HH:MM, lascia vuoto per non modificare): ");
        String newTimeString = scanner.nextLine();
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        Timestamp newTime = Timestamp.newBuilder()
                .setSeconds(java.time.LocalTime.parse(newTimeString, timeFormatter).toSecondOfDay())
                .build();

        System.out.print("Nuova Classe di Servizio (lascia vuoto per non modificare): ");
        String newServiceClass = scanner.nextLine();

// Modifica il biglietto
        OperationResponse response = ticketClient.modifyTicket(
                ticketId,
                newDate.getSeconds() != 0 ? newDate : null,
                newTime.getSeconds() != 0 ? newTime : null,
                newServiceClass.isEmpty() ? null : newServiceClass
        );

        // Mostra il risultato
        if (response.getSuccess()) {
            System.out.println("\nBiglietto modificato con successo!");
            System.out.println(response.getMessage());
        } else {
            System.out.println("\nModifica biglietto fallita: " + response.getMessage());
        }
    }

    /**
     * Metodo main che avvia l'applicazione client.
     *
     * @param args Argomenti da riga di comando (opzionali: host e porta)
     */
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        // Parsing degli argomenti da riga di comando
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Porta non valida. Utilizzo della porta predefinita: " + DEFAULT_PORT);
            }
        }

        // Crea e avvia il client
        ClientMain client = new ClientMain(host, port);
        try {
            client.run();
        } finally {
            client.shutdown();
        }
    }
}
