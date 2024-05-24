<script setup lang="ts">
import { Ref, ref } from 'vue';

import { makeDictionary } from './util';
import { IEntitySearchData, ISearchDictionary } from '../../../interfaces';
import InfoComponent from '../../../components/InfoComponent.vue';
import TextareaComponent from '../../../components/TextareaComponent.vue';
import CheckboxComponent from '../../../components/CheckboxComponent.vue';
import ButtonComponent from '../../../components/ButtonComponent.vue';
import SliderComponent from '../../../components/SliderComponent.vue';

const customDictInputEl: Ref<HTMLElement> = ref(null);

const cCustomDictInputToggle: Ref<CheckboxComponent> = ref(null);
const tSearchtext: Ref<TextareaComponent> = ref(null);
const tDictionary: Ref<TextareaComponent> = ref(null);
const cSource_Iconclass: Ref<CheckboxComponent> = ref(null);
const cSettings_ClassDuplicates: Ref<CheckboxComponent> = ref(null);
const sSimilarity: Ref<SliderComponent> = ref(null);

const exampleDicts = {
  LIST: ['michelangelo', 'van gogh'],
  SIMPLE: { artist: ['michelangelo', 'van gogh'] },
  EXTENDED: { entity1: { label: 'artist', patterns: ['michelangelo', 'van gogh'], kb_id: 'entity1', kb_url: 'entity1_url' } },
};

function setDictExample(exampleObj: unknown) {
  tDictionary.value.setText(JSON.stringify(exampleObj, null, 2));
  tDictionary.value.expand();
}

function validate(params: IEntitySearchData): IEntitySearchData {
  params.body.text = params.body.text.trim();
  if (!params.body.text.length) throw new RangeError('Search text must not be empty');
  if (!params.body.dictionary && !params.searchParams.iconclass)
    throw new RangeError('Select a predefined dictionary or provide one individually');

  return params;
}

defineExpose({
  clear() {
    tSearchtext.value.clear();
    tDictionary.value.clear();
    tDictionary.value.collapse();

    cSource_Iconclass.value.reset();

    sSimilarity.value.reset();
  },
  getParams(): IEntitySearchData {
    return validate({
      endpoint: '/annotation/entitylinking/text',
      searchParams: {
        ...(!tDictionary.value.getText().trim().length ? { iconclass: cSource_Iconclass.value.getChecked() } : {}),

        allowDuplicates: cSettings_ClassDuplicates.value.getChecked(),
      },
      body: {
        text: tSearchtext.value.getText(),
        dictionary: tDictionary.value.getText().trim().length ? makeDictionary(tDictionary.value.getText()) : null,
        threshold: sSimilarity.value.getValue(),
        language: 'en',
      },

      mock: {
        json: {
          el_result: {
            ents: [{ start: 8, end: 16, label: 'van gogh', text: 'van gogh', idx: 1 }],
            doc: 'Vincent van Gogh was a dutch post-impressionist painter',
            ids: { '1': 'van gogh', '2': 'van gogh' },
          },
        },
        html: '<div class="entities" style="line-height: 2.5; direction: ltr">Vincent \n<mark class="entity" style="background: #ddd; padding: 0.45em 0.6em; margin: 0 0.25em; line-height: 1; border-radius: 0.35em;">\n    van Gogh\n    <span style="font-size: 0.8em; font-weight: bold; line-height: 1; border-radius: 0.35em; vertical-align: middle; margin-left: 0.5rem">van gogh<\/span>\n<\/mark>\n was a dutch post-impressionist painter<\/div>',
      },
    });
  },
});
</script>

<template>
  <TextareaComponent placeholder="e.g. Vincent van Gogh was a dutch post-impressionist painter" ref="tSearchtext">
    <template #prompt> Type the text you want to run entity linking on (single words or whole sentences) </template>
    <template #info>
      Use Entity recognition to map as many words as possible, of your input text, to entities from the datasources. If you want to search
      for a single term, that is described by your text, choose 'Terminology search' above.
    </template>
  </TextareaComponent>
  <h4>
    Dictionaries
    <InfoComponent> Choose an existing dictionary or define your own dictionary (also in combination). </InfoComponent>
  </h4>
  <CheckboxComponent ref="cSource_Iconclass" checked>ICONCLASS</CheckboxComponent>
  <div ref="customDictInputEl">
    <TextareaComponent placeholder="Start with an example below" monospace ref="tDictionary" />
    <div class="search-entity-examples">
      <span class="desktop">Apply Example</span>
      <ButtonComponent contrast small @click="setDictExample(exampleDicts.LIST)">Entity List</ButtonComponent>
      <ButtonComponent contrast small @click="setDictExample(exampleDicts.SIMPLE)">Labelled Entity Lists</ButtonComponent>
      <ButtonComponent contrast small @click="setDictExample(exampleDicts.EXTENDED)">Arbitrary</ButtonComponent>
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
  <SliderComponent default="0.6" ref="sSimilarity">
    <template #label>
      Similarity
      <InfoComponent>
        If set, the result hierarchy graph may contain the same class multiple times (in different branches). This will give you a more
        complete view of the original results but may show the same information multiple times. Every class will still be shown only once
        per branch in the hierarchy tree to avoid circles (circles are possible in graphs but not in trees like taxonomies).
      </InfoComponent>
    </template>
    <template #label-min>narrow</template>
    <template #label-max>wide</template>
  </SliderComponent>
</template>

<style scoped lang="scss">
@import '@/assets/scss/shared';

.search-entity {
  &-examples {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
    margin-top: calc(-1 * var(--space-s));
    margin-bottom: var(--space-m);
    color: var(--color-fg-gray);

    span {
      margin-right: var(--space-s);

      @include font-s;
    }
  }
}
</style>
