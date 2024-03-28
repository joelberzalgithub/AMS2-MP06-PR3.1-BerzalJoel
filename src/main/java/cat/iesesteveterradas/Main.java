package cat.iesesteveterradas;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.basex.api.client.ClientSession;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Directori d'entrada (a on es troben els arxius XQuery)
        File inputDir = new File("./data/input");

        // Directori de sortida
        File outputDir = new File("./data/output");

        // Creem el directori de sortida en cas que no existeixi
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    
        // Llistem tots els arxius XQuery dins del directori
        File[] queryFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xquery"));

        // Ens connectem al BaseX Server
        try (ClientSession session = new ClientSession("127.0.0.1", 1984, "admin", "admin")) {
            logger.info("Connectat al BaseX Server.");
            session.execute(new Open("factbook"));
            
            // Recorrem cadascun dels arxius XQuery
            for (File queryFile : queryFiles) {
                // Executem la consulta contra el BaseX Server
                runXQuery(session, queryFile, outputDir);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static void runXQuery(ClientSession session, File queryFile, File outputDir) {
        String queryName = queryFile.getName().replaceFirst("[.][^.]+$", "");
        File outputFile = new File(outputDir, queryName + ".xml");

        try {
            // Llegim el contingut de l'arxiu XQuery
            String query = readFile(queryFile);

            // Executem la consulta contra el BaseX Server
            String result = session.execute(new XQuery(query));

            // Imprimim el resultat per pantalla
            logger.info("Resultat de la Query:");
            logger.info(result);

            // Desem el resultat en un arxiu XML
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(result);
                System.out.println("Resultat de " + queryName + " desat a " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Error en desar el resultat de " + queryName + ": " + e.getMessage());
            }
        } catch (IOException e) {
            logger.error("Error en executar la consulta de " + queryName + ": " + e.getMessage());
        }
    }

    public static String readFile(File file) {
        StringBuilder content = new StringBuilder();
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                content.append(sc.nextLine()).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
