<script setup lang="ts">
import { onMounted, Ref, ref } from 'vue';

import { ITerminologySearchData } from '../../../interfaces';
import { RestService } from '../../../services/RestService';
import InfoComponent from '../../../components/InfoComponent.vue';
import TextareaComponent from '../../../components/TextareaComponent.vue';
import CheckboxComponent from '../../../components/CheckboxComponent.vue';
import CheckbuttonComponent from '../../../components/CheckbuttonComponent.vue';
import LoaderComponent from '../../../components/LoaderComponent.vue';

type TTS4TIBOntology = {
  label: string;
  paramValue: string;
  collections?: string[];
};

const tSearchtext: Ref<TextareaComponent> = ref(null);
const cSource_Wikidata: Ref<CheckboxComponent> = ref(null);
const cSource_WikidataDBPedia: Ref<CheckboxComponent> = ref(null);
const cSource_Iconclass: Ref<CheckboxComponent> = ref(null);
const cSource_GND: Ref<CheckboxComponent> = ref(null);
const cSource_Getty: Ref<CheckboxComponent> = ref(null);
const cSource_TS4TIB: Ref<CheckboxComponent> = ref(null);
const cSettings_ClassDuplicates: Ref<CheckboxComponent> = ref(null);
const loaderTS4TIBEl: Ref<LoaderComponent> = ref(null);

const wikidataCheckboxDisabled = ref(true);
const ts4tibCheckboxDisabled = ref(true);

const ts4tibCollections: Ref<string[]> = ref([]);
const ts4tibOntologies: Ref<TTS4TIBOntology[]> = ref([]);

function changeWikidataDBPedia() {
  wikidataCheckboxDisabled.value = cSource_WikidataDBPedia.value.getChecked();
  cSource_Wikidata.value.setChecked(false);
}
onMounted(changeWikidataDBPedia);

onMounted(() => {
  loaderTS4TIBEl.value.start();
  let openResponses = 2;
  const complete = () => {
    if (--openResponses) return;
    loaderTS4TIBEl.value.stop();
  };

  RestService.GET('/annotation/parameterOptions/ts4tib_collection')
    .mock({
      collections: [
        'DataPLANT',
        'ESS',
        'NFDI4CHEM',
        'Educational Resources',
        'FID move',
        'NFDI4CAT',
        'FAIR Data Spaces',
        'TIB',
        'Foundational Ontologies',
        'FID BAUdigital',
        'CoyPu',
        'NFDI4CULTURE',
        'NFDI4Energy',
        'NFDI4ING',
      ],
    })
    .call()
    .then((response: { collections: string[] }) => {
      ts4tibCollections.value = response.collections;
    })
    .finally(complete);

  RestService.GET('/annotation/parameterOptions/ts4tib_ontology')
    .mock({
      ontologies: Array(500).fill({ collections: ['FAIR Data Spaces', 'ESS'], label: 'ABCD Base Ontology', paramValue: 'abcd' }) /* [
        { collections: ['FAIR Data Spaces', 'ESS'], label: 'ABCD Base Ontology', paramValue: 'abcd' },
        { collections: ['FAIR Data Spaces'], label: 'Audubon Core main vocabulary', paramValue: 'ac' },
        { collections: ['FAIR Data Spaces'], label: 'variant controlled vocabulary', paramValue: 'acvariant' },
        { collections: ['NFDI4CHEM'], label: 'CHEBI Integrated Role Ontology', paramValue: 'chiro' },
        { collections: ['NFDI4ING', 'NFDI4CHEM', 'ESS'], label: 'The Data Use Ontology', paramValue: 'duo' },
        {
          collections: ['FAIR Data Spaces', 'NFDI4CHEM', 'ESS', 'DataPLANT', 'NFDI4CAT'],
          label: 'The Environment Ontology',
          paramValue: 'envo',
        },
        { collections: ['NFDI4CHEM'], label: 'Physico-chemical methods and properties', paramValue: 'fix' },
      ] */,
    })
    .call()
    .then((response: { ontologies: TTS4TIBOntology[] }) => {
      ts4tibOntologies.value = response.ontologies;
    })
    .finally(complete);
});

function getSelectedTS4TIB(category: 'collection' | 'ontology'): string[] {
  return Array.from(document.querySelectorAll(`.ts4tib-${category}`))
    .map((selectElement: HTMLElement) => {
      return selectElement.querySelector('input').checked ? selectElement.getAttribute('data-id') : null;
    })
    .filter((id: string) => id);
}

function validate(params: ITerminologySearchData): ITerminologySearchData {
  params.searchParams.searchtext = params.searchParams.searchtext.trim();
  if (!params.searchParams.searchtext.length) throw new RangeError('Search text must not be empty');

  if ((params.searchParams.ts4tib_collection ?? '').length === 0) delete params.searchParams.ts4tib_collection;
  if ((params.searchParams.ts4tib_ontology ?? '').length === 0) delete params.searchParams.ts4tib_ontology;
  if (!params.searchParams?.ts4tib_collection && !params.searchParams.ts4tib_ontology) delete params.searchParams.ts4tib;

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

    wikidataCheckboxDisabled.value = !cSource_Wikidata.value.getChecked();
    ts4tibCheckboxDisabled.value = !cSource_TS4TIB.value.getChecked();
  },
  async getParams(): Promise<ITerminologySearchData> {
    return validate({
      endpoint: '/annotation/terminology',
      searchParams: {
        searchtext: tSearchtext.value.getText(),
        wikidata: cSource_Wikidata.value.getChecked(),
        wikidata_dbpedia: cSource_WikidataDBPedia.value.getChecked(),
        iconclass: cSource_Iconclass.value.getChecked(),
        gnd: cSource_GND.value.getChecked(),
        ts4tib: cSource_TS4TIB.value.getChecked(),
        aat: cSource_Getty.value.getChecked(),
        allowDuplicates: cSettings_ClassDuplicates.value.getChecked(),

        ...(cSource_TS4TIB.value.getChecked()
          ? {
              ts4tib_collection: encodeURIComponent(getSelectedTS4TIB('collection').join(',')),
              ts4tib_ontology: encodeURIComponent(getSelectedTS4TIB('ontology').join(',')),
            }
          : {}),
      },

      mock: (await import('./mock.terminology.json')).default,
    });
  },
});
</script>

<template>
  <TextareaComponent placeholder="e.g. Vincent Van Gogh" ref="tSearchtext">
    <template #prompt> Type the term you want to search for (single word or phrase) </template>
    <template #info>
      Terminology search is used to search for a term in a controlled vocabulary. The annotation service will propose entities, that are
      best matching the whole provided phrase. If you want to receive all contained entities within your phrase, choose 'Entity recognition'
      above.
    </template>
  </TextareaComponent>
  <h4>
    Terminology sources
    <InfoComponent>
      Please select the datasources you want to search. Currently we don't provide DBpedia as a standalone source.
    </InfoComponent>
  </h4>
  <CheckboxComponent ref="cSource_Wikidata" :class="wikidataCheckboxDisabled ? 'disable' : ''">WIKIDATA</CheckboxComponent>
  <CheckboxComponent ref="cSource_WikidataDBPedia" checked @change="changeWikidataDBPedia()">WIKIDATA + DBPedia</CheckboxComponent>
  <CheckboxComponent ref="cSource_Iconclass" checked>ICONCLASS</CheckboxComponent>
  <CheckboxComponent ref="cSource_GND">GND (Gemeinsame Normdatei)</CheckboxComponent>
  <CheckboxComponent ref="cSource_Getty">Getty</CheckboxComponent>
  <CheckboxComponent ref="cSource_TS4TIB" @change="ts4tibCheckboxDisabled = !ts4tibCheckboxDisabled"
    >TIB Terminology Service</CheckboxComponent
  >
  <div class="ts4tib" v-show="!ts4tibCheckboxDisabled">
    <LoaderComponent ref="loaderTS4TIBEl" />
    <h5>Collections</h5>
    <div class="ts4tib-selects">
      <div class="ts4tib-selects-wrapper">
        <CheckbuttonComponent
          class="ts4tib-collection"
          v-for="(collection, index) in ts4tibCollections"
          :key="index"
          :data-id="collection"
          >{{ collection }}</CheckbuttonComponent
        >
      </div>
    </div>
    <h5>Ontologies</h5>
    <div class="ts4tib-selects">
      <div class="ts4tib-selects-wrapper">
        <CheckbuttonComponent
          class="ts4tib-ontology"
          v-for="(ontology, index) in ts4tibOntologies"
          :key="index"
          :data-id="ontology.label"
          >{{ ontology.label }}</CheckbuttonComponent
        >
      </div>
    </div>
  </div>
  <h4>Settings</h4>
  <CheckboxComponent ref="cSettings_ClassDuplicates">
    Show Class Duplicates
    <InfoComponent>
      If set, the result hierarchy graph may contain the same class multiple times (in different branches). This will give you a more
      complete view of the original results but may show the same information multiple times. Every class will still be shown only once per
      branch in the hierarchy tree to avoid circles (circles are possible in graphs but not in trees like taxonomies).
    </InfoComponent>
  </CheckboxComponent>
</template>

<style scoped lang="scss">
@import '@/assets/scss/shared';

.ts4tib {
  &-selects {
    position: relative;
    margin-bottom: calc(-1 * var(--space-xs));

    &-wrapper {
      max-height: 20rem;
      overflow-y: scroll;
    }

    &::after {
      content: '';
      position: absolute;
      display: block;
      bottom: 0;
      width: 100%;
      height: 0.75rem;
      background: linear-gradient(0deg, var(--color-bg-light) 0%, transparent 100%);
      pointer-events: none;
    }
  }
}
</style>
