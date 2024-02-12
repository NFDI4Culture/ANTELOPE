import { Injectable } from '@angular/core';
import { IEntity } from 'app/interfaces/IEntity';

@Injectable({
  providedIn: 'root'
})
export class FileExportsService {

  constructor() { }

  private static downloadFile(fileName: string, fileExtension: string, contents: string): void {
    const downloadAnchor = document.createElement("a");
    downloadAnchor.setAttribute("href", `data:text/${fileExtension.toLowerCase()};charset=utf-8,${
      encodeURIComponent(contents)
    }`);
    downloadAnchor.setAttribute("download", `${fileName}.${fileExtension.toLowerCase()}`);
    document.body.appendChild(downloadAnchor);
    setTimeout(() => {
      downloadAnchor.click();
      
      downloadAnchor.remove();
    }, 0);
  }

  public static exportJSON(entities: IEntity[]): void {
    FileExportsService.downloadFile(`antelope-${Date.now()}`, "JSON", JSON.stringify(entities));
  }

  public static exportCSV(entities: IEntity[]): void {
    const filterOrderSerialObj = (obj: { [ key: string ]: any; }):
      { [ key: string ]: string|number|boolean; }  => {
      const serialKeys: string[] = [];
      for(const key in obj) {
        if([ "string", "number", "boolean" ].includes(typeof(obj[key]))) {
          serialKeys.push(key);
        }
      }
      serialKeys.sort();
      const filteredObj: { [ key: string ]: string|number|boolean; } = {};
      for(const key of serialKeys) {
        filteredObj[key] = obj[key];
      }
      return filteredObj;
    };

    FileExportsService.downloadFile(`antelope-${Date.now()}`, "CSV", `${
      Object.keys(filterOrderSerialObj(entities[0])).join(",").trim()
    }\n${
      Array.from(entities)
      .map((e: IEntity) => Object.values(filterOrderSerialObj(e)).join(",").trim())
      .join("\n")
    }`.trim());
  }

}
