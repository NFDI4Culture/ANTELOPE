import { ComponentFixture, TestBed } from '@angular/core/testing';
<<<<<<< HEAD
=======
import { ReactiveFormsModule } from '@angular/forms';
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa

import { AnnotationServiceUIComponent } from './annotation-service-ui.component';

describe('AnnotationServiceUIComponent', () => {
  let component: AnnotationServiceUIComponent;
  let fixture: ComponentFixture<AnnotationServiceUIComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
<<<<<<< HEAD
=======
      imports: [ ReactiveFormsModule ],
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa
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
