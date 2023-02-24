import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnnotationserviceResultSelectcomponentComponent } from './annotationservice-result-selectcomponent.component';

describe('AnnotationserviceResultSelectcomponentComponent', () => {
  let component: AnnotationserviceResultSelectcomponentComponent;
  let fixture: ComponentFixture<AnnotationserviceResultSelectcomponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AnnotationserviceResultSelectcomponentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AnnotationserviceResultSelectcomponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
