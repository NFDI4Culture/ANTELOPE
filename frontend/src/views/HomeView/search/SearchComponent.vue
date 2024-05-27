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
        ResultsService.defineHTMLResults(`
                    <svg #barchart id="barchart"></svg>
                `);

        resultsCache.set(2, {
          results,

          isHTML: true,
        });

        setTimeout(() => {
          const data = results[0] as unknown as {
            id: string;
            URI: string;
            label: string;
            source: string;
            classes: string;
            score: number;
          }[];
          const util = {
            label(d: any): string {
              if (d.label !== undefined) {
                // if (d.label.length < 24) {
                return d.label as string;
                // } else {
                //   return d.label.substring(0, 20) as string + "[..]";
                // }
              } else {
                return 'Label';
              }
            },
            score(d: any): number {
              if (d.score !== undefined) {
                return d.score as number;
              } else {
                return 0.0;
              }
            },
          };
          const margin = { top: 20, right: 20, bottom: 30, left: 40 },
            width = 700 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;
          const x = d3.scaleBand().range([0, width]).padding(0.1);
          const y = d3.scaleLinear().range([height, Math.min(...data.map((d: any) => d.score))]);
          // append the svg object to the body of the page
          // append a 'group' element to 'svg'
          // moves the 'group' element to the top left margin
          const svg = d3
            .select('#barchart')
            .attr('width', width + margin.left + margin.right)
            .attr('height', height + margin.top + margin.bottom)
            .append('g')
            .attr('transform', 'translate(' + margin.left.toString() + ',' + margin.top.toString() + ')');
          // Scale the range of the data in the domains
          x.domain(
            data.map(function (d: any): string {
              return util.label(d);
            })
          );
          // y.domain([Math.min(...data.map((d:any) => d.score)), Math.max(...data.map((d:any) => d.score))]);
          y.domain([0, 1]);
          // append the rectangles for the bar chart
          svg
            .selectAll('.bar')
            .data(data)
            .enter()
            .append('rect')
            .attr('class', 'bar')
            .attr('x', function (d: any): number {
              return x(util.label(d)) as number;
            })
            .attr('width', x.bandwidth())
            .attr('y', function (d: any): number {
              return y(util.score(d));
            })
            .attr('height', function (d: any) {
              return height - y(d.score);
            })
            .attr('fill', 'var(--color-primary)')
            .append('title')
            .text((d: any) => util.label(d) + ': ' + util.score(d).toString());
          svg
            .append('g')
            .attr('transform', 'translate(0,' + height.toString() + ')')
            .call(d3.axisBottom(x))
            .selectAll('text');
          svg.append('g').call(d3.axisLeft(y));
        }, 50);

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
