import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultsGraphBarchartComponent } from './results-graph-barchart.component';

describe('ResultsGraphBarchartComponent', () => {
  let component: ResultsGraphBarchartComponent;
  let fixture: ComponentFixture<ResultsGraphBarchartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResultsGraphBarchartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResultsGraphBarchartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
