import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphTidytreeComponent } from './graph-tidytree.component';

describe('GraphTidytreeComponent', () => {
  let component: GraphTidytreeComponent;
  let fixture: ComponentFixture<GraphTidytreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GraphTidytreeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GraphTidytreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
