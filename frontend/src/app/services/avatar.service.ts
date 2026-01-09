import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from './user.service';
import { USER_BASE_URL } from '../constants/constants';
import { AVATAR_BASE_URL } from '../constants/constants';

interface AvatarResponse { avatarUrl: string; }

@Injectable({ providedIn: 'root' })
export class AvatarService {
  private apiUrl = `${AVATAR_BASE_URL}`;

  constructor(private http: HttpClient) {}

  uploadAvatar(file: File, userId: string): Observable<AvatarResponse> {
    const form = new FormData();
    form.append('avatar', file);
    form.append('userId', userId);
    return this.http.post<AvatarResponse>(this.apiUrl, form);
  }

  updateMyAvatar(file: File): Observable<User> {
    const form = new FormData();
    form.append('avatar', file); // field name expected by backend
    return this.http.put<User>(`${USER_BASE_URL}/me`, form);
  }

  buildAvatarUrl(filename: string | null | undefined): string {
    return filename ? `${AVATAR_BASE_URL}/${filename}` : 'assets/default.jpg';
  }

}
