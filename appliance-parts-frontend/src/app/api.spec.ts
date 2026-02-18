import { TestBed } from '@angular/core/testing';
import { ApiService } from './api';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('searchParts', () => {
    it('should return parts array', () => {
      const mockParts = [
        { partId: 'WPW10123456', partName: 'Washer Drain Pump' }
      ];

      service.searchParts('WPW').subscribe(parts => {
        expect(parts).toEqual(mockParts);
      });

      const req = httpMock.expectOne(`${baseUrl}/parts/search?query=WPW`);
      expect(req.request.method).toBe('GET');
      req.flush(mockParts);
    });

    it('should encode query parameter', () => {
      service.searchParts('WPW 10123456').subscribe();

      const req = httpMock.expectOne(`${baseUrl}/parts/search?query=WPW%2010123456`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('should handle timeout error', (done) => {
      service.searchParts('WPW').subscribe({
        error: (error) => {
          expect(error.message).toContain('timed out');
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/parts/search?query=WPW`);
      expect(req.request.method).toBe('GET');
      // Simulate timeout by not flushing
    });
  });

  describe('comparePart', () => {
    it('should return comparison data without sort', () => {
      const mockComparison = {
        partId: 'WPW10123456',
        partName: 'Washer Drain Pump',
        options: []
      };

      service.comparePart('WPW10123456').subscribe(data => {
        expect(data).toEqual(mockComparison);
      });

      const req = httpMock.expectOne(`${baseUrl}/parts/WPW10123456/compare`);
      expect(req.request.method).toBe('GET');
      req.flush(mockComparison);
    });

    it('should include sort parameter when provided', () => {
      service.comparePart('WPW10123456', 'CHEAPEST').subscribe();

      const req = httpMock.expectOne(`${baseUrl}/parts/WPW10123456/compare?sort=CHEAPEST`);
      expect(req.request.method).toBe('GET');
      req.flush({});
    });
  });

  describe('saveQuote', () => {
    it('should save quote successfully', () => {
      const payload = { partSupplierId: 1, quantity: 2 };
      const mockResponse = { orderId: 1 };

      service.saveQuote(payload).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/quotes`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(payload);
      req.flush(mockResponse);
    });
  });

  describe('getQuotes', () => {
    it('should return quotes array', () => {
      const mockQuotes = [
        { orderId: 1, status: 'QUOTE', totalAmount: 100.00 }
      ];

      service.getQuotes().subscribe(quotes => {
        expect(quotes).toEqual(mockQuotes);
      });

      const req = httpMock.expectOne(`${baseUrl}/quotes`);
      expect(req.request.method).toBe('GET');
      req.flush(mockQuotes);
    });
  });

  describe('deleteQuote', () => {
    it('should delete quote successfully', () => {
      service.deleteQuote(1).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/quotes/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  describe('updateQuoteSupplier', () => {
    it('should update quote supplier successfully', () => {
      const mockResponse = { orderId: 1 };

      service.updateQuoteSupplier(1, 2).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/quotes/1/supplier`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ newPartSupplierId: 2 });
      req.flush(mockResponse);
    });
  });
});
