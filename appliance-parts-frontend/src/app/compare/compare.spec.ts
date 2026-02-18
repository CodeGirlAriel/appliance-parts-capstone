import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CompareComponent } from './compare';
import { ApiService } from '../api';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { of, throwError } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('CompareComponent', () => {
  let component: CompareComponent;
  let fixture: ComponentFixture<CompareComponent>;
  let apiService: jasmine.SpyObj<ApiService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: any;

  const mockComparison = {
    partId: 'WPW10123456',
    partName: 'Washer Drain Pump',
    options: [
      {
        partSupplierId: 1,
        supplierId: 1,
        supplierName: 'AppliancePartsPros',
        partCost: 50.00,
        numInStock: 12,
        shippingTime: 3
      },
      {
        partSupplierId: 2,
        supplierId: 2,
        supplierName: 'RepairClinic',
        partCost: 55.00,
        numInStock: 8,
        shippingTime: 5
      }
    ]
  };

  beforeEach(async () => {
    const apiSpy = jasmine.createSpyObj('ApiService', ['comparePart', 'saveQuote']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const routeSpy = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('WPW10123456')
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [CompareComponent, HttpClientTestingModule],
      providers: [
        { provide: ApiService, useValue: apiSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: routeSpy },
        ChangeDetectorRef
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CompareComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    activatedRoute = TestBed.inject(ActivatedRoute);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with partId from route', () => {
    expect(component.partId).toBe('WPW10123456');
  });

  it('should load comparison data on init', (done) => {
    apiService.comparePart.and.returnValue(of(mockComparison));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      expect(apiService.comparePart).toHaveBeenCalledWith('WPW10123456', undefined);
      expect(component.comparison).toEqual(mockComparison);
      expect(component.loading).toBe(false);
      expect(component.quantities[1]).toBe(1);
      expect(component.quantities[2]).toBe(1);
      done();
    }, 100);
  });

  it('should handle error when loading comparison', (done) => {
    const error = { status: 404, message: 'Not found' };
    apiService.comparePart.and.returnValue(throwError(() => error));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      expect(component.error).toContain('Part not found');
      expect(component.loading).toBe(false);
      expect(component.comparison).toBeNull();
      done();
    }, 100);
  });

  it('should reload comparison when sort mode changes', (done) => {
    apiService.comparePart.and.returnValue(of(mockComparison));
    
    const event = { target: { value: 'CHEAPEST' } };
    component.onSortChange(event);
    
    setTimeout(() => {
      expect(apiService.comparePart).toHaveBeenCalledWith('WPW10123456', 'CHEAPEST');
      done();
    }, 100);
  });

  it('should save quote successfully', (done) => {
    apiService.comparePart.and.returnValue(of(mockComparison));
    apiService.saveQuote.and.returnValue(of({ orderId: 1 }));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      component.quantities[1] = 2;
      component.saveQuote(1);
      
      expect(apiService.saveQuote).toHaveBeenCalledWith({
        partSupplierId: 1,
        quantity: 2
      });
      
      setTimeout(() => {
        expect(component.successMessage).toContain('Quote saved successfully');
        expect(component.saving[1]).toBe(false);
        expect(component.quantities[1]).toBe(1);
        done();
      }, 100);
    }, 100);
  });

  it('should not save quote if quantity is invalid', () => {
    component.quantities[1] = 0;
    component.saveQuote(1);
    
    expect(apiService.saveQuote).not.toHaveBeenCalled();
    expect(component.error).toBe('Please enter a valid quantity.');
  });

  it('should not save quote if quantity is negative', () => {
    component.quantities[1] = -1;
    component.saveQuote(1);
    
    expect(apiService.saveQuote).not.toHaveBeenCalled();
    expect(component.error).toBe('Please enter a valid quantity.');
  });

  it('should handle error when saving quote', (done) => {
    apiService.comparePart.and.returnValue(of(mockComparison));
    const error = { status: 500, error: { message: 'Server error' } };
    apiService.saveQuote.and.returnValue(throwError(() => error));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      component.quantities[1] = 2;
      component.saveQuote(1);
      
      setTimeout(() => {
        expect(component.error).toContain('Failed to save quote');
        expect(component.saving[1]).toBe(false);
        done();
      }, 100);
    }, 100);
  });

  it('should initialize quantities for all options', (done) => {
    apiService.comparePart.and.returnValue(of(mockComparison));
    
    fixture.detectChanges();
    
    setTimeout(() => {
      expect(component.quantities[1]).toBe(1);
      expect(component.quantities[2]).toBe(1);
      done();
    }, 100);
  });
});
