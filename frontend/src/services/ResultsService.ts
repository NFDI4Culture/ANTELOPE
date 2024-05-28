import type { IResults } from "../interfaces";
import { AEventEmitter } from "./AEventEmitter";


class Results extends AEventEmitter<IResults|string> {
    private currentResults: IResults|null = null;

    constructor() {
        super("results");
    }
    
    public defineResults(results: IResults) {
        this.currentResults = results;

        this.dispatchEvent("define", results);
    }
    
    public defineHTMLResults(results: string) {
        this.dispatchEvent("html", results);
    }
    
    public clearResults() {
        this.currentResults = null;

        this.dispatchEvent("clear");
    }

    public getResults() {
        return this.currentResults;
    }
}


export const ResultsService = new Results();