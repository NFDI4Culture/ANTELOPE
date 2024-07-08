package org.tib.osl.annotationservice.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import iart.client.*;
import iart.indexer.Data.Concept;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.tib.osl.annotationservice.service.api.dto.Dictionary;
import org.tib.osl.annotationservice.service.api.dto.Dictionary.DictionaryTypeEnum;
import org.tib.osl.annotationservice.service.api.dto.FullDictionaryValue;
import org.tib.osl.annotationservice.service.api.dto.TextEntityLinkingRequest;
import org.tib.osl.annotationservice.web.api.AnnotationApiDelegate;

@Service
public class AnnotationService implements AnnotationApiDelegate {

    @Autowired
    private SpringTemplateEngine templateEngine;

    private Logger log = LoggerFactory.getLogger(AnnotationService.class);

    public enum SearchMode {
        TERMINOLOGY_SEARCH,
        ENITTY_RECOGNITION,
    }

    // @Override
    // public Optional<NativeWebRequest> getRequest() {
    //     return StatusApiDelegate.super.getRequest();
    // }

    @Override
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("Service is running", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getEntitiesSelectComponent() {
        String[] entitiesToSelect = new String[] {};

        try {
            /*ResponseEntity<String> entitySearchResult = getEntities(entitiesToSearch, null, null, null);
            JSONObject result = new JSONObject(entitySearchResult.getBody());
            JSONArray entitiesArr = result.getJSONArray("entities");
            List<String> entitiesList = entitiesArr.toList().stream().map(Object::toString).collect(java.util.stream.Collectors.toList());
            entitiesToSelect = entitiesList.toArray(new String[entitiesList.size()]);
            */
            entitiesToSelect = new String[] {};

            Locale locale = Locale.forLanguageTag("en");
            Context context = new Context(locale);
            context.setVariable("entities", entitiesToSelect);
            String content = templateEngine.process("annotationService-selectComponent", context);

            //String content = "test";
            return new ResponseEntity<>(content, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Interner Fehler", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getTerminology(
        String searchText,
        Boolean wikidata,
        Boolean wikidataDbpedia,
        Boolean iconclass,
        Boolean ts4tib,
        Boolean lobidGnd,
        Boolean gettyAAT,
        String ts4tib_collection,
        String ts4tib_ontology,
        Boolean allowDuplicates
    ) {
        List<String> requestBody = new ArrayList<String>();
        requestBody.add(searchText);
        return search(
            requestBody,
            SearchMode.TERMINOLOGY_SEARCH,
            wikidata,
            wikidataDbpedia,
            iconclass,
            ts4tib,
            ts4tib_ontology,
            lobidGnd,
            gettyAAT,
            allowDuplicates
        );
    }

    @Override
    public ResponseEntity<String> getTextEntities(
        TextEntityLinkingRequest request,
        Boolean wikidata,
        Boolean wikidataDbpedia,
        Boolean iconclass,
        Boolean ts4tib,
        String ts4tib_collection,
        String ts4tib_ontology,
        Boolean allowDuplicates
    ) {
        try {
            String dictName = null;
            String kbUrl = null;
            if (iconclass != null && iconclass) {
                dictName = "iconclass";
                kbUrl = "https://iconclass.org/en/";
            }
            //System.out.println(request.toString());
            JSONObject el_results = VecnerClient.callEntityLinking(request, dictName, kbUrl);

            return new ResponseEntity<String>(el_results.toString(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getImageEntities(
        String model,
        Boolean iconclass,
        MultipartFile image,
        String text,
        Dictionary dictionary,
        BigDecimal threshold
    ) {
        if (model == null) {
            model = "ClipClassification";
        }

        try {
            //log.info(text);
            JSONArray resultArr = new JSONArray();
            List<iart.client.PluginResult> response = null;
            if (iconclass != null && iconclass) {
                resultArr = getImageEntitiesIconClass(model, image, text, threshold);
            } else {
                response = iArtClient.analyze(model, image.getBytes(), dictionary.getListOfWords());
                for (PluginResult actEntry : response) {
                    try {
                        for (Concept actConcept : actEntry.getResult().getClassifier().getConceptsList()) {
                            //if( threshold != null && actConcept.getProb() < threshold.doubleValue()) {
                            //    continue;
                            //}
                            JSONObject actResultObj = new JSONObject();
                            actResultObj.put("label", actConcept.getConcept());
                            actResultObj.put("score", actConcept.getProb());
                            resultArr.put(actResultObj);
                        }
                    } catch (Exception e) {
                        log.error("error during result creation for object: " + actEntry, e);
                        e.printStackTrace();
                    }
                }
            }

            //String responseString = new Gson().toJson(response);
            JSONArray resultContainer = new JSONArray();
            resultContainer.put(resultArr);
            String responseString = resultContainer.toString();
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private JSONArray getImageEntitiesIconClass(String model, MultipartFile image, String imageText, BigDecimal threshold)
        throws Exception {
        // get vector of image
        List<iart.client.PluginResult> response = iArtClient.analyze("ClipImageEmbeddingFeature", image.getBytes(), null);
        List<Float> imgVector = response.get(0).getResult().getFeature().getFeatureList();

        // if imageText is given, get vector of that too
        List<Float> imgTextVector = null;
        log.info("1" + imageText);
        if (imageText != null && !imageText.isEmpty()) {
            log.info("2" + imageText);
            List<String> textList = new ArrayList<String>();
            textList.add(imageText);
            List<iart.client.PluginResult> imageTextResponse = iArtClient.analyze("ClipTextEmbeddingFeature", null, textList);
            imgTextVector = getClipVectorFromPluginResults(imageTextResponse).get(0);
        }

        // get iconclass dictionary vectors
        log.info("parse dict embeddings file..");
        JSONArray resultArr = new JSONArray();

        for (int i = 0; i < 2; i++) {
            try (
                CSVReader br = new CSVReaderBuilder(
                    new BufferedReader(
                        new InputStreamReader(
                            Thread
                                .currentThread()
                                .getContextClassLoader()
                                .getResourceAsStream("dict/iconclass/txt_cliptextembeddings_" + i + ".csv")
                        )
                    )
                )
                    .withCSVParser(new CSVParserBuilder().withSeparator(';').withQuoteChar('"').build())
                    .withSkipLines(0)
                    .build()
            ) {
                String[] values;
                br.readNextSilently();
                while ((values = br.readNext()) != null) {
                    String id = values[0];
                    String txt = values[1];

                    String vectorStr = values[2];
                    vectorStr = vectorStr.replace("[", "");
                    vectorStr = vectorStr.replace("]", "");
                    vectorStr = vectorStr.replaceAll(" ", "");
                    //log.info(vectorStr);
                    List<Float> txtVector = Arrays
                        .asList(vectorStr.split(","))
                        .stream()
                        .map(e -> Float.parseFloat(e))
                        .collect(Collectors.toList());
                    double similarityImageToDictEntity = Math.abs(cosineSimilarity(txtVector, imgVector));
                    double normedSimilarityImageToDictEntity = (similarityImageToDictEntity + 1.) / 2.;
                    double similarityScore = normedSimilarityImageToDictEntity;

                    if (imgTextVector != null) {
                        double similarityImageTextToDictEntity = Math.abs(cosineSimilarity(txtVector, imgTextVector));
                        double normedSimilarityImageTextToDictEntity = (similarityImageTextToDictEntity + 1.) / 2.;
                        similarityScore = (normedSimilarityImageToDictEntity + normedSimilarityImageTextToDictEntity) / 2.;
                    }
                    JSONObject actResultObj = new JSONObject();
                    actResultObj.put("label", id + ": " + txt);
                    actResultObj.put("score", similarityScore);
                    resultArr.put(actResultObj);
                    //log.info(actResultObj.toString());
                }
                br.close();
            }
        }

        return resultArr;
    }

    private List<List<Float>> getClipVectorFromPluginResults(List<iart.client.PluginResult> pluginResults) {
        List<List<Float>> resultArr = new ArrayList<List<Float>>();
        for (PluginResult actEntry : pluginResults) {
            List<Float> actVector = actEntry.getResult().getFeature().getFeatureList();
            resultArr.add(actVector);
        }
        return resultArr;
    }

    @Override
    public ResponseEntity<String> getClipTextEmbedding(List<String> requestBody) {
        try {
            List<iart.client.PluginResult> response = iArtClient.analyze("ClipTextEmbeddingFeature", null, requestBody);
            List<List<Float>> resultVectors = getClipVectorFromPluginResults(response);
            String responseString = resultVectors.toString();
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getClipImageEmbedding(MultipartFile image) {
        try {
            List<iart.client.PluginResult> response = iArtClient.analyze("ClipImageEmbeddingFeature", image.getBytes(), null);
            List<List<Float>> resultVectors = getClipVectorFromPluginResults(response);
            String responseString = resultVectors.toString();
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static double cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Override
    public ResponseEntity<String> getParameterImageModels() {
        try {
            JSONArray resultArr = new JSONArray();

            List<PluginInfo> response = iArtClient.getPluginList();
            //System.out.println(response);
            for (PluginInfo pluginInfo : response) {
                JSONObject o = new JSONObject();
                o.put("name", pluginInfo.getName());
                o.put("type", pluginInfo.getType());
                resultArr.put(o);
            }
            JSONObject resultObj = new JSONObject();
            resultObj.put("models", resultArr);
            String responseString = resultObj.toString(0);
            //String responseString = new Gson().toJson(response);

            //log.info(responseString);

            return new ResponseEntity<String>(responseString, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getParameterTs4tibCollection() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            JSONArray resultArr = new JSONArray();
            HttpGet request = new HttpGet(new URI("https://service.tib.eu/ts4tib/api/ontologies/schemavalues?schema=collection"));
            request.addHeader("content-type", "application/json");
            CloseableHttpResponse response = httpClient.execute(request);
            String ts4tibResponse = EntityUtils.toString(response.getEntity());
            JSONArray collectionObjs = new JSONObject(ts4tibResponse).getJSONObject("_embedded").getJSONArray("strings");
            for (int i = 0; i < collectionObjs.length(); i++) {
                JSONObject actCollectionObj = collectionObjs.getJSONObject(i);
                resultArr.put(actCollectionObj.getString("content"));
            }
            JSONObject result = new JSONObject();
            result.put("collections", resultArr);
            String responseString = result.toString(0);
            //log.info(responseString);
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> getParameterTs4tibOntology(String collection) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            JSONArray resultArr = new JSONArray();
            HttpGet request = new HttpGet(new URI("https://service.tib.eu/ts4tib/api/ontologies?size=1000"));
            request.addHeader("content-type", "application/json");
            CloseableHttpResponse response = httpClient.execute(request);
            if (response != null && response.getStatusLine() != null) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    return new ResponseEntity<String>("Tib terminology service is unavailable", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            String ts4tibResponse = EntityUtils.toString(response.getEntity());
            //log.debug(ts4tibResponse);
            JSONArray ontologyObjs = new JSONObject(ts4tibResponse).optJSONObject("_embedded").optJSONArray("ontologies");
            for (int i = 0; i < ontologyObjs.length(); i++) {
                JSONObject actOntologyObj = ontologyObjs.getJSONObject(i);
                String ontoId = actOntologyObj.optString("ontologyId");
                String ontoLabel = actOntologyObj.getJSONObject("config").optString("title");
                JSONObject ontoCollectionsObj = actOntologyObj.getJSONObject("config").getJSONArray("classifications").optJSONObject(0);
                JSONArray ontoCollections = new JSONArray();
                if (ontoCollectionsObj != null) {
                    ontoCollections = ontoCollectionsObj.optJSONArray("collection");
                }
                JSONObject resultOntoObj = new JSONObject();
                resultOntoObj.put("paramValue", ontoId);
                resultOntoObj.put("label", ontoLabel);
                resultOntoObj.put("collections", ontoCollections);
                resultArr.put(resultOntoObj);
            }
            JSONObject result = new JSONObject();
            result.put("ontologies", resultArr);
            String responseString = result.toString(0);
            //log.info(responseString);
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> search(
        List<String> requestBody,
        SearchMode searchMode,
        Boolean wikidata,
        Boolean wikidataDbpedia,
        Boolean iconclass,
        Boolean ts4tib,
        String ts4tibOntology,
        Boolean lobidGnd,
        Boolean gettyAAT,
        boolean allowDuplicates
    ) {
        // decide which datasources to use
        // if no parameter is given, all datasources are used
        boolean useAllSources = true;

        if (wikidata != null || wikidataDbpedia != null || iconclass != null || ts4tib != null || lobidGnd != null || gettyAAT != null) {
            useAllSources = false;
        }

        // get Entity Recognition from Falcon API
        List<String> falconResults;
        if (useAllSources || (wikidata != null && wikidata) || (wikidataDbpedia != null && wikidataDbpedia)) {
            boolean useDbpedia = false;
            if (useAllSources || (wikidataDbpedia != null && wikidataDbpedia == true)) {
                useDbpedia = true;
            }
            try {
                falconResults = EntityRecognition.getFalconResults(requestBody, useDbpedia, searchMode);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to request Falcon API", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            falconResults = new ArrayList<>();
        }

        // get Entities from Iconclass
        List<String> iconclassNotations;
        if (useAllSources || (iconclass != null && iconclass)) {
            try {
                iconclassNotations = EntityRecognition.getIconclassNotations(requestBody);
                //System.out.println( "iconClassResults:"+iconclassNotations );
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to request Iconclass API", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            iconclassNotations = new ArrayList<>();
        }

        // get Entities from tib terminology service
        List<String> olsResults;
        if (useAllSources || (ts4tib != null && ts4tib)) {
            try {
                olsResults = EntityRecognition.getTs4TibResults(requestBody, ts4tibOntology);
                //System.out.println( "Tib Terminology service (TS4TIB) Results:"+olsResults );
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to request tib terminology service (TS4TIB) API", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            olsResults = new ArrayList<>();
        }

        // Get Entities from Lobid GND
        List<String> gndResults;
        if (useAllSources || (lobidGnd != null && lobidGnd)) {
            try {
                gndResults = EntityRecognition.getLobidGndResults(requestBody);
                //System.out.println("Lobid GND Results: " + gndResults);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to request Lobid GND API", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            gndResults = new ArrayList<>();
        }

        // Get Entities from Getty AAT
        List<String> aatResults;
        if (useAllSources || (gettyAAT != null && gettyAAT)) {
            try {
                aatResults = EntityRecognition.getGettyAATResults(requestBody);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Failed to request Getty AAT", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            aatResults = new ArrayList<>();
        }

        // init executorService for parallel fetching of results
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // init result containers for superclasses of the falcon entities
        Future<Map<String, String>> wikidataResultsByEntity = null;
        Future<Map<String, String>> dbpediaResultsByEntity = null;
        Future<Map<String, String>> iconclassResultsByNotation = null;
        Future<Map<String, String>> olsResultsByEntity = null;
        Future<Map<String, String>> gndResultsByEntity = null;
        Future<Map<String, String>> aatResultsByEntity = null;

        // get all superclasses for the falcon entities from wikidata
        try {
            Callable<Map<String, String>> wikiDataCall = new Callable<>() {
                @Override
                public Map<String, String> call() throws Exception {
                    return EntityRecognition.getWikiDataClasses(falconResults);
                }
            };
            wikidataResultsByEntity = executor.submit(wikiDataCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request WikiData API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // get all superclasses for the falcon entities from dbpedia

        try {
            Callable<Map<String, String>> dbPediaCall = new Callable<>() {
                @Override
                public Map<String, String> call() throws Exception {
                    return EntityRecognition.getDbPediaClasses(falconResults);
                }
            };
            dbpediaResultsByEntity = executor.submit(dbPediaCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request DBPedia API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // get all superclasses for the iconclass entities from iconclass
        try {
            Callable<Map<String, String>> iconclassCall = new Callable<>() {
                @Override
                public Map<String, String> call() throws Exception {
                    //return null;
                    return EntityRecognition.geIconclassSuperClasses(iconclassNotations);
                }
            };
            iconclassResultsByNotation = executor.submit(iconclassCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request Iconclass API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // get all superclasses for the ols entities from ols
        try {
            Callable<Map<String, String>> olsCall = new Callable<>() {
                @Override
                public Map<String, String> call() throws Exception {
                    //return null;
                    return EntityRecognition.getOlsSuperClasses(olsResults);
                }
            };
            olsResultsByEntity = executor.submit(olsCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request ols API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // get all superclasses for the gnd entities from lobid
        try {
            Callable<Map<String, String>> lobidGndCall = new Callable<>() {
                @Override
                public Map<String, String> call() throws Exception {
                    //return null;
                    return EntityRecognition.getLobidGndSuperClasses(gndResults);
                }
            };
            gndResultsByEntity = executor.submit(lobidGndCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request lobid gnd API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // get all superclasses for the Getty AAT entities
        try {
            Callable<Map<String, String>> aatCall = new Callable<>() {
                @Override
                public Map<String, String> call() throws Exception {
                    //return null;
                    return EntityRecognition.getGettyAATSuperClasses(aatResults);
                }
            };
            aatResultsByEntity = executor.submit(aatCall);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to request Getty AAT API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        while (
            !wikidataResultsByEntity.isDone() ||
            !dbpediaResultsByEntity.isDone() ||
            !iconclassResultsByNotation.isDone() ||
            !olsResultsByEntity.isDone() ||
            !gndResultsByEntity.isDone() ||
            !aatResultsByEntity.isDone()
        ) {
            log.debug("wait for completion");
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        log.info("Results are fetched, now building category tree...");

        // build hierarchy
        JSONObject finalResult = null;
        try {
            finalResult =
                EntityRecognition.combineResults(
                    falconResults,
                    wikidataResultsByEntity.get(),
                    dbpediaResultsByEntity.get(),
                    iconclassNotations,
                    iconclassResultsByNotation.get(),
                    olsResults,
                    olsResultsByEntity.get(),
                    gndResults,
                    gndResultsByEntity.get(),
                    aatResults,
                    aatResultsByEntity.get(),
                    allowDuplicates
                );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to build category tree", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // return result
        return new ResponseEntity<String>(finalResult.toString(), HttpStatus.OK);
    }
}
