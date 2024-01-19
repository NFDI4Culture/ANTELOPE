import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnnotationServiceSelectionComponent } from './annotation-service-selection.component';

describe('AnnotationServiceSelectionComponent', () => {
  let component: AnnotationServiceSelectionComponent;
  let fixture: ComponentFixture<AnnotationServiceSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AnnotationServiceSelectionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AnnotationServiceSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
