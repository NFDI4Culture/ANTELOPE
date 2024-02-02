import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphSelectionCardComponent } from './graph-selection-card.component';

describe('GraphSelectionCardComponent', () => {
  let component: GraphSelectionCardComponent;
  let fixture: ComponentFixture<GraphSelectionCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GraphSelectionCardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GraphSelectionCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
