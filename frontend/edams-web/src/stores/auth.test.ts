/**
 * Auth Store Tests
 */
import { useAuthStore } from './auth';
import { act } from '@testing-library/react';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: jest.fn((key: string) => store[key] || null),
    setItem: jest.fn((key: string, value: string) => { store[key] = value; }),
    removeItem: jest.fn((key: string) => { delete store[key]; }),
    clear: jest.fn(() => { store = {}; }),
  };
})();
Object.defineProperty(window, 'localStorage', { value: localStorageMock });

describe('Auth Store', () => {
  beforeEach(() => {
    // Reset store state
    useAuthStore.setState({
      user: null,
      token: null,
      refreshToken: null,
      isLoggedIn: false,
      loading: false,
      mfaRequired: false,
      mfaToken: null,
    });
    jest.clearAllMocks();
  });

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      const state = useAuthStore.getState();
      
      expect(state.user).toBeNull();
      expect(state.token).toBeNull();
      expect(state.refreshToken).toBeNull();
      expect(state.isLoggedIn).toBe(false);
      expect(state.loading).toBe(false);
      expect(state.mfaRequired).toBe(false);
      expect(state.mfaToken).toBeNull();
    });
  });

  describe('setUser', () => {
    it('should set user data', () => {
      const testUser = { id: '1', username: 'testuser', realName: 'Test User' };
      
      useAuthStore.getState().setUser(testUser);
      
      const state = useAuthStore.getState();
      expect(state.user).toEqual(testUser);
    });
  });

  describe('setToken', () => {
    it('should set token and update isLoggedIn', () => {
      useAuthStore.getState().setToken('test-token', 'refresh-token');
      
      const state = useAuthStore.getState();
      expect(state.token).toBe('test-token');
      expect(state.refreshToken).toBe('refresh-token');
      expect(state.isLoggedIn).toBe(true);
    });
  });

  describe('login', () => {
    it('should update state on successful login', async () => {
      // Mock the login API call
      const mockLogin = jest.fn().mockResolvedValue({
        data: {
          token: 'test-token',
          refreshToken: 'refresh-token',
          user: { id: '1', username: 'testuser' },
        },
      });
      
      useAuthStore.setState({ login: mockLogin });
      
      await act(async () => {
        await useAuthStore.getState().login({
          username: 'testuser',
          password: 'testpass',
        });
      });
      
      expect(mockLogin).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'testpass',
      });
    });
  });

  describe('logout', () => {
    it('should clear all auth state', () => {
      // Set some state first
      useAuthStore.setState({
        user: { id: '1', username: 'testuser' },
        token: 'test-token',
        isLoggedIn: true,
      });
      
      useAuthStore.getState().logout();
      
      const state = useAuthStore.getState();
      expect(state.user).toBeNull();
      expect(state.token).toBeNull();
      expect(state.isLoggedIn).toBe(false);
    });
  });

  describe('setMFARequired', () => {
    it('should set MFA required state', () => {
      useAuthStore.getState().setMFARequired('mfa-token-123');
      
      const state = useAuthStore.getState();
      expect(state.mfaRequired).toBe(true);
      expect(state.mfaToken).toBe('mfa-token-123');
    });
  });

  describe('computed isLoggedIn', () => {
    it('should return true when token exists', () => {
      useAuthStore.setState({ token: 'test-token' });
      expect(useAuthStore.getState().isLoggedIn).toBe(true);
    });

    it('should return false when token is null', () => {
      useAuthStore.setState({ token: null });
      expect(useAuthStore.getState().isLoggedIn).toBe(false);
    });
  });
});
