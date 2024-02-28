import { Component, ElementRef } from '@angular/core';
import { EntitySelectService } from 'app/services/entity-select/entity-select.service';
import { IEntity } from 'app/interfaces/IEntity';


@Component({
  selector: 'jhi-results-table',
  templateUrl: './results-table.component.html',
  styleUrls: ['./results-table.component.scss'],
  providers: [ EntitySelectService ]
})
export class ResultsTableComponent {

  public entities?: IEntity[];

  constructor(private elRef: ElementRef) { }

  createTable(entities: IEntity[]): void {
    this.entities = entities;
  }

  public async copyId(entity: IEntity): Promise<void> {
    EntitySelectService.copy(entity);

    await navigator.clipboard.writeText(entity.id.toString());
  }

  public view(URI: string): void {
    window.open(URI, "_blank");
  }
  
}