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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchyFetcherDBpedia extends HierarchyFetcher{
    
    private static Logger log = LoggerFactory.getLogger(HierarchyFetcherDBpedia.class);

    public HierarchyFetcherDBpedia(Map<String, String> resultContainer, JSONArray falconResultsToProcess) {
        super(resultContainer, falconResultsToProcess);
    }    


    @Override
    public void run() {
        
        //String entityLabel = ((JSONArray)actEntity).get(0).toString();
        String url = (super.falconResultsToProcess).get(0).toString();
                        
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
            super.resultsByEntity.put(super.falconResultsToProcess.toString(), result);
            log.debug("dbpedia result fetched sucessfully");
        } catch ( Exception e) {
            log.warn( "unable to receive dbPedia Classes for '"+super.falconResultsToProcess.toString()+"' error: "+e.getMessage() );
        }
        
    }
    
}
