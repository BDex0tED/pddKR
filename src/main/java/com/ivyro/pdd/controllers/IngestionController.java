package com.ivyro.pdd.controllers;

import com.ivyro.pdd.services.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {
    private final IngestionService ingestionService;

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestData(){
        ingestionService.ingestData();
        return ResponseEntity.ok("Ingestion started!");
    }
}
