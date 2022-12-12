package org.tib.osl.annotationservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
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
    public ResponseEntity<String> getEntities(List<String> requestBody, 
    Boolean wikidata,
    Boolean wikidataDbpedia,
    Boolean iconclass) {
        
        // decide which datasources to use
        // if no parameter is given, all datasources are used
        boolean useAllSources = true;

        if( wikidata != null || wikidataDbpedia != null || iconclass != null) {
            useAllSources = false;
        }



        // get Entity Recognition from Falcon API
        List<String> falconResults;
        if( useAllSources || (wikidata != null && wikidata) || (wikidataDbpedia != null && wikidataDbpedia)) {
            boolean useDbpedia = false;
            if( useAllSources || (wikidataDbpedia != null && wikidataDbpedia == true)) {
                useDbpedia = true;
            }
            try {
                falconResults = EntityRecognition.getFalconResults(requestBody, useDbpedia);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to request Falcon API", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            falconResults = new ArrayList<>();
        }

        // get Entities from Iconclass
        List<String> iconclassNotations;
        if( useAllSources || (iconclass != null && iconclass) ){
            try {
                iconclassNotations = EntityRecognition.getIconclassNotations(requestBody);
                System.out.println( "iconClassResults:"+iconclassNotations );
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to request Iconclass API", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            iconclassNotations = new ArrayList<>();
        }

       
        // init executorService for parallel fetching of results
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // init result containers for superclasses of the falcon entities
        Future<Map<String, String>> wikidataResultsByEntity = null;
        Future<Map<String, String>> dbpediaResultsByEntity = null;
        Future<Map<String, String>> iconclassResultsByNotation = null;

        // get all superclasses for the falcon entities from wikidata
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

        // get all superclasses for the iconclass entities from iconclass
        try {
            Callable<Map<String, String>> iconclassCall = new Callable<>() {

                @Override
                public Map<String, String> call() throws Exception {
                    //return null;
                   return EntityRecognition.geIconclassSuperClasses(iconclassNotations);
                }
            };
            iconclassResultsByNotation = executor.submit(iconclassCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request Iconclass API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        while( !wikidataResultsByEntity.isDone() || !dbpediaResultsByEntity.isDone() || !iconclassResultsByNotation.isDone()) {
            log.debug("wait for completion");
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        log.info("Results are fetched, now building category tree...");
        
        // build hierarchy
        JSONObject finalResult = null;
        try {
            finalResult = EntityRecognition.combineResults(falconResults, wikidataResultsByEntity.get(), dbpediaResultsByEntity.get(), iconclassNotations, iconclassResultsByNotation.get());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to build category tree", HttpStatus.INTERNAL_SERVER_ERROR);
            
        }
        
        // return result
        return new ResponseEntity<String>( finalResult.toString(), HttpStatus.OK );
    }
}


