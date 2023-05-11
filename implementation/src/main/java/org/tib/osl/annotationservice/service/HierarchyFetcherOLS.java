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
            
            JSONArray jsonFetchResultArr = json.getJSONObject("_embedded").optJSONArray("terms");
            JSONArray resultArr = new JSONArray();
            for( int i=0; i<jsonFetchResultArr.length(); i++){ 
                JSONObject j = jsonFetchResultArr.getJSONObject(i);
                String actNodeName = entityLabel;
                String actNodeUri = entityUrl;
                String actSuperClassUri = "";
                String actSuperClassName = "superClassName";

                // embedd respone data into falcon json format
                JSONObject obj = new JSONObject();
                obj.put("class", actNodeUri);
                obj.put("superclass", actSuperClassUri);
                obj.put("classLabel", actNodeName);
                obj.put("superclassLabel", actSuperClassName);
                // add json object to result json array
                resultArr.put(obj);
                log.debug("iconclass result fetched sucessfully");
            }

            
            
            super.entitiesToProcess.put("label", entityLabel);

            resultsByEntity.put(entitiesToProcess.toString(), resultArr.toString());
            log.debug("ols result fetched sucessfully");
        } catch ( Exception e) {
            log.error( "unable to receive ols Classes for '"+super.entitiesToProcess.toString()+"' error: "+e.getMessage() );
            log.error(url);
            e.printStackTrace();
        }
        
    }
    
}
