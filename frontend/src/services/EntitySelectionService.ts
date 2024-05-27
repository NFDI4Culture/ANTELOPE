import type { IEntity } from "../interfaces";
import { AEventEmitter } from "./AEventEmitter";


class EntitySelection extends AEventEmitter<IEntity> {
    private entities: Map<string, IEntity> = new Map();

    constructor() {
        super("entity-selection");
    }

    private getUniqueIdentifier(entity: IEntity): string {
        return `${entity.id}:${entity.label}`;
    }

    public select(entity: IEntity) {
        this.entities.set(this.getUniqueIdentifier(entity), entity);

        this.dispatchEvent("select", entity);
    }
    
    public unselect(entity: IEntity) {
        this.entities.delete(this.getUniqueIdentifier(entity));

        this.dispatchEvent("unselect", entity);
    }

    public unselectAll() {
        this.getStored()
        .forEach((entity: IEntity) => {
            this.dispatchEvent("unselect", entity);
        });
        
        this.entities.clear();
    }

    public getStored(): Array<IEntity> {
        return Array.from(this.entities.values());
    }
}


export const EntitySelectionService = new EntitySelection();