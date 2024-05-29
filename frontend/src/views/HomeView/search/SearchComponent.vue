<script setup lang="ts">
import { onMounted, Ref, ref } from 'vue';
import * as d3 from 'd3';

import { IResults } from '../../../interfaces';
import { EnvService } from '../../../services/EnvService';
import { RestService } from '../../../services/RestService';
import { ResultsService } from '../../../services/ResultsService';
import TabsComponent from '../../../components/TabsComponent.vue';
import ButtonComponent from '../../../components/ButtonComponent.vue';
import LoaderComponent from '../../../components/LoaderComponent.vue';
import ResultsComponent from '../results/ResultsComponent.vue';
import HTMLResultsComponent from '../results/HTMLResultsComponent.vue';
import SearchTerminologyComponent from './SearchTerminologyComponent.vue';
import SearchEntityComponent from './SearchEntityComponent.vue';
import SearchImageComponent from './SearchImageComponent.vue';

const tabsEl: Ref<TabsComponent> = ref(null);
const loaderEl: Ref<LoaderComponent> = ref(null);
const resultsEl: Ref<ResultsComponent> = ref(null);
const searchTerminologyEl: Ref<SearchTerminologyComponent> = ref(null);
const searchEntityEl: Ref<SearchEntityComponent> = ref(null);
const searchImageEl: Ref<SearchImageComponent> = ref(null);

const resultsCache: Map<
  number,
  {
    results: IResults;

    isHTML?: boolean;
  }
> = new Map();

onMounted(() => {
  tabsEl.value.activateTab(parseInt(new URLSearchParams(window.location.search).get('s')));
  tabsEl.value.onTabChange((index: number) => {
    window.history.replaceState(null, null, `${document.location.pathname}?s=${index}`);

    ResultsService.clearResults();

    resultsEl.value.clearError();

    if (!resultsCache.has(index)) return;
    resultsCache.get(index).isHTML
      ? ResultsService.defineHTMLResults((resultsCache.get(index).results as unknown as { html: string }).html)
      : ResultsService.defineResults(resultsCache.get(index).results);
  });
});

function getActiveTab(): SearchTerminologyComponent {
  return (
    {
      0: searchTerminologyEl.value,
      1: searchEntityEl.value,
      2: searchImageEl.value,
    }[tabsEl.value.getIndex()] ?? searchTerminologyEl.value
  );
}

function search() {
  ResultsService.clearResults();

  let params;
  try {
    params = getActiveTab().getParams();
    for (let key in params.searchParams) {
      if (params.searchParams[key] !== false) continue;
      //delete params.searchParams[key]; // Force opt-in implicity
    }
  } catch (err) {
    EnvService.IS_DEV && console.error(err);

    resultsEl.value.setError(err);

    return;
  }

  resultsEl.value.clearError();
  loaderEl.value.start();

  (params.body === undefined
    ? RestService.GET(params.endpoint, params.searchParams, params.headers)
    : RestService.POST(params.endpoint, params.searchParams, params.body, params.headers)
  )
    .mock(params.mock)
    .call()
    .then((results: IResults) => {
      // TODO: TEMPORARY; Remove eventually:
      if (tabsEl.value.getIndex() === 1) {
        ResultsService.defineHTMLResults((results as unknown as { html: string }).html);

        resultsCache.set(1, {
          results,

          isHTML: true,
        });

        return;
      } else if (tabsEl.value.getIndex() === 2) {
        const processedResults: string = `
          <table style="border-spacing: 0; width: 100%; text-align: left;">
            <tr>
              <th style="padding: var(--space-xs);">Entity</th>
              <th style="padding-left: var(--space-xs);">Similarity Score</th>
            </tr>
            ${(results as unknown as [{ score: number; label: string }[]])[0]
              .sort((a, b) => b.score - a.score)
              .map(
                item => `<tr>
                <td style="padding: var(--space-xs); background-color: var(--color-bg-gray);">${
                  item.label
                }</td><td style="padding-left: var(--space-xs); width: 10rem; color: var(--color-code); font-weight: bold;">${item.score.toFixed(
                  3
                )}</td>
              </tr>`
              )
              .join('\n')}
          </table>
        `;
        ResultsService.defineHTMLResults(processedResults);

        resultsCache.set(2, {
          results: { html: processedResults } as unknown as IResults,

          isHTML: true,
        });

        return;
      }

      ResultsService.defineResults(results);

      resultsCache.set(tabsEl.value.getIndex(), {
        results,
      });
    })
    .catch((err: Error) => {
      EnvService.IS_DEV && console.error(err);

      resultsEl.value.setError(err);
    })
    .finally(() => loaderEl.value.stop());
}

function clear() {
  getActiveTab().clear();

  ResultsService.clearResults();

  resultsCache.delete(tabsEl.value.getIndex());

  resultsEl.value.clearError();
}
</script>

<template>
  <div class="search">
    <TabsComponent ref="tabsEl">
      <div tab-label="Terminology Search">
        <SearchTerminologyComponent ref="searchTerminologyEl" />
      </div>
      <div tab-label="Entity Linking">
        <SearchEntityComponent ref="searchEntityEl" />
      </div>
      <div tab-label="Image Recognition">
        <SearchImageComponent ref="searchImageEl" />
      </div>
    </TabsComponent>
    <div class="search-actions">
      <div></div>
      <div>
        <ButtonComponent @click="search()">Search</ButtonComponent>
      </div>
      <div>
        <ButtonComponent contrast @click="clear()">Clear</ButtonComponent>
      </div>
    </div>
  </div>
  <LoaderComponent ref="loaderEl" />
  <ResultsComponent ref="resultsEl" />
  <HTMLResultsComponent />
</template>

<style scoped lang="scss">
@import '@/assets/scss/shared';

.search {
  width: 100%;

  h4:not(:first-child) {
    margin-top: var(--space-s) !important;
  }

  &-actions {
    justify-content: space-between;
    margin-top: var(--space-m);
    width: 100%;

    @include flex;

    & > div {
      &:nth-child(2) {
        text-align: center;
      }
      &:nth-child(3) {
        text-align: right;
      }
    }
  }
}
</style>
