package org.tib.osl.annotationservice.service;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HierarchyFetcherWikiData extends HierarchyFetcher{
    private static Logger log = LoggerFactory.getLogger(HierarchyFetcherWikiData.class);

    public HierarchyFetcherWikiData(Map<String, String> resultContainer, JSONObject falconResultsToProcess) {
        super(resultContainer, falconResultsToProcess);
    }    


    @Override
    public void run() {
        //String entityLabel = ((JSONArray)actEntity).get(0).toString();
        String url = super.falconResultsToProcess.get("URI").toString();
        String[] urlParts = url.replace(">", "").split("/");
        String objId = urlParts[ urlParts.length-1 ];
        // init connection to wikiData api
        String result = "";
        String sparqlQuery = "SELECT ?class ?classLabel ?superclass ?superclassLabel WHERE { wd:"+objId+" wdt:P31*/wdt:P279* ?class. ?class wdt:P31/wdt:P279 ?superclass. SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } } ";
        //String sparqlQuery = "SELECT ?class ?classLabel ?superclass ?superclassLabel WHERE { wd:"+entityId+" wdt:P31/wdt:P279* ?class. ?class wdt:P31/wdt:P279 ?superclass. SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } } ";
        HttpGet get = null;
        try {
            get = new HttpGet(new URI("https://query.wikidata.org/sparql?format=json&query="+URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8)+""));
        } catch (Exception e) {
            log.error( "unable init WikiData url error: "+e.getMessage() );
        }
            //get.addHeader("content-type", "application/json");
        //System.out.println(sparqlQuery);
        result = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(get)) {

            result = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(result);
            //super.resultsByEntity.put(super.falconResultsToProcess.toString(), json.getJSONObject("results").optJSONArray("bindings").toString());
            //System.out.println( "Entity:"+actEntity+" result:"+ result );
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
            String entityLabel = super.falconResultsToProcess.get("surface form").toString(); // default setting
            String resourceUrl = "https://www.wikidata.org/wiki/Special:EntityData/"+objId+".json";
            try {
                get = new HttpGet(new URI(resourceUrl));
                CloseableHttpResponse response2 = httpClient.execute(get);
                String r = EntityUtils.toString(response2.getEntity());
                JSONObject resourceJson = new JSONObject(r);
                entityLabel = resourceJson.getJSONObject("entities").getJSONObject(objId).getJSONObject("labels").getJSONObject("en").getString("value");
            } catch (Exception e) {
                log.error( "unable init WikiData url error: "+resourceUrl+e.getMessage() );
            }
            falconResultsToProcess.put("label", entityLabel);

            resultsByEntity.put(falconResultsToProcess.toString(), resultArr.toString());
            log.debug("wikidata result fetched sucessfully");
        } catch ( Exception e) {
            log.warn( "unable to receive wikiData Classes for '"+super.falconResultsToProcess.toString()+"' error: "+e.getMessage() );
            e.printStackTrace();
        }
    }
}
