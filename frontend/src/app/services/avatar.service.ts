import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
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
}
