import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnnotationServiceResultSelectcomponentComponent } from './annotation-service-result-selectcomponent.component';

describe('AnnotationServiceResultSelectcomponentComponent', () => {
  let component: AnnotationServiceResultSelectcomponentComponent;
  let fixture: ComponentFixture<AnnotationServiceResultSelectcomponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AnnotationServiceResultSelectcomponentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AnnotationServiceResultSelectcomponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
