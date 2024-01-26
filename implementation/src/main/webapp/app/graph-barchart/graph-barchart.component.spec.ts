import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphBarchartComponent } from './graph-barchart.component';

describe('GraphBarchartComponent', () => {
  let component: GraphBarchartComponent;
  let fixture: ComponentFixture<GraphBarchartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GraphBarchartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraphBarchartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
