#!/usr/bin/env python3

import os
import sys
import json
import argparse
from datetime import datetime
from pathlib import Path
from typing import List, Dict, Optional, Any
from dataclasses import dataclass, asdict
import subprocess

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from analyze_results import PerformanceAnalyzer


@dataclass
class TestReport:
    report_id: str
    report_time: str
    test_environment: str
    test_duration: int
    total_tests: int
    passed_tests: int
    failed_tests: int
    overall_status: str
    performance_metrics: Dict[str, Any]
    api_metrics: Dict[str, Any]
    threshold_results: List[Dict]
    recommendations: List[str]


class ReportGenerator:
    def __init__(self, report_dir: str = "./performance-reports"):
        self.report_dir = Path(report_dir)
        self.jtl_dir = self.report_dir / "jtl"
        self.html_dir = self.report_dir / "html"
        self.charts_dir = self.report_dir / "charts"

        self.report_dir.mkdir(parents=True, exist_ok=True)
        self.jtl_dir.mkdir(exist_ok=True)
        self.html_dir.mkdir(exist_ok=True)
        self.charts_dir.mkdir(exist_ok=True)

    def discover_jtl_files(self) -> List[Path]:
        if not self.jtl_dir.exists():
            return []

        jtl_files = sorted(self.jtl_dir.glob("*.jtl"))
        return jtl_files

    def generate_report(self, output_format: str = "html") -> str:
        print("=" * 60)
        print("EDAMS 性能测试报告生成")
        print("=" * 60)

        jtl_files = self.discover_jtl_files()

        if not jtl_files:
            print("未找到JTL结果文件，生成空报告...")
            return self._generate_empty_report(output_format)

        all_metrics = []
        all_threshold_results = []

        print(f"\n发现 {len(jtl_files)} 个测试结果文件:")
        for jtl_file in jtl_files:
            print(f"  - {jtl_file.name}")

            try:
                analyzer = PerformanceAnalyzer(str(jtl_file))
                metrics = analyzer.analyze()
                all_metrics.append({
                    "file": jtl_file.name,
                    "metrics": asdict(metrics)
                })
                all_threshold_results.extend(analyzer.threshold_results)
            except Exception as e:
                print(f"  警告: 分析 {jtl_file.name} 失败: {e}")

        report = self._create_report_data(all_metrics, all_threshold_results)

        if output_format == "html":
            return self._generate_html_report(report)
        elif output_format == "json":
            return self._generate_json_report(report)
        elif output_format == "markdown":
            return self._generate_markdown_report(report)
        else:
            raise ValueError(f"不支持的输出格式: {output_format}")

    def _create_report_data(self, all_metrics: List[Dict], threshold_results: List) -> TestReport:
        total_tests = len(all_metrics)
        passed_tests = sum(
            1 for r in threshold_results
            if hasattr(r, 'status') and r.status.value == "pass"
        )
        failed_tests = len(threshold_results) - passed_tests

        performance_summary = self._calculate_performance_summary(all_metrics)

        api_metrics = self._calculate_api_metrics(all_metrics)

        recommendations = self._generate_recommendations(all_metrics, threshold_results)

        return TestReport(
            report_id=f"RPT-{datetime.now().strftime('%Y%m%d%H%M%S')}",
            report_time=datetime.now().isoformat(),
            test_environment=os.environ.get('BASE_URL', 'http://localhost:8080'),
            test_duration=300,
            total_tests=total_tests,
            passed_tests=passed_tests,
            failed_tests=failed_tests,
            overall_status="PASS" if failed_tests == 0 else "FAIL",
            performance_metrics=performance_summary,
            api_metrics=api_metrics,
            threshold_results=[asdict(r) for r in threshold_results],
            recommendations=recommendations
        )

    def _calculate_performance_summary(self, all_metrics: List[Dict]) -> Dict:
        if not all_metrics:
            return {}

        p95_values = [m["metrics"].get("p95_response_time", 0) for m in all_metrics]
        p99_values = [m["metrics"].get("p99_response_time", 0) for m in all_metrics]
        success_rates = [m["metrics"].get("success_rate", 0) for m in all_metrics]
        throughputs = [m["metrics"].get("throughput", 0) for m in all_metrics]

        return {
            "avg_p95_response_time": round(sum(p95_values) / len(p95_values), 2) if p95_values else 0,
            "avg_p99_response_time": round(sum(p99_values) / len(p99_values), 2) if p99_values else 0,
            "avg_success_rate": round(sum(success_rates) / len(success_rates), 2) if success_rates else 0,
            "max_throughput": max(throughputs) if throughputs else 0,
            "avg_throughput": round(sum(throughputs) / len(throughputs), 2) if throughputs else 0,
        }

    def _calculate_api_metrics(self, all_metrics: List[Dict]) -> Dict:
        if not all_metrics:
            return {}

        all_requests = sum(m["metrics"].get("total_requests", 0) for m in all_metrics)
        all_errors = sum(m["metrics"].get("error_count", 0) for m in all_metrics)

        return {
            "total_requests": all_requests,
            "total_errors": all_errors,
            "overall_error_rate": round((all_errors / all_requests * 100), 2) if all_requests > 0 else 0,
        }

    def _generate_recommendations(self, all_metrics: List[Dict], threshold_results: List) -> List[str]:
        recommendations = []

        failed_thresholds = [r for r in threshold_results if hasattr(r, 'status') and r.status.value == "fail"]

        if any("P95" in r.name for r in failed_thresholds):
            recommendations.append("优化P95响应时间: 考虑增加缓存、优化数据库查询或增加服务器资源")

        if any("P99" in r.name for r in failed_thresholds):
            recommendations.append("优化P99响应时间: 检查长时间运行的查询，考虑异步处理或增加超时配置")

        if any("成功率" in r.name or "错误率" in r.name for r in failed_thresholds):
            recommendations.append("优化系统稳定性: 分析错误日志，修复导致高错误率的根本原因")

        if not failed_thresholds:
            recommendations.append("当前性能表现良好，建议持续监控并设置容量规划阈值")

        return recommendations

    def _generate_html_report(self, report: TestReport) -> str:
        report_file = self.report_dir / f"performance_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.html"

        status_color = "#52c41a" if report.overall_status == "PASS" else "#f5222d"

        html_content = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EDAMS 性能测试报告 - {report.report_id}</title>
    <style>
        * {{ margin: 0; padding: 0; box-sizing: border-box; }}
        body {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: #f0f2f5; color: #333; }}
        .container {{ max-width: 1400px; margin: 0 auto; padding: 30px; }}
        .header {{ background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%); color: white; padding: 30px; border-radius: 8px; margin-bottom: 30px; }}
        .header h1 {{ font-size: 28px; margin-bottom: 10px; }}
        .header .meta {{ opacity: 0.9; font-size: 14px; }}
        .card {{ background: white; border-radius: 8px; padding: 24px; margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }}
        .card-title {{ font-size: 16px; font-weight: 600; color: #262626; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }}
        .grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; }}
        .stat {{ background: #fafafa; padding: 20px; border-radius: 6px; text-align: center; }}
        .stat-value {{ font-size: 32px; font-weight: 700; color: #1890ff; }}
        .stat-label {{ font-size: 14px; color: #8c8c8c; margin-top: 8px; }}
        .stat-value.success {{ color: #52c41a; }}
        .stat-value.fail {{ color: #f5222d; }}
        table {{ width: 100%; border-collapse: collapse; }}
        th, td {{ padding: 12px 16px; text-align: left; border-bottom: 1px solid #f0f0f0; }}
        th {{ background: #fafafa; font-weight: 600; color: #262626; }}
        tr:hover {{ background: #fafafa; }}
        .status-badge {{ display: inline-block; padding: 4px 12px; border-radius: 4px; font-size: 12px; font-weight: 600; }}
        .status-pass {{ background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }}
        .status-fail {{ background: #fff2f0; color: #f5222d; border: 1px solid #ffccc7; }}
        .recommendation {{ background: #e6f7ff; border-left: 4px solid #1890ff; padding: 12px 16px; margin: 10px 0; border-radius: 0 4px 4px 0; }}
        .metric-bar {{ height: 24px; background: #f0f0f0; border-radius: 4px; overflow: hidden; margin: 8px 0; }}
        .metric-bar-fill {{ height: 100%; background: linear-gradient(90deg, #52c41a, #73d13d); transition: width 0.3s ease; }}
        .metric-bar-fill.warning {{ background: linear-gradient(90deg, #faad14, #ffc53d); }}
        .metric-bar-fill.danger {{ background: linear-gradient(90deg, #f5222d, #ff7875); }}
        .two-col {{ display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }}
        @media (max-width: 768px) {{ .two-col {{ grid-template-columns: 1fr; }} }}
        .footer {{ text-align: center; padding: 20px; color: #8c8c8c; font-size: 12px; }}
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>EDAMS 性能测试报告</h1>
            <div class="meta">
                <p>报告ID: {report.report_id}</p>
                <p>生成时间: {report.report_time}</p>
                <p>测试环境: {report.test_environment}</p>
            </div>
        </div>

        <div class="grid">
            <div class="stat">
                <div class="stat-value {"success" if report.overall_status == "PASS" else "fail"}">{report.overall_status}</div>
                <div class="stat-label">测试状态</div>
            </div>
            <div class="stat">
                <div class="stat-value">{report.total_tests}</div>
                <div class="stat-label">测试场景数</div>
            </div>
            <div class="stat">
                <div class="stat-value success">{report.passed_tests}</div>
                <div class="stat-label">通过项</div>
            </div>
            <div class="stat">
                <div class="stat-value {"fail" if report.failed_tests > 0 else ""}">{report.failed_tests}</div>
                <div class="stat-label">失败项</div>
            </div>
        </div>

        <div class="card">
            <div class="card-title">性能指标摘要</div>
            <div class="grid">
                <div class="stat">
                    <div class="stat-value">{report.performance_metrics.get('avg_p95_response_time', 0)}ms</div>
                    <div class="stat-label">平均P95响应时间</div>
                </div>
                <div class="stat">
                    <div class="stat-value">{report.performance_metrics.get('avg_p99_response_time', 0)}ms</div>
                    <div class="stat-label">平均P99响应时间</div>
                </div>
                <div class="stat">
                    <div class="stat-value">{report.performance_metrics.get('avg_success_rate', 0)}%</div>
                    <div class="stat-label">平均成功率</div>
                </div>
                <div class="stat">
                    <div class="stat-value">{report.performance_metrics.get('max_throughput', 0)}</div>
                    <div class="stat-label">最大吞吐量 (req/s)</div>
                </div>
            </div>
        </div>

        <div class="two-col">
            <div class="card">
                <div class="card-title">API性能指标</div>
                <p>总请求数: {report.api_metrics.get('total_requests', 0)}</p>
                <p>错误总数: {report.api_metrics.get('total_errors', 0)}</p>
                <p>错误率: {report.api_metrics.get('overall_error_rate', 0)}%</p>
            </div>

            <div class="card">
                <div class="card-title">性能压测验收标准</div>
                <table>
                    <tr><th>指标</th><th>目标值</th><th>状态</th></tr>
                    <tr><td>API响应时间 P50</td><td>≤100ms</td><td><span class="status-badge status-pass">待测试</span></td></tr>
                    <tr><td>API响应时间 P95</td><td>≤500ms</td><td><span class="status-badge status-pass">待测试</span></td></tr>
                    <tr><td>API响应时间 P99</td><td>≤1000ms</td><td><span class="status-badge status-pass">待测试</span></td></tr>
                    <tr><td>并发用户支持</td><td>≥1000</td><td><span class="status-badge status-pass">待测试</span></td></tr>
                    <tr><td>系统可用性</td><td>≥99.9%</td><td><span class="status-badge status-pass">待测试</span></td></tr>
                </table>
            </div>
        </div>

        <div class="card">
            <div class="card-title">阈值检查结果</div>
            <table>
                <tr><th>指标</th><th>期望值</th><th>实际值</th><th>状态</th></tr>
"""

        for result in report.threshold_results:
            status_class = "status-pass" if result.get("status") == "pass" else "status-fail"
            status_text = "通过" if result.get("status") == "pass" else "失败"
            html_content += f"""
                <tr>
                    <td>{result.get('name', '')}</td>
                    <td>{result.get('expected', '')}</td>
                    <td>{result.get('actual', '')}{result.get('unit', '')}</td>
                    <td><span class="status-badge {status_class}">{status_text}</span></td>
                </tr>
"""

        html_content += """
            </table>
        </div>

        <div class="card">
            <div class="card-title">优化建议</div>
"""

        for rec in report.recommendations:
            html_content += f'<div class="recommendation">{rec}</div>\n'

        html_content += f"""
        </div>

        <div class="footer">
            <p>EDAMS 企业数据资产管理系统 | 性能测试报告 | {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
        </div>
    </div>
</body>
</html>
"""

        with open(report_file, 'w', encoding='utf-8') as f:
            f.write(html_content)

        print(f"\nHTML报告已生成: {report_file}")
        return str(report_file)

    def _generate_json_report(self, report: TestReport) -> str:
        report_file = self.report_dir / f"performance_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"

        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump(asdict(report), f, indent=2, ensure_ascii=False)

        print(f"JSON报告已生成: {report_file}")
        return str(report_file)

    def _generate_markdown_report(self, report: TestReport) -> str:
        report_file = self.report_dir / f"performance_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.md"

        md_content = f"""# EDAMS 性能测试报告

## 基本信息

| 项目 | 值 |
|------|-----|
| 报告ID | {report.report_id} |
| 生成时间 | {report.report_time} |
| 测试环境 | {report.test_environment} |
| 测试时长 | {report.test_duration}秒 |

## 测试结果摘要

| 指标 | 值 |
|------|-----|
| 测试状态 | **{report.overall_status}** |
| 测试场景数 | {report.total_tests} |
| 通过项 | {report.passed_tests} |
| 失败项 | {report.failed_tests} |

## 性能指标

- 平均P95响应时间: {report.performance_metrics.get('avg_p95_response_time', 0)}ms
- 平均P99响应时间: {report.performance_metrics.get('avg_p99_response_time', 0)}ms
- 平均成功率: {report.performance_metrics.get('avg_success_rate', 0)}%
- 最大吞吐量: {report.performance_metrics.get('max_throughput', 0)} req/s

## 优化建议

"""

        for i, rec in enumerate(report.recommendations, 1):
            md_content += f"{i}. {rec}\n"

        with open(report_file, 'w', encoding='utf-8') as f:
            f.write(md_content)

        print(f"Markdown报告已生成: {report_file}")
        return str(report_file)

    def _generate_empty_report(self, output_format: str) -> str:
        print("生成空报告模板...")

        empty_data = {
            "report_id": f"RPT-{datetime.now().strftime('%Y%m%d%H%M%S')}",
            "report_time": datetime.now().isoformat(),
            "message": "未找到测试结果文件，请先运行性能测试"
        }

        if output_format == "json":
            report_file = self.report_dir / "empty_report.json"
            with open(report_file, 'w', encoding='utf-8') as f:
                json.dump(empty_data, f, indent=2)
        else:
            report_file = self.report_dir / "empty_report.html"
            html = f"""<!DOCTYPE html>
<html><head><title>EDAMS Performance Report</title></head>
<body>
<h1>EDAMS 性能测试报告</h1>
<p>报告ID: {empty_data['report_id']}</p>
<p>生成时间: {empty_data['report_time']}</p>
<p style="color: orange;">{empty_data['message']}</p>
</body></html>"""
            with open(report_file, 'w', encoding='utf-8') as f:
                f.write(html)

        return str(report_file)


def main():
    parser = argparse.ArgumentParser(
        description="EDAMS性能测试报告生成工具",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )

    parser.add_argument(
        "--dir", "-d",
        default="./performance-reports",
        help="报告目录路径 (默认: ./performance-reports)"
    )

    parser.add_argument(
        "--format", "-f",
        choices=["html", "json", "markdown"],
        default="html",
        help="输出格式 (默认: html)"
    )

    parser.add_argument(
        "--all",
        action="store_true",
        help="生成所有格式的报告"
    )

    args = parser.parse_args()

    generator = ReportGenerator(args.dir)

    if args.all:
        for fmt in ["html", "json", "markdown"]:
            try:
                generator.generate_report(fmt)
            except Exception as e:
                print(f"生成 {fmt} 报告失败: {e}")
    else:
        try:
            output_file = generator.generate_report(args.format)
            print(f"\n报告生成成功: {output_file}")
        except Exception as e:
            print(f"报告生成失败: {e}")
            sys.exit(1)


if __name__ == "__main__":
    main()
