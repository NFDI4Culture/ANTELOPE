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
            
            String actNotationName = (this.notationToProcess).get("label").toString();
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
            String lastSuperclass = null;
            String lastSuperclassNotationName = null;
            String actChildclass = pageUri;

            for( int i=superclassNotationsArr.length()-1; i>=0; i--){ 
                if( lastSuperclass != null){
                    actChildclass = lastSuperclass;
                    actNotationName = lastSuperclassNotationName;
                }

                String superclassNotation = superclassNotationsArr.getString(i);
                String fileName2 = superclassNotation+".json";
                String fileUri2 = host+fileName2;
                                
                log.debug("fetch: "+fileUri2);
                in = new BufferedInputStream(new URL( fileUri2 ).openStream());
                tokener = new JSONTokener(in);
                JSONObject superclassNotationJson = new JSONObject(tokener);
                log.debug("superclass:"+superclassNotationJson.toString());
                
                String actSuperClass = host+superclassNotation;
                String actSuperClassNotationName = superclassNotationJson.getJSONObject("txt").getString("en");

                // embedd respone data into falcon json format
                JSONObject obj = new JSONObject();
                obj.put("class", actChildclass);
                obj.put("superclass", actSuperClass);
                 obj.put("classLabel", actNotationName);
                obj.put("superclassLabel", actSuperClassNotationName);
                
                // add json object to result json array
                resultArr.put(obj);
                log.debug("iconclass result fetched sucessfully");

                // save used notation for use as childClass in next result (results in iconclass are already hierarchic)
                lastSuperclass = actSuperClass;
                lastSuperclassNotationName = actNotationName;
            }
            resultsByEntity.put(notationToProcess.toString(), resultArr.toString());
        } catch ( Exception e) {
            log.warn( "unable to receive iconclass broader Notations for '"+this.notationToProcess.toString()+"' error: "+e.getMessage() );
        }
        
    }
    
}
