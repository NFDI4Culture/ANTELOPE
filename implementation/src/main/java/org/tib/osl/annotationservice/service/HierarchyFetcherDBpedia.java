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

public class HierarchyFetcherDBpedia extends HierarchyFetcher{
    
    private static Logger log = LoggerFactory.getLogger(HierarchyFetcherDBpedia.class);

    public HierarchyFetcherDBpedia(Map<String, String> resultContainer, JSONObject falconResultsToProcess) {
        super(resultContainer, falconResultsToProcess);
    }    


    @Override
    public void run() {
        
        //String entityLabel = ((JSONArray)actEntity).get(0).toString();
        String url = (super.falconResultsToProcess).get("URI").toString();
        String[] urlParts = url.replace(">", "").split("/");
        String objId = urlParts[ urlParts.length-1 ];

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
        HttpGet get = null;
        try {
            get = new HttpGet(new URI("https://dbpedia.org/sparql/?format=application%2Fsparql-results%2Bjson&timeout=30000&query="+URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8)+""));
        } catch (Exception e) {
            log.error( "unable init DBpedia url error: "+e.getMessage() );
        }
            result = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(get)) {

            result = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(result);
            //super.resultsByEntity.put(super.falconResultsToProcess.toString(), json.getJSONObject("results").optJSONArray("bindings").toString());
            JSONArray jsonFetchResultArr = json.getJSONObject("results").optJSONArray("bindings");
            JSONArray resultArr = new JSONArray();
            for( int i=0; i<jsonFetchResultArr.length(); i++){ 
                JSONObject j = jsonFetchResultArr.getJSONObject(i);
                String actNodeName = j.getJSONObject("classLabel").getString("value");
                String actNodeUri = j.getJSONObject("class").getString("value");
                String actSuperClassUri = j.getJSONObject("superclass").getString("value");
                String actSuperClassName = j.getJSONObject("superclassLabel").getString("value");

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

            // falcon includes only the surface form label, we want to show the wikidata/dbpedia label of the entity
            //String entityLabel = super.falconResultsToProcess.get("surface form").toString(); // default setting
            String entityLabel = objId;
            
            falconResultsToProcess.put("label", entityLabel);

            resultsByEntity.put(falconResultsToProcess.toString(), resultArr.toString());
            log.debug("dbpedia result fetched sucessfully");
        } catch ( Exception e) {
            log.warn( "unable to receive dbPedia Classes for '"+super.falconResultsToProcess.toString()+"' error: "+e.getMessage() );
        }
        
    }
    
}
