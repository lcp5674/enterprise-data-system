import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/app_config.dart';

class ApiException implements Exception {
  final String message;
  final int? statusCode;

  ApiException(this.message, {this.statusCode});

  @override
  String toString() => message;
}

class ApiService {
  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  String? _token;

  void setToken(String? token) {
    _token = token;
  }

  Map<String, String> get _headers {
    final headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };
    if (_token != null) {
      headers['Authorization'] = 'Bearer $_token';
    }
    return headers;
  }

  Future<T> _handleResponse<T>(
    http.Response response,
    T Function(dynamic json) parser,
  ) async {
    if (response.statusCode >= 200 && response.statusCode < 300) {
      final json = jsonDecode(response.body);
      return parser(json);
    } else if (response.statusCode == 401) {
      throw ApiException('未授权，请重新登录', statusCode: 401);
    } else if (response.statusCode == 403) {
      throw ApiException('没有权限访问', statusCode: 403);
    } else if (response.statusCode == 404) {
      throw ApiException('资源不存在', statusCode: 404);
    } else {
      final json = jsonDecode(response.body);
      final message = json['message'] ?? '请求失败';
      throw ApiException(message, statusCode: response.statusCode);
    }
  }

  Future<T> get<T>(
    String path, {
    Map<String, String>? queryParameters,
    T Function(dynamic json)? parser,
  }) async {
    try {
      var uri = Uri.parse('${AppConfig.baseUrl}$path');
      if (queryParameters != null && queryParameters.isNotEmpty) {
        uri = uri.replace(queryParameters: queryParameters);
      }

      final response = await http
          .get(uri, headers: _headers)
          .timeout(AppConfig.apiTimeout);

      if (parser != null) {
        return _handleResponse(response, parser);
      }
      return jsonDecode(response.body) as T;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException('网络请求失败: $e');
    }
  }

  Future<T> post<T>(
    String path, {
    Map<String, dynamic>? body,
    T Function(dynamic json)? parser,
  }) async {
    try {
      final uri = Uri.parse('${AppConfig.baseUrl}$path');
      final response = await http
          .post(
            uri,
            headers: _headers,
            body: body != null ? jsonEncode(body) : null,
          )
          .timeout(AppConfig.apiTimeout);

      if (parser != null) {
        return _handleResponse(response, parser);
      }
      return jsonDecode(response.body) as T;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException('网络请求失败: $e');
    }
  }

  Future<T> put<T>(
    String path, {
    Map<String, dynamic>? body,
    T Function(dynamic json)? parser,
  }) async {
    try {
      final uri = Uri.parse('${AppConfig.baseUrl}$path');
      final response = await http
          .put(
            uri,
            headers: _headers,
            body: body != null ? jsonEncode(body) : null,
          )
          .timeout(AppConfig.apiTimeout);

      if (parser != null) {
        return _handleResponse(response, parser);
      }
      return jsonDecode(response.body) as T;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException('网络请求失败: $e');
    }
  }

  Future<T> delete<T>(
    String path, {
    T Function(dynamic json)? parser,
  }) async {
    try {
      final uri = Uri.parse('${AppConfig.baseUrl}$path');
      final response = await http
          .delete(uri, headers: _headers)
          .timeout(AppConfig.apiTimeout);

      if (parser != null) {
        return _handleResponse(response, parser);
      }
      return jsonDecode(response.body) as T;
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException('网络请求失败: $e');
    }
  }
}
