import { Component, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../api';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-reports',
  imports: [CommonModule, RouterModule],
  templateUrl: './reports.html'
})
export class ReportsComponent {

  orders: any[] = [];
  loading: boolean = true;
  error: string = '';
  reportTitle: string = 'Appliance Parts Orders & Quotes Report';
  generatedDate: Date = new Date();

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {
    this.loadOrders();
  }

  loadOrders() {
    this.loading = true;
    this.error = '';
    this.orders = [];
    this.generatedDate = new Date();
    
    // Load all orders (quotes and purchased orders)
    this.api.getAllOrders().subscribe({
      next: (data) => {
        this.orders = Array.isArray(data) ? data : [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading orders:', err);
        this.error = 'Failed to load orders for report. Please try again.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  exportToCSV() {
    if (this.orders.length === 0) {
      this.error = 'No orders available to export.';
      return;
    }

    // Create CSV headers
    const headers = [
      'Order ID',
      'Part ID',
      'Part Name',
      'Supplier',
      'Quantity',
      'Unit Price',
      'Subtotal',
      'Tax Amount',
      'Total Amount',
      'Date Created',
      'Status'
    ];

    // Create CSV rows
    const rows = this.orders.flatMap(order => {
      return order.items.map((item: any) => [
        order.orderId,
        item.part.partId,
        item.part.partName,
        item.supplierName || 'N/A',
        item.quantity,
        item.unitPrice.toFixed(2),
        (item.unitPrice * item.quantity).toFixed(2),
        order.taxAmount.toFixed(2),
        order.totalAmount.toFixed(2),
        new Date(order.createdAt).toLocaleString(),
        order.status
      ]);
    });

    // Combine headers and rows
    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.map((cell: any) => `"${cell}"`).join(','))
    ].join('\n');

    // Create blob and download
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    link.setAttribute('href', url);
    link.setAttribute('download', `orders-report-${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  calculateTotalAmount(): number {
    return this.orders.reduce((sum, order) => sum + parseFloat(order.totalAmount || 0), 0);
  }

  calculateTotalTax(): number {
    return this.orders.reduce((sum, order) => sum + parseFloat(order.taxAmount || 0), 0);
  }

  calculateTotalSubtotal(): number {
    return this.orders.reduce((sum, order) => {
      const itemsSubtotal = order.items.reduce((itemSum: number, item: any) => 
        itemSum + (parseFloat(item.unitPrice || 0) * (item.quantity || 0)), 0);
      return sum + itemsSubtotal;
    }, 0);
  }

  getQuotesCount(): number {
    return this.orders.filter(order => order.status === 'QUOTE').length;
  }

  getPurchasedOrdersCount(): number {
    return this.orders.filter(order => 
      order.status === 'NEW' || 
      order.status === 'PROCESSING' || 
      order.status === 'COMPLETED'
    ).length;
  }

  getCanceledOrdersCount(): number {
    return this.orders.filter(order => order.status === 'CANCELED').length;
  }

  getRowColor(status: string, index: number): string {
    // Color code by status
    if (status === 'QUOTE') {
      return index % 2 === 0 ? '#fff9e6' : '#fff4d6'; // Light yellow for quotes
    } else if (status === 'NEW' || status === 'PROCESSING') {
      return index % 2 === 0 ? '#e8f5e9' : '#c8e6c9'; // Light green for purchased
    } else if (status === 'COMPLETED') {
      return index % 2 === 0 ? '#e3f2fd' : '#bbdefb'; // Light blue for completed
    } else if (status === 'CANCELED') {
      return index % 2 === 0 ? '#ffebee' : '#ffcdd2'; // Light red for canceled
    }
    return index % 2 === 0 ? '#ffffff' : '#f9f9f9'; // Default
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'QUOTE':
        return '#f57c00'; // Orange
      case 'NEW':
        return '#388e3c'; // Green
      case 'PROCESSING':
        return '#1976d2'; // Blue
      case 'COMPLETED':
        return '#0288d1'; // Dark blue
      case 'CANCELED':
        return '#d32f2f'; // Red
      default:
        return '#000000'; // Black
    }
  }
}

