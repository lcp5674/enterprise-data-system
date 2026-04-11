/**
 * Auth Service Tests
 */
import * as authService from './auth';

// Mock request
jest.mock('./request', () => ({
  post: jest.fn(),
  get: jest.fn(),
  put: jest.fn(),
}));

import { post, get, put } from './request';

const mockPost = post as jest.Mock;
const mockGet = get as jest.Mock;
const mockPut = put as jest.Mock;

describe('Auth Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('login', () => {
    it('should call post with correct parameters', async () => {
      const mockResponse = { data: { token: 'test-token' } };
      mockPost.mockResolvedValueOnce(mockResponse);

      const result = await authService.login({
        username: 'testuser',
        password: 'testpass',
      });

      expect(mockPost).toHaveBeenCalledWith('/api/auth/login', {
        username: 'testuser',
        password: 'testpass',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('logout', () => {
    it('should call post to logout endpoint', async () => {
      mockPost.mockResolvedValueOnce({ success: true });

      const result = await authService.logout();

      expect(mockPost).toHaveBeenCalledWith('/api/auth/logout');
      expect(result).toEqual({ success: true });
    });
  });

  describe('getCurrentUser', () => {
    it('should call get to fetch current user', async () => {
      const mockUser = { id: '1', username: 'testuser', realName: 'Test User' };
      mockGet.mockResolvedValueOnce({ data: mockUser });

      const result = await authService.getCurrentUser();

      expect(mockGet).toHaveBeenCalledWith('/api/auth/current');
      expect(result).toEqual(mockUser);
    });
  });

  describe('refreshToken', () => {
    it('should call post to refresh token', async () => {
      const mockResponse = { data: { token: 'new-token' } };
      mockPost.mockResolvedValueOnce(mockResponse);

      const result = await authService.refreshToken('refresh-token');

      expect(mockPost).toHaveBeenCalledWith('/api/auth/refresh', {
        refreshToken: 'refresh-token',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updatePassword', () => {
    it('should call put to update password', async () => {
      mockPut.mockResolvedValueOnce({ success: true });

      const result = await authService.updatePassword({
        oldPassword: 'old',
        newPassword: 'new',
      });

      expect(mockPut).toHaveBeenCalledWith('/api/auth/password', {
        oldPassword: 'old',
        newPassword: 'new',
      });
      expect(result).toEqual({ success: true });
    });
  });
});
