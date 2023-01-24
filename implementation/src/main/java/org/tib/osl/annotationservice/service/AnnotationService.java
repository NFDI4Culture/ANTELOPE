package org.tib.osl.annotationservice.service;

<<<<<<< HEAD
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
=======
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

>>>>>>> 2e5545b139a65403936136a7357967bad22257fa
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
<<<<<<< HEAD
import org.tib.osl.annotationservice.web.api.AnnotationApiDelegate;
import org.tib.osl.annotationservice.web.api.StatusApiDelegate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
=======

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tib.osl.annotationservice.web.api.AnnotationApiDelegate;
import org.tib.osl.annotationservice.web.api.StatusApiDelegate;
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa


@Service
public class AnnotationService implements StatusApiDelegate, AnnotationApiDelegate {
<<<<<<< HEAD
=======
    private Logger log = LoggerFactory.getLogger(AnnotationService.class);
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return StatusApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("Service is running", HttpStatus.OK);
    }

    @Override
<<<<<<< HEAD
    public ResponseEntity<String> getWikiDataAnnotation(List<String> requestBody) {
       
        List<String> falconResults = new ArrayList<>();
        try {
            // init connection to falcon api
            for( String actText : requestBody) {
                String result = "";
                HttpPost post = new HttpPost(new URI("https://labs.tib.eu/falcon/falcon2/api?mode=long"));
                post.addHeader("content-type", "application/json");

                StringBuilder json = new StringBuilder();
                json.append("{");     
                json.append("\"text\":\""+actText+"\"");
                json.append("}");

                System.out.println(json.toString());
                // send a JSON data
                post.setEntity(new StringEntity(json.toString()));

                try (CloseableHttpClient httpClient = HttpClients.createDefault();
                    CloseableHttpResponse response = httpClient.execute(post)) {

                    result = EntityUtils.toString(response.getEntity());
                    falconResults.add(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request Falcon API", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        List<String> wikidataResults = new ArrayList<>();
        try {
            // get superclass triples from wikiData for each entity that falcon delivered (get class tree)
            for( String actFalconResult : falconResults) {
                JSONObject obj = new JSONObject(actFalconResult);
                JSONArray entities = obj.getJSONArray("entities_wikidata");
                for( Object actEntity : entities) {
                    //String entityLabel = ((JSONArray)actEntity).get(0).toString();
                    String entityUrl = ((JSONArray)actEntity).get(1).toString();
                    String[] entityUrlParts = entityUrl.replace(">", "").split("/");
                    String entityId = entityUrlParts[ entityUrlParts.length-1 ];
                    // init connection to wikiData api
                    String result = "";
                    String sparqlQuery = "SELECT ?class ?classLabel ?superclass ?superclassLabel WHERE { wd:"+entityId+" wdt:P31/wdt:P279 ?class. ?class wdt:P279 ?superclass. SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } } ";
                    HttpGet get = new HttpGet(new URI("https://query.wikidata.org/sparql?format=json&query="+URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8)+""));
                    //get.addHeader("content-type", "application/json");
                    System.out.println(sparqlQuery);
                
                    try (CloseableHttpClient httpClient = HttpClients.createDefault();
                        CloseableHttpResponse response = httpClient.execute(get)) {

                        result = EntityUtils.toString(response.getEntity());
                        System.out.println( result );
                        wikidataResults.add(result);
                    }
                }
            }     
=======
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
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request WikiData API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

<<<<<<< HEAD
        try {
            // combine falcon and Wikidata Results
            JSONObject finalResult = new JSONObject();

            // add all entities from falcon
            JSONArray entitiesArr = new JSONArray();
            for (String actFalconResult : falconResults){
                JSONObject actFalconJson = new JSONObject( actFalconResult );
                JSONArray falconEntities = actFalconJson.optJSONArray("entities_wikidata");
                entitiesArr.putAll( falconEntities );
            }
            finalResult.put("entities", entitiesArr);

            // add all relations from falcon
            JSONArray relationsArr = new JSONArray();
            for (String actFalconResult : falconResults){
                JSONObject actFalconJson = new JSONObject( actFalconResult );
                JSONArray falconRels = actFalconJson.optJSONArray("relations_wikidata");
                relationsArr.putAll( falconRels );
            }
            finalResult.put("relations", relationsArr);

            // add hierarchy from Wikidata
            JSONArray hierarchyArr = new JSONArray();
            for( String actWDResult : wikidataResults) {
                JSONObject actWDJson = new JSONObject( actWDResult );
                JSONArray wdHierarchy = actWDJson.getJSONObject("results").optJSONArray("bindings") ;
                hierarchyArr.putAll( wdHierarchy );
            }
            finalResult.put("hierarchy", hierarchyArr);


            return new ResponseEntity<String>( finalResult.toString(), HttpStatus.OK );
        } catch ( Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to build result data", HttpStatus.INTERNAL_SERVER_ERROR);
        }
            
        
    }

   

=======
        

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
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa
}


