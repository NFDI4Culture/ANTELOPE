package org.tib.osl.annotationservice.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tib.osl.annotationservice.service.AnnotationService.SearchMode;
import org.tib.osl.annotationservice.service.api.dto.FullDictionaryValue;


public class EntityRecognition {
    private static Logger log = LoggerFactory.getLogger(EntityRecognition.class);

    /**
     * connects to the Falcon web API and retrieve results for every text in requestText
     * @param requestText list of texts, used for entity recognition with falcon
     * @return falcon specific json result containing result arrays under the keys "entities_wikidata" and "relations_wikidata"
     * @throws Exception
     */
    protected static List<String> getFalconResults( List<String> requestText, boolean useDbpedia, SearchMode searchMode) throws Exception {
        List<String> falconResults = new ArrayList<>();
        String mode = "long";
        if( searchMode.equals(SearchMode.TERMINOLOGY_SEARCH)) {
            mode = "short";
        } else if ( searchMode.equals(SearchMode.ENITTY_RECOGNITION)) {
            mode = "long";
        }
        // init connection to falcon api
        for( String actText : requestText) {
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(actText); 

            actText = StandardCharsets.ISO_8859_1.decode(buffer).toString();


            String resultStr = "";
            String url = "https://labs.tib.eu/falcon/falcon2/api?mode="+mode+"&k=10";
            if( useDbpedia ){
                url += "&db=1";
            }
            HttpPost post = new HttpPost(new URI(url));
            post.addHeader("content-type", "application/json; charset=UTF-8");

            // escape spccial chars in request body
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("text", actText);
            String payload = jsonObject.toString();

            log.debug(payload);
            // send a JSON data
            post.setEntity(new StringEntity(payload, "UTF-8"));
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {

                resultStr = EntityUtils.toString(response.getEntity());
                
                log.debug( resultStr.toString() );
                JSONObject resultJson = new JSONObject( resultStr );  
                JSONObject normalizedResultJson = new JSONObject();
                String[] resultArrKeys = null;
                if( useDbpedia ) {
                    resultArrKeys = new String[]{"entities_wikidata", "entities_dbpedia"}; 
                } else {
                    resultArrKeys = new String[]{"entities_wikidata"}; 
                }
                for( String actResultArrKey : resultArrKeys) {
                    if( !resultJson.has(actResultArrKey)) {
                        continue;
                    }
                    JSONArray entities = resultJson.getJSONArray(actResultArrKey);
                    JSONArray normalizedEntities = new JSONArray();
                    java.util.Iterator<Object> iterator = entities.iterator();
                    while (iterator.hasNext()) {
                        JSONObject actEntity = (JSONObject)iterator.next();
                        String[]uriParts = actEntity.getString("URI").split("/");
                        String id = uriParts[ uriParts.length -1 ];
                        JSONObject normalizedEntity = new JSONObject();
                        normalizedEntity.put("label", actEntity.getString("surface form"));
                        normalizedEntity.put("id", id);
                        normalizedEntity.put("URI", actEntity.getString("URI"));
                        normalizedEntity.put("source", actResultArrKey.split("_")[1]);
                        normalizedEntities.put(normalizedEntity);
                    }
                    normalizedResultJson.put(actResultArrKey, normalizedEntities);
                }

                falconResults.add(normalizedResultJson.toString());
            }
        }
        System.out.println("falconResult:"+falconResults);
        return falconResults;
    }

    protected static List<String> getIconclassNotations( List<String> requestText) throws Exception {
        List<String> iconclassResults = new ArrayList<>();

        // init connection to falcon api
        for( String actText : requestText) {
            String result = "";
            // see https://iconclass.org/docs#/default/api_search_api_search_get
            HttpGet request = new HttpGet(new URI("https://iconclass.org/api/search?q="+URLEncoder.encode(actText, StandardCharsets.UTF_8)+"&lang=en&size=10&page=1&sort=rank&keys=1"));
            request.addHeader("content-type", "application/json");
            
             // init json objects to fill with results
            JSONObject jsonResult = new JSONObject();
            JSONArray arr = new JSONArray();
            jsonResult.put("notations_iconclass", arr);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(request)) {

                result = EntityUtils.toString(response.getEntity());
                log.debug( result.toString() );
                JSONObject actJsonResult = new JSONObject( result );
                JSONArray resultArr = actJsonResult.getJSONArray("result"); 
                
                // for each respondet iconclass notation for the act requestText: fetch details and combine all the results in a json object
                for( Object notationObj : resultArr){
                    String actNotationName = notationObj.toString();
                    
                    // fetch detail of act notation
                    String host = "https://iconclass.org/";
                    String fileName =actNotationName.replace(" ", "%28")+".json";
                    String fileUri = host+fileName;
                    String pageUri = host+actNotationName;
                    log.debug("fetch: "+fileUri);
                    //BufferedInputStream in = new BufferedInputStream(new URL( fileUri ).openStream());
                    // parse json response
                    //JSONTokener tokener = new JSONTokener(in);
                    //JSONObject notationJson = new JSONObject(tokener);

                    JSONObject notationJson = null;
                    URL url = new URL( fileUri );
                    try (InputStream input = url.openStream()) {
                        InputStreamReader isr = new InputStreamReader(input);
                        BufferedReader reader = new BufferedReader(isr);
                        StringBuilder json = new StringBuilder();
                        int c;
                        while ((c = reader.read()) != -1) {
                            json.append((char) c);
                        }
                        String jsonStr = json.toString();
                        log.debug(url.toString()+" result= "+jsonStr);
                        notationJson = new JSONObject(jsonStr);
                    }

                    // embedd respone data into falcon json format
                    JSONObject obj = new JSONObject();
                    obj.put("id", actNotationName);
                    obj.put("URI", pageUri);
                    String name = notationJson.getJSONObject("txt").getString("en");
                    obj.put("label", name);
                    obj.put("source", "iconclass");
                    // add json object to result json array
                    arr.put(obj);
                    
                }
            }
            iconclassResults.add( jsonResult.toString() );
        }
        
        System.out.println("iconclassResults:"+iconclassResults);
        return iconclassResults;
    }

/**
     * connects to the tib terminology service web API and retrieve results for every text in requestText
     * @param requestText list of texts, used for entity recognition 
     * @return 
     * @throws Exception
     */
    protected static List<String> getTs4TibResults( List<String> requestText, String ontologyList) throws Exception {
        List<String> results = new ArrayList<>();
        String baseUrl = "https://service.tib.eu/ts4tib/api/";
        
        // init connection 
        for( String actText : requestText) {
            String result = "";
            String encodedText = URLEncoder.encode(actText, StandardCharsets.UTF_8);
            String ontologies = "";
            if( ontologyList != null ) {
                ontologies = "&ontology="+URLEncoder.encode(ontologyList, StandardCharsets.UTF_8);;
            }
            
            HttpGet request = new HttpGet(new URI( baseUrl + "search?q=" + encodedText + "&obsoletes=false&local=false&rows=50&format=json"+ontologies));
            request.addHeader("content-type", "application/json");
            
                // init json objects to fill with results
            JSONObject jsonResult = new JSONObject();
            JSONArray arr = new JSONArray();
            jsonResult.put("entities_ols", arr);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(request)) {

                result = EntityUtils.toString(response.getEntity());
                //log.debug( result.toString() );
                JSONObject actJsonResult = new JSONObject( result );
                JSONArray resultArr = actJsonResult.getJSONObject("response").getJSONArray("docs"); 
                
                // for each responded entity for the act requestText: fetch details and combine all the results in a json object
                for( int i=0; i<resultArr.length(); i++) {
                    JSONObject actEntityObj = (JSONObject)resultArr.get(i);
                    String actEntityId = actEntityObj.optString("obo_id");
                    String actEntityType = actEntityObj.getString("type");
                    String actEntityIRI = actEntityObj.getString("iri");
                    String actEntityName = actEntityObj.getString("label");
                    String actOntology = actEntityObj.getString("ontology_prefix");

                    // embedd respone data into falcon json format
                    JSONObject obj = new JSONObject();
                    obj.put("id", actEntityId);
                    obj.put("URI", actEntityIRI);
                    obj.put("label", actEntityName);
                    obj.put("source", actOntology);
                    obj.put("type", actEntityType);
                    // add json object to result json array
                    arr.put(obj);
                    
                }
            }
            results.add( jsonResult.toString() );
            
        }
        return results;
    }

    protected static List<String> getLobidGndResults(List<String> requestText) throws Exception {
    List<String> results = new ArrayList<>();
    String baseUrl = "https://lobid.org/gnd/search?q=preferredName:";

    for (String actText : requestText) {
        String result = "";

        // Encode the search text
        String encodedText = URLEncoder.encode(actText, StandardCharsets.UTF_8);

        // Create the API request URL
        String requestUrl = baseUrl + encodedText + "%20OR%20variantName:" + encodedText + "%20OR%20preferredNameForTheSubjectHeading:"+ encodedText +"&format=json&size=50";

        HttpGet request = new HttpGet(new URI(requestUrl));
        request.addHeader("content-type", "application/json");

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            result = EntityUtils.toString(response.getEntity());
            JSONObject jsonResult = new JSONObject(result);
            JSONArray entitiesArray = jsonResult.getJSONArray("member");

            // Extract the required fields from the response
            JSONArray extractedEntities = new JSONArray();
            for (int i = 0; i < entitiesArray.length(); i++) {
                JSONObject actEntityObj = entitiesArray.getJSONObject(i);
                //System.out.println( actEntityObj.toString() );
                String actEntityId = actEntityObj.getString("gndIdentifier");
                String actEntityUri = actEntityObj.getString("id");
                //String actEntityType = actEntityObj.getString("type");
                String actEntityName = actEntityObj.getString("preferredName");


                JSONObject extractedEntity = new JSONObject();
                extractedEntity.put("id", actEntityId);
                extractedEntity.put("URI", actEntityUri);
                extractedEntity.put("label", actEntityName);
                extractedEntity.put("name", actEntityName);
                extractedEntity.put("obj", actEntityObj);

                extractedEntities.put(extractedEntity);
            }

            results.add(extractedEntities.toString());
        }
    }
    return results;
}

    /**
     * connecting the wikidata sparql web api. get all superclasses for every recognized entity within the falconResult parameter List
     * @param falconResults Every element contains a json object in falcon specific format. one json object per requested entity
     * @return map of wikidata results, grouped by entity name in wikidata specific json format
     * @throws Exception
     */
    public static Map<String, String> getWikiDataClasses( List<String> falconResults ) throws Exception {
        Map<String, String> wikidataResultsByEntity = new HashMap<>();
        List<JSONArray> falconResultsToProcess = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<Future<?>>();
        try {
            // get superclass triples from wikiData for each entity that falcon delivered (get class tree)
            for( String actFalconResult : falconResults) {
                JSONObject obj = new JSONObject(actFalconResult);
                if( obj.has("entities_wikidata")) {
                    JSONArray entities = obj.getJSONArray("entities_wikidata");
                    falconResultsToProcess.add(entities);
                }
                if( obj.has("relations_wikidata")) {
                    JSONArray relations = obj.getJSONArray("relations_wikidata");
                    falconResultsToProcess.add(relations);
                }
                for( JSONArray objects : falconResultsToProcess) {
                    for( Object actObject : objects) {
                        HierarchyFetcherWikiData fetcher = new HierarchyFetcherWikiData(wikidataResultsByEntity, (JSONObject)actObject);
                        Future<?> actFuture = executorService.submit(fetcher);
                        futures.add(actFuture);    
                    }
                } 
            } 
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println( "error in getWikiDataClasses:" + e.getMessage() );
        }    

        // wait for http requests to terminate
        while( !futures.stream().allMatch(f -> f.isDone())) {
            log.debug("wait for completion of wikidata fetching");
            Thread.sleep(200);
        }
        executorService.shutdown();

        return wikidataResultsByEntity;
    }

     /**
     * connecting the wikidata sparql web api. get all superclasses for every recognized entity within the falconResult parameter List
     * @param falconResults Every element contains a json object in falcon specific format. one json object per requested entity
     * @return map of wikidata results, grouped by entity name in wikidata specific json format
     * @throws Exception
     */
    public static Map<String, String> getDbPediaClasses( List<String> falconResults ) throws Exception {
        Map<String, String> dbpediaResultsByEntity = new HashMap<>();
        List<JSONArray> falconResultsToProcess = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<Future<?>>();
        try {
            // get superclass triples from wikiData for each entity that falcon delivered (get class tree)
            for( String actFalconResult : falconResults) {
                JSONObject obj = new JSONObject(actFalconResult);
                if( obj.has("entities_dbpedia")) {
                    JSONArray entities = obj.getJSONArray("entities_dbpedia");
                    falconResultsToProcess.add(entities);
                }
                if( obj.has("relations_dbpedia")) {
                    JSONArray relations = obj.getJSONArray("relations_dbpedia");
                    falconResultsToProcess.add(relations);
                }
                for( JSONArray objects : falconResultsToProcess) {
                    for( Object actObject : objects) {
                        HierarchyFetcherDBpedia fetcher = new HierarchyFetcherDBpedia(dbpediaResultsByEntity, (JSONObject)actObject);
                        Future<?> actFuture = executorService.submit(fetcher);
                        futures.add(actFuture);
                    }
                } 
            } 
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println( "error in getDbPediaClasses:" + e.getMessage() );
        }
       
         // wait for http requests to terminate
         while( !futures.stream().allMatch(f -> f.isDone())) {
            log.debug("wait for completion of dbpedia fetching");
            Thread.sleep(200);
        }
        executorService.shutdown();
        
        return dbpediaResultsByEntity;
    }

     /**
     * connecting the Iconclass web api. get all superclasses for every recognized entity within the Result parameter List
     * @param iconclassResults Every element contains a json object in iconclass specific format. one json object per requested entity
     * @return map of iconclass results, grouped by entity name in iconclass specific json format
     * @throws Exception
     */
    
     public static Map<String, String> geIconclassSuperClasses( List<String> iconclassResults ) throws Exception {
        Map<String, String> resultsByEntity = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<Future<?>>();
        try {
            // get superclass triples from iconclass for each notation that was given (get class tree)
            for( String actResults : iconclassResults) {
                JSONObject obj = new JSONObject(actResults);
                JSONArray notations = obj.getJSONArray("notations_iconclass");
                for( Object actNotation : notations) {
                    HierarchyFetcherIconclass fetcher = new HierarchyFetcherIconclass(resultsByEntity, (JSONObject)actNotation);
                    Future<?> actFuture = executorService.submit(fetcher);
                    futures.add(actFuture);
                }
            } 
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println( "error in geIconclassSuperClasses:" + e.getMessage() );
        }
       
         // wait for http requests to terminate
         while( !futures.stream().allMatch(f -> f.isDone())) {
            log.debug("wait for completion of iconclass fetching");
            Thread.sleep(200);
        }
        executorService.shutdown();
        
        return resultsByEntity;
    }

    public static Map<String, String> getLobidGndSuperClasses(List<String> lobidGndResults) throws Exception {
    Map<String, String> resultsByEntity = new ConcurrentHashMap<>();
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    List<Future<?>> futures = new ArrayList<Future<?>>();

    try {
        // Get superclass triples from Lobid-GND for each entity in the results
        for (String actResults : lobidGndResults) {
            JSONArray entities = new JSONArray(actResults);
            for( Object actEntity : entities) {
                HierarchyFetcherLobidGND fetcher = new HierarchyFetcherLobidGND(resultsByEntity, (JSONObject)actEntity);
                Future<?> actFuture = executorService.submit(fetcher);
                futures.add(actFuture);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Error in getLobidGndSuperClasses: " + e.getMessage());
    }

    // Wait for HTTP requests to terminate
    while (!futures.stream().allMatch(f -> f.isDone())) {
        log.debug("Waiting for completion of Lobid-GND fetching");
        Thread.sleep(200);
    }

    executorService.shutdown();

    return resultsByEntity;
}

         /**
     * connecting the osl web api. get all superclasses for every recognized entity within the Result parameter List
     * @param results Every element contains a json object in ols specific format. one json object per requested entity
     * @return map of ols results, grouped by entity name in ols specific json format
     * @throws Exception
     */
    
     public static Map<String, String> getOlsSuperClasses( List<String> olsResults ) throws Exception {
        Map<String, String> resultsByEntity = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<Future<?>>();
        try {
            // get superclass triples from iconclass for each notation that was given (get class tree)
            for( String actResults : olsResults) {
                JSONObject obj = new JSONObject(actResults);
                JSONArray entities = obj.getJSONArray("entities_ols");
                for( Object actEntity : entities) {
                    HierarchyFetcherOLS fetcher = new HierarchyFetcherOLS(resultsByEntity, (JSONObject)actEntity);
                    Future<?> actFuture = executorService.submit(fetcher);
                    futures.add(actFuture);
                }
            } 
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println( "error in getOLSSuperClasses:" + e.getMessage() );
        }
       
         // wait for http requests to terminate
         while( !futures.stream().allMatch(f -> f.isDone())) {
            log.debug("wait for completion of ols fetching");
            Thread.sleep(200);
        }
        executorService.shutdown();
        
        return resultsByEntity;
    }




    /**
     * combine results from falcon and its sources and build a category tree datastructure. 
     * @param falconResults
     * @param wikidataResultsByEntity 
     * @param dbpediaResultsByEntity 
     * @return jsonobject with the keys "entities", "relations" and "hierarchy"
     * @throws Exception
     */
    public static JSONObject combineResults(
            List<String> falconResults, 
            Map<String,String> wikidataResultsByEntity, 
            Map<String,String> dbpediaResultsByEntity,
            List<String> iconclassResults,
            Map<String,String> iconclassResultsByEntity,
            List<String> olsResults,
            Map<String,String> olsResultsByEntity,
            List<String> gndResults,
            Map<String,String> gndResultsByEntity,
            boolean allowDuplicates
            )throws Exception {
    
       //wikidataResultsByEntity.putAll(dbpediaResultsByEntity);

        // combine falcon and Wikidata Results
        JSONObject finalResult = new JSONObject();

        // ========== section entities =============

        //log.debug( wikidataResultsByEntity.toString() );
        // add all entities from falcon
        JSONArray entitiesArr = new JSONArray();
        for (String actFalconResult : falconResults){
            JSONObject actFalconJson = new JSONObject( actFalconResult );
            JSONArray falconEntities = new JSONArray();
            
            String[] keysToProcess = new String[] {"entities_wikidata", "entities_dbpedia"};
            for( String actKey : keysToProcess) { 
                // falcon doesnt return the entity label correctly, (only the search term). get the label from own query results
                Map<String,String> actResultsByEntity = wikidataResultsByEntity;
                if( actKey == "entities_dbpedia"){
                    actResultsByEntity = dbpediaResultsByEntity;
                }
                if( actFalconJson.optJSONArray(actKey) != null) {
                    JSONArray actEntitiesList = actFalconJson.optJSONArray(actKey);
                    for (int i = 0; i < actEntitiesList.length(); i++){
                        JSONObject actEntity = actEntitiesList.getJSONObject(i);
                        //if( actResultsByEntity.containsKey(keysToProcess))
                        for( String actResultStr : actResultsByEntity.keySet() ){
                            JSONObject actResultObj = new JSONObject(actResultStr);
                            log.debug(actResultObj.getString("URI")+""+ actEntity.getString("URI") );
                           

                            if( actResultObj.getString("URI").equals( actEntity.getString("URI"))){
                                log.debug("match");
                                actEntity.put("label", actResultObj.getString("label")); 
                                falconEntities.put(actEntity);
                            }
                        }
                        
                    }                     
                    //falconEntities.putAll(actEntitiesList);
                }
            }

            entitiesArr.putAll( falconEntities );
        }

        // ========== section relations =============

        // add all relations from falcon
        JSONArray relationsArr = new JSONArray();
        for (String actFalconResult : falconResults){
            JSONObject actFalconJson = new JSONObject( actFalconResult );
            JSONArray falconRels = new JSONArray();

            String[] keysToProcess = new String[] {"relations_wikidata", "relations_dbpedia"};
            for( String actKey : keysToProcess) { 
                if( actFalconJson.optJSONArray(actKey) != null) {
                    falconRels.putAll( actFalconJson.optJSONArray(actKey) );
                }
            }
            relationsArr.putAll( falconRels );
        }
        finalResult.put("relations", relationsArr);

        // ========== section iconclass notations =============
        // add all notations from iconclass
        for (String actIconclassResult : iconclassResults){
            JSONObject actJson = new JSONObject( actIconclassResult );
            entitiesArr.putAll( actJson.optJSONArray("notations_iconclass"));
        }

        // ========== section OLS =================
        for (String actEntity : olsResults){
            JSONObject actJson = new JSONObject( actEntity );
            entitiesArr.putAll( actJson.optJSONArray("entities_ols"));
        }

        // ========== section GND =================
        for (String actEntity : gndResults){
            entitiesArr.putAll( new JSONArray(actEntity));
        }

        finalResult.put("entities", entitiesArr);
        
        // ========== section hierarchy ===============
        // create empty root node
        JSONObject resultHierarchy = new JSONObject();
        resultHierarchy.put("id", "0");
        resultHierarchy.put("name", "results");
        resultHierarchy.put("link", "");
        resultHierarchy.put("children", new JSONArray());
        
        // build hierarchy from Wikidata and add to root node
        if( !wikidataResultsByEntity.isEmpty() ) {
            JSONObject wdHierarchy = TreeBuilder.buildCategoryTree( wikidataResultsByEntity , 1, "Wikidata", "http://wikidata.org", "label", "URI", allowDuplicates);
            resultHierarchy.getJSONArray("children").put(wdHierarchy);
        }

        // build hierarchy from dbPedia and add to root node
        if( !dbpediaResultsByEntity.isEmpty() ) {
            JSONObject dpHierarchy = TreeBuilder.buildCategoryTree( dbpediaResultsByEntity , 1000, "DBpedia", "http://dbpedia.org", "label", "URI", allowDuplicates);
            resultHierarchy.getJSONArray("children").put(dpHierarchy);
        }

        // build hierarchy from iconclass and add to root node
        if( !iconclassResultsByEntity.isEmpty() ) {
            JSONObject icHierarchy = TreeBuilder.buildCategoryTree( iconclassResultsByEntity , 10000, "Iconclass", "http://iconclass.org", "label", "URI", allowDuplicates);
            resultHierarchy.getJSONArray("children").put(icHierarchy);
        }

        // build hierarchy from gnd and add to root node
        if( !gndResultsByEntity.isEmpty() ) {
            JSONObject gndHierarchy = TreeBuilder.buildCategoryTree( gndResultsByEntity , 100000, "GND", "http://gnd.network", "label", "URI", allowDuplicates);
            resultHierarchy.getJSONArray("children").put(gndHierarchy);
        }

         // build hierarchy from ols and add to root node
         if( !olsResultsByEntity.isEmpty() ) {
            // split ts4tib results by source (ontology)
            Map<String, Map<String,String>> olsResultsByEntityBySource = new HashMap<>();
            for (Map.Entry<String, String> entry : olsResultsByEntity.entrySet()) {
                JSONObject actEntity = new JSONObject(entry.getKey());
                String source = actEntity.getString("source");
                
		        if( !olsResultsByEntityBySource.containsKey( source ) ){
                    olsResultsByEntityBySource.put(source, new HashMap<>());
                }
			    Map<String, String> targetMap = olsResultsByEntityBySource.get(source);
                targetMap.put(entry.getKey(), entry.getValue());
		    }
            
            int rootNodeId = 1000000;
            for( String actSource : olsResultsByEntityBySource.keySet()) {
                Map<String, String> actOlsResultsByEntity = olsResultsByEntityBySource.get(actSource);
                String actSourceUri = "https://terminology.tib.eu/ts/ontologies/"+actSource;
                JSONObject olsHierarchy = TreeBuilder.buildCategoryTree( actOlsResultsByEntity , rootNodeId, actSource, actSourceUri, "label", "URI", allowDuplicates);
                resultHierarchy.getJSONArray("children").put(olsHierarchy);
                rootNodeId = rootNodeId * 10;
            }
            
        }

        finalResult.put( "hierarchy", resultHierarchy );

        return finalResult;
    }

    public static Map<String,FullDictionaryValue> getIconclassDict() throws Exception{
        
       
        Map<String,FullDictionaryValue> result = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/dict/iconclass/kw_en_keys.txt"))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                FullDictionaryValue actResultEntry = new FullDictionaryValue();
                String[] values = line.split("[|]");
                //System.out.println(values.toString());
                String entity_id = values[0];
                List<String> labels = new ArrayList<>();
                labels.add(values[1]);
                
                actResultEntry.setKbId(values[0]);
                actResultEntry.setLabel(values[1]);
                actResultEntry.setPatterns(labels);
                actResultEntry.setKbUrl("http://iconclass.org/"+values[0]);
                result.put(entity_id, actResultEntry );
                i++;
                if( i > 10){
                    break;
                }
            }
        }
        return result;
    }

    
   
}
