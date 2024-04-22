import { Component, ElementRef, ViewChild } from '@angular/core';
import * as d3 from 'd3';

type ENTITIES = {
  id: string;
  URI: string;
  label: string;
  source: string;
  classes: string;
  score: number;
}

@Component({
  selector: 'jhi-results-graph-barchart',
  templateUrl: './results-graph-barchart.component.html',
  styleUrls: ['./results-graph-barchart.component.scss']
})
export class ResultsGraphBarchartComponent {
  @ViewChild('barchart')
  svg!: ElementRef;

  clear(): void {
    const svg = d3.select("#barchart");
    svg.selectAll("*").remove();

  }

  createChartFromClassificationResult(data: ENTITIES[]): void {
    let preparedData = {};

    preparedData = data;
    const helpFunctions = {
      label(d: any): string {
        if (d.label !== undefined) {

         // if (d.label.length < 24) {
            return d.label as string;
         // } else {
         //   return d.label.substring(0, 20) as string + "[..]";
         // }
        } else {
          return "Label";
        }
      },
      score(d: any): number {
        if (d.score !== undefined) {
          return d.score as number;
        } else {
          return 0.0;
        }
      },
 

      width: 800
    }

    this.createChart(preparedData, helpFunctions);
  }

  createChart(data: any,
    {
      score = (d: any): number => 0.0,
      label = (d: any): string => "label"
    }): void {
    // set the dimensions and margins of the graph
    const margin = { top: 20, right: 20, bottom: 30, left: 40 },
      width = 960 - margin.left - margin.right,
      height = 500 - margin.top - margin.bottom;

    // set the ranges
    const x = d3.scaleBand()
      .range([0, width])
      .padding(0.1);
    const y = d3.scaleLinear()
      .range([height, Math.min(...data.map((d:any) => d.score as string))]);

    // append the svg object to the body of the page
    // append a 'group' element to 'svg'
    // moves the 'group' element to the top left margin
    const svg = d3.select("#barchart")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform",
        "translate(" + margin.left.toString() + "," + margin.top.toString() + ")");

    // format the data
    data.forEach(function (d: any) {
      d.score = +d.score;
    });

    // Scale the range of the data in the domains
    x.domain(data.map(function (d: any): string { return label(d) }));
    // y.domain([Math.min(...data.map((d:any) => d.score)), Math.max(...data.map((d:any) => d.score))]);
    y.domain([0, 1]);


    // append the rectangles for the bar chart
    svg.selectAll(".bar")
      .data(data)
      .enter().append("rect")
      .attr("class", "bar")
      .attr("x", function (d: any): number { return x(label(d)) as number })
      .attr("width", x.bandwidth())
      .attr("y", function (d: any): number { return y(score(d)) })
      .attr("height", function (d: any) { return height - y(d.score); })
      .append("title").text((d:any) => label(d)+": "+score(d).toString());

    // add the x Axis
    svg.append("g")
      .attr("transform", "translate(0," + height.toString() + ")")
      .call(d3.axisBottom(x))
      .selectAll("text")  
      .style("text-anchor", "end")
      .attr("dx", "-.8em")
      .attr("dy", ".15em")
      .attr("transform", "rotate(-65)");
      

    // add the y Axis
    svg.append("g")
      .call(d3.axisLeft(y));

    

  }

}
