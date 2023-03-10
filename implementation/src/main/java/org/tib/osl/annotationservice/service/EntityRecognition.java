package org.tib.osl.annotationservice.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            String resultStr = "";
            String url = "https://labs.tib.eu/falcon/falcon2/api?mode="+mode+"&k=10";
            if( useDbpedia ){
                url += "&db=1";
            }
            HttpPost post = new HttpPost(new URI(url));
            post.addHeader("content-type", "application/json");

            StringBuilder json = new StringBuilder();
            json.append("{");     
            json.append("\"text\":\""+actText+"\"");
            json.append("}");

            log.debug(json.toString());
            // send a JSON data
            post.setEntity(new StringEntity(json.toString()));
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {

                resultStr = EntityUtils.toString(response.getEntity());
                
                // log.debug( result.toString() );
                JSONObject resultJson = new JSONObject( resultStr );  
                JSONObject normalizedResultJson = new JSONObject();
                String[] resultArrKeys = new String[]{"entities_wikidata", "entities_dbpedia"};
                for( String actResultArrKey : resultArrKeys) {
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

        // now make a call to the iconclass api to get more data about the entities (notations) found
        // see https://iconclass.org/docs#/default/api_search_api_search_get
        
        // uri build for method 'json' respond with null for some entities, e.g for "47I41( 9q5243)"
        /*
        String result = "";
        String uri = "https://iconclass.org/json?";
        for( String actNotation : notationsFound){
            uri += "notation="+actNotation+"&";
        }

        log.debug("call:"+uri);
        HttpGet request = new HttpGet(new URI(uri));
        request.addHeader("content-type", "application/json");
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(request)) {

            result = EntityUtils.toString(response.getEntity());
            log.debug( result.toString() );
            JSONObject actJsonResult = new JSONObject( result );
            JSONArray resultArr = actJsonResult.getJSONArray("result"); 
            for( Object obj : resultArr){
                iconclassResults.add( obj.toString() );
            }
        }*/
        

        System.out.println("iconclassResults:"+iconclassResults);
        return iconclassResults;
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
            Map<String,String> iconclassResultsByEntity) throws Exception {
    
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
                if( actFalconJson.optJSONArray(actKey) != null) {
                    falconEntities.putAll( actFalconJson.optJSONArray(actKey) );
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

        // ========== section notations =============
        // add all notations from iconclass
        for (String actIconclassResult : iconclassResults){
            JSONObject actJson = new JSONObject( actIconclassResult );
            entitiesArr.putAll( actJson.optJSONArray("notations_iconclass"));
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
            JSONObject wdHierarchy = TreeBuilder.buildCategoryTree( wikidataResultsByEntity , 1, "Wikidata", "http://wikidata.org", "label", "URI");
            resultHierarchy.getJSONArray("children").put(wdHierarchy);
        }

        // build hierarchy from dbPedia and add to root node
        if( !dbpediaResultsByEntity.isEmpty() ) {
            JSONObject dpHierarchy = TreeBuilder.buildCategoryTree( dbpediaResultsByEntity , 1000, "DBpedia", "http://dbpedia.org", "label", "URI");
            resultHierarchy.getJSONArray("children").put(dpHierarchy);
        }

        // build hierarchy from iconclass and add to root node
        if( !iconclassResultsByEntity.isEmpty() ) {
            JSONObject icHierarchy = TreeBuilder.buildCategoryTree( iconclassResultsByEntity , 10000, "Iconclass", "http://iconclass.org", "label", "URI");
            resultHierarchy.getJSONArray("children").put(icHierarchy);
        }

        finalResult.put( "hierarchy", resultHierarchy );

        return finalResult;
    }

   
}
