package org.tib.osl.annotationservice.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tib.osl.annotationservice.web.api.AnnotationApiDelegate;
import org.tib.osl.annotationservice.web.api.StatusApiDelegate;


@Service
public class AnnotationService implements StatusApiDelegate, AnnotationApiDelegate {
    private Logger log = LoggerFactory.getLogger(AnnotationService.class);

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return StatusApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("Service is running", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getWikiDataAnnotation(List<String> requestBody) {
        
        // get Entity Recognition from Falcon API
        List<String> falconResults;
        try {
            falconResults = EntityRecognition.getFalconResults(requestBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request Falcon API", HttpStatus.INTERNAL_SERVER_ERROR);
        }
       
        // init executorService for parallel fetching of results
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // get all superclasses for the falcon entities from wikidata
        Future<Map<String, String>> wikidataResultsByEntity = null;
        try {
            Callable<Map<String, String>> wikiDataCall = new Callable<>() {

                @Override
                public Map<String, String> call() throws Exception {
                    return EntityRecognition.getWikiDataClasses(falconResults);
                }
            };
            wikidataResultsByEntity = executor.submit(wikiDataCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request WikiData API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // get all superclasses for the falcon entities from dbpedia
        Future<Map<String, String>> dbpediaResultsByEntity = null;
        try {
            Callable<Map<String, String>> dbPediaCall = new Callable<>() {

                @Override
                public Map<String, String> call() throws Exception {
                    return EntityRecognition.getDbPediaClasses(falconResults);
                }
            };
            dbpediaResultsByEntity = executor.submit(dbPediaCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request DBPedia API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        while( !wikidataResultsByEntity.isDone() || !dbpediaResultsByEntity.isDone()) {
            log.debug("wait for completion");
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }

        // wait for Results to process until all results are complete
        /*try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                log.debug("Timeout in fetching results. force shutdown of executor");
            }
    
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }*/
        
        log.info("Results are fetched, now building category tree...");
        
        // build hierarchy
        JSONObject finalResult = null;
        //try {
            try {
                finalResult = EntityRecognition.combineResults(falconResults, wikidataResultsByEntity.get(), dbpediaResultsByEntity.get());
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to build category tree", HttpStatus.INTERNAL_SERVER_ERROR);
                
            }
        //} catch (Exception e) {
        //    e.printStackTrace();
        //    
        //}

        // return result
        return new ResponseEntity<String>( finalResult.toString(), HttpStatus.OK );
    }

    

    

    
   

}


