import type { ISearchDictionary } from "../../../interfaces";


export function makeDictionary(dictionaryStr: string): ISearchDictionary {
    if(!dictionaryStr.trim().length) return {
        dictionaryType: "ListOfWords",
        listOfWords: []
    };  // TODO: Request backend null handler

    let obj;
    try {
        obj = JSON.parse(dictionaryStr);
    } catch {
        throw new SyntaxError("Invalid dictionary format (requires array of labels, label-associated object, or a entity-wise details object)");
    }

    let uniformObj;
    if(Array.isArray(obj)) {
        uniformObj = {
            dictionaryType: "ListOfWords",
            listOfWords: obj
        }
    } else if(Array.isArray(Object.values(obj)[0])) {
        uniformObj = {
            dictionaryType: "SimpleDictionary",
            simpleDictionary: obj
        }
    } else {
        uniformObj = {
            dictionaryType: "FullDictionary",
            fullDictionary: obj
        }
    }

    return uniformObj;
}