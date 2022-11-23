package org.tib.osl.annotationservice.service;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchyFetcherIconclass implements Runnable{
    
    private static Logger log = LoggerFactory.getLogger(HierarchyFetcherIconclass.class);
    public Map<String, String> resultsByEntity;
    private JSONObject notationToProcess;

    public HierarchyFetcherIconclass(Map<String, String> resultContainer, JSONObject notationToProcess) {
        this.resultsByEntity = resultContainer;
        this.notationToProcess = notationToProcess;
    }    


    @Override
    public void run() {
        try {
            
            String actNotationName = (this.notationToProcess).get("notationName").toString();
            String url = (this.notationToProcess).get("URI").toString();
                            
            // fetch detail of act notation
            String host = "https://iconclass.org/";
            String fileUri = url+".json";
            String pageUri = url;
            log.debug("fetch: "+fileUri);
            BufferedInputStream in = new BufferedInputStream(new URL( fileUri ).openStream());
            JSONTokener tokener = new JSONTokener(in);
            JSONObject notationJson = new JSONObject(tokener);
                
            // fetch superclass json
            JSONArray superclassNotationsArr = notationJson.getJSONArray("p");
            JSONArray resultArr = new JSONArray();
            for( int i=0; i<superclassNotationsArr.length(); i++){ 
                String superclassNotation = superclassNotationsArr.getString(i);
                String fileName2 = superclassNotation+".json";
                String fileUri2 = host+fileName2;
                String pageUri2 = host+superclassNotation;
                log.debug("fetch: "+fileUri2);
                in = new BufferedInputStream(new URL( fileUri2 ).openStream());
                tokener = new JSONTokener(in);
                JSONObject superclassNotationJson = new JSONObject(tokener);
                log.debug("superclass:"+superclassNotationJson.toString());

                // embedd respone data into falcon json format
                JSONObject obj = new JSONObject();
                obj.put("class", pageUri);
                obj.put("superclass", pageUri2);

                
                obj.put("classLabel", actNotationName);
                obj.put("superclassLabel", superclassNotationJson.getJSONObject("txt").getString("en"));
                // add json object to result json array
                resultArr.put(obj);
                log.debug("iconclass result fetched sucessfully");
            }
            resultsByEntity.put(notationToProcess.toString(), resultArr.toString());
        } catch ( Exception e) {
            log.warn( "unable to receive iconclass broader Notations for '"+this.notationToProcess.toString()+"' error: "+e.getMessage() );
        }
        
    }
    
}
