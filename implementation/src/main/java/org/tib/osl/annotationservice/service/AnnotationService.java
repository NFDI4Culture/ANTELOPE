package org.tib.osl.annotationservice.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tib.osl.annotationservice.service.api.dto.TextEntityLinkingRequest;
import org.tib.osl.annotationservice.service.api.dto.TextEntityLinkingRequest.DictionaryTypeEnum;
import org.tib.osl.annotationservice.web.api.AnnotationApiDelegate;

import iart.client.*;

import com.google.gson.Gson;  
import org.apache.commons.io.IOUtils;
import java.util.Base64;


@Service
public class AnnotationService implements AnnotationApiDelegate {
    @Autowired
    private SpringTemplateEngine templateEngine;
    private Logger log = LoggerFactory.getLogger(AnnotationService.class);
    public enum SearchMode {TERMINOLOGY_SEARCH, ENITTY_RECOGNITION};

   // @Override
   // public Optional<NativeWebRequest> getRequest() {
   //     return StatusApiDelegate.super.getRequest();
   // }

    @Override
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("Service is running", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getEntitiesSelectComponent(){
               
        String[] entitiesToSelect = new String[]{};

        try {
            /*ResponseEntity<String> entitySearchResult = getEntities(entitiesToSearch, null, null, null);
            JSONObject result = new JSONObject(entitySearchResult.getBody());
            JSONArray entitiesArr = result.getJSONArray("entities");
            List<String> entitiesList = entitiesArr.toList().stream().map(Object::toString).collect(java.util.stream.Collectors.toList());
            entitiesToSelect = entitiesList.toArray(new String[entitiesList.size()]);
            */
            entitiesToSelect = new String[]{};
        
            
            Locale locale = Locale.forLanguageTag("de");
            Context context = new Context(locale);
            context.setVariable("entities", entitiesToSelect);
            String content = templateEngine.process("annotationService-selectComponent", context);

            
            //String content = "test";
            return new ResponseEntity<>(content, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Interner Fehler", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getTerminology(
    String searchText, 
    Boolean wikidata,
    Boolean wikidataDbpedia,
    Boolean iconclass,
    Boolean ts4tib,
    Boolean lobidGnd,
    String ts4tib_collection,
    String ts4tib_ontology,
    Boolean allowDuplicates
    ) {
        List<String> requestBody = new ArrayList<String>();
        requestBody.add(searchText);
        return search(requestBody, SearchMode.TERMINOLOGY_SEARCH, wikidata, wikidataDbpedia, iconclass, ts4tib, ts4tib_ontology, lobidGnd, allowDuplicates);
    }

    @Override
    public ResponseEntity<String> getTextEntities(TextEntityLinkingRequest request, 
    Boolean wikidata,
    Boolean wikidataDbpedia,
    Boolean iconclass,
    Boolean ts4tib,
    String ts4tib_collection,
    String ts4tib_ontology,
    Boolean allowDuplicates
    ) {

        try {
        
            if( request.getListOfWords() == null && request.getSimpleDictionary() == null && request.getFullDictionary() == null) {
                request.setDictionaryType(DictionaryTypeEnum.FULLDICTIONARY);
                request.setFullDictionary( EntityRecognition.getIconclassDict());
            }
            //System.out.println(request.toString());
            JSONObject el_results = VecnerClient.callEntityLinking(request);
            
            return new ResponseEntity<String>( el_results.toString(), HttpStatus.OK );

        } catch ( Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getImageEntities(String model, MultipartFile image) {
        if( model == null) {
            model = "KaggleResnetClassifier";
        }
        try {
            JSONArray resultArr = new JSONArray();
            System.out.println(image);
            byte[] bytes = IOUtils.toByteArray(image.getInputStream());
            String base64imageString = Base64.getEncoder().encodeToString(bytes);
            List<iart.client.PluginResult> response = iArtClient.analyze(model, base64imageString);
            
            
            String responseString = new Gson().toJson(response);
            return new ResponseEntity<String>(responseString, HttpStatus.OK);

        } catch ( Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getParameterImageModels() {
        try {
            JSONArray resultArr = new JSONArray();
            
            List<PluginInfo> response = iArtClient.getPluginList();
            System.out.println(response);
            for (PluginInfo pluginInfo : response) {
                JSONObject o = new JSONObject();
                o.put("name", pluginInfo.getName());
                o.put("type", pluginInfo.getType()); 
                resultArr.put(o);
            }
            JSONObject resultObj = new JSONObject();
            resultObj.put("models", resultArr);
            String responseString = resultObj.toString(0);
            //String responseString = new Gson().toJson(response);

            //log.info(responseString);
            
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
            
        } catch ( Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getParameterTs4tibCollection () {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            JSONArray resultArr = new JSONArray();
            HttpGet request = new HttpGet(new URI("https://service.tib.eu/ts4tib/api/ontologies/schemavalues?schema=collection"));
            request.addHeader("content-type", "application/json");
            CloseableHttpResponse response = httpClient.execute(request);
            String ts4tibResponse = EntityUtils.toString(response.getEntity());
            JSONArray collectionObjs = new JSONObject( ts4tibResponse ).getJSONObject("_embedded").getJSONArray("strings");
            for( int i=0; i<collectionObjs.length(); i++) {
                JSONObject actCollectionObj = collectionObjs.getJSONObject(i);
                resultArr.put(actCollectionObj.getString("content"));
            }
            JSONObject result = new JSONObject();
            result.put("collections", resultArr);
            String responseString = result.toString(0);
            //log.info(responseString);
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
            
        } catch ( Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

   
    @Override
    public ResponseEntity<String> getParameterTs4tibOntology (String collection) {
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            JSONArray resultArr = new JSONArray();
            HttpGet request = new HttpGet(new URI("https://service.tib.eu/ts4tib/api/ontologies?size=1000"));
            request.addHeader("content-type", "application/json");
            CloseableHttpResponse response = httpClient.execute(request);
            if (response != null && response.getStatusLine() != null) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    return new ResponseEntity<String>("Tib terminology service is unavailable", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            String ts4tibResponse = EntityUtils.toString(response.getEntity());
            //log.debug(ts4tibResponse);
            JSONArray ontologyObjs = new JSONObject( ts4tibResponse ).optJSONObject("_embedded").optJSONArray("ontologies");
            for( int i=0; i<ontologyObjs.length(); i++) {
                JSONObject actOntologyObj = ontologyObjs.getJSONObject(i);
                String ontoId = actOntologyObj.optString("ontologyId");
                String ontoLabel = actOntologyObj.getJSONObject("config").optString("title");
                JSONObject ontoCollectionsObj = actOntologyObj.getJSONObject("config").getJSONArray("classifications").optJSONObject(0);
                JSONArray ontoCollections = new JSONArray();
                if( ontoCollectionsObj != null) {
                    ontoCollections = ontoCollectionsObj.optJSONArray("collection");
                }
                JSONObject resultOntoObj = new JSONObject();
                resultOntoObj.put("paramValue", ontoId);
                resultOntoObj.put("label", ontoLabel);
                resultOntoObj.put("collections", ontoCollections);
                resultArr.put(resultOntoObj);
            }
            JSONObject result = new JSONObject();
            result.put("ontologies", resultArr);
            String responseString = result.toString(0);
            //log.info(responseString);
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
            
        } catch ( Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            
        }
    }

    public ResponseEntity<String> search(
    List<String> requestBody, 
    SearchMode searchMode,
    Boolean wikidata,
    Boolean wikidataDbpedia,
    Boolean iconclass,
    Boolean ts4tib, 
    String ts4tibOntology,
    Boolean lobidGnd,
    boolean allowDuplicates
    ) {
        
        // decide which datasources to use
        // if no parameter is given, all datasources are used
        boolean useAllSources = true;
        
        if (wikidata != null || wikidataDbpedia != null || iconclass != null || ts4tib != null || lobidGnd != null) {
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
                falconResults = EntityRecognition.getFalconResults(requestBody, useDbpedia, searchMode);
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

       // get Entities from tib terminology service 
       List<String> olsResults;
       if( useAllSources || (ts4tib != null && ts4tib) ){
           try {
            olsResults = EntityRecognition.getTs4TibResults(requestBody, ts4tibOntology);
               System.out.println( "Tib Terminology service (TS4TIB) Results:"+olsResults );
           } catch (Exception e) {
               e.printStackTrace();
               return new ResponseEntity<>("Failed to request tib terminology service (TS4TIB) API", HttpStatus.INTERNAL_SERVER_ERROR);
           }
       } else {
        olsResults = new ArrayList<>();
       }

       // Get Entities from Lobid GND
        List<String> gndResults;
        if (useAllSources || (lobidGnd != null && lobidGnd)) {
            try {
                gndResults = EntityRecognition.getLobidGndResults(requestBody);
                //System.out.println("Lobid GND Results: " + gndResults);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to request Lobid GND API", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            gndResults = new ArrayList<>();
        }

        // init executorService for parallel fetching of results
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // init result containers for superclasses of the falcon entities
        Future<Map<String, String>> wikidataResultsByEntity = null;
        Future<Map<String, String>> dbpediaResultsByEntity = null;
        Future<Map<String, String>> iconclassResultsByNotation = null;
        Future<Map<String, String>> olsResultsByEntity = null;
        Future<Map<String, String>> gndResultsByEntity = null;

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

        // get all superclasses for the ols entities from ols
           try {
            Callable<Map<String, String>> olsCall = new Callable<>() {

                @Override
                public Map<String, String> call() throws Exception {
                    //return null;
                   return EntityRecognition.getOlsSuperClasses(olsResults);
                }
            };
            olsResultsByEntity = executor.submit(olsCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request ols API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // get all superclasses for the gnd entities from lobid
           try {
            Callable<Map<String, String>> lobidGndCall = new Callable<>() {

                @Override
                public Map<String, String> call() throws Exception {
                    //return null;
                   return EntityRecognition.getLobidGndSuperClasses(gndResults);
                }
            };
            gndResultsByEntity = executor.submit(lobidGndCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request lobid gnd API", HttpStatus.INTERNAL_SERVER_ERROR);
        }


        while( 
            !wikidataResultsByEntity.isDone() 
        || !dbpediaResultsByEntity.isDone() 
        || !iconclassResultsByNotation.isDone()
        || !olsResultsByEntity.isDone()
        || !gndResultsByEntity.isDone()) {
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
            finalResult = EntityRecognition.combineResults(
                falconResults, 
                wikidataResultsByEntity.get(), 
                dbpediaResultsByEntity.get(), 
                iconclassNotations, 
                iconclassResultsByNotation.get(),
                olsResults,
                olsResultsByEntity.get(),
                gndResults,
                gndResultsByEntity.get(),
                allowDuplicates
                );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to build category tree", HttpStatus.INTERNAL_SERVER_ERROR);
            
        }
        
        // return result
        return new ResponseEntity<String>( finalResult.toString(), HttpStatus.OK );
    }

 




}