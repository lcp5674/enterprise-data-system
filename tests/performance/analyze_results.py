#!/usr/bin/env python3

import sys
import os
import json
import argparse
from datetime import datetime
from typing import Dict, List, Optional, Any
from dataclasses import dataclass, asdict
from enum import Enum

try:
    import pandas as pd
except ImportError:
    print("警告: pandas未安装，请运行: pip install pandas")
    pd = None


class ThresholdLevel(Enum):
    PASS = "pass"
    WARNING = "warning"
    FAIL = "fail"


@dataclass
class PerformanceMetrics:
    total_requests: int = 0
    success_count: int = 0
    error_count: int = 0
    success_rate: float = 0.0
    avg_response_time: float = 0.0
    p50_response_time: float = 0.0
    p90_response_time: float = 0.0
    p95_response_time: float = 0.0
    p99_response_time: float = 0.0
    max_response_time: float = 0.0
    min_response_time: float = 0.0
    throughput: float = 0.0
    errors: Dict[str, int] = None

    def __post_init__(self):
        if self.errors is None:
            self.errors = {}


@dataclass
class ThresholdResult:
    name: str
    expected: str
    actual: float
    unit: str
    status: ThresholdLevel
    message: str


class PerformanceAnalyzer:
    P50_THRESHOLD_MS = 100
    P95_THRESHOLD_MS = 500
    P99_THRESHOLD_MS = 1000
    SEARCH_RESPONSE_THRESHOLD_MS = 200
    LINEAGE_QUERY_THRESHOLD_MS = 500
    CONCURRENT_USERS_TARGET = 1000
    AVAILABILITY_TARGET = 99.9
    ERROR_RATE_THRESHOLD = 0.01

    def __init__(self, jtl_file: str):
        self.jtl_file = jtl_file
        self.metrics: Optional[PerformanceMetrics] = None
        self.threshold_results: List[ThresholdResult] = []

    def analyze(self) -> PerformanceMetrics:
        if pd is None:
            raise RuntimeError("pandas库未安装，无法分析JTL文件")

        if not os.path.exists(self.jtl_file):
            raise FileNotFoundError(f"JTL文件不存在: {self.jtl_file}")

        print(f"正在分析文件: {self.jtl_file}")

        try:
            df = pd.read_csv(self.jtl_file)
        except Exception as e:
            print(f"读取CSV文件失败: {e}")
            df = self._parse_csv_manually()

        self.metrics = self._calculate_metrics(df)
        self._check_thresholds()
        return self.metrics

    def _parse_csv_manually(self) -> pd.DataFrame:
        data = []
        with open(self.jtl_file, 'r') as f:
            header = f.readline().strip().split(',')
            for line in f:
                values = line.strip().split(',')
                if len(values) >= len(header):
                    row = dict(zip(header, values))
                    data.append(row)

        return pd.DataFrame(data)

    def _calculate_metrics(self, df: pd.DataFrame) -> PerformanceMetrics:
        elapsed_col = 'elapsed'
        success_col = 'success'
        time_col = 'timeStamp'
        response_code_col = 'responseCode'

        if elapsed_col not in df.columns:
            print(f"警告: 列 '{elapsed_col}' 不存在")
            return PerformanceMetrics()

        df['elapsed'] = pd.to_numeric(df['elapsed'], errors='coerce').fillna(0)

        total_requests = len(df)

        if success_col in df.columns:
            success_count = df[success_col].astype(str).str.lower().isin(['true', 'true']).sum()
            error_count = total_requests - success_count
        else:
            success_count = total_requests
            error_count = 0

        success_rate = (success_count / total_requests * 100) if total_requests > 0 else 0

        elapsed_times = df['elapsed']

        metrics = PerformanceMetrics(
            total_requests=total_requests,
            success_count=int(success_count),
            error_count=int(error_count),
            success_rate=round(success_rate, 2),
            avg_response_time=round(elapsed_times.mean(), 2) if len(elapsed_times) > 0 else 0,
            p50_response_time=round(elapsed_times.quantile(0.5), 2) if len(elapsed_times) > 0 else 0,
            p90_response_time=round(elapsed_times.quantile(0.9), 2) if len(elapsed_times) > 0 else 0,
            p95_response_time=round(elapsed_times.quantile(0.95), 2) if len(elapsed_times) > 0 else 0,
            p99_response_time=round(elapsed_times.quantile(0.99), 2) if len(elapsed_times) > 0 else 0,
            max_response_time=round(elapsed_times.max(), 2) if len(elapsed_times) > 0 else 0,
            min_response_time=round(elapsed_times.min(), 2) if len(elapsed_times) > 0 else 0,
        )

        if time_col in df.columns:
            df['timeStamp'] = pd.to_numeric(df[time_col], errors='coerce')
            timestamps = df['timeStamp'].dropna()
            if len(timestamps) > 1:
                time_diff = (timestamps.max() - timestamps.min()) / 1000.0
                if time_diff > 0:
                    metrics.throughput = round(total_requests / time_diff, 2)

        if response_code_col in df.columns:
            error_codes = df[df[response_code_col].astype(str).str.startswith('5')][response_code_col].value_counts()
            metrics.errors = error_codes.to_dict()

        return metrics

    def _check_thresholds(self):
        if self.metrics is None:
            return

        self.threshold_results = []

        self._add_threshold_result(
            "P50响应时间",
            f"≤{self.P50_THRESHOLD_MS}ms",
            self.metrics.p50_response_time,
            "ms",
            self.metrics.p50_response_time <= self.P50_THRESHOLD_MS
        )

        self._add_threshold_result(
            "P95响应时间",
            f"≤{self.P95_THRESHOLD_MS}ms",
            self.metrics.p95_response_time,
            "ms",
            self.metrics.p95_response_time <= self.P95_THRESHOLD_MS
        )

        self._add_threshold_result(
            "P99响应时间",
            f"≤{self.P99_THRESHOLD_MS}ms",
            self.metrics.p99_response_time,
            "ms",
            self.metrics.p99_response_time <= self.P99_THRESHOLD_MS
        )

        self._add_threshold_result(
            "成功率",
            f"≥{self.AVAILABILITY_TARGET}%",
            self.metrics.success_rate,
            "%",
            self.metrics.success_rate >= self.AVAILABILITY_TARGET
        )

    def _add_threshold_result(self, name: str, expected: str, actual: float, unit: str, passed: bool):
        status = ThresholdLevel.PASS if passed else ThresholdLevel.FAIL
        message = f"{name}: 实际值 {actual}{unit}, 期望值 {expected}"

        self.threshold_results.append(ThresholdResult(
            name=name,
            expected=expected,
            actual=actual,
            unit=unit,
            status=status,
            message=message
        ))

    def print_results(self):
        if self.metrics is None:
            print("未进行分析，请先调用 analyze() 方法")
            return

        print("\n" + "=" * 60)
        print("性能测试结果分析")
        print("=" * 60)
        print(f"总请求数:        {self.metrics.total_requests}")
        print(f"成功请求:        {self.metrics.success_count}")
        print(f"失败请求:        {self.metrics.error_count}")
        print(f"成功率:          {self.metrics.success_rate:.2f}%")
        print("-" * 60)
        print(f"平均响应时间:    {self.metrics.avg_response_time:.2f}ms")
        print(f"P50响应时间:     {self.metrics.p50_response_time:.2f}ms")
        print(f"P90响应时间:     {self.metrics.p90_response_time:.2f}ms")
        print(f"P95响应时间:     {self.metrics.p95_response_time:.2f}ms")
        print(f"P99响应时间:     {self.metrics.p99_response_time:.2f}ms")
        print(f"最大响应时间:    {self.metrics.max_response_time:.2f}ms")
        print(f"最小响应时间:    {self.metrics.min_response_time:.2f}ms")
        print("-" * 60)
        print(f"吞吐量:          {self.metrics.throughput:.2f} req/s")

        if self.metrics.errors:
            print("-" * 60)
            print("错误分布:")
            for code, count in self.metrics.errors.items():
                print(f"  {code}: {count}次")

        print("=" * 60)

    def print_threshold_check(self):
        if not self.threshold_results:
            return

        print("\n阈值检查:")
        print("-" * 60)

        all_passed = True
        for result in self.threshold_results:
            status_icon = "✓" if result.status == ThresholdLevel.PASS else "✗"
            status_color = "\033[92m" if result.status == ThresholdLevel.PASS else "\033[91m"
            reset_color = "\033[0m"

            print(f"{status_color}{status_icon} {result.message}{reset_color}")

            if result.status != ThresholdLevel.PASS:
                all_passed = False

        if all_passed:
            print("\n\033[92m✓ 所有性能指标均满足要求\033[0m")
        else:
            print("\n\033[91m✗ 部分性能指标未达到要求，需要优化\033[0m")

    def export_json(self, output_file: str):
        if self.metrics is None:
            print("未进行分析，无法导出JSON")
            return

        data = {
            "analysis_time": datetime.now().isoformat(),
            "source_file": self.jtl_file,
            "metrics": asdict(self.metrics),
            "threshold_results": [
                {
                    "name": r.name,
                    "expected": r.expected,
                    "actual": r.actual,
                    "unit": r.unit,
                    "status": r.status.value,
                    "message": r.message
                }
                for r in self.threshold_results
            ]
        }

        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)

        print(f"JSON报告已导出: {output_file}")


def main():
    parser = argparse.ArgumentParser(
        description="EDAMS性能测试结果分析工具",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  python analyze_results.py results.jtl
  python analyze_results.py results.jtl --output report.json
  python analyze_results.py results.jtl --export-json
        """
    )

    parser.add_argument(
        "jtl_file",
        nargs="?",
        help="JMeter JTL结果文件路径"
    )

    parser.add_argument(
        "--output", "-o",
        help="输出JSON报告文件路径"
    )

    parser.add_argument(
        "--export-json", "-e",
        action="store_true",
        help="导出JSON格式报告"
    )

    parser.add_argument(
        "--quiet", "-q",
        action="store_true",
        help="静默模式，仅输出结果"
    )

    args = parser.parse_args()

    if not args.jtl_file:
        parser.print_help()
        print("\n请提供JTL文件路径")
        sys.exit(1)

    if not os.path.exists(args.jtl_file):
        print(f"错误: 文件不存在 - {args.jtl_file}")
        sys.exit(1)

    try:
        analyzer = PerformanceAnalyzer(args.jtl_file)
        analyzer.analyze()

        if not args.quiet:
            analyzer.print_results()
            analyzer.print_threshold_check()

        if args.export_json or args.output:
            output_file = args.output or args.jtl_file.replace('.jtl', '_report.json')
            analyzer.export_json(output_file)

        failed_count = sum(1 for r in analyzer.threshold_results if r.status == ThresholdLevel.FAIL)
        sys.exit(0 if failed_count == 0 else 1)

    except Exception as e:
        print(f"分析失败: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
