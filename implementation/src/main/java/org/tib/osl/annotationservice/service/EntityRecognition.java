package org.tib.osl.annotationservice.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class EntityRecognition {
    private static Logger log = LoggerFactory.getLogger(EntityRecognition.class);

    /**
     * connects to the Falcon web API and retrieve results for every text in requestText
     * @param requestText list of texts, used for entity recognition with falcon
     * @return falcon specific json result containing result arrays under the keys "entities_wikidata" and "relations_wikidata"
     * @throws Exception
     */
    protected static List<String> getFalconResults( List<String> requestText) throws Exception {
        List<String> falconResults = new ArrayList<>();
       
        // init connection to falcon api
        for( String actText : requestText) {
            String result = "";
            HttpPost post = new HttpPost(new URI("https://labs.tib.eu/falcon/falcon2/api?mode=long&db=1&k=10"));
            post.addHeader("content-type", "application/json");

            StringBuilder json = new StringBuilder();
            json.append("{");     
            json.append("\"text\":\""+actText+"\"");
            json.append("}");

            
            // send a JSON data
            post.setEntity(new StringEntity(json.toString()));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {

                result = EntityUtils.toString(response.getEntity());
                falconResults.add(result);
            }
        }
        System.out.println("falconResult:"+falconResults);
        return falconResults;
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
                        
                        //String entityLabel = ((JSONArray)actEntity).get(0).toString();
                        String url = ((JSONArray)actObject).get(1).toString();
                        String[] urlParts = url.replace(">", "").split("/");
                        String objId = urlParts[ urlParts.length-1 ];
                        // init connection to wikiData api
                        String result = "";
                        String sparqlQuery = "SELECT ?class ?classLabel ?superclass ?superclassLabel WHERE { wd:"+objId+" wdt:P31*/wdt:P279* ?class. ?class wdt:P31/wdt:P279 ?superclass. SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } } ";
                        //String sparqlQuery = "SELECT ?class ?classLabel ?superclass ?superclassLabel WHERE { wd:"+entityId+" wdt:P31/wdt:P279* ?class. ?class wdt:P31/wdt:P279 ?superclass. SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } } ";
                        HttpGet get = new HttpGet(new URI("https://query.wikidata.org/sparql?format=json&query="+URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8)+""));
                        //get.addHeader("content-type", "application/json");
                        //System.out.println(sparqlQuery);
                        result = null;
                        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                            CloseableHttpResponse response = httpClient.execute(get)) {

                            result = EntityUtils.toString(response.getEntity());
                            wikidataResultsByEntity.put(actObject.toString(), result);
                            //System.out.println( "Entity:"+actEntity+" result:"+ result );
                            
                        } catch ( Exception e) {
                            log.warn( "unable to receive wikiData Classes for '"+actObject.toString()+"' error: "+e.getMessage() );
                        }
                        
                        
                    }
                } 
            } 
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println( "error in getWikiDataClasses:" + e.getMessage() );
        }    
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
                        
                        //String entityLabel = ((JSONArray)actEntity).get(0).toString();
                        String url = ((JSONArray)actObject).get(0).toString();
                        
                        // init connection to dbpedia api (virtuoso)
                        String result = "";
                        String sparqlQuery = ""+
                            "SELECT *"+
                            "WHERE {"+ 
                               "<"+url+"> rdf:type*/rdfs:subClassOf* ?class ."+
                                "?class rdf:type*/rdfs:subClassOf* ?superclass."+
                                "?class rdfs:label ?classLabel."+
                                "?superclass rdfs:label ?superclassLabel."+
                                "FILTER(LANG(?classLabel) = 'en' && LANG(?superclassLabel) = 'en' && ?class != ?superclass)."+
                            "}";
                        HttpGet get = new HttpGet(new URI("https://dbpedia.org/sparql/?format=application%2Fsparql-results%2Bjson&timeout=30000&query="+URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8)+""));
                        result = null;
                        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                            CloseableHttpResponse response = httpClient.execute(get)) {

                            result = EntityUtils.toString(response.getEntity());
                            dbpediaResultsByEntity.put(actObject.toString(), result);
                        } catch ( Exception e) {
                            log.warn( "unable to receive dbPedia Classes for '"+actObject.toString()+"' error: "+e.getMessage() );
                        }
                        
                        
                    }
                } 
            } 
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println( "error in getDbPediaClasses:" + e.getMessage() );
        }    
        return dbpediaResultsByEntity;
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
            Map<String,String> dbpediaResultsByEntity) throws Exception {
    
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
            falconEntities.putAll( actFalconJson.optJSONArray("entities_wikidata") );
            falconEntities.putAll( actFalconJson.optJSONArray("entities_dbpedia") );
            entitiesArr.putAll( falconEntities );
        }
        finalResult.put("entities", entitiesArr);

        // ========== section relations =============

        // add all relations from falcon
        JSONArray relationsArr = new JSONArray();
        for (String actFalconResult : falconResults){
            JSONObject actFalconJson = new JSONObject( actFalconResult );
            JSONArray falconRels = new JSONArray();
            falconRels.putAll( actFalconJson.optJSONArray("relations_wikidata") );
            falconRels.putAll( actFalconJson.optJSONArray("relations_dbpedia") );
            relationsArr.putAll( falconRels );
        }
        finalResult.put("relations", relationsArr);
        
        // ========== section hierarchy ===============

        // build hierarchy from Wikidata
        JSONObject wdHierarchy = TreeBuilder.buildCategoryTree( wikidataResultsByEntity , 1, "Wikidata", "http://wikidata.org", 0, 1);

        // build hierarchy from dbPedia
        JSONObject dpHierarchy = TreeBuilder.buildCategoryTree( dbpediaResultsByEntity , 1000, "DBpedia", "http://dbpedia.org", 1, 0);


        // create empty root node
        JSONObject resultHierarchy = new JSONObject();
        
        resultHierarchy.put("id", "0");
        resultHierarchy.put("name", "results");
        resultHierarchy.put("link", "");
        resultHierarchy.put("children", new JSONArray());
        resultHierarchy.getJSONArray("children").put(wdHierarchy);
        resultHierarchy.getJSONArray("children").put(dpHierarchy);

        finalResult.put( "hierarchy", resultHierarchy );

        return finalResult;
    }

   
}
