import { TestBed } from '@angular/core/testing';

import { ScrollAbilityService } from './scroll-ability.service';

describe('ScrollAbilityService', () => {
  let service: ScrollAbilityService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ScrollAbilityService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
