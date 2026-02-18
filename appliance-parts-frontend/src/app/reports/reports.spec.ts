import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReportsComponent } from './reports';
import { ApiService } from '../api';
import { ChangeDetectorRef } from '@angular/core';
import { of, throwError } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ReportsComponent', () => {
  let component: ReportsComponent;
  let fixture: ComponentFixture<ReportsComponent>;
  let apiService: jasmine.SpyObj<ApiService>;
  let cdr: ChangeDetectorRef;

  const mockQuotes = [
    {
      orderId: 1,
      status: 'QUOTE',
      totalAmount: 108.00,
      taxAmount: 8.00,
      createdAt: '2024-01-01T00:00:00',
      items: [
        {
          orderItemId: 1,
          part: { partId: 'WPW10123456', partName: 'Washer Drain Pump' },
          supplierName: 'AppliancePartsPros',
          quantity: 2,
          unitPrice: 50.00
        }
      ]
    },
    {
      orderId: 2,
      status: 'QUOTE',
      totalAmount: 55.00,
      taxAmount: 4.00,
      createdAt: '2024-01-02T00:00:00',
      items: [
        {
          orderItemId: 2,
          part: { partId: 'WPW10315885', partName: 'Washer Agitator' },
          supplierName: 'RepairClinic',
          quantity: 1,
          unitPrice: 55.00
        }
      ]
    }
  ];

  beforeEach(async () => {
    const apiSpy = jasmine.createSpyObj('ApiService', ['getQuotes']);

    await TestBed.configureTestingModule({
      imports: [ReportsComponent, HttpClientTestingModule],
      providers: [
        { provide: ApiService, useValue: apiSpy },
        ChangeDetectorRef
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReportsComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    cdr = TestBed.inject(ChangeDetectorRef);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.reportTitle).toBe('Appliance Parts Quotes Report');
    expect(component.quotes).toEqual([]);
    expect(component.loading).toBe(true);
    expect(component.error).toBe('');
    expect(component.generatedDate).toBeInstanceOf(Date);
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
      expect(component.error).toContain('Failed to load quotes');
      expect(component.loading).toBe(false);
      expect(component.quotes).toEqual([]);
      done();
    }, 100);
  });

  it('should calculate total amount correctly', () => {
    component.quotes = mockQuotes;
    const total = component.calculateTotalAmount();
    expect(total).toBe(163.00); // 108.00 + 55.00
  });

  it('should calculate total tax correctly', () => {
    component.quotes = mockQuotes;
    const totalTax = component.calculateTotalTax();
    expect(totalTax).toBe(12.00); // 8.00 + 4.00
  });

  it('should calculate total subtotal correctly', () => {
    component.quotes = mockQuotes;
    const subtotal = component.calculateTotalSubtotal();
    expect(subtotal).toBe(155.00); // (2 * 50.00) + (1 * 55.00)
  });

  it('should export to CSV when quotes are available', () => {
    component.quotes = mockQuotes;
    spyOn(document, 'createElement').and.callThrough();
    
    component.exportToCSV();
    
    expect(component.error).toBe('');
  });

  it('should show error when exporting with no quotes', () => {
    component.quotes = [];
    component.exportToCSV();
    
    expect(component.error).toBe('No quotes available to export.');
  });

  it('should update generated date when loading quotes', (done) => {
    const initialDate = component.generatedDate;
    apiService.getQuotes.and.returnValue(of(mockQuotes));
    
    component.loadQuotes();
    
    setTimeout(() => {
      expect(component.generatedDate).not.toBe(initialDate);
      done();
    }, 100);
  });

  it('should handle empty quotes array', (done) => {
    apiService.getQuotes.and.returnValue(of([]));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      expect(component.quotes).toEqual([]);
      expect(component.loading).toBe(false);
      expect(component.calculateTotalAmount()).toBe(0);
      expect(component.calculateTotalTax()).toBe(0);
      expect(component.calculateTotalSubtotal()).toBe(0);
      done();
    }, 100);
  });
});

