import { Component, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../api';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'app-quotes',
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './quotes.html'
})
export class QuotesComponent {

  quotes: any[] = [];
  loading: boolean = true;
  error: string = '';
  successMessage: string = '';
  deleting: { [key: number]: boolean } = {}; // Track deleting state per quote
  updating: { [key: number]: boolean } = {}; // Track updating state per quote
  editingQuote: { [key: number]: boolean } = {}; // Track which quote is being edited
  supplierOptions: { [key: number]: any[] } = {}; // Store supplier options for each quote
  selectedSupplier: { [key: number]: number | null } = {}; // Track selected supplier for each quote

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {
    this.loadQuotes();
  }

  loadQuotes() {
    this.loading = true;
    this.error = '';
    this.quotes = [];
    
    console.log('Loading quotes...');
    
    this.api.getQuotes().subscribe({
      next: (data) => {
        console.log('Quotes data received:', data);
        console.log('Data type:', typeof data);
        console.log('Is array:', Array.isArray(data));
        console.log('Data length:', data?.length);
        console.log('Raw data:', JSON.stringify(data, null, 2));
        
        this.quotes = Array.isArray(data) ? data : [];
        this.loading = false;
        
        if (this.quotes.length > 0) {
          console.log('First quote:', this.quotes[0]);
          console.log('First quote items:', this.quotes[0]?.items);
        }
        
        this.cdr.detectChanges(); // Force change detection
        console.log('After update - quotes:', this.quotes);
        console.log('After update - loading:', this.loading);
      },
      error: (err) => {
        console.error('Error loading quotes:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Error body:', err.error);
        
        if (err.status === 0 || err.message?.includes('Failed to fetch')) {
          this.error = 'Cannot connect to backend. Please make sure the Spring Boot server is running.';
        } else if (err.status === 500) {
          this.error = 'Server error: ' + (err.error?.message || 'Internal server error');
        } else {
          this.error = 'Failed to load quotes: ' + (err.error?.message || err.message || 'Unknown error');
        }
        this.loading = false;
        this.cdr.detectChanges(); // Force change detection
      }
    });
  }

  deleteQuote(orderId: number) {
    if (!confirm(`Are you sure you want to delete quote #${orderId}? This action cannot be undone.`)) {
      return;
    }

    this.deleting[orderId] = true;
    this.error = '';
    this.successMessage = '';

    console.log('Deleting quote:', orderId);

    this.api.deleteQuote(orderId).subscribe({
      next: () => {
        console.log('Quote deleted successfully');
        this.successMessage = `Quote #${orderId} deleted successfully.`;
        this.deleting[orderId] = false;
        
        // Reload quotes to refresh the list
        this.loadQuotes();
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (err) => {
        console.error('Error deleting quote:', err);
        console.error('Error status:', err.status);
        console.error('Error body:', err.error);
        
        if (err.status === 404) {
          this.error = 'Quote not found. It may have already been deleted.';
        } else if (err.status === 500) {
          this.error = 'Server error: ' + (err.error?.message || 'Internal server error');
        } else {
          this.error = 'Failed to delete quote: ' + (err.error?.message || err.message || 'Unknown error');
        }
        this.deleting[orderId] = false;
        this.cdr.detectChanges();
      }
    });
  }

  startEditQuote(quote: any) {
    const orderId = quote.orderId;
    this.editingQuote[orderId] = true;
    this.error = '';
    this.successMessage = '';
    
    // Get the part ID from the first item
    const partId = quote.items && quote.items.length > 0 ? quote.items[0].part.partId : null;
    
    if (partId) {
      // Load supplier options for this part
      this.api.comparePart(partId).subscribe({
        next: (comparison) => {
          this.supplierOptions[orderId] = comparison.options || [];
          // Set current supplier as default selection
          if (quote.items && quote.items.length > 0) {
            const currentSupplierId = quote.items[0].partSupplierId || quote.items[0].selectedSupplier?.partSupplierId;
            this.selectedSupplier[orderId] = currentSupplierId || (comparison.options && comparison.options.length > 0 ? comparison.options[0].partSupplierId : null);
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error loading supplier options:', err);
          this.error = 'Failed to load supplier options. Please try again.';
          this.editingQuote[orderId] = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  cancelEditQuote(orderId: number) {
    this.editingQuote[orderId] = false;
    this.supplierOptions[orderId] = [];
    this.selectedSupplier[orderId] = null;
    this.error = '';
    this.cdr.detectChanges();
  }

  updateQuoteSupplier(quote: any) {
    const orderId = quote.orderId;
    const newPartSupplierId = this.selectedSupplier[orderId];
    
    if (!newPartSupplierId) {
      this.error = 'Please select a supplier.';
      this.cdr.detectChanges();
      return;
    }
    
    // Check if supplier is actually different
    const currentSupplierId = quote.items && quote.items.length > 0 
      ? (quote.items[0].partSupplierId || quote.items[0].selectedSupplier?.partSupplierId)
      : null;
    
    if (currentSupplierId === newPartSupplierId) {
      this.error = 'Please select a different supplier.';
      this.cdr.detectChanges();
      return;
    }

    this.updating[orderId] = true;
    this.error = '';
    this.successMessage = '';

    console.log('Updating quote:', orderId, 'to supplier:', newPartSupplierId);

    this.api.updateQuoteSupplier(orderId, newPartSupplierId).subscribe({
      next: (updatedQuote) => {
        console.log('Quote updated successfully:', updatedQuote);
        this.successMessage = `Quote #${orderId} updated successfully.`;
        this.updating[orderId] = false;
        this.editingQuote[orderId] = false;
        this.supplierOptions[orderId] = [];
        this.selectedSupplier[orderId] = null;
        
        // Reload quotes to refresh the list
        this.loadQuotes();
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (err) => {
        console.error('Error updating quote:', err);
        console.error('Error status:', err.status);
        console.error('Error body:', err.error);
        
        if (err.status === 404) {
          this.error = 'Quote not found.';
        } else if (err.status === 500) {
          this.error = 'Server error: ' + (err.error?.message || 'Internal server error');
        } else {
          this.error = 'Failed to update quote: ' + (err.error?.message || err.message || 'Unknown error');
        }
        this.updating[orderId] = false;
        this.cdr.detectChanges();
      }
    });
  }
}