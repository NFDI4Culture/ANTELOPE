package org.tib.osl.annotationservice.service;

import java.net.URI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tib.osl.annotationservice.service.api.dto.FullDictionaryValue;
import org.tib.osl.annotationservice.service.api.dto.TextEntityLinkingRequest;

import org.tib.osl.annotationservice.service.api.dto.TextEntityLinkingRequest.DictionaryTypeEnum;

import com.google.gson.Gson;

public class VecnerClient {
    private static Logger log = LoggerFactory.getLogger(VecnerClient.class);

    private static String callVisualize( JSONObject request ) throws Exception {
        
        String url = "http://localhost:5000/visualize";
        String resultStr = "";
        
        HttpPost post = new HttpPost(new URI(url));
        post.addHeader("content-type", "application/json; charset=UTF-8");

        String payload = request.toString();

        log.debug(payload);
        // send a JSON data
        post.setEntity(new StringEntity(payload, "UTF-8"));
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(post)) {

            resultStr = EntityUtils.toString(response.getEntity());
            log.debug( resultStr.toString() );
            
            return resultStr;
        }
    }

    public static JSONObject callEntityLinking(TextEntityLinkingRequest request) throws Exception {
        log.info(request.toString());
        String url = "http://localhost:5000/entitylinking";
        String elResultStr = "";
        
        HttpPost post = new HttpPost(new URI(url));
        post.addHeader("content-type", "application/json; charset=UTF-8");

        // vecner is called in a two step process: 1) get entity linking using simple dictionary 2) get visualization using optional additional data from full dictionary 
        Map<String, List<String>> simpleDictionary = new HashMap<>();
        switch (request.getDictionaryType()) {
            case LISTOFWORDS:
                for( String word : request.getListOfWords()){
                    simpleDictionary.put(word, Arrays.asList(word));
                }

                break;
            case SIMPLEDICTIONARY:
                simpleDictionary = request.getSimpleDictionary();
                break;
            case FULLDICTIONARY:
                Map<String, FullDictionaryValue> fullDict = request.getFullDictionary();
                for( String key : fullDict.keySet()) {
                    FullDictionaryValue val = fullDict.get(key);
                    simpleDictionary.put(val.getKbId(), val.getPatterns());
                }
                break;
        }

        // escape spccial chars in request body
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", request.getText());
        jsonObject.put("dict", simpleDictionary);
        jsonObject.put("threshold", request.getThreshold());
        String payload = jsonObject.toString();
        //Gson gson = new Gson();
        //String payload = gson.toJson(jsonObject);
        //log.debug("test");
        log.debug("payload"+payload);
        // send a JSON data
        post.setEntity(new StringEntity(payload, "UTF-8"));
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(post)) {

            elResultStr = EntityUtils.toString(response.getEntity());
            
            //log.debug( elResultStr.toString() );
            JSONObject elResultJson = new JSONObject( elResultStr );  
            
            JSONObject vRequest = new JSONObject();
            
            log.debug("vizualize");
            log.debug(request.toString());
            if( request.getFullDictionary() != null ) {
                log.debug("found full dict, extend el_result...");
                for( Object e : elResultJson.getJSONArray("ents") ){
                    JSONObject ent = (JSONObject)e;
                    String ent_id = ent.getString("label");
                    ent.put("label", request.getFullDictionary().get(ent_id).getLabel());
                    ent.put("kb_id", request.getFullDictionary().get(ent_id).getKbId());
                    ent.put("kb_url", request.getFullDictionary().get(ent_id).getKbUrl());
                }
            }
            vRequest.put("el_result", elResultJson);

            String vResultStr = callVisualize( vRequest ); 
            log.debug("test2"+vResultStr);
            JSONObject result = new JSONObject();
            result.put("json", vRequest);
            result.put("html", vResultStr);
            return result;
        }
        
    }
}
