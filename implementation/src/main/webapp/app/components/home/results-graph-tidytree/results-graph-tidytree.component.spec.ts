import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultsGraphTidytreeComponent } from './results-graph-tidytree.component';

describe('ResultsGraphTidytreeComponent', () => {
  let component: ResultsGraphTidytreeComponent;
  let fixture: ComponentFixture<ResultsGraphTidytreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResultsGraphTidytreeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultsGraphTidytreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
