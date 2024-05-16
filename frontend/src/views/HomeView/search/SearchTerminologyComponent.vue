<script setup lang="ts">
    import { Ref, ref } from "vue";
    
    import { ITerminologySearchData } from "../../../interfaces";
    import InfoComponent from "../../../components/InfoComponent.vue";
    import TextareaComponent from "../../../components/TextareaComponent.vue";
    import CheckboxComponent from "../../../components/CheckboxComponent.vue";
        
    const tSearchtext: Ref<TextareaComponent> = ref(null);
    const cSource_Wikidata: Ref<CheckboxComponent> = ref(null);
    const cSource_WikidataDBPedia: Ref<CheckboxComponent> = ref(null);
    const cSource_Iconclass: Ref<CheckboxComponent> = ref(null);
    const cSource_GND: Ref<CheckboxComponent> = ref(null);
    const cSource_Getty: Ref<CheckboxComponent> = ref(null);
    const cSource_TS4TIB: Ref<CheckboxComponent> = ref(null);
    const cSettings_ClassDuplicates: Ref<CheckboxComponent> = ref(null);

    function validate(params: ITerminologySearchData): ITerminologySearchData {
        params.searchParams.searchtext = params.searchParams.searchtext.trim();
        if(!params.searchParams.searchtext.length) throw new RangeError("Search text must not be empty");
        
        return params;
    }
    
    defineExpose({
        clear() {
            tSearchtext.value.clear();

            cSource_Wikidata.value.reset();
            cSource_WikidataDBPedia.value.reset();
            cSource_Iconclass.value.reset();
            cSource_GND.value.reset();
            cSource_TS4TIB.value.reset();
            cSource_Getty.value.reset();
            cSettings_ClassDuplicates.value.reset();
        },
        getParams(): ITerminologySearchData {
            return validate({
                endpoint: "/annotation/terminology",
                searchParams: {
                    searchtext: tSearchtext.value.getText(),
                    wikidata: cSource_Wikidata.value.getChecked(),
                    wikidata_dbpedia: cSource_WikidataDBPedia.value.getChecked(),
                    iconclass: cSource_Iconclass.value.getChecked(),
                    gnd: cSource_GND.value.getChecked(),
                    ts4tib: cSource_TS4TIB.value.getChecked(),
                    aat: cSource_Getty.value.getChecked(),
                    allowDuplicates: cSettings_ClassDuplicates.value.getChecked()
                },
                
                mock: {"entities":[{"label":"Vincent van Gogh","id":"Q5582","source":"wikidata","URI":"http://www.wikidata.org/entity/Q5582"},{"label":"Vincent van Gogh","id":"Q5582","source":"wikidata","URI":"http://www.wikidata.org/entity/Q5582"},{"label":"Vincent van Gogh","id":"Q5582","source":"wikidata","URI":"http://www.wikidata.org/entity/Q5582"},{"label":"4457 van Gogh","id":"Q154160","source":"wikidata","URI":"http://www.wikidata.org/entity/Q154160"},{"label":"Van Gogh Museum","id":"Q224124","source":"wikidata","URI":"http://www.wikidata.org/entity/Q224124"},{"label":"Vincent van Gogh","id":"Q1960369","source":"wikidata","URI":"http://www.wikidata.org/entity/Q1960369"},{"label":"Vincent van Gogh","id":"Q1960369","source":"wikidata","URI":"http://www.wikidata.org/entity/Q1960369"},{"label":"Vincent Willem van Gogh","id":"Q2185351","source":"wikidata","URI":"http://www.wikidata.org/entity/Q2185351"},{"label":"Vincent van Gogh","id":"Q4013062","source":"wikidata","URI":"http://www.wikidata.org/entity/Q4013062"},{"label":"Vincent van Gogh","id":"Q4013062","source":"wikidata","URI":"http://www.wikidata.org/entity/Q4013062"},{"label":"Vincent_van_Gogh","id":"Vincent_van_Gogh","source":"dbpedia","URI":"http://dbpedia.org/resource/Vincent_van_Gogh"},{"label":"Vincent_van_Gogh_chronology","id":"Vincent_van_Gogh_chronology","source":"dbpedia","URI":"http://dbpedia.org/resource/Vincent_van_Gogh_chronology"},{"label":"Van_Gogh_Museum","id":"Van_Gogh_Museum","source":"dbpedia","URI":"http://dbpedia.org/resource/Van_Gogh_Museum"},{"label":"Van_Gogh_(Van_Gogh_album)","id":"Van_Gogh_(Van_Gogh_album)","source":"dbpedia","URI":"http://dbpedia.org/resource/Van_Gogh_(Van_Gogh_album)"},{"label":"Copies_by_Vincent_van_Gogh","id":"Copies_by_Vincent_van_Gogh","source":"dbpedia","URI":"http://dbpedia.org/resource/Copies_by_Vincent_van_Gogh"},{"label":"Portraits_by_Vincent_van_Gogh","id":"Portraits_by_Vincent_van_Gogh","source":"dbpedia","URI":"http://dbpedia.org/resource/Portraits_by_Vincent_van_Gogh"},{"label":"Fondation_Vincent_van_Gogh_Arles","id":"Fondation_Vincent_van_Gogh_Arles","source":"dbpedia","URI":"http://dbpedia.org/resource/Fondation_Vincent_van_Gogh_Arles"}],"hierarchy":{"children":[{"children":[{"children":[{"children":[{"children":[],"name":"minor planet","link":"http://www.wikidata.org/entity/Q1022867","id":"4"},{"children":[],"name":"astronomical object type","link":"http://www.wikidata.org/entity/Q17444909","id":"5"},{"children":[],"name":"small Solar System body","link":"http://www.wikidata.org/entity/Q193275","id":"6"}],"name":"asteroid","link":"http://www.wikidata.org/entity/Q3863","id":"3"}],"name":"4457 van Gogh","link":"http://www.wikidata.org/entity/Q154160","id":"2"},{"children":[{"children":[{"children":[],"name":"museum building","link":"http://www.wikidata.org/entity/Q24699794","id":"1004"},{"children":[],"name":"museum of culture","link":"http://www.wikidata.org/entity/Q28737012","id":"1005"},{"children":[],"name":"type of museum","link":"http://www.wikidata.org/entity/Q63982911","id":"1006"}],"name":"art museum","link":"http://www.wikidata.org/entity/Q207694","id":"1003"}],"name":"Van Gogh Museum","link":"http://www.wikidata.org/entity/Q224124","id":"1002"},{"children":[{"children":[{"children":[],"name":"mammal","link":"http://www.wikidata.org/entity/Q110551885","id":"2004"},{"children":[],"name":"natural person","link":"http://www.wikidata.org/entity/Q154954","id":"2005"},{"children":[],"name":"omnivore","link":"http://www.wikidata.org/entity/Q164509","id":"2006"},{"children":[],"name":"person","link":"http://www.wikidata.org/entity/Q215627","id":"2007"},{"children":[],"name":"individual animal","link":"http://www.wikidata.org/entity/Q26401003","id":"2008"},{"children":[],"name":"organisms known by a particular common name","link":"http://www.wikidata.org/entity/Q55983715","id":"2009"}],"name":"human","link":"http://www.wikidata.org/entity/Q5","id":"2003"}],"name":"Vincent Willem van Gogh","link":"http://www.wikidata.org/entity/Q2185351","id":"2002"},{"children":[],"name":"Vincent van Gogh","link":"http://www.wikidata.org/entity/Q1960369","id":"3002"},{"children":[{"children":[{"children":[],"name":"moving image","link":"http://www.wikidata.org/entity/Q10301427","id":"4004"},{"children":[],"name":"series","link":"http://www.wikidata.org/entity/Q20937557","id":"4005"},{"children":[],"name":"audiovisual work","link":"http://www.wikidata.org/entity/Q2431196","id":"4006"},{"children":[],"name":"visual artwork","link":"http://www.wikidata.org/entity/Q4502142","id":"4007"}],"name":"film","link":"http://www.wikidata.org/entity/Q11424","id":"4003"}],"name":"Vincent van Gogh","link":"http://www.wikidata.org/entity/Q4013062","id":"4002"},{"children":[],"name":"Vincent van Gogh","link":"http://www.wikidata.org/entity/Q5582","id":"5002"}],"name":"Wikidata","link":"http://wikidata.org","id":1},{"children":[{"children":[{"children":[{"children":[],"name":"work","link":"http://dbpedia.org/ontology/Work","id":"1003"}],"name":"artwork","link":"http://dbpedia.org/ontology/Artwork","id":"1002"}],"name":"Copies_by_Vincent_van_Gogh","link":"http://dbpedia.org/resource/Copies_by_Vincent_van_Gogh","id":"1001"},{"children":[{"children":[],"name":"agent","link":"http://dbpedia.org/ontology/Agent","id":"2002"},{"children":[],"name":"architectural structure","link":"http://dbpedia.org/ontology/ArchitecturalStructure","id":"2003"},{"children":[],"name":"building","link":"http://dbpedia.org/ontology/Building","id":"2004"},{"children":[],"name":"museum","link":"http://dbpedia.org/ontology/Museum","id":"2005"},{"children":[],"name":"organisation","link":"http://dbpedia.org/ontology/Organisation","id":"2006"}],"name":"Fondation_Vincent_van_Gogh_Arles","link":"http://dbpedia.org/resource/Fondation_Vincent_van_Gogh_Arles","id":"2001"},{"children":[],"name":"Portraits_by_Vincent_van_Gogh","link":"http://dbpedia.org/resource/Portraits_by_Vincent_van_Gogh","id":"3001"},{"children":[{"children":[{"children":[],"name":"musical work","link":"http://dbpedia.org/ontology/MusicalWork","id":"4003"}],"name":"album","link":"http://dbpedia.org/ontology/Album","id":"4002"}],"name":"Van_Gogh_(Van_Gogh_album)","link":"http://dbpedia.org/resource/Van_Gogh_(Van_Gogh_album)","id":"4001"},{"children":[],"name":"Van_Gogh_Museum","link":"http://dbpedia.org/resource/Van_Gogh_Museum","id":"5001"},{"children":[{"children":[{"children":[{"children":[],"name":"species","link":"http://dbpedia.org/ontology/Species","id":"6004"}],"name":"eukaryote","link":"http://dbpedia.org/ontology/Eukaryote","id":"6003"}],"name":"animal","link":"http://dbpedia.org/ontology/Animal","id":"6002"},{"children":[{"children":[],"name":"person","link":"http://dbpedia.org/ontology/Person","id":"6006"}],"name":"artist","link":"http://dbpedia.org/ontology/Artist","id":"6003"},{"children":[],"name":"writer","link":"http://dbpedia.org/ontology/Writer","id":"6007"}],"name":"Vincent_van_Gogh","link":"http://dbpedia.org/resource/Vincent_van_Gogh","id":"6001"},{"children":[],"name":"Vincent_van_Gogh_chronology","link":"http://dbpedia.org/resource/Vincent_van_Gogh_chronology","id":"7001"}],"name":"DBpedia","link":"http://dbpedia.org","id":1000}],"name":"results","link":"","id":"0"},"relations":[]}
            });
        }
    });
</script>

<template>
    <TextareaComponent placeholder="e.g. Vincent Van Gogh" ref="tSearchtext">
        <template #prompt>
            Type the term you want to search for (single word or phrase)
        </template>
        <template #info>
            Terminology search is used to search for a term in a controlled vocabulary. The annotation service will propose entities, that are best matching the whole provided phrase. If you want to receive all contained entities within your phrase, choose 'Entity recognition' above.
        </template>
    </TextareaComponent>
    <h4>
        Terminology sources
        <InfoComponent>
            Please select the datasources you want to search. Currently we don't provide DBpedia as a standalone source.
        </InfoComponent>
    </h4>
    <CheckboxComponent ref="cSource_Wikidata">WIKIDATA</CheckboxComponent>
    <CheckboxComponent ref="cSource_WikidataDBPedia" checked>WIKIDATA + DBPedia</CheckboxComponent>
    <CheckboxComponent ref="cSource_Iconclass" checked>ICONCLASS</CheckboxComponent>
    <CheckboxComponent ref="cSource_GND">GND (Gemeinsame Normdatei)</CheckboxComponent>
    <CheckboxComponent ref="cSource_Getty">Getty</CheckboxComponent>
    <CheckboxComponent ref="cSource_TS4TIB">TIB Terminology Service</CheckboxComponent>
    <h4>Settings</h4>
    <CheckboxComponent ref="cSettings_ClassDuplicates">
        Show Class Duplicates
        <InfoComponent>
            If set, the result hierarchy graph may contain the same class multiple times (in different branches). This will give you a more complete view of the original results but may show the same information multiple times. Every class will still be shown only once per branch in the hierarchy tree to avoid circles (circles are possible in graphs but not in trees like taxonomies).
        </InfoComponent>
    </CheckboxComponent>
</template>