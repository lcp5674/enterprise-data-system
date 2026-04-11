import 'package:intl/intl.dart';

class DateUtils {
  static String formatDate(DateTime? date, {String pattern = 'yyyy-MM-dd'}) {
    if (date == null) return '-';
    return DateFormat(pattern).format(date);
  }

  static String formatDateTime(DateTime? date) {
    if (date == null) return '-';
    return DateFormat('yyyy-MM-dd HH:mm').format(date);
  }

  static String formatRelativeTime(DateTime date) {
    final now = DateTime.now();
    final diff = now.difference(date);

    if (diff.inSeconds < 60) {
      return '刚刚';
    } else if (diff.inMinutes < 60) {
      return '${diff.inMinutes}分钟前';
    } else if (diff.inHours < 24) {
      return '${diff.inHours}小时前';
    } else if (diff.inDays < 7) {
      return '${diff.inDays}天前';
    } else if (diff.inDays < 30) {
      return '${(diff.inDays / 7).floor()}周前';
    } else if (diff.inDays < 365) {
      return '${(diff.inDays / 30).floor()}月前';
    } else {
      return '${(diff.inDays / 365).floor()}年前';
    }
  }
}

class StringUtils {
  static bool isEmpty(String? str) {
    return str == null || str.isEmpty;
  }

  static bool isNotEmpty(String? str) {
    return !isEmpty(str);
  }

  static String defaultIfEmpty(String? str, String defaultValue) {
    return isEmpty(str) ? defaultValue : str!;
  }

  static String truncate(String str, int maxLength, {String suffix = '...'}) {
    if (str.length <= maxLength) return str;
    return '${str.substring(0, maxLength)}$suffix';
  }
}

class NumberUtils {
  static String formatFileSize(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    if (bytes < 1024 * 1024 * 1024) {
      return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
    }
    return '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(1)} GB';
  }

  static String formatNumber(num number) {
    if (number < 1000) return number.toString();
    if (number < 10000) return '${(number / 1000).toStringAsFixed(1)}K';
    if (number < 1000000) return '${(number / 10000).toStringAsFixed(1)}W';
    return '${(number / 1000000).toStringAsFixed(1)}M';
  }

  static String formatPercent(double value, {int decimals = 1}) {
    return '${(value * 100).toStringAsFixed(decimals)}%';
  }
}

class Validators {
  static String? required(String? value, {String? message}) {
    if (value == null || value.isEmpty) {
      return message ?? '此字段不能为空';
    }
    return null;
  }

  static String? email(String? value) {
    if (value == null || value.isEmpty) return null;
    final regex = RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$');
    if (!regex.hasMatch(value)) {
      return '请输入有效的邮箱地址';
    }
    return null;
  }

  static String? phone(String? value) {
    if (value == null || value.isEmpty) return null;
    final regex = RegExp(r'^1[3-9]\d{9}$');
    if (!regex.hasMatch(value)) {
      return '请输入有效的手机号码';
    }
    return null;
  }

  static String? minLength(String? value, int min, {String? message}) {
    if (value == null || value.isEmpty) return null;
    if (value.length < min) {
      return message ?? '长度不能少于$min个字符';
    }
    return null;
  }

  static String? maxLength(String? value, int max, {String? message}) {
    if (value == null || value.isEmpty) return null;
    if (value.length > max) {
      return message ?? '长度不能超过$max个字符';
    }
    return null;
  }

  static String? combine(List<String? Function()> validators) {
    for (final validator in validators) {
      final result = validator();
      if (result != null) return result;
    }
    return null;
  }
}
