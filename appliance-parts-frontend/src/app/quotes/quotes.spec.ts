import { ComponentFixture, TestBed } from '@angular/core/testing';
import { QuotesComponent } from './quotes';
import { ApiService } from '../api';
import { ChangeDetectorRef } from '@angular/core';
import { of, throwError } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('QuotesComponent', () => {
  let component: QuotesComponent;
  let fixture: ComponentFixture<QuotesComponent>;
  let apiService: jasmine.SpyObj<ApiService>;
  let cdr: ChangeDetectorRef;

  const mockQuotes = [
    {
      orderId: 1,
      status: 'QUOTE',
      totalAmount: 100.00,
      taxAmount: 8.00,
      createdAt: '2024-01-01T00:00:00',
      items: [
        {
          orderItemId: 1,
          part: { partId: 'WPW10123456', partName: 'Washer Drain Pump' },
          supplierName: 'AppliancePartsPros',
          quantity: 2,
          unitPrice: 50.00,
          partSupplierId: 1,
          selectedSupplier: { partSupplierId: 1 }
        }
      ]
    }
  ];

  const mockComparison = {
    partId: 'WPW10123456',
    partName: 'Washer Drain Pump',
    options: [
      {
        partSupplierId: 1,
        supplierName: 'AppliancePartsPros',
        partCost: 50.00
      },
      {
        partSupplierId: 2,
        supplierName: 'RepairClinic',
        partCost: 55.00
      }
    ]
  };

  beforeEach(async () => {
    const apiSpy = jasmine.createSpyObj('ApiService', [
      'getQuotes',
      'deleteQuote',
      'updateQuoteSupplier',
      'comparePart'
    ]);

    await TestBed.configureTestingModule({
      imports: [QuotesComponent, HttpClientTestingModule],
      providers: [
        { provide: ApiService, useValue: apiSpy },
        ChangeDetectorRef
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(QuotesComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    cdr = TestBed.inject(ChangeDetectorRef);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load quotes on init', (done) => {
    apiService.getQuotes.and.returnValue(of(mockQuotes));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      expect(apiService.getQuotes).toHaveBeenCalled();
      expect(component.quotes).toEqual(mockQuotes);
      expect(component.loading).toBe(false);
      done();
    }, 100);
  });

  it('should handle error when loading quotes', (done) => {
    const error = { status: 500, error: { message: 'Server error' } };
    apiService.getQuotes.and.returnValue(throwError(() => error));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      expect(component.error).toContain('Server error');
      expect(component.loading).toBe(false);
      expect(component.quotes).toEqual([]);
      done();
    }, 100);
  });

  it('should delete quote successfully', (done) => {
    apiService.getQuotes.and.returnValue(of(mockQuotes));
    apiService.deleteQuote.and.returnValue(of({}));
    
    spyOn(window, 'confirm').and.returnValue(true);
    
    fixture.detectChanges();
    
    setTimeout(() => {
      component.deleteQuote(1);
      
      expect(apiService.deleteQuote).toHaveBeenCalledWith(1);
      
      setTimeout(() => {
        expect(component.successMessage).toContain('deleted successfully');
        expect(component.deleting[1]).toBe(false);
        done();
      }, 100);
    }, 100);
  });

  it('should not delete quote if user cancels', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    
    component.deleteQuote(1);
    
    expect(apiService.deleteQuote).not.toHaveBeenCalled();
  });

  it('should handle error when deleting quote', (done) => {
    apiService.getQuotes.and.returnValue(of(mockQuotes));
    const error = { status: 404, error: { message: 'Not found' } };
    apiService.deleteQuote.and.returnValue(throwError(() => error));
    
    spyOn(window, 'confirm').and.returnValue(true);
    
    fixture.detectChanges();
    
    setTimeout(() => {
      component.deleteQuote(1);
      
      setTimeout(() => {
        expect(component.error).toContain('Quote not found');
        expect(component.deleting[1]).toBe(false);
        done();
      }, 100);
    }, 100);
  });

  it('should start editing quote', (done) => {
    apiService.getQuotes.and.returnValue(of(mockQuotes));
    apiService.comparePart.and.returnValue(of(mockComparison));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      component.startEditQuote(mockQuotes[0]);
      
      expect(apiService.comparePart).toHaveBeenCalledWith('WPW10123456');
      
      setTimeout(() => {
        expect(component.editingQuote[1]).toBe(true);
        expect(component.supplierOptions[1]).toEqual(mockComparison.options);
        done();
      }, 100);
    }, 100);
  });

  it('should cancel editing quote', () => {
    component.editingQuote[1] = true;
    component.supplierOptions[1] = mockComparison.options;
    component.selectedSupplier[1] = 1;
    
    component.cancelEditQuote(1);
    
    expect(component.editingQuote[1]).toBe(false);
    expect(component.supplierOptions[1]).toEqual([]);
    expect(component.selectedSupplier[1]).toBeNull();
  });

  it('should update quote supplier successfully', (done) => {
    apiService.getQuotes.and.returnValue(of(mockQuotes));
    apiService.updateQuoteSupplier.and.returnValue(of(mockQuotes[0]));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      component.selectedSupplier[1] = 2;
      component.updateQuoteSupplier(mockQuotes[0]);
      
      expect(apiService.updateQuoteSupplier).toHaveBeenCalledWith(1, 2);
      
      setTimeout(() => {
        expect(component.successMessage).toContain('updated successfully');
        expect(component.updating[1]).toBe(false);
        expect(component.editingQuote[1]).toBe(false);
        done();
      }, 100);
    }, 100);
  });

  it('should not update if no supplier selected', () => {
    component.selectedSupplier[1] = null;
    component.updateQuoteSupplier(mockQuotes[0]);
    
    expect(apiService.updateQuoteSupplier).not.toHaveBeenCalled();
    expect(component.error).toBe('Please select a supplier.');
  });

  it('should not update if same supplier selected', () => {
    mockQuotes[0].items[0].partSupplierId = 1;
    component.selectedSupplier[1] = 1;
    component.updateQuoteSupplier(mockQuotes[0]);
    
    expect(apiService.updateQuoteSupplier).not.toHaveBeenCalled();
    expect(component.error).toBe('Please select a different supplier.');
  });
});
