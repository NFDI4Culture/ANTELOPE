import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';

import { AnnotationServiceUIComponent } from './annotation-service-ui.component';

describe('AnnotationServiceUIComponent', () => {
  let component: AnnotationServiceUIComponent;
  let fixture: ComponentFixture<AnnotationServiceUIComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ ReactiveFormsModule ],
      declarations: [ AnnotationServiceUIComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AnnotationServiceUIComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
