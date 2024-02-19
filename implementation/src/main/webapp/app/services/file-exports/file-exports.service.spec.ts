import { TestBed } from '@angular/core/testing';

import { FileExportsService } from './file-exports.service';

describe('FileExportsService', () => {
  let service: FileExportsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FileExportsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
