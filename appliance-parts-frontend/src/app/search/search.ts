import { Component, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../api';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

@Component({
  standalone: true,
  selector: 'app-search',
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './search.html'
})
export class SearchComponent {

  query = '';
  results: any[] = [];
  searching: boolean = false;
  hasSearched: boolean = false;
  error: string = '';

  constructor(
    private api: ApiService, 
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  search() {
    if (!this.query.trim()) {
      return;
    }

    this.searching = true;
    this.hasSearched = true;
    this.error = '';
    this.results = [];

    console.log('Searching for:', this.query);
    console.log('API URL will be:', `http://localhost:8080/parts/search?query=${encodeURIComponent(this.query)}`);

    // Add a timeout fallback in case the observable never completes
    const timeoutId = setTimeout(() => {
      if (this.searching) {
        console.error('Search timeout - observable did not complete');
        this.error = 'Search request timed out. Please check your browser console and ensure the backend is running.';
        this.searching = false;
      }
    }, 15000); // 15 second fallback timeout

    this.api.searchParts(this.query).subscribe({
      next: data => {
        clearTimeout(timeoutId);
        console.log('Search results received:', data);
        console.log('Results type:', typeof data);
        console.log('Results is array:', Array.isArray(data));
        console.log('Results length:', data?.length);
        console.log('Raw data:', JSON.stringify(data, null, 2));
        this.results = Array.isArray(data) ? data : [];
        this.searching = false;
        this.cdr.detectChanges(); // Force change detection
        console.log('After update - results:', this.results);
        console.log('After update - searching:', this.searching);
      },
      error: err => {
        clearTimeout(timeoutId);
        console.error('API error details:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Error body:', err.error);
        console.error('Full error object:', JSON.stringify(err, null, 2));
        
        if (err.status === 0 || err.message?.includes('Failed to fetch')) {
          this.error = 'Cannot connect to backend. Please make sure the Spring Boot server is running on http://localhost:8080 and CORS is configured correctly.';
        } else if (err.status === 404) {
          this.error = 'Search endpoint not found. Please check backend configuration.';
        } else if (err.status === 500) {
          this.error = 'Server error: ' + (err.error?.message || 'Internal server error');
        } else if (err.message?.includes('timeout')) {
          this.error = 'Request timed out. The backend may be slow or unresponsive.';
        } else {
          this.error = 'Failed to search: ' + (err.error?.message || err.message || 'Unknown error. Check browser console for details.');
        }
        this.searching = false;
        this.results = [];
        this.cdr.detectChanges(); // Force change detection
      },
      complete: () => {
        clearTimeout(timeoutId);
        console.log('Search observable completed');
        // Ensure searching is set to false even if complete is called
        this.searching = false;
        this.cdr.detectChanges(); // Force change detection
      }
    });
  }

  goToCompare(partId: string) {
    this.router.navigate(['/compare', partId]);
  }
}