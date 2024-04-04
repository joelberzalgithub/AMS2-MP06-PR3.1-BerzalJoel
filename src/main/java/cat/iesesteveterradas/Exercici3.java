package cat.iesesteveterradas;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.basex.api.client.ClientSession;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Exercici3 {
    private static final Logger logger = LoggerFactory.getLogger(Exercici3.class);
    private static final String OUTPUT_FILE = "./data/noms_propis.txt";

    public static void main(String[] args) throws Exception {
        String[] names = getNames();

        String text = String.join(" ", names);

        String basePath = System.getProperty("user.dir") + "/data/models/";


        // Updated paths for the provided model files
        InputStream modelInSentence = new FileInputStream(basePath + "opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
        InputStream modelInToken = new FileInputStream(basePath + "opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin");
        InputStream modelInPOS = new FileInputStream(basePath + "en-pos-maxent.bin");
        InputStream modelInPerson = new FileInputStream(basePath + "en-ner-person.bin");

        // Sentence detection
        SentenceModel modelSentence = new SentenceModel(modelInSentence);
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(modelSentence);
        String[] sentences = sentenceDetector.sentDetect(text);
        logger.info("Sentence Detection:");
        Arrays.stream(sentences).forEach(sentence -> logger.info(sentence));

        // Tokenization
        TokenizerModel modelToken = new TokenizerModel(modelInToken);
        TokenizerME tokenizer = new TokenizerME(modelToken);
        logger.info("\nTokenization and POS Tagging:");
        for (String sentence : sentences) {
            try {
                String[] tokens = tokenizer.tokenize(sentence);

                // POS Tagging
                POSModel modelPOS = new POSModel(modelInPOS);
                POSTaggerME posTagger = new POSTaggerME(modelPOS);
                String[] tags = posTagger.tag(tokens);

                for (int i = 0; i < tokens.length; i++) {
                    logger.info(tokens[i] + " (" + tags[i] + ")");
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        TokenNameFinderModel modelPerson = new TokenNameFinderModel(modelInPerson);
        NameFinderME nameFinder = new NameFinderME(modelPerson);
        logger.info("\nNamed Entity Recognition:");

        List<String> namedEntities = new ArrayList<>();

        for (String sentence : sentences) {
            String[] tokens = tokenizer.tokenize(sentence);
            opennlp.tools.util.Span[] nameSpans = nameFinder.find(tokens);
            for (opennlp.tools.util.Span s : nameSpans) {
                logger.info("Entity: " + tokens[s.getStart()]);
            }
        }

        // Clean up IO resources
        modelInSentence.close();
        modelInToken.close();
        modelInPOS.close();
        modelInPerson.close();

        // Inicialitza Stanford CoreNLP
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Crea un document amb el text
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        // Obté les frases del document
        List<CoreMap> sentencesList2 = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentencesList2) {
            // Mostra tokens i etiquetes POS de cada frase
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                logger.info(word + " (" + pos + ")");
            }

            // Mostra el reconeixement d'entitats anomenades
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                logger.info("Entity: " + word + " (" + ne + ")");
            }


            String currentEntityType = "";
            String fullEntityWord = "";
            // Reconeixement de Named Entity Recognition (NER)
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.getString(CoreAnnotations.TextAnnotation.class);
                String ner = token.getString(CoreAnnotations.NamedEntityTagAnnotation.class);

                // Comprova si el token és una entitat anomenada (NER)

                if (!"O".equals(ner)) {
                    logger.info("Entity Detected: " + word + " - Entity Type: " + ner);

                    if (currentEntityType.equals(ner)) {
                        fullEntityWord += " " + word;
                    } else {
                        if (!fullEntityWord.isEmpty()) {
                            namedEntities.add(fullEntityWord + " - Entity Type: " + currentEntityType +"\n");
                        }
                        currentEntityType = ner;
                        fullEntityWord = word;
                    }
                } else {
                    if (!fullEntityWord.isEmpty()) {
                        namedEntities.add(fullEntityWord + " - Entity Type: " + currentEntityType +"\n");
                        fullEntityWord = "";
                    }
                }
            }

            if (!fullEntityWord.isEmpty()) {
                namedEntities.add(fullEntityWord + " - Entity Type: " + currentEntityType +"\n");
            }

            writeNamedEntitiesToFile(OUTPUT_FILE, namedEntities);

            // Anàlisi de sentiments
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            logger.info("Sentiment: " + sentiment);
        }
    }

    public static String[] getNames() throws IOException {
        final String host = "127.0.0.1";
        final int port = 1984;
        final String username = "admin";
        final String password = "admin";

        String[] splitResult;

        try (ClientSession session = new ClientSession(host, port, username, password)) {
            logger.info("Connected to BaseX server.");
            session.execute(new Open("boardgames"));

            String query = "declare option output:method 'text';\n" +
                    "declare option output:indent 'no';\n" +
                    "\n" +
                    "let $result := \n" +
                    "  for $question in /posts/row\n" +
                    "  let $views := xs:integer($question/@ViewCount)\n" +
                    "  where $question/@PostTypeId = 1\n" +
                    "  order by $views descending\n" +
                    "  return string-join((data($question/@Title), data($question/@Body)))\n" +
                    "return subsequence($result, 1, 5)";

            String result = session.execute(new XQuery(query));
            splitResult = result.split("\n");
        }

        return splitResult;
    }

    private static void writeNamedEntitiesToFile(String filename, List<String> namedEntities) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String entity : namedEntities) {
                writer.write(entity);
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("Error writing named entities to file: " + e.getMessage());
        }
    }

}
