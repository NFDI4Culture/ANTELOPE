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
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchyFetcherGettyAAT extends HierarchyFetcher{
    
    private static Logger log = LoggerFactory.getLogger(HierarchyFetcherOLS.class);

    public HierarchyFetcherGettyAAT(Map<String, String> resultContainer, JSONObject aatEntityToProcess) {
        super(resultContainer, aatEntityToProcess);
    }    

    @Override
    public void run() {
        
        log.debug( "GettyAAT_fetch:");
        try {
            JSONArray resultArr = new JSONArray();
            //super.entitiesToProcess.put("label", super.entitiesToProcess.getString("label"));
            getParentClasses( super.entitiesToProcess.getJSONObject("obj"), resultArr );
            super.resultsByEntity.put( super.entitiesToProcess.toString(), resultArr.toString());
            //System.out.println(resultsByEntity);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
       
    private void getParentClasses(JSONObject entity, JSONArray result) {
        log.debug( "getparentCLasses getty");
        
        // get full entity info
        String[] uriParts = entity.getString("id").split("/");
        
        String url = "http://vocabsservices.getty.edu/AATService.asmx/AATGetSubject?subjectID="+uriParts[uriParts.length-1];
        //log.debug(url);
        HttpGet get = null;
        try {
            get = new HttpGet(new URI(url));
        } catch (Exception e) {
            log.error( "unable init getty AAT url error: "+e.getMessage() );
        }
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(get)) {
            String responseStr = EntityUtils.toString(response.getEntity());
            //log.debug(responseStr);
            JSONObject entityObj = XML.toJSONObject(responseStr);
            log.info("--------------------------------------------------------------");
            log.debug(entityObj.toString());
            JSONArray parents = new JSONArray();
            parents.put( entityObj.getJSONObject("Vocabulary").getJSONObject("Subject").getJSONObject("Parent_Relationships").getJSONObject("Preferred_Parent"));
            JSONArray nonPrefferedParents = entityObj.getJSONObject("Vocabulary").getJSONObject("Subject").getJSONObject("Parent_Relationships").optJSONArray("Non-Preferred_Parent");
            if( nonPrefferedParents != null){
                parents.putAll( nonPrefferedParents );
            }
            
            for( int i = 0 ; i < parents.length(); i++){
                
                // each parent hierarchy starts with the original entity as child
                String entityId = entityObj.getJSONObject("Vocabulary").getJSONObject("Subject").getNumber("Subject_ID").toString();
                String entityLabel = entityObj.getJSONObject("Vocabulary").getJSONObject("Subject").getJSONObject("Terms").getJSONObject("Preferred_Term").getString("Term_Text");
                JSONObject actParentEntity = parents.getJSONObject(i);
                String actParentHierarchy = actParentEntity.getString("Parent_String");
                log.debug(actParentHierarchy);
                // comma seperated list: "drinking vessels [300194567], vessels for serving and consuming food [300198938]"
                for( String s : actParentHierarchy.split(",") ){ 
                    String parentEntityId = s.replaceAll(".*\\[", "").replace("]", "");
                    String parentEntityLabel = s.split("\\[")[0];
                    //String parentEntityUrl = actParentEntity.getString("id");
                    // create a new json object to add to results
                    JSONObject obj = new JSONObject();
                    obj.put("class", entityId);
                    obj.put("superclass", parentEntityId);
                    obj.put("classLabel", entityLabel);
                    obj.put("superclassLabel", parentEntityLabel);
                    // add json object to result json array
                    result.put(obj);
                    // set parent to actClass to add the next hierarchy level
                    entityId = parentEntityId;
                    entityLabel = parentEntityLabel;
                } 
            }
        }catch ( Exception e) {
            log.error( "unable to receive Getty AAT Classes for '"+super.entitiesToProcess.toString()+"' error: "+e.getMessage() );
            e.printStackTrace();
        }
    }
}
