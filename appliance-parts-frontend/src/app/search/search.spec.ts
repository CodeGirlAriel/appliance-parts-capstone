import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchComponent } from './search';
import { ApiService } from '../api';
import { Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { of, throwError } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('SearchComponent', () => {
  let component: SearchComponent;
  let fixture: ComponentFixture<SearchComponent>;
  let apiService: jasmine.SpyObj<ApiService>;
  let router: jasmine.SpyObj<Router>;
  let cdr: ChangeDetectorRef;

  const mockParts = [
    { partId: 'WPW10123456', partName: 'Washer Drain Pump' },
    { partId: 'WPW10315885', partName: 'Washer Agitator' }
  ];

  beforeEach(async () => {
    const apiSpy = jasmine.createSpyObj('ApiService', ['searchParts']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [SearchComponent, HttpClientTestingModule],
      providers: [
        { provide: ApiService, useValue: apiSpy },
        { provide: Router, useValue: routerSpy },
        ChangeDetectorRef
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    cdr = TestBed.inject(ChangeDetectorRef);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty query and no results', () => {
    expect(component.query).toBe('');
    expect(component.results).toEqual([]);
    expect(component.searching).toBe(false);
    expect(component.hasSearched).toBe(false);
    expect(component.error).toBe('');
  });

  it('should not search if query is empty', () => {
    component.query = '';
    component.search();
    
    expect(apiService.searchParts).not.toHaveBeenCalled();
    expect(component.results).toEqual([]);
  });

  it('should not search if query is only whitespace', () => {
    component.query = '   ';
    component.search();
    
    expect(apiService.searchParts).not.toHaveBeenCalled();
    expect(component.results).toEqual([]);
  });

  it('should search for parts and display results', (done) => {
    apiService.searchParts.and.returnValue(of(mockParts));
    
    component.query = 'WPW';
    component.search();
    
    expect(component.searching).toBe(true);
    expect(apiService.searchParts).toHaveBeenCalledWith('WPW');
    
    setTimeout(() => {
      expect(component.results).toEqual(mockParts);
      expect(component.searching).toBe(false);
      expect(component.hasSearched).toBe(true);
      expect(component.error).toBe('');
      done();
    }, 100);
  });

  it('should handle search error - connection error', (done) => {
    const error = { status: 0, message: 'Failed to fetch' };
    apiService.searchParts.and.returnValue(throwError(() => error));
    
    component.query = 'WPW';
    component.search();
    
    setTimeout(() => {
      expect(component.error).toContain('Cannot connect to backend');
      expect(component.searching).toBe(false);
      expect(component.results).toEqual([]);
      done();
    }, 100);
  });

  it('should handle search error - 404', (done) => {
    const error = { status: 404, message: 'Not found' };
    apiService.searchParts.and.returnValue(throwError(() => error));
    
    component.query = 'WPW';
    component.search();
    
    setTimeout(() => {
      expect(component.error).toContain('Search endpoint not found');
      expect(component.searching).toBe(false);
      done();
    }, 100);
  });

  it('should handle search error - 500', (done) => {
    const error = { status: 500, error: { message: 'Internal error' } };
    apiService.searchParts.and.returnValue(throwError(() => error));
    
    component.query = 'WPW';
    component.search();
    
    setTimeout(() => {
      expect(component.error).toContain('Server error');
      expect(component.searching).toBe(false);
      done();
    }, 100);
  });

  it('should navigate to compare page when goToCompare is called', () => {
    const partId = 'WPW10123456';
    component.goToCompare(partId);
    
    expect(router.navigate).toHaveBeenCalledWith(['/compare', partId]);
  });

  it('should handle non-array response', (done) => {
    apiService.searchParts.and.returnValue(of({ data: mockParts } as any));
    
    component.query = 'WPW';
    component.search();
    
    setTimeout(() => {
      expect(component.results).toEqual([]);
      done();
    }, 100);
  });
});
