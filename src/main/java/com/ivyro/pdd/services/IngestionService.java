package com.ivyro.pdd.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private final VectorStore vectorStore;
    private final TextParserService parser;

    @Value("classpath:/pdd/pdd.docx")
    private Resource pddDocx;

    @Value("classpath:/pdd/pdd.txt")
    private Resource pddTxt;


    public void ingestDataDocx() {
        try{
            log.info("Starting ingestion...");
            var pdfReader = new TikaDocumentReader(pddDocx);
            List<Document> rawDocuments = pdfReader.get();
            for (Document doc : rawDocuments) {
                List<Document> chunkedDocuments = parser.parse(doc);

                if (!chunkedDocuments.isEmpty()) {
                    vectorStore.accept(chunkedDocuments);
                    log.info("Saved {} chunks to VectorStore.", chunkedDocuments.size());
                }
            }
            log.info("Ingestion completed successfully");
        }catch (Exception e){
            log.error("Ingestion failed: ", e);
        }
        log.info("VectorStore was filled with pddPdf data!");
    }

    public void ingestDataTxt() {
        try{
            log.info("Starting TXT ingestion...");
            var pdfReader = new TikaDocumentReader(pddTxt);
            List<Document> rawDocuments = pdfReader.get();
            for (Document doc : rawDocuments) {
                List<Document> chunkedDocuments = parser.parse(doc);

                if (!chunkedDocuments.isEmpty()) {
                    vectorStore.accept(chunkedDocuments);
                    log.info("Saved {} chunks to VectorStore.", chunkedDocuments.size());
                }
            }
            log.info("TXT Ingestion completed successfully");
        }catch (Exception e){
            log.error("TXT Ingestion failed: ", e);
        }
        log.info("VectorStore was filled with pddTXT data!");
    }

}
