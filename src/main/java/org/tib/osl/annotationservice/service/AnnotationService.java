package org.tib.osl.annotationservice.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.tib.osl.annotationservice.web.api.AnnotationApiDelegate;
import org.tib.osl.annotationservice.web.api.StatusApiDelegate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;


@Service
public class AnnotationService implements StatusApiDelegate, AnnotationApiDelegate {

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
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request WikiData API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

   

}


