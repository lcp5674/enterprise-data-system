/**
 * Request Service Tests
 */
import request, { AxiosRequestConfig } from './request';

// Mock axios
jest.mock('axios', () => ({
  create: jest.fn(() => ({
    interceptors: {
      request: { use: jest.fn(), eject: jest.fn() },
      response: { use: jest.fn(), eject: jest.fn() },
    },
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    patch: jest.fn(),
    request: jest.fn(),
  })),
}));

describe('Request Service', () => {
  describe('HTTP Methods', () => {
    it('should expose get method', () => {
      expect(typeof request.get).toBe('function');
    });

    it('should expose post method', () => {
      expect(typeof request.post).toBe('function');
    });

    it('should expose put method', () => {
      expect(typeof request.put).toBe('function');
    });

    it('should expose delete method', () => {
      expect(typeof request.delete).toBe('function');
    });

    it('should expose patch method', () => {
      expect(typeof request.patch).toBe('function');
    });

    it('should expose upload method', () => {
      expect(typeof request.upload).toBe('function');
    });

    it('should expose download method', () => {
      expect(typeof request.download).toBe('function');
    });
  });

  describe('Request Configuration', () => {
    it('should have default timeout configured', () => {
      // 验证默认配置存在
      expect(request.defaults.timeout).toBeDefined();
    });
  });
});
