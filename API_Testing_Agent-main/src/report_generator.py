"""
Report Generator for ACMS API Testing

Generates HTML and JSON reports from test execution results.
"""

import json
import logging
from pathlib import Path
from typing import Dict, List, Any
from datetime import datetime
from .test_runner import ExecutionSummary, TestResult

logger = logging.getLogger(__name__)


class DateTimeEncoder(json.JSONEncoder):
    """JSON encoder that handles datetime objects."""
    def default(self, obj: Any) -> Any:
        if isinstance(obj, datetime):
            return obj.isoformat()
        return super().default(obj)


class ReportGenerator:
    """Generate test execution reports."""
    
    def __init__(self, summary: ExecutionSummary):
        """
        Initialize report generator.
        
        Args:
            summary: ExecutionSummary with test results
        """
        self.summary = summary
    
    def generate_html_report(self, output_file: Path) -> None:
        """
        Generate HTML report.
        
        Args:
            output_file: Path to save HTML report
        """
        html_content = self._build_html()
        
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(html_content)
        
        logger.info(f"Generated HTML report: {output_file}")
    
    def generate_json_report(self, output_file: Path) -> None:
        """
        Generate JSON report.
        
        Args:
            output_file: Path to save JSON report
        """
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        # Create a clean dict to avoid datetime serialization issues
        summary_dict = self.summary.to_dict()
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(summary_dict, f, indent=2, cls=DateTimeEncoder)
        
        logger.info(f"Generated JSON report: {output_file}")
    
    def generate_markdown_report(self, output_file: Path) -> None:
        """
        Generate Markdown report.
        
        Args:
            output_file: Path to save Markdown report
        """
        md_content = self._build_markdown()
        
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(md_content)
        
        logger.info(f"Generated Markdown report: {output_file}")
    
    def _build_html(self) -> str:
        """Build HTML report content."""
        passed_pct = self.summary.success_rate
        failed_pct = 100 - passed_pct
        
        # Color based on success
        if passed_pct >= 80:
            status_color = "#28a745"  # Green
            status_text = "✓ PASS"
        elif passed_pct >= 50:
            status_color = "#ffc107"  # Yellow
            status_text = "⚠ PARTIAL"
        else:
            status_color = "#dc3545"  # Red
            status_text = "✗ FAIL"
        
        # Build test results table
        results_rows = ""
        for result in self.summary.test_results:
            status_icon = "✓" if result.status.value == "PASSED" else "✗"
            status_color = "#28a745" if result.status.value == "PASSED" else "#dc3545"
            
            results_rows += f"""
            <tr>
                <td>{result.test_name}</td>
                <td>{result.test_file}</td>
                <td style="color: {status_color}; font-weight: bold;">{status_icon} {result.status.value}</td>
                <td>{result.duration_ms:.2f}ms</td>
                <td>{result.error_message or "-"}</td>
            </tr>
            """
        
        html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ACMS API Test Report</title>
    <style>
        * {{
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }}
        
        body {{
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f5f5f5;
            color: #333;
            line-height: 1.6;
        }}
        
        .container {{
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }}
        
        header {{
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 8px;
            margin-bottom: 30px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }}
        
        header h1 {{
            font-size: 2.5em;
            margin-bottom: 10px;
        }}
        
        header p {{
            font-size: 1.1em;
            opacity: 0.9;
        }}
        
        .summary {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }}
        
        .summary-card {{
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            border-left: 4px solid #667eea;
        }}
        
        .summary-card.passed {{
            border-left-color: #28a745;
        }}
        
        .summary-card.failed {{
            border-left-color: #dc3545;
        }}
        
        .summary-card.skipped {{
            border-left-color: #6c757d;
        }}
        
        .summary-card h3 {{
            color: #667eea;
            font-size: 0.9em;
            text-transform: uppercase;
            margin-bottom: 10px;
            opacity: 0.7;
        }}
        
        .summary-card .value {{
            font-size: 2em;
            font-weight: bold;
            color: #333;
        }}
        
        .status-badge {{
            display: inline-block;
            padding: 10px 20px;
            border-radius: 4px;
            color: white;
            font-weight: bold;
            font-size: 1.1em;
            margin-bottom: 20px;
        }}
        
        .status-pass {{
            background-color: #28a745;
        }}
        
        .status-partial {{
            background-color: #ffc107;
        }}
        
        .status-fail {{
            background-color: #dc3545;
        }}
        
        section {{
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }}
        
        section h2 {{
            color: #667eea;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #667eea;
        }}
        
        table {{
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }}
        
        table th {{
            background-color: #f8f9fa;
            padding: 12px;
            text-align: left;
            font-weight: 600;
            color: #333;
            border-bottom: 2px solid #dee2e6;
        }}
        
        table td {{
            padding: 12px;
            border-bottom: 1px solid #dee2e6;
        }}
        
        table tr:hover {{
            background-color: #f8f9fa;
        }}
        
        .progress-bar {{
            width: 100%;
            height: 30px;
            background-color: #e9ecef;
            border-radius: 4px;
            overflow: hidden;
            margin-top: 10px;
        }}
        
        .progress-fill {{
            height: 100%;
            background: linear-gradient(90deg, #28a745 0%, #20c997 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
        }}
        
        .error-message {{
            background-color: #f8d7da;
            color: #721c24;
            padding: 12px;
            border-radius: 4px;
            margin-top: 10px;
            border-left: 4px solid #f5c6cb;
        }}
        
        footer {{
            text-align: center;
            padding: 20px;
            color: #666;
            font-size: 0.9em;
        }}
        
        .chart {{
            display: flex;
            justify-content: space-around;
            align-items: flex-end;
            height: 200px;
            margin-top: 20px;
            gap: 20px;
        }}
        
        .chart-bar {{
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: flex-end;
        }}
        
        .bar {{
            width: 100%;
            background-color: #667eea;
            border-radius: 4px 4px 0 0;
            transition: all 0.3s ease;
        }}
        
        .bar:hover {{
            background-color: #764ba2;
            transform: translateY(-5px);
        }}
        
        .bar-label {{
            margin-top: 10px;
            font-weight: bold;
            font-size: 0.9em;
        }}
        
        .bar-value {{
            color: white;
            font-weight: bold;
            padding: 5px;
            font-size: 0.9em;
        }}
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>🧪 ACMS API Test Report</h1>
            <p>Comprehensive Test Execution Results</p>
        </header>
        
        <div class="status-badge status-{('pass' if passed_pct >= 80 else 'partial' if passed_pct >= 50 else 'fail')}">
            {status_text} - {passed_pct:.1f}% Success Rate
        </div>
        
        <div class="summary">
            <div class="summary-card">
                <h3>Total Tests</h3>
                <div class="value">{self.summary.total_tests}</div>
            </div>
            <div class="summary-card passed">
                <h3>Passed</h3>
                <div class="value" style="color: #28a745;">{self.summary.passed_tests}</div>
            </div>
            <div class="summary-card failed">
                <h3>Failed</h3>
                <div class="value" style="color: #dc3545;">{self.summary.failed_tests}</div>
            </div>
            <div class="summary-card skipped">
                <h3>Skipped</h3>
                <div class="value" style="color: #6c757d;">{self.summary.skipped_tests}</div>
            </div>
            <div class="summary-card">
                <h3>Execution Time</h3>
                <div class="value">{self.summary.execution_time_seconds:.2f}s</div>
            </div>
        </div>
        
        <section>
            <h2>📊 Test Results Summary</h2>
            <div class="progress-bar">
                <div class="progress-fill" style="width: {passed_pct}%;">
                    {passed_pct:.1f}%
                </div>
            </div>
            
            <div class="chart">
                <div class="chart-bar">
                    <div class="bar" style="height: {(self.summary.passed_tests / max(self.summary.total_tests, 1)) * 100}%;">
                        <div class="bar-value">{self.summary.passed_tests}</div>
                    </div>
                    <div class="bar-label">Passed</div>
                </div>
                <div class="chart-bar">
                    <div class="bar" style="height: {(self.summary.failed_tests / max(self.summary.total_tests, 1)) * 100}%; background-color: #dc3545;">
                        <div class="bar-value">{self.summary.failed_tests}</div>
                    </div>
                    <div class="bar-label">Failed</div>
                </div>
                <div class="chart-bar">
                    <div class="bar" style="height: {(self.summary.skipped_tests / max(self.summary.total_tests, 1)) * 100}%; background-color: #6c757d;">
                        <div class="bar-value">{self.summary.skipped_tests}</div>
                    </div>
                    <div class="bar-label">Skipped</div>
                </div>
            </div>
        </section>
        
        <section>
            <h2>📋 Test Details</h2>
            <table>
                <thead>
                    <tr>
                        <th>Test Name</th>
                        <th>Test File</th>
                        <th>Status</th>
                        <th>Duration</th>
                        <th>Error Details</th>
                    </tr>
                </thead>
                <tbody>
                    {results_rows}
                </tbody>
            </table>
        </section>
        
        {"" if not self.summary.errors else f'''
        <section>
            <h2>⚠️ Execution Errors</h2>
            {"".join(f'<div class="error-message">{error}</div>' for error in self.summary.errors)}
        </section>
        '''}
        
        <footer>
            <p>Generated on {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
            <p>ACMS API Testing Agent v1.0.0</p>
        </footer>
    </div>
</body>
</html>
"""
        return html
    
    def _build_markdown(self) -> str:
        """Build Markdown report content."""
        passed_pct = self.summary.success_rate
        
        md = f"""# ACMS API Test Report

**Generated**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

## Summary

| Metric | Value |
|--------|-------|
| Total Tests | {self.summary.total_tests} |
| Passed | {self.summary.passed_tests} ✓ |
| Failed | {self.summary.failed_tests} ✗ |
| Skipped | {self.summary.skipped_tests} ⊘ |
| Errors | {self.summary.error_tests} |
| Success Rate | {passed_pct:.1f}% |
| Total Duration | {self.summary.execution_time_seconds:.2f}s |
| Avg Duration | {(self.summary.total_duration_ms / max(self.summary.total_tests, 1)):.2f}ms |

## Status

{'✓ PASS' if passed_pct >= 80 else '⚠ PARTIAL' if passed_pct >= 50 else '✗ FAIL'} - {passed_pct:.1f}% Success Rate

## Test Results

"""
        
        # Group results by file
        by_file: Dict[str, List[TestResult]] = {}
        for result in self.summary.test_results:
            if result.test_file not in by_file:
                by_file[result.test_file] = []
            by_file[result.test_file].append(result)
        
        for test_file in sorted(by_file.keys()):
            results = by_file[test_file]
            passed = sum(1 for r in results if r.status.value == "PASSED")
            failed = sum(1 for r in results if r.status.value == "FAILED")
            
            md += f"### {test_file}\n\n"
            md += f"**Results**: {passed} passed, {failed} failed\n\n"
            md += "| Test | Status | Duration | Error |\n"
            md += "|------|--------|----------|-------|\n"
            
            for result in results:
                status = "✓" if result.status.value == "PASSED" else "✗"
                error = result.error_message[:50] + "..." if result.error_message else "-"
                md += f"| {result.test_name} | {status} {result.status.value} | {result.duration_ms:.2f}ms | {error} |\n"
            
            md += "\n"
        
        # Errors
        if self.summary.errors:
            md += "## Errors\n\n"
            for error in self.summary.errors:
                md += f"- {error}\n"
        
        return md


def main():
    """Demonstrate report generation."""
    import sys
    
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Create sample summary
    from .test_runner import ExecutionSummary, TestResult, TestStatus
    
    summary = ExecutionSummary(
        total_tests=10,
        passed_tests=8,
        failed_tests=2,
        skipped_tests=0,
        error_tests=0,
        total_duration_ms=1000.5
    )
    
    # Add sample results
    for i in range(8):
        summary.test_results.append(TestResult(
            test_name=f"test_case_{i}",
            test_file="test_agents.py",
            status=TestStatus.PASSED,
            duration_ms=100.0
        ))
    
    for i in range(2):
        summary.test_results.append(TestResult(
            test_name=f"test_failure_{i}",
            test_file="test_policies.py",
            status=TestStatus.FAILED,
            duration_ms=50.0,
            error_message="Assertion failed"
        ))
    
    # Generate reports
    generator = ReportGenerator(summary)
    
    generator.generate_html_report(Path("test_report.html"))
    generator.generate_markdown_report(Path("test_report.md"))
    generator.generate_json_report(Path("test_report.json"))
    
    print("✓ Reports generated successfully!")


if __name__ == "__main__":
    main()
