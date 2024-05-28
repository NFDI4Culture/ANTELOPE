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

public class VecnerClient {
    private static Logger log = LoggerFactory.getLogger(VecnerClient.class);

    private static String getBaseUrl() {
        String url = System.getenv("VECNER_SERVICE_URL");
        if( url == null) {
            url = "http://localhost:5000";
        }
        return url;
    }
    
    private static String callVisualize( JSONObject request ) throws Exception {
        
        String url = getBaseUrl()+ "/visualize";
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

    public static JSONObject callEntityLinking(TextEntityLinkingRequest request, String dictName, String kbUrl) throws Exception {
        log.info(request.toString());
        String url = getBaseUrl()+ "/entitylinking";
        String elResultStr = "";
        
        HttpPost post = new HttpPost(new URI(url));
        post.addHeader("content-type", "application/json; charset=UTF-8");

        // vecner is called in a two step process: 1) get entity linking using simple dictionary 2) get visualization using optional additional data from full dictionary 
        Map<String, List<String>> simpleDictionary = new HashMap<>();
        if( request.getDictionary() != null && request.getDictionary().getDictionaryType() != null) {
        switch (request.getDictionary().getDictionaryType()) {
            case LISTOFWORDS:
                for( String word : request.getDictionary().getListOfWords()){
                    simpleDictionary.put(word, Arrays.asList(word));
                }

                break;
            case SIMPLEDICTIONARY:
                simpleDictionary = request.getDictionary().getSimpleDictionary();
                break;
            case FULLDICTIONARY:
                Map<String, FullDictionaryValue> fullDict = request.getDictionary().getFullDictionary();
                for( String key : fullDict.keySet()) {
                    FullDictionaryValue val = fullDict.get(key);
                    simpleDictionary.put(val.getKbId(), val.getPatterns());
                }
                break;
        }
        }

        // escape spccial chars in request body
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", request.getText());
        if( dictName != null) {
            jsonObject.put("dict", dictName);
        } else {
            jsonObject.put("dict", simpleDictionary);
        }
        jsonObject.put("threshold", request.getThreshold());
        String payload = jsonObject.toString();
        //Gson gson = new Gson();
        //String payload = gson.toJson(jsonObject);
        //log.debug("test");
        //log.debug("payload"+payload);
        // send a JSON data
        post.setEntity(new StringEntity(payload, "UTF-8"));
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(post)) {

            elResultStr = EntityUtils.toString(response.getEntity());
            
            //log.debug( elResultStr.toString() );
            JSONObject elResultJson = new JSONObject( elResultStr );  
            
            JSONObject vRequest = new JSONObject();
            
            log.debug("vizualize");
            //log.debug(request.toString());
            if( request.getDictionary() != null) {
                if( request.getDictionary().getFullDictionary()  != null &&  !request.getDictionary().getFullDictionary().isEmpty()) {
                    log.debug("found full dict, extend el_result...");
                    for( Object e : elResultJson.getJSONArray("ents") ){
                        JSONObject ent = (JSONObject)e;
                        //System.out.println(ent);
                        String ent_id = ent.getString("label"); 
                        if(request.getDictionary().getFullDictionary().containsKey(ent_id)) {
                            ent.put("label", request.getDictionary().getFullDictionary().get(ent_id).getLabel());
                            ent.put("kb_id", request.getDictionary().getFullDictionary().get(ent_id).getKbId());
                            ent.put("kb_url", request.getDictionary().getFullDictionary().get(ent_id).getKbUrl());
                        } else {
                            System.err.println( "unknown id:"+ent_id+" in:"+request.getDictionary().getFullDictionary().keySet());
                            throw new IllegalStateException("dictionary error. Unknown id:"+ent_id);
                        }
                    }
                }
            }
            vRequest.put("el_result", elResultJson);
            if( kbUrl != null) {
                vRequest.put("static_kb_url", kbUrl);
            }
            String vResultStr = callVisualize( vRequest ); 
            //log.debug("test2"+vResultStr);
            JSONObject result = new JSONObject();
            result.put("json", vRequest);
            result.put("html", vResultStr);
            return result;
        }
        
    }
}
