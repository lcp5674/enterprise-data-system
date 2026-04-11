import 'api_service.dart';

/// 工作流服务
class WorkflowService {
  static final ApiService _api = ApiService();

  /// 获取我的待办任务
  static Future<List<Map<String, dynamic>>> getTodoTasks({
    int page = 1,
    int pageSize = 20,
  }) async {
    final response = await _api.get(
      '/process-tasks/todo',
      queryParameters: {
        'current': page.toString(),
        'size': pageSize.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data']?['records'] ?? response['data']?['list'] ?? response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }

  /// 获取我的已办任务
  static Future<List<Map<String, dynamic>>> getDoneTasks({
    int page = 1,
    int pageSize = 20,
  }) async {
    final response = await _api.get(
      '/process-tasks/done',
      queryParameters: {
        'current': page.toString(),
        'size': pageSize.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data']?['records'] ?? response['data']?['list'] ?? response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }

  /// 获取我的抄送任务
  static Future<List<Map<String, dynamic>>> getCcTasks({
    int page = 1,
    int pageSize = 20,
  }) async {
    final response = await _api.get(
      '/process-tasks/cc',
      queryParameters: {
        'current': page.toString(),
        'size': pageSize.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data']?['records'] ?? response['data']?['list'] ?? response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }

  /// 获取任务详情
  static Future<Map<String, dynamic>> getTaskDetail(String taskId) async {
    final response = await _api.get(
      '/process-tasks/$taskId',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 审批通过任务
  static Future<bool> approveTask(
    String taskId, {
    String? comment,
    Map<String, dynamic>? variables,
  }) async {
    final response = await _api.post(
      '/process-tasks/$taskId/approve',
      body: {
        if (comment != null) 'comment': comment,
        if (variables != null) 'variables': variables,
      },
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 审批拒绝任务
  static Future<bool> rejectTask(
    String taskId, {
    String? comment,
  }) async {
    final response = await _api.post(
      '/process-tasks/$taskId/reject',
      body: {
        if (comment != null) 'comment': comment,
      },
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 退回任务
  static Future<bool> backTask(
    String taskId, {
    String? comment,
  }) async {
    final response = await _api.post(
      '/process-tasks/$taskId/back',
      body: {
        if (comment != null) 'comment': comment,
      },
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 转办任务
  static Future<bool> transferTask(
    String taskId, {
    required String assigneeId,
    String? comment,
  }) async {
    final response = await _api.post(
      '/process-tasks/$taskId/transfer',
      body: {
        'assigneeId': assigneeId,
        if (comment != null) 'comment': comment,
      },
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 委托任务
  static Future<bool> delegateTask(
    String taskId, {
    required String delegateId,
    String? comment,
  }) async {
    final response = await _api.post(
      '/process-tasks/$taskId/delegate',
      body: {
        'delegateId': delegateId,
        if (comment != null) 'comment': comment,
      },
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 发送任务提醒
  static Future<bool> sendTaskReminder(String taskId) async {
    final response = await _api.post(
      '/process-tasks/$taskId/remind',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 批量审批
  static Future<bool> batchApprove(
    List<String> taskIds, {
    required int result,
    String? comment,
  }) async {
    final response = await _api.post(
      '/process-tasks/batch-approve',
      queryParameters: {'result': result.toString()},
      body: {
        'taskIds': taskIds,
        if (comment != null) 'comment': comment,
      },
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 获取流程实例详情
  static Future<Map<String, dynamic>> getProcessInstance(String instanceId) async {
    final response = await _api.get(
      '/workflows/$instanceId',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 获取流程历史
  static Future<List<Map<String, dynamic>>> getProcessHistory(String instanceId) async {
    final response = await _api.get(
      '/workflows/$instanceId/history',
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }
}
