export interface IEntity {
    id: string;
	label: string;
    source: string;
    URI: string;

    classes?: string;
    description?: string;
    imageURI?: string;
    score?: number;
}

export interface IHierarchyNode {
    children: IHierarchyNode[];
    id: string;
    link: string;
    name: string;
}

export interface IResults {
    entities: IEntity[];
    hierarchy: IHierarchyNode;
    relations: [];
}

interface ISearchData<S = { [ key: string]: string|number|boolean; }, B = undefined> {
    endpoint: string;
    searchParams: S & {
        wikidata?: string;
        wikidata_dbpedia?: string;
        iconclass?: boolean;
        gnd?: boolean;
        ts4tib?: boolean;
        aat?: boolean;
        allowDuplicates?: boolean;
    };
    body?: B;
    headers?: { [ key: string ]: string; };

    mock?: unknown; // Optional mock response
}

export interface ITerminologySearchData extends ISearchData<{
    searchtext: string;
}> {};

export interface ISearchDictionary {
    dictionaryType: "ListOfWords" | "SimpleDictionary" | "FullDictionary";

    listOfWords?: string[];
    simpleDictionary?: {
        [ key: string ]: string[];
    };
    fullDictionary?: {
        [ key: string]: {
            label: string;
            patterns: string[];

            kb_id?: string;
            kb_url?: string;
        };
    };
}

export interface IEntitySearchData extends ISearchData <{}, {
    text: string;
    dictionary: ISearchDictionary;
    threshold: number;
    language: string;
}> {};

export interface IImageSearchData extends ISearchData <{
    model?: string;
}, {
    image: File;
    text: string;
    dictionary: ISearchDictionary;
    threshold: number;
    language: string;
}> {};