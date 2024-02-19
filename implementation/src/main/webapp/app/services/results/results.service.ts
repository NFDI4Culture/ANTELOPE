import { Injectable } from '@angular/core';
import { IEntity } from 'app/interfaces/IEntity';

@Injectable({
  providedIn: 'root'
})
export class ResultsService {

  private static entities: IEntity[] = [];

  public static set(entities: IEntity[]):void {
    this.entities = entities;
  }

  public static get(): IEntity[] {
    return this.entities;
  }

  public static clear():void {
    this.entities = [];
  }

}
