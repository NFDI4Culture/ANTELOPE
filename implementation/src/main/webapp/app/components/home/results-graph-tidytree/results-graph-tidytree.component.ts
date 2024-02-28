import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { EntitySelectService } from 'app/services/entity-select/entity-select.service';
import { IEntity } from 'app/interfaces/IEntity';
import * as d3 from 'd3';
import { HierarchyNode } from 'd3';
import { ScrollAbilityService } from 'app/services/scroll-ability/scroll-ability.service';

const MAX_LABEL_LENGTH = 24;

type NodeData = { x: number, y: number, parentId: string };

type HierarchyTree = {
  id: string;
  name: string;
  link: string;
  children: [];
}

const TRANSPARENT = "#00000000";

@Component({
  selector: 'jhi-results-graph-tidytree',
  templateUrl: './results-graph-tidytree.component.html',
  styleUrls: ['./results-graph-tidytree.component.scss'],
  providers: [ EntitySelectService ]
})
export class ResultsGraphTidytreeComponent implements OnInit {
  @ViewChild('tree')
  svg!: ElementRef;

  constructor(private elRef: ElementRef) { }
  
  ngOnInit(): void {
    const getNodeChildren = (dataId: string): HTMLElement[] => this
    .elRef.nativeElement
    .querySelector(`#${this.getNodeId(dataId)}`)
    .children as HTMLElement[];

    EntitySelectService.on("select", (entity: IEntity) => {
      getNodeChildren(entity.id)[2].style.stroke = "black";
      getNodeChildren(entity.id)[4].style.stroke = "black";
    });
    EntitySelectService.on("unselect", (entity: IEntity) => {
      getNodeChildren(entity.id)[2].style.stroke = TRANSPARENT;
      getNodeChildren(entity.id)[4].style.stroke = TRANSPARENT;
    });
    EntitySelectService.on("copy", (entity: IEntity) => {
      getNodeChildren(entity.id)[3].style.fill = "white";
    });
    EntitySelectService.on("uncopy", (entity: IEntity) => {
      getNodeChildren(entity.id)[3].style.fill = TRANSPARENT;
    });
  }
  
  clear(): void {
    const svg = d3.select("#tree");
    svg.selectAll("*").remove();
  }

  createTreeFromWikiDataHierarchy(data: HierarchyTree): void {
    let preparedData = {};

    preparedData = data;
    
    const helpFunctions = {
      label(d: any): string {
        if (d.data.name !== undefined) {

          if (d.data.name.length < MAX_LABEL_LENGTH) {
            return d.data.name as string;
          } else {
            return d.data.name.substring(0, MAX_LABEL_LENGTH - 4) as string + "[..]";
          }
        } else {
          return "label";
        }
      },
      title: (d: any, n: d3.HierarchyNode<HierarchyTree>): string => n.data.name + " (" + n.data.link + ")", // hover text
      link: (d: any, n: d3.HierarchyNode<HierarchyTree>): string => n.data.link,

      width: 800
    }

    this.createTree(preparedData, helpFunctions);
  }

  // Copyright 2021 Observable, Inc.
  // Released under the ISC license.
  // https://observablehq.com/@d3/tree
  createTree(data: any, { // data is either tabular (array of objects) or hierarchy (nested objects)
    // path, // as an alternative to id and parentId, returns an array identifier, imputing internal nodes
    id = Array.isArray(data) ? (d: d3.HierarchyNode<NodeData>) => d.id : null, // if tabular data, given a d in data, returns a unique identifier (string)
    parentId = Array.isArray(data) ? (d: d3.HierarchyNode<NodeData>) => d.parent?.id : null, // if tabular data, given a node d, returns its parent’s identifier
    children = (d: any): Iterable<any> | null | undefined => d !== undefined ? d.children as Iterable<any> : [], // if hierarchical data, given a d in data, returns its children
    tree = d3.tree, // layout algorithm (typically d3.tree or d3.cluster)
    sort = null, // how to sort nodes prior to layout (e.g., (a, b) => d3.descending(a.height, b.height))
    label = (d: any): string => "label", // given a node d, returns the display name
    title = (d: any, n: any): string => "title", // given a node d, returns its hover text
    link = (d: any, n: any): string => "link", // given a node d, its link (if any)
    width = 640, // outer width, in pixels
    height = 1200, // outer height, in pixels
    r = 10, // radius of nodes
    padding = 1, // horizontal padding for first and last column
    fillBackgroundClass = "#18A0FB",
    fillBackgroundInstance = "#F9CD0E",
    fillBackgroundSource = "#C4C4C4",
    fillForeground = "white",
    stroke = "black", // stroke for links,
    strokeWidth = 0.75, // stroke width for links
    strokeOpacity = 0.25, // stroke opacity for links
    strokeLinejoin = null, // stroke line join for links
    strokeLinecap = null, // stroke line cap for links
  }): void {
    // If id and parentId options are specified, or the path option, use d3.stratify
    // to convert tabular data to a hierarchy; otherwise we assume that the data is
    // specified as an object {children} with nested objects (a.k.a. the “flare.json”
    // format), and use d3.hierarchy.

    let root: d3.HierarchyNode<NodeData> = null as unknown as d3.HierarchyNode<NodeData>;
    root = (id != null || parentId != null)
    ? (d3.stratify().id((d: any, i: any, data2: any): string | null | undefined => d["id"] as string).parentId((d: any, i: any, data2: any): string | null | undefined => d["parentId"] as string)(data) as HierarchyNode<NodeData>)
    : d3.hierarchy(data, children) as HierarchyNode<NodeData>;

    // Compute labels and titles.
    const descendants = root.descendants();
    const L: {
      label: string;
      depth: number;
    }[] | null = descendants
    .map((d: any) => ({
      label: label(d),
      depth: d.depth
    }))
    .filter((d: any) => d.depth >= 2);

    // Compute the layout.
    const dx = 30; // Vertical margin between nodes
    const dy = [...L].sort((a, b) => b.label.length - a.label.length)[0].label.length * 12 * 1.125;
    tree<NodeData>().nodeSize([dx, dy])(root);

    // Center the tree.
    let x0 = Infinity;
    let x1 = -x0;
    root.each((d: any) => {
      if(d.depth < 2) {return;}
      if (d.x > x1) { x1 = d.x };
      if (d.x < x0) { x0 = d.x };
    });

    // Compute the default height.
    // if (!height) {height = x1 - x0 + dx * 2;}
    // if (!width) {width = x1 - x0 + dx * 2;}
    height = x1 - x0 + dx * 2;
    width = x1 - x0 + dy * 2;

    const linkGenerator = d3.linkHorizontal()
    .x((d: any) => d.y as number)
    .y((d: any) => d.x as number);
    /* const lineGenerator = d3.line<d3.HierarchyPointNode<NodeData>>()
    .x((d: any) => d.x as number)
    .y((d: any) => d.y as number); */

    const levelsH: number = [...L].map(l => l.depth)
    .sort((a, b) => b - a)[0];
    const levelsV: number = (Object.entries([...L].reduce((a, b) => {
      a[b.depth] = (isNaN(a[b.depth]) ? 0 : a[b.depth]) + 1; 
      return a;
    }, {} as { [key: string]: number; }))
    .sort((a, b) => a[1] - b[1])
    .pop() ?? [null, 0])[1];

    // SVG
    const svg = d3.select("#tree")
    .attr("viewBox", [-dy * padding, x0 - dx, width, height])
    .attr("viewBox", [x1-dy , x0 - dx, width, height])
    .attr("viewBow", [0,0, width, height])
    .attr("width", "100%")
    .attr("height", height)
    .attr("style", "position: relative; min-height: 600px; min-width: 600px; max-width: 100%; height: auto; height: intrinsic;")
    .attr("font-family", "sans-serif")
    .attr("font-size", "1rem");
    
    // GRAPH
    const g = svg.append("g")
    g.append("g")
    .attr("fill", "none")
    .attr("stroke", stroke)
    .attr("stroke-opacity", strokeOpacity)
    .attr("stroke-linecap", strokeLinecap)
    .attr("stroke-linejoin", strokeLinejoin)
    .attr("stroke-width", strokeWidth)
    .selectAll("path")
    .data(root.links().filter((d: any) => d.source.depth >= 2))
    .join("path")
    .attr("d", (d: any) => linkGenerator(d) as unknown as string);

    // NODE
    const node = g.append("g")
    .selectAll("a")
    .data(root.descendants().filter((d: any) => d.depth >= 2))
    .join("a")
    .attr("id", (d: any) => this.getNodeId(d.data.id))
    .attr("transform", (d: any) => `translate(${d.y as string},${d.x as string})`)
    // .attr("xlink:href", (d: any) => link(d.data, d))
    // .attr("target", linkTarget)
    .attr("data-id", 3);

    // TITLE
    node.append("title")
    .text((d: any) => title(d.data, d));

    // BACKGROUND
    node.append("rect")
    .attr("rx", 5)
    .attr("ry", 5)
    .attr("x", function () { return this.getBBox().x - 8; })
    .attr("y", function () { return this.getBBox().y - 15 })
    .attr("width", function (_, i: any) { return L[i].label.length * 14; })
    .attr("height", 30)
    .style("fill", fillForeground);

    const getFill = (_: any, i: number): string => {
      switch(L[i].depth) {
        case 0:
          return fillBackgroundSource;
        case 1:
          return fillBackgroundSource;
        case 2:
          return fillBackgroundInstance;
        default:
          return fillBackgroundClass
      }
    };

    // CIRCLE
    node.append("circle")
    .attr("fill", getFill)
    .attr("stroke", TRANSPARENT)
    .attr("stroke-width", "1.35")
    .attr("r", r);

    // INNER CIRCLE
    node.append("circle")
    .attr("fill", getFill)
    .attr("r", r / 2);

    // TEXT
    node.append("text")
    .attr("dy", "0.38em")
    .attr("x", 20)
    .attr("text-anchor", "start")
    .attr("paint-order", "stroke")
    .attr("fill", stroke)
    .attr("stroke", TRANSPARENT)
    .attr("stroke-width", "0.325")
    .attr("stroke-opacity", "1.0")
    .text((_, i: any) => L[i].label)

    // INTERACTION
    node.on("click", (_, d: any) => {
      (d.depth >= 2)
      && EntitySelectService.select({
        label: label(d),
        id: d.data.id,
        URI: d.data.link,
        description: d.data.description,
        source: d.data.source,
        classes: d.data.classes,
        score: d.data.score
      });
    });
    
    const zoomPadding = 150;
    const zoom = d3.zoom<SVGSVGElement, unknown>()
    .scaleExtent([1.0 - Math.min(levelsV / 50, 0.35), levelsV / 5])
    .extent([
      [-zoomPadding, -zoomPadding],
      [((dy + 8) * levelsH) + zoomPadding, (dy * levelsV) + zoomPadding]
    ])
    .on("zoom", ({ transform }) => {
      ScrollAbilityService.disable();

      g.attr("transform", transform);
      svg.attr("height", height * transform.k * 2)
      // resize viewbox e.g. if we zoom in, the graph gets larger and we want still to see it when scrolling down
      // .attr("viewBox", [-dy * padding, x0 - dx, width, height * transform.k * 1.05])
    })
    .on("end", () => {
      ScrollAbilityService.enable();
    }) as any;

    svg.call(zoom);
  }

  private getNodeId(entityId: string): string {
    return `node--${entityId}`;
  }

}