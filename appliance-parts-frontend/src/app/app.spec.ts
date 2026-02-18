import { ComponentFixture, TestBed } from '@angular/core/testing';
import { App } from './app';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

describe('App', () => {
  let component: App;
  let fixture: ComponentFixture<App>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App, RouterTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(App);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have title signal', () => {
    expect(component.title()).toBe('appliance-parts-frontend');
  });

  it('should have router injected', () => {
    expect(component.router).toBeTruthy();
    expect(component.router).toBe(router);
  });
});
