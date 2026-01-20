import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AvatarService } from './avatar.service';
import { User } from './user.service';
import { AVATAR_BASE_URL, USER_BASE_URL } from '../constants/constants';

describe('AvatarService', () => {
  let service: AvatarService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AvatarService]
    });
    service = TestBed.inject(AvatarService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should upload avatar', () => {
    const mockFile = new File([''], 'test.png', { type: 'image/png' });
    const userId = '123';
    const mockResponse = { avatarUrl: 'http://example.com/avatar.png' };

    service.uploadAvatar(mockFile, userId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${AVATAR_BASE_URL}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    expect(req.request.body.has('avatar')).toBeTrue();
    expect(req.request.body.has('userId')).toBeTrue();
    req.flush(mockResponse);
  });

  it('should update my avatar', () => {
    const mockFile = new File([''], 'me.png', { type: 'image/png' });
    const mockUser: User = {
      name: 'Test User',
      email: 'test@example.com',
      role: 'USER',
      avatar: 'new-avatar.png',
      ownProfile: true,
      products: []
    };

    service.updateMyAvatar(mockFile).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${USER_BASE_URL}/me`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body instanceof FormData).toBeTrue();
    expect(req.request.body.has('avatar')).toBeTrue();
    req.flush(mockUser);
  });

  it('should build correct avatar url when filename is provided', () => {
    const filename = 'image.png';
    const expected = `${AVATAR_BASE_URL}/${filename}`;
    expect(service.buildAvatarUrl(filename)).toBe(expected);
  });

  it('should build default avatar url when filename is null', () => {
    expect(service.buildAvatarUrl(null)).toBe('assets/default.jpg');
  });

  it('should build default avatar url when filename is undefined', () => {
    expect(service.buildAvatarUrl(undefined)).toBe('assets/default.jpg');
  });
});
