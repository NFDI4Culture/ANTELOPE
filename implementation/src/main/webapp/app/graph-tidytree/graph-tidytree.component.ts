import { Component, OnInit } from '@angular/core';
import * as d3 from 'd3';
import { HierarchyNode } from 'd3';

type NodeData = {x:number, y:number, parentId: string};

type WikiDataClass = {
  type: string; 
  value: string; // e.g uri
};

type WikiDataClassLabel = {
  "xml:lang": string; // language
  type: string; 
  value: string; // classname
}

type WikiDataHierarchy = {
  class: WikiDataClass;
  classLabel: WikiDataClassLabel;
  superclass: WikiDataClass
  superclassLabel: WikiDataClassLabel;
}

type HierarchyTree = {
  id: string;
  name: string;
  link: string;
  children: [];
}

@Component({
  selector: 'jhi-graph-tidytree',
  templateUrl: './graph-tidytree.component.html',
  styleUrls: ['./graph-tidytree.component.scss']
})
export class GraphTidytreeComponent implements OnInit {
  
  //tree:SVGSVGElement|null = null;
  
  constructor() { }

  ngOnInit(): void {  }

  clear():void {
    const svg = d3.select("#tree");
    svg.selectAll("*").remove();
    
  }

  createTreeFromWikiDataHierarchy( data: HierarchyTree): void {
   let preparedData = {};
    
    preparedData = data;
    const helpFunctions = {
      label(d:any):string {
        if(d.data.name !== undefined) {
          
          if(d.data.name.length < 20) {
            return d.data.name as string;
          } else {
            return d.data.name.substring(0,16) as string +"[..]";
          }
        } else {
          return "label" ;
        }
      },
      title: (d:any, n:d3.HierarchyNode<HierarchyTree>):string => n.data.name+" ("+n.data.link+")", // hover text
      link: (d:any, n:d3.HierarchyNode<HierarchyTree>):string => n.data.link,
      
      width: 800
    }
 
    this.createTree(preparedData, helpFunctions);
  }

   // Copyright 2021 Observable, Inc.
    // Released under the ISC license.
    // https://observablehq.com/@d3/tree
  createTree(data : any, { // data is either tabular (array of objects) or hierarchy (nested objects)
      //path, // as an alternative to id and parentId, returns an array identifier, imputing internal nodes
      id = Array.isArray(data) ? (d:d3.HierarchyNode<NodeData>) => d.id : null, // if tabular data, given a d in data, returns a unique identifier (string)
      parentId = Array.isArray(data) ? (d:d3.HierarchyNode<NodeData>) => d.parent?.id : null, // if tabular data, given a node d, returns its parent’s identifier
      children = (d:any):Iterable<any> | null | undefined => d !== undefined ? d.children as Iterable<any> : [], // if hierarchical data, given a d in data, returns its children
      tree = d3.tree, // layout algorithm (typically d3.tree or d3.cluster)
      sort = null, // how to sort nodes prior to layout (e.g., (a, b) => d3.descending(a.height, b.height))
      label = (d:any):string => "label", // given a node d, returns the display name
      title = (d:any, n:any):string => "title", // given a node d, returns its hover text
      link =  (d:any, n:any):string => "link", // given a node d, its link (if any)
      linkTarget = "_blank", // the target attribute for links (if any)
      width = 640, // outer width, in pixels
      height = 1200, // outer height, in pixels
      r = 4, // radius of nodes
      padding = 1, // horizontal padding for first and last column
      fill = "black", // fill for nodes
      fillOpacity = null, // fill opacity for nodes
      stroke = "#333", // stroke for links
      strokeWidth = 1.5, // stroke width for links
      strokeOpacity = 0.2, // stroke opacity for links
      strokeLinejoin = null, // stroke line join for links
      strokeLinecap = null, // stroke line cap for links
      halo = "#fff", // color of label halo 
      haloWidth = 5, // padding around the labels
    } ): void {

      // If id and parentId options are specified, or the path option, use d3.stratify
      // to convert tabular data to a hierarchy; otherwise we assume that the data is
      // specified as an object {children} with nested objects (a.k.a. the “flare.json”
      // format), and use d3.hierarchy.


      let root:d3.HierarchyNode<NodeData> = null as unknown as d3.HierarchyNode<NodeData>;
     
      if( id != null || parentId != null ){
        //console.log("use d3.stratify()");
        //console.debug( data );
        root = d3.stratify().id((d:any, i:any, data2:any): string|null|undefined => d["id"] as string).parentId((d:any, i:any, data2:any): string|null|undefined => d["parentId"] as string)(data) as HierarchyNode<NodeData> ;
      
      } else {
        //console.log("use d3.hierarchy()");
        root =  d3.hierarchy(data, children ) as HierarchyNode<NodeData> ;
      }
          
      // Compute labels and titles.
      const descendants = root.descendants();
      const L:string[]|null = descendants.map((d:any) => label(d));

      // Compute the layout.
      const dx = 13;
      const dy = width / (root.height + padding);
      tree<NodeData>().nodeSize([dx, dy])(root);

      // Center the tree.
      let x0 = Infinity;
      let x1 = -x0;
      root.each((d:any) => {
        if (d.x > x1) {x1 = d.x};
        if (d.x < x0) {x0 = d.x};
      });

      // Compute the default height.
      //if (!height) {height = x1 - x0 + dx * 2;}
      //if (!width) {width = x1 - x0 + dx * 2;}
      height = x1 - x0 + dx * 2;
      width = x1 - x0 + dy * 2;
      const linkGenerator = d3.linkHorizontal()
      .x((d:any) => d.y as number)
      .y((d:any) => d.x as number);

      const lineGenerator = d3.line<d3.HierarchyPointNode<NodeData>>()
      .x((d:any) => d.x as number)
      .y((d:any) => d.y as number);

      const svg = d3.select("#tree")
         .attr("viewBox", [-dy * padding, x0 - dx, width, height])
          //.attr("viewBox", [x1-dy , x0 - dx, width, height])
          //.attr("viewBow", [0,0, width, height])
        
          .attr("width", "100%")
          .attr("height", height)
          .attr("style", "position: relative ; min-height: 600px; min-width: 600px; max-width: 100%; height: auto; height: intrinsic; margin: 10px; margin-bottom:150px; z-index:100")
          .attr("font-family", "sans-serif")
          .attr("font-size", 11);
      
      const g = svg.append("g")
     

      g.append("g")
          .attr("fill", "none")
          .attr("stroke", stroke)
          .attr("stroke-opacity", strokeOpacity)
          .attr("stroke-linecap", strokeLinecap)
          .attr("stroke-linejoin", strokeLinejoin)
          .attr("stroke-width", strokeWidth)
        .selectAll("path")
          .data(root.links())
          .join("path")
            .attr("d", (d:any)=> linkGenerator(d) as unknown as string )
             ;

      const node = g.append("g")
        .selectAll("a")
        .data(root.descendants())
        .join("a")
          .attr("xlink:href", (d:any) => link(d.data, d))
          .attr("target",  linkTarget)
          .attr("transform", (d:any) => `translate(${d.y as string},${d.x as string})`);

      node.append("circle")
          .attr("fill", "#f9cd0e")
          .attr("stroke", "black")
          .attr("stroke-width", "1.25px")
          .attr("r", r);

     
      node.append("title") 
          .text((d:any) => title(d.data, d));
      
      
      const textBackground = node.append("rect")
        .attr("rx", 5)
        .attr("ry", 5)
        .attr("x", function(d:any){ return  this.getBBox().x + 5;})
        .attr("y", function(d:any, i:any){ return  this.getBBox().y - 8 })
        .attr("width", function(d:any, i:any){ return this.getBBox().width + (L[i].length * 6);})
        .attr("height", function(d:any) {return 14;})
        .style("fill", "#FFFFFF");

     
      const textNode = node.append("text") 
          .attr("dy", "0.32em")
          .attr("x", (d:any) => 6 )
          .attr("text-anchor", (d:any) => "start")
          .attr("paint-order", "stroke")
          .attr("fill", "none")
          .attr("stroke", stroke)
          .attr("stroke-width", "")
          .attr("stroke-opacity", "0.8")
          .text((d:any, i:any) => L[i] )
       
      const zoom = d3.zoom<SVGSVGElement, unknown>()
        .extent([[0, 0], [width, height]])
        //.translateExtent([[-0.5*height,-0.5*width],[0.5*height,0.5*height]])
        .scaleExtent([-8, 8])

        .on("zoom", ({transform}) => {
          g.attr("transform", transform);
          svg.attr("height", height*transform.k)
          // resize viewbox e.g. if we zoom in, the graph gets larger and we want still to see it when scrolling down
          .attr("viewBox", [-dy * padding, x0 - dx, width, height*1.1*transform.k])
        }) as any;

      svg.call(zoom);
          
      
    
      
    }

    
    
}


