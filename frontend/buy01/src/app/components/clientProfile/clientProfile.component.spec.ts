import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientprofileComponent } from './clientprofile.component';

describe('ClientprofileComponent', () => {
  let component: ClientprofileComponent;
  let fixture: ComponentFixture<ClientprofileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientprofileComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ClientprofileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
