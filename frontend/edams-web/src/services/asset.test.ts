/**
 * Asset Service Tests
 */
import * as assetService from './asset';

// Mock request
jest.mock('./request', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn(),
}));

import { get, post, put, delete: del } from './request';

const mockGet = get as jest.Mock;
const mockPost = post as jest.Mock;
const mockPut = put as jest.Mock;
const mockDel = del as jest.Mock;

describe('Asset Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getAssetList', () => {
    it('should fetch asset list with pagination', async () => {
      const mockData = {
        list: [
          { id: '1', name: 'asset1' },
          { id: '2', name: 'asset2' },
        ],
        total: 100,
      };
      mockGet.mockResolvedValueOnce({ data: mockData });

      const result = await assetService.getAssetList({ page: 1, pageSize: 10 });

      expect(mockGet).toHaveBeenCalledWith('/api/assets', {
        params: { page: 1, pageSize: 10 },
      });
      expect(result).toEqual(mockData);
    });

    it('should support search parameters', async () => {
      mockGet.mockResolvedValueOnce({ data: { list: [], total: 0 } });

      await assetService.getAssetList({
        keyword: 'test',
        type: 'TABLE',
        sensitivityLevel: 'SENSITIVE',
      });

      expect(mockGet).toHaveBeenCalledWith('/api/assets', {
        params: {
          page: 1,
          pageSize: 10,
          keyword: 'test',
          type: 'TABLE',
          sensitivityLevel: 'SENSITIVE',
        },
      });
    });
  });

  describe('getAssetDetail', () => {
    it('should fetch asset detail by id', async () => {
      const mockAsset = { id: '1', name: 'test-asset', type: 'TABLE' };
      mockGet.mockResolvedValueOnce({ data: mockAsset });

      const result = await assetService.getAssetDetail('1');

      expect(mockGet).toHaveBeenCalledWith('/api/assets/1');
      expect(result).toEqual(mockAsset);
    });
  });

  describe('createAsset', () => {
    it('should create new asset', async () => {
      const newAsset = { name: 'new-asset', type: 'TABLE' };
      const mockResponse = { data: { id: '123', ...newAsset } };
      mockPost.mockResolvedValueOnce(mockResponse);

      const result = await assetService.createAsset(newAsset);

      expect(mockPost).toHaveBeenCalledWith('/api/assets', newAsset);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updateAsset', () => {
    it('should update existing asset', async () => {
      const updates = { name: 'updated-name' };
      mockPut.mockResolvedValueOnce({ success: true });

      const result = await assetService.updateAsset('1', updates);

      expect(mockPut).toHaveBeenCalledWith('/api/assets/1', updates);
      expect(result).toEqual({ success: true });
    });
  });

  describe('deleteAsset', () => {
    it('should delete asset by id', async () => {
      mockDel.mockResolvedValueOnce({ success: true });

      const result = await assetService.deleteAsset('1');

      expect(mockDel).toHaveBeenCalledWith('/api/assets/1');
      expect(result).toEqual({ success: true });
    });
  });

  describe('searchAssets', () => {
    it('should perform asset search', async () => {
      const mockResults = {
        list: [{ id: '1', name: 'search-result' }],
        total: 1,
      };
      mockGet.mockResolvedValueOnce({ data: mockResults });

      const result = await assetService.searchAssets('test-query');

      expect(mockGet).toHaveBeenCalledWith('/api/assets/search', {
        params: { keyword: 'test-query' },
      });
      expect(result).toEqual(mockResults);
    });
  });

  describe('getAssetSuggestions', () => {
    it('should return search suggestions', async () => {
      const mockSuggestions = ['suggestion1', 'suggestion2', 'suggestion3'];
      mockGet.mockResolvedValueOnce({ data: mockSuggestions });

      const result = await assetService.getAssetSuggestions('test');

      expect(mockGet).toHaveBeenCalledWith('/api/assets/suggestions', {
        params: { keyword: 'test' },
      });
      expect(result).toEqual(mockSuggestions);
    });
  });
});
