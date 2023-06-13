package org.tib.osl.annotationservice.service;



import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchyFetcherOLS extends HierarchyFetcher{
    
    private static Logger log = LoggerFactory.getLogger(HierarchyFetcherOLS.class);

    public HierarchyFetcherOLS(Map<String, String> resultContainer, JSONObject olsResultsToProcess) {
        super(resultContainer, olsResultsToProcess);
    }    


    @Override
    public void run() {
        
        String result = "";
        String enitityType = super.entitiesToProcess.getString("type");
        String entityId = super.entitiesToProcess.getString("id");
        String entityLabel = super.entitiesToProcess.getString("label");
        String entityUrl = super.entitiesToProcess.getString("URI");
        String url = "";
        String baseUrl = "https://service.tib.eu/ts4tib/api/";
        Map<String, String> urlByType = Map.of(
            "class","terms?iri=" + URLEncoder.encode(entityUrl, StandardCharsets.UTF_8), 
            "property", "properties?iri=" + URLEncoder.encode(entityUrl, StandardCharsets.UTF_8),
            "individual", "individuals?iri=" + URLEncoder.encode(entityUrl, StandardCharsets.UTF_8),
            "ontology", "ontologies?iri=" + URLEncoder.encode(entityUrl, StandardCharsets.UTF_8));
        if( urlByType.containsKey( enitityType )) {
            url = baseUrl + urlByType.get( enitityType );
        } else {
            throw new IllegalArgumentException("unknown type: '"+enitityType+"' in ols result for entity: '"+entityId+"'");
        }

        HttpGet get = null;
        try {
            get = new HttpGet(new URI(url));
        } catch (Exception e) {
            log.error( "unable init ols url error: "+e.getMessage() );
        }
        result = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(get)) {

            result = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(result);
            log.debug(json.toString());
            JSONArray entitiesJsonArr = json.getJSONObject("_embedded").optJSONArray("terms");
            JSONArray resultArr = new JSONArray();
            for( int i=0; i<entitiesJsonArr.length(); i++){ 
                JSONObject entity = entitiesJsonArr.getJSONObject(i);
                getParentClasses( entity, resultArr );
            }
            
            super.entitiesToProcess.put("label", entityLabel);

            resultsByEntity.put(entitiesToProcess.toString(), resultArr.toString());
            log.debug("ols result fetched sucessfully");
        }catch ( Exception e) {
            log.error( "unable to receive ols Classes for '"+super.entitiesToProcess.toString()+"' error: "+e.getMessage() );
            
            e.printStackTrace();
        }
       
        
    }

    private void getParentClasses(JSONObject entity, JSONArray result) {
        
        String actNodeName = entity.getString("label");
        String actNodeUri = entity.getString("iri");
        Boolean actNodeIsRoot = entity.getBoolean("is_root");
        String actNodeParentsUrl = entity.getJSONObject("_links").getJSONObject("hierarchicalParents").getString("href");

        // break call (recursion end)
        if( actNodeIsRoot ) {
            return;
        }

        // get parents
        HttpGet get = null;
        try {
            get = new HttpGet(new URI(actNodeParentsUrl));
        } catch (Exception e) {
            log.error( "unable init ols url error: "+e.getMessage() );
        }
        String parentsResult = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(get)) {

            parentsResult = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(parentsResult);
            
            JSONArray parentsArr = json.getJSONObject("_embedded").optJSONArray("terms");
            for( int i=0; i<parentsArr.length(); i++){ 
                JSONObject actParent = parentsArr.getJSONObject(i);
                String actParentName = actParent.getString("label");
                String actParentUri = actParent.getString("iri");
               
                // create a new json object to add to results
                JSONObject obj = new JSONObject();
                obj.put("class", actNodeUri);
                obj.put("superclass", actParentUri);
                obj.put("classLabel", actNodeName);
                obj.put("superclassLabel", actParentName);
                // add json object to result json array
                result.put(obj);
                
                // recursive call to get parents for actParent
                getParentClasses(actParent, result);
            }
        } catch ( Exception e) {
            log.error( "unable to receive ols Classes for '"+super.entitiesToProcess.toString()+"' error: "+e.getMessage() );
            
            e.printStackTrace();
        }

        
        
        
    }
    
}
