package org.tib.osl.annotationservice.service;

import java.util.Map;

import org.json.JSONArray;
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

    public HierarchyFetcherWikiData(Map<String, String> resultContainer, JSONArray falconResultsToProcess) {
        super(resultContainer, falconResultsToProcess);
    }    


    @Override
    public void run() {
        //String entityLabel = ((JSONArray)actEntity).get(0).toString();
        String url = super.falconResultsToProcess.get(1).toString();
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
            super.resultsByEntity.put(super.falconResultsToProcess.toString(), result);
            //System.out.println( "Entity:"+actEntity+" result:"+ result );
            log.debug("wikidata result fetched sucessfully");
        } catch ( Exception e) {
            log.warn( "unable to receive wikiData Classes for '"+super.falconResultsToProcess.toString()+"' error: "+e.getMessage() );
        }
    }
}
