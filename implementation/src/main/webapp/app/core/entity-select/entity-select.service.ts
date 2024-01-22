import { EventEmitter } from 'events';
import { Injectable } from '@angular/core';
import { IEntity } from 'app/interfaces/IEntity';


@Injectable({
  providedIn: 'root'
})
export class EntitySelectService {

  private static eventEmitter = new EventEmitter();

  public static selected: IEntity|null = null;

  public static select(entity: IEntity): void {
    EntitySelectService.unselect();
    
    EntitySelectService.eventEmitter.emit("select", entity);

    EntitySelectService.selected = entity;
  }
  
  public static unselect(): void {
    EntitySelectService.selected
    && EntitySelectService.eventEmitter.emit("unselect", EntitySelectService.selected);

    EntitySelectService.selected = null;
  }
  
  public static copy(entity: IEntity): void {
    EntitySelectService.eventEmitter.emit("copy", entity);
  }
  
  public static uncopy(entity: IEntity): void {
    EntitySelectService.eventEmitter.emit("uncopy", entity);
  }

  public static on(eventName: string, callback: ((entity: IEntity) => void)): void {
    this.eventEmitter.on(eventName, callback);
  }

}
