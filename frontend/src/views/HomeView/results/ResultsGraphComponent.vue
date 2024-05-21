<script setup lang="ts">
import { onMounted, Ref, ref } from 'vue';
import * as d3 from 'd3';
import { HierarchyNode } from 'd3';

import { IEntity, IResults, IHierarchyNode } from '../../../interfaces';
import { ResultsService } from '../../../services/ResultsService';
import { EntitySelectionService } from '../../../services/EntitySelectionService';
import { ScrollAbilityService } from '../../../services/ScrollAbilityService';
import ButtonComponent from '../../../components/ButtonComponent.vue';
import EntityComponent from '../../../components/EntityComponent.vue';

const TRANSPARENT = '#00000000';

const preselectedEntity: Ref<IEntity> = ref(null);
const hasPreselectedEntity = ref(false);
const interactionInfoHTML = ref('');

const svgEl: Ref<HTMLElement> = ref(null);

ResultsService.on('clear', () => {
  d3.select('#tree').selectAll('*').remove();
});

onMounted(() => {
  document.addEventListener('keydown', e => {
    if (e.keyCode !== 16) return;
    enableSVGInteraction();
  });
  document.addEventListener('keyup', disableSVGInteraction);

  disableSVGInteraction();
});

function getNodeId(entity: IEntity): string {
  return `node--${entity.id}-${entity.label.replace(/[^\w\d_-]+/g, '')}`;
}

function getNodeChildren(entity: IEntity): HTMLElement[] {
  return svgEl.value.querySelector(`#${getNodeId(entity)}`).children as unknown as HTMLElement[];
}

function calcTextWidth(text: string, fontSize: string | number = '1rem'): number {
  const dummy = document.createElement('div');
  dummy.textContent = text;
  dummy.style.width = 'fit-content';
  dummy.style.fontSize = fontSize.toString();
  dummy.style.visibility = 'hidden';
  document.body.appendChild(dummy);
  const width = dummy.offsetWidth;
  document.body.removeChild(dummy);
  return width;
}

function enableSVGInteraction() {
  svgEl.value.classList.add('active');

  interactionInfoHTML.value = "Release <b style='color: var(--color-error);'>SHIFT</b> to disable graph interaction";

  ScrollAbilityService.disableScroll();
}
function disableSVGInteraction() {
  svgEl.value.classList.remove('active');

  interactionInfoHTML.value = 'Hold <b>SHIFT</b> to interact with the tree';

  ScrollAbilityService.enableScroll();
}

function mouseEnableSVGInteraction(e) {
  /* e.preventDefault();

        enableSVGInteraction();

        svgEl.value.dispatchEvent(new MouseEvent("mousedown", {
            screenX: e.screenX,
            screenY: e.screenY,
            clientX: e.clientX,
            clientY: e.clientY,
            relatedTarget: e.target,
            // @ts-ignore
            view: window
        })); */
}

function selectPreselected() {
  EntitySelectionService.select(preselectedEntity.value);
}
function viewPreselected() {
  window.open(preselectedEntity.value.URI, '_blank').focus();
}

//setTimeout(() => preselect({"label":"Vincent van Gogh","id":"Q5582","source":"wikidata","URI":"http://www.wikidata.org/entity/Q5582"}), 2000);

let unselectPreselectedTimeout;

function preselect(entity: IEntity) {
  unselectPreselected();

  clearTimeout(unselectPreselectedTimeout);

  hasPreselectedEntity.value = true;
  preselectedEntity.value = entity;

  const entityNodeChildren = getNodeChildren(preselectedEntity.value);
  entityNodeChildren[2].style.stroke = 'var(--color-fg-dark)';
  entityNodeChildren[3].style.stroke = 'var(--color-fg-dark)';
}

function unselectPreselected() {
  if (!hasPreselectedEntity.value) return;

  const entityNodeChildren = getNodeChildren(preselectedEntity.value);
  entityNodeChildren[2].style.stroke = TRANSPARENT;
  entityNodeChildren[3].style.stroke = TRANSPARENT;

  hasPreselectedEntity.value = false;

  unselectPreselectedTimeout = setTimeout(() => {
    preselectedEntity.value = null;
  }, 450);
}

type NodeData = { x: number; y: number; parentId: string };

ResultsService.on('define', (results: IResults) => {
  createTree(results.hierarchy, {
    label(d: any): string {
      if (d.data.name !== undefined) {
        if (d.data.name.length < 40) {
          return d.data.name as string;
        } else {
          return (d.data.name.substring(0, 40 - 4) as string) + '[..]';
        }
      } else {
        return 'label';
      }
    },
    title: (d: any, n: d3.HierarchyNode<IHierarchyNode>): string => n.data.name + ' (' + n.data.link + ')', // hover text
    link: (d: any, n: d3.HierarchyNode<IHierarchyNode>): string => n.data.link,

    width: 800,
  });
});

// Copyright 2021 Observable, Inc.
// Released under the ISC license.
// https://observablehq.com/@d3/tree
function createTree(
  data: any,
  {
    // data is either tabular (array of objects) or hierarchy (nested objects)
    // path, // as an alternative to id and parentId, returns an array identifier, imputing internal nodes
    id = Array.isArray(data) ? (d: d3.HierarchyNode<NodeData>) => d.id : null, // if tabular data, given a d in data, returns a unique identifier (string)
    parentId = Array.isArray(data) ? (d: d3.HierarchyNode<NodeData>) => d.parent?.id : null, // if tabular data, given a node d, returns its parent’s identifier
    children = (d: any): Iterable<any> | null | undefined => (d !== undefined ? (d.children as Iterable<any>) : []), // if hierarchical data, given a d in data, returns its children
    tree = d3.tree, // layout algorithm (typically d3.tree or d3.cluster)
    sort = null, // how to sort nodes prior to layout (e.g., (a, b) => d3.descending(a.height, b.height))
    label = (d: any): string => 'label', // given a node d, returns the display name
    title = (d: any, n: any): string => 'title', // given a node d, returns its hover text
    link = (d: any, n: any): string => 'link', // given a node d, its link (if any)
    width = 640, // outer width, in pixels
    height = 1200, // outer height, in pixels
    r = 10, // radius of nodes
    padding = 1, // horizontal padding for first and last column
    fillBackgroundClass = '#F9CD0E',
    fillBackgroundInstance = '#18A0FB',
    fillBackgroundSource = '#C4C4C4',
    fillForeground = 'var(--color-bg-light)',
    stroke = 'var(--color-fg-dark)', // stroke for links,
    strokeWidth = 0.75, // stroke width for links
    strokeOpacity = 0.25, // stroke opacity for links
    strokeLinejoin = null, // stroke line join for links
    strokeLinecap = null, // stroke line cap for links
  }
): void {
  // If id and parentId options are specified, or the path option, use d3.stratify
  // to convert tabular data to a hierarchy; otherwise we assume that the data is
  // specified as an object {children} with nested objects (a.k.a. the “flare.json”
  // format), and use d3.hierarchy.

  const root: d3.HierarchyNode<NodeData> =
    id != null || parentId != null
      ? (d3
          .stratify()
          .id((d: any, i: any, data2: any): string | null | undefined => d['id'] as string)
          .parentId((d: any, i: any, data2: any): string | null | undefined => d['parentId'] as string)(data) as HierarchyNode<NodeData>)
      : (d3.hierarchy(data, children) as HierarchyNode<NodeData>);

  // Compute labels and titles.
  const descendants = root.descendants();
  const L:
    | {
        label: string;
        depth: number;
      }[]
    | null = descendants
    .map((d: any) => ({
      label: label(d),
      depth: d.depth,
    }))
    .filter((d: any) => d.depth >= 2);

  // Compute the layout.
  const dx = 30; // Vertical margin between nodes
  const dy = [...L].sort((a, b) => b.label.length - a.label.length)[0].label.length * 10;
  tree().nodeSize([dx, dy])(root);

  // Center the tree.
  let x0 = Infinity;
  let x1 = -x0;
  root.each((d: any) => {
    if (d.depth < 2) {
      return;
    }
    if (d.x > x1) {
      x1 = d.x;
    }
    if (d.x < x0) {
      x0 = d.x;
    }
  });

  // Compute the default height.
  // if (!height) {height = x1 - x0 + dx * 2;}
  // if (!width) {width = x1 - x0 + dx * 2;}
  height = x1 - x0 + dx * 2;
  width = x1 - x0 + dy * 2;
  const linkGenerator = d3
    .linkHorizontal()
    .x((d: any) => d.y as number)
    .y((d: any) => d.x as number);
  /* const lineGenerator = d3.line<d3.HierarchyPointNode<NodeData>>()
        .x((d: any) => d.x as number)
        .y((d: any) => d.y as number); */

  const levelsH: number = [...L].map(l => l.depth).sort((a, b) => b - a)[0];
  const levelsV: number = (Object.entries(
    [...L].reduce((a, b) => {
      a[b.depth] = (isNaN(a[b.depth]) ? 0 : a[b.depth]) + 1;
      return a;
    }, {} as { [key: string]: number })
  )
    .sort((a, b) => a[1] - b[1])
    .pop() ?? [null, 0])[1];

  // SVG
  const svg = d3
    .select('#tree')
    .attr('viewBox', [-dy * padding, x0 - dx, width, height])
    // .attr("viewBox", [x1-dy , x0 - dx, width, height])
    // .attr("viewBow", [0,0, width, height])
    .attr('width', '100%')
    .attr('height', height)
    .attr('style', 'position: relative; max-width: 100%; height: auto; height: intrinsic; margin: 10px; margin-bottom: 150px; z-index:100;')
    .attr('font-family', 'sans-serif')
    .attr('font-size', '1rem');

  // GRAPH
  const g = svg.append('g');

  const coreZoom = Math.max(Math.min(root.descendants().length, 50), 10) / 50;
  const coreTransform = `translate(${-1.75 * coreZoom * dy}, ${dx / 2}) scale(${coreZoom})`;

  // LINKS
  g.append('g')
    .attr('transform', coreTransform)
    .attr('fill', 'none')
    .attr('stroke', stroke)
    .attr('stroke-opacity', strokeOpacity)
    .attr('stroke-linecap', strokeLinecap)
    .attr('stroke-linejoin', strokeLinejoin)
    .attr('stroke-width', strokeWidth)
    .selectAll('path')
    .data(root.links().filter((d: any) => d.source.depth >= 2))
    .join('path')
    .attr('d', (d: any) => linkGenerator(d) as unknown as string);

  // NODE
  const node = g
    .append('g')
    .attr('transform', coreTransform)
    .selectAll('a')
    .data(root.descendants().filter((d: any) => d.depth >= 2))
    .join('a')
    .attr('id', (d: any) => getNodeId({ id: d.data.id, label: label(d) } as IEntity))
    .attr('transform', (d: any) => `translate(${d.y as string},${d.x as string})`)
    // .attr("xlink:href", (d: any) => link(d.data, d))
    // .attr("target", linkTarget)
    .attr('data-id', 3);

  // TITLE
  node.append('title').text((d: any) => title(d.data, d));

  // BACKGROUND
  node
    .append('rect')
    .attr('rx', 5)
    .attr('ry', 5)
    .attr('x', function () {
      return this.getBBox().x - 8;
    })
    .attr('y', function () {
      return this.getBBox().y - 15;
    })
    .attr('width', function (_, i: any) {
      return L[i].label.length * 14;
    })
    .attr('height', 30)
    .style('fill', 'var(--graph-bg-color)');

  const getFill = (_: any, i: number): string => {
    switch (L[i].depth) {
      case 0:
        return fillBackgroundSource;
      case 1:
        return fillBackgroundSource;
      case 2:
        return fillBackgroundInstance;
      default:
        return fillBackgroundClass;
    }
  };

  // CIRCLE
  node.append('circle').attr('fill', getFill).attr('stroke', TRANSPARENT).attr('stroke-width', '1.35').attr('r', r);

  // INNER CIRCLE
  /* node.append("circle")
        .attr("fill", getFill)
        .attr("r", r / 2); */

  // TEXT
  node
    .append('text')
    .attr('dy', '0.4em')
    .attr('x', 20)
    .attr('text-anchor', 'start')
    .attr('paint-order', 'stroke')
    .attr('fill', stroke)
    .attr('stroke', TRANSPARENT)
    .attr('stroke-width', '0.25')
    .attr('stroke-opacity', '1.0')
    .text((_, i: any) => L[i].label);

  // INTERACTION
  node.on('click', (_, d: any) => {
    d.depth >= 2 &&
      preselect({
        label: label(d),
        id: d.data.id,
        URI: d.data.link,
        description: d.data.description,
        source: d.data.source,
        classes: d.data.classes,
        score: d.data.score,
      });
  });

  const zoom = d3
    .zoom<SVGSVGElement, unknown>()
    .scaleExtent([1.0 - Math.min(levelsV / 50, 0.35), levelsV / 5])
    .extent([
      [-dx, -dx],
      [dy * levelsH + dx, dy * levelsV + dx],
    ])
    .on('zoom', ({ transform }) => {
      // ScrollAbilityService.disableScroll();

      g.attr('transform', transform);
      // svg.attr("height", height * transform.k * 2)
      // resize viewbox e.g. if we zoom in, the graph gets larger and we want still to see it when scrolling down
      // .attr("viewBox", [-dy * padding, x0 - dx, width, height * transform.k * 1.05])
    })
    .on('end', () => {
      // ScrollAbilityService.enableScroll();
    }) as any;

  svg.call(zoom);
}
</script>

<template>
  <div class="results-graph" @mousedown="mouseEnableSVGInteraction">
    <div class="results-graph-selection" :class="hasPreselectedEntity ? 'active' : ''">
      <a class="results-graph-selection-close" @click="unselectPreselected()"></a>
      <EntityComponent>
        <template #label>
          {{ preselectedEntity?.label }}
        </template>
        <template #id>
          {{ preselectedEntity?.id }}
        </template>
      </EntityComponent>
      <!-- <p v-show="preselectedEntity?.description">
                {{ preselectedEntity?.description }}
            </p> -->
      <img v-show="preselectedEntity?.imageURI" :src="preselectedEntity?.imageURI" />
      <div>
        <ButtonComponent small class="selection-button--select desktop" @click="selectPreselected()">Select</ButtonComponent>
        <ButtonComponent small contrast class="selection-button--view" @click="viewPreselected()">View</ButtonComponent>
      </div>
    </div>
    <div class="results-graph-d3">
      <span v-html="interactionInfoHTML"></span>
      <div class="results-graph-wrapper">
        <svg id="tree" ref="svgEl"></svg>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import '@/assets/scss/shared';

.results-graph {
  $offset: 3rem;

  position: relative;
  display: flex;
  flex-direction: row;
  //margin-left: calc(-1 * $wrapperWidth / 2 + var(--space-m));
  //margin-left: calc(-1 * (($wrapperWidth / 2) + $offset));
  margin-left: calc(var(--main-width) * -0.5);
  left: 50%;
  //width: calc($wrapperWidth - (var(--space-m) * 2));
  //width: calc($wrapperWidth + (2 * $offset));
  width: var(--main-width);
  height: fit-content;

  /* &::before, &::after {
            content: "";
            display: block;
            width: var(--space-l);
            background: radial-gradient(var(--color-fg-contrast) 15%, transparent 15%) 0 0;
            background-size: 0.4rem 0.4rem;
        }
        & > div {
            margin: 0 var(--space-s) !important;
        } */

  span {
    display: block;
    margin-bottom: var(--space-m);
    color: var(--color-fg-gray);

    @include font-s;
  }

  &-d3 {
    width: 100%;
  }
  &-wrapper {
    position: relative;
    text-align: center;

    &::before,
    &::after {
      content: '';
      position: absolute;
      display: block;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      z-index: 1;
    }
    &::before {
      background: linear-gradient(
        0deg,
        var(--color-bg-light) 0%,
        transparent $offset,
        transparent calc(100% - $offset),
        var(--color-bg-light) 100%
      );
    }
    &::after {
      background: linear-gradient(
        90deg,
        var(--color-bg-light) 0%,
        transparent $offset,
        transparent calc(100% - $offset),
        var(--color-bg-light) 100%
      );
    }

    svg {
      --graph-bg-color: var(--color-bg-light);

      margin: 0 !important;
      height: fit-content;
      background-color: var(--graph-bg-color);
      pointer-events: none;
      z-index: 0 !important;

      &.active {
        --graph-bg-color: var(--color-interact);

        pointer-events: auto;
      }
    }
  }
  &-selection {
    position: sticky;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    top: var(--space-m);
    left: 0;
    width: 0;
    height: fit-content;
    opacity: 0;
    overflow: hidden;
    pointer-events: none;
    user-select: none;
    transform: translateY(1rem);
    transition: transform 400ms, opacity 400ms;
    z-index: 10;

    &.active {
      margin-left: var(--space-m);
      padding: calc(1.35 * var(--space-xs));
      width: fit-content;
      opacity: 1;
      overflow: visible;
      pointer-events: auto;
      user-select: auto;
      transform: translateY(0);
    }

    &::before {
      content: '';
      position: absolute;
      display: block;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: var(--color-bg-light);
      border: 1px solid var(--color-fg-contrast);
      border-radius: var(--border-radius-s);
      z-index: -1;

      @include shadow;
    }

    & > *:not(:last-child) {
      margin-bottom: var(--space-xs);
    }

    p {
      width: max-content;
    }
    img {
      width: 100%;
      height: 8.25rem;
      object-fit: cover;
      object-position: center 25%;
    }
    div {
      display: flex;
      flex-direction: row;
    }
    div:last-child {
      margin-top: 0.5rem;
    }

    $closeButtonSize: 1.55rem;

    &::after,
    &-close {
      position: absolute;
      margin-top: calc($closeButtonSize * -0.5);
      margin-right: calc($closeButtonSize * -0.5);
      top: 0;
      right: 0;
      border-radius: 100%;
    }
    &::after {
      content: '';
      // display: block;
      display: none;
      width: calc($closeButtonSize + 0.5px);
      height: calc($closeButtonSize);
      border: 1px solid var(--color-fg-contrast);
      transform: translateX(1px) translateY(-0.75px);
      width: $closeButtonSize;
      z-index: -2;

      @include shadow;
    }
    &-close {
      display: flex;
      align-items: center;
      justify-content: center;
      width: $closeButtonSize;
      height: $closeButtonSize;
      background-color: var(--color-bg-contrast);

      @include icon('cross', calc($closeButtonSize * 0.65));

      &:hover {
        filter: brightness(0.95);
      }
    }

    &-button {
      &--select {
        @include icon('plus', 0.8em);
      }
      &--view {
        @include icon('arrow-r', 0.8em);
      }
    }
  }
}
</style>
