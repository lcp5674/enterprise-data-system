// EDAMS Mobile - API Service 单元测试
// 运行: flutter test test/unit/api_service_test.dart

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:edams_mobile/services/api_service.dart';

@GenerateMocks([http.Client])
import 'api_service_test.mocks.dart';

void main() {
  group('ApiService 单元测试', () {
    late ApiService apiService;
    late MockClient mockClient;

    setUp(() {
      mockClient = MockClient();
      apiService = ApiService(client: mockClient);
    });

    test('GET /api/auth/login 返回成功响应', () async {
      when(mockClient.get(
        any,
        headers: anyNamed('headers'),
      )).thenAnswer((_) async => http.Response(
        '{"code": 200, "data": {"token": "test_token"}}',
        200,
      ));

      final response = await apiService.get('/api/auth/login');
      expect(response['code'], 200);
      expect(response['data']['token'], 'test_token');
    });

    test('POST /api/assets 创建资产成功', () async {
      when(mockClient.post(
        any,
        headers: anyNamed('headers'),
        body: anyNamed('body'),
      )).thenAnswer((_) async => http.Response(
        '{"code": 200, "data": {"id": 1, "name": "test_asset"}}',
        200,
      ));

      final response = await apiService.post('/api/v1/assets', body: {
        'name': 'test_asset',
        'assetType': 'DATABASE_TABLE',
      });

      expect(response['code'], 200);
      expect(response['data']['name'], 'test_asset');
    });

    test('API 请求带Token认证', () async {
      when(mockClient.get(
        any,
        headers: anyNamed('headers'),
      )).thenAnswer((_) async => http.Response('{}', 200));

      await apiService.get('/api/v1/assets');

      final captured = verify(mockClient.get(
        any,
        headers: captureAnyNamed('headers'),
      )).captured.first as Map<String, String>;

      expect(captured.containsKey('Authorization'), true);
      expect(captured['Authorization'], startsWith('Bearer '));
    });

    test('网络错误时抛出异常', () async {
      when(mockClient.get(any, headers: anyNamed('headers')))
          .thenThrow(http.ClientException('Network error'));

      expect(
        () => apiService.get('/api/v1/assets'),
        throwsA(isA<Exception>()),
      );
    });
  });
}
