#!/usr/bin/env python3
"""
ACMS API Testing Agent - Main Entry Point

Comprehensive API testing agent with Llama3 integration.
Generates, executes, and reports on API tests for the ACMS system.
"""

import logging
import sys
import json
from pathlib import Path
from datetime import datetime
from typing import Optional, Dict, Any

from colorama import Fore, Style, init
import click

from src.config import Config
from src.ollama_agent import OllamaAgent, TestCaseGenerator
from src.mock_data import MockDataGenerator
from src.test_runner import TestRunner
from src.report_generator import ReportGenerator
from src.validators import (
    ResponseValidator, SchemaValidator, BusinessLogicValidator,
    SecurityValidator, ComplianceValidator
)

# Initialize colorama
init(autoreset=True)

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class ACMSTestingAgent:
    """Main ACMS Testing Agent."""
    
    def __init__(self, config: Optional[Config] = None):
        """
        Initialize testing agent.
        
        Args:
            config: Configuration object (defaults to Config())
        """
        self.config = config or Config()
        self.llm_agent = OllamaAgent(base_url="http://localhost:11434", model="llama3")
        self.mock_generator = MockDataGenerator(seed=42)
        self.test_runner = TestRunner(
            test_dir=self.config.output_dir / "tests" / "python",
            api_url=self.config.api_base_url
        )
    
    def check_prerequisites(self) -> bool:
        """
        Check all prerequisites are met.
        
        Returns:
            True if all prerequisites are met
        """
        click.echo(f"\n{Fore.CYAN}Checking prerequisites...{Style.RESET_ALL}")
        
        all_ok = True
        
        # Check Ollama
        click.echo("  Checking Ollama connection...", nl=False)
        if self.llm_agent.check_connection():
            click.echo(f" {Fore.GREEN}✓{Style.RESET_ALL}")
        else:
            click.echo(f" {Fore.RED}✗{Style.RESET_ALL}")
            click.echo(f"    {Fore.RED}Error: Ollama not running. Start with: ollama serve{Style.RESET_ALL}")
            all_ok = False
        
        # Check Llama3 model
        click.echo("  Checking Llama3 model...", nl=False)
        if self.llm_agent.check_model_available():
            click.echo(f" {Fore.GREEN}✓{Style.RESET_ALL}")
        else:
            click.echo(f" {Fore.RED}✗{Style.RESET_ALL}")
            click.echo(f"    {Fore.RED}Error: Llama3 not installed. Install with: ollama pull llama3{Style.RESET_ALL}")
            all_ok = False
        
        # Check spec files - try multiple paths
        spec_checks = {
            "Constitution": [
                Path(".specify/memory/constitution.md"),
                Path("../.specify/memory/constitution.md"),
                Path("../../.specify/memory/constitution.md"),
            ],
            "Specification": [
                Path("specs/001-acms-core-apis/spec.md"),
                Path("../specs/001-acms-core-apis/spec.md"),
                Path("../../specs/001-acms-core-apis/spec.md"),
                Path("../../../specs/001-acms-core-apis/spec.md"),
            ],
            "Plan": [
                Path("specs/001-acms-core-apis/plan.md"),
                Path("../specs/001-acms-core-apis/plan.md"),
                Path("../../specs/001-acms-core-apis/plan.md"),
                Path("../../../specs/001-acms-core-apis/plan.md"),
            ],
        }
        
        for name, paths in spec_checks.items():
            click.echo(f"  Checking {name} file...", nl=False)
            found = False
            for path in paths:
                if path.exists():
                    click.echo(f" {Fore.GREEN}✓{Style.RESET_ALL}")
                    found = True
                    break
            if not found:
                click.echo(f" {Fore.YELLOW}⚠{Style.RESET_ALL}")
                logger.warning(f"{name} file not found")
        
        # Check pytest
        click.echo("  Checking pytest...", nl=False)
        import subprocess
        try:
            subprocess.run(["pytest", "--version"], capture_output=True, check=True, timeout=5)
            click.echo(f" {Fore.GREEN}✓{Style.RESET_ALL}")
        except (subprocess.CalledProcessError, FileNotFoundError):
            click.echo(f" {Fore.YELLOW}⚠{Style.RESET_ALL}")
            logger.warning("pytest not found. Install with: pip install pytest")
        
        return all_ok
    
    def generate_test_cases(self, resource: str = "agents") -> str:
        """
        Generate test cases using Llama3.
        
        Args:
            resource: API resource to test
            
        Returns:
            Generated test code
        """
        click.echo(f"\n{Fore.CYAN}Generating test cases for '{resource}'...{Style.RESET_ALL}")
        
        # Find specification files
        spec_path = None
        constitution_path = None
        
        # Try multiple locations
        spec_locations = [
            Path("specs/001-acms-core-apis/spec.md"),
            Path("../specs/001-acms-core-apis/spec.md"),
            Path("../../specs/001-acms-core-apis/spec.md"),
            Path("../../../specs/001-acms-core-apis/spec.md"),
        ]
        
        constitution_locations = [
            Path(".specify/memory/constitution.md"),
            Path("../.specify/memory/constitution.md"),
            Path("../../.specify/memory/constitution.md"),
        ]
        
        for loc in spec_locations:
            if loc.exists():
                spec_path = loc
                break
        
        for loc in constitution_locations:
            if loc.exists():
                constitution_path = loc
                break
        
        if not spec_path:
            click.echo(f"  {Fore.RED}✗ Spec file not found{Style.RESET_ALL}")
            return ""
        
        if not constitution_path:
            click.echo(f"  {Fore.YELLOW}⚠ Constitution file not found (optional){Style.RESET_ALL}")
        
        click.echo(f"  Using spec: {spec_path}")
        if constitution_path:
            click.echo(f"  Using constitution: {constitution_path}")
        
        # Generate tests
        try:
            generator = TestCaseGenerator(self.llm_agent)
            
            if constitution_path:
                test_code = generator.generate_test_cases_chat(
                    str(constitution_path),
                    str(spec_path),
                    resource=resource
                )
            else:
                test_code = generator.generate_test_cases_chat(
                    str(spec_path),
                    str(spec_path),
                    resource=resource
                )
            
            if test_code:
                click.echo(f"  {Fore.GREEN}✓ Generated {len(test_code.splitlines())} lines of test code{Style.RESET_ALL}")
                return test_code
            else:
                click.echo(f"  {Fore.RED}✗ Failed to generate test code{Style.RESET_ALL}")
                return ""
        
        except Exception as e:
            click.echo(f"  {Fore.RED}✗ Error: {e}{Style.RESET_ALL}")
            logger.error(f"Test generation error: {e}")
            return ""
    
    def save_test_code(self, test_code: str, resource: str) -> Optional[Path]:
        """
        Save generated test code to file.
        
        Args:
            test_code: Generated test code
            resource: API resource name
            
        Returns:
            Path to saved file or None
        """
        output_dir = self.config.output_dir / "tests" / "python"
        output_dir.mkdir(parents=True, exist_ok=True)
        
        output_file = output_dir / f"test_{resource}_generated.py"
        
        try:
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(test_code)
            
            click.echo(f"  {Fore.GREEN}✓ Saved to {output_file}{Style.RESET_ALL}")
            return output_file
        
        except Exception as e:
            click.echo(f"  {Fore.RED}✗ Error saving file: {e}{Style.RESET_ALL}")
            logger.error(f"Error saving test file: {e}")
            return None
    
    def generate_mock_data(self, num_agents: int = 5) -> Dict[str, Any]:
        """
        Generate mock data.
        
        Args:
            num_agents: Number of agents to generate
            
        Returns:
            Generated mock data
        """
        click.echo(f"\n{Fore.CYAN}Generating mock data...{Style.RESET_ALL}")
        
        try:
            scenario = self.mock_generator.generate_full_scenario(num_agents=num_agents)
            
            click.echo(f"  {Fore.GREEN}✓ Generated {len(scenario['agents'])} agents{Style.RESET_ALL}")
            click.echo(f"  {Fore.GREEN}✓ Generated {len(scenario['policies'])} policies{Style.RESET_ALL}")
            click.echo(f"  {Fore.GREEN}✓ Generated {len(scenario['commissions'])} commissions{Style.RESET_ALL}")
            click.echo(f"  {Fore.GREEN}✓ Generated {len(scenario['payments'])} payments{Style.RESET_ALL}")
            
            # Save mock data
            mock_file = self.config.output_dir / "mock_data.json"
            self.mock_generator.save_to_file(scenario, str(mock_file))
            
            return scenario
        
        except Exception as e:
            click.echo(f"  {Fore.RED}✗ Error: {e}{Style.RESET_ALL}")
            logger.error(f"Mock data generation error: {e}")
            return {}
    
    def run_tests(self, test_pattern: Optional[str] = None) -> Dict[str, Any]:
        """
        Run generated tests.
        
        Args:
            test_pattern: Optional pattern to filter tests
            
        Returns:
            Execution summary as dictionary
        """
        click.echo(f"\n{Fore.CYAN}Running tests...{Style.RESET_ALL}")
        
        try:
            if test_pattern:
                summary = self.test_runner.run_test_by_pattern(test_pattern)
            else:
                summary = self.test_runner.run_all_tests()
            
            click.echo(f"  {Fore.GREEN}✓ Tests completed{Style.RESET_ALL}")
            click.echo(f"    Passed: {summary.passed_tests}/{summary.total_tests}")
            click.echo(f"    Failed: {summary.failed_tests}/{summary.total_tests}")
            click.echo(f"    Success Rate: {summary.success_rate:.1f}%")
            
            return summary.to_dict()
        
        except Exception as e:
            click.echo(f"  {Fore.RED}✗ Error: {e}{Style.RESET_ALL}")
            logger.error(f"Test execution error: {e}")
            return {}
    
    def generate_reports(self, summary: Dict[str, Any]) -> None:
        """
        Generate execution reports.
        
        Args:
            summary: Execution summary
        """
        click.echo(f"\n{Fore.CYAN}Generating reports...{Style.RESET_ALL}")
        
        try:
            # For now, just save summary as JSON
            from src.test_runner import ExecutionSummary, TestResult, TestStatus
            
            # Convert summary dict back to object - exclude calculated fields
            summary_data = {k: v for k, v in summary.items() if k not in ['success_rate', 'execution_time_seconds']}
            
            # Convert test results back to TestResult objects
            test_results = []
            if 'test_results' in summary_data:
                for result in summary_data['test_results']:
                    if isinstance(result, dict):
                        # Convert dict back to TestResult
                        test_results.append(TestResult(
                            test_name=result.get('test_name', ''),
                            test_file=result.get('test_file', ''),
                            status=TestStatus(result.get('status', 'ERROR')),
                            duration_ms=result.get('duration_ms', 0),
                            error_message=result.get('error_message'),
                            assertions=result.get('assertions', []),
                            tags=result.get('tags', [])
                        ))
                    else:
                        test_results.append(result)
                summary_data['test_results'] = test_results
            
            summary_obj = ExecutionSummary(**summary_data)
            
            # Generate HTML report
            generator = ReportGenerator(summary_obj)
            html_file = self.config.output_dir / "test_report.html"
            generator.generate_html_report(html_file)
            click.echo(f"  {Fore.GREEN}✓ HTML report: {html_file}{Style.RESET_ALL}")
            
            # Generate Markdown report
            md_file = self.config.output_dir / "test_report.md"
            generator.generate_markdown_report(md_file)
            click.echo(f"  {Fore.GREEN}✓ Markdown report: {md_file}{Style.RESET_ALL}")
            
            # Generate JSON report
            json_file = self.config.output_dir / "test_report.json"
            generator.generate_json_report(json_file)
            click.echo(f"  {Fore.GREEN}✓ JSON report: {json_file}{Style.RESET_ALL}")
        
        except Exception as e:
            click.echo(f"  {Fore.YELLOW}⚠ Error generating reports: {e}{Style.RESET_ALL}")
            logger.warning(f"Report generation error: {e}")


@click.group()
@click.version_option(version="1.0.0", prog_name="ACMS Testing Agent")
def cli():
    """
    ACMS API Testing Agent - Automated API Testing with Llama3
    
    Complete workflow for generating and executing API tests.
    """
    pass


@cli.command()
def check():
    """Check prerequisites and configuration."""
    agent = ACMSTestingAgent()
    
    click.echo(f"\n{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}ACMS API Testing Agent - Prerequisites Check{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    
    all_ok = agent.check_prerequisites()
    
    click.echo(f"\n{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    if all_ok:
        click.echo(f"{Fore.GREEN}✓ All prerequisites met!{Style.RESET_ALL}")
    else:
        click.echo(f"{Fore.YELLOW}⚠ Some prerequisites missing. Please resolve above issues.{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}{'='*60}{Style.RESET_ALL}\n")


@cli.command()
@click.option('--resource', '-r', default='agents', help='API resource to test (e.g., agents, policies)')
@click.option('--mock-agents', '-m', default=5, help='Number of mock agents to generate')
@click.option('--output', '-o', type=click.Path(), help='Output directory')
def generate(resource: str, mock_agents: int, output: Optional[str]):
    """Generate test cases and mock data."""
    agent = ACMSTestingAgent()
    
    if output:
        agent.config.output_dir = Path(output)
    
    click.echo(f"\n{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}ACMS API Testing Agent - Test Generation{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    
    # Check prerequisites
    if not agent.check_prerequisites():
        click.echo(f"\n{Fore.RED}Prerequisites not met. Exiting.{Style.RESET_ALL}")
        sys.exit(1)
    
    # Generate mock data
    mock_data = agent.generate_mock_data(num_agents=mock_agents)
    
    # Generate test code
    test_code = agent.generate_test_cases(resource=resource)
    
    if test_code:
        agent.save_test_code(test_code, resource)
    
    click.echo(f"\n{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    click.echo(f"{Fore.GREEN}✓ Generation complete!{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}{'='*60}{Style.RESET_ALL}\n")


@cli.command()
@click.option('--pattern', '-p', help='Test pattern to run')
@click.option('--output', '-o', type=click.Path(), help='Output directory')
def run(pattern: Optional[str], output: Optional[str]):
    """Run generated tests."""
    agent = ACMSTestingAgent()
    
    if output:
        agent.config.output_dir = Path(output)
    
    click.echo(f"\n{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}ACMS API Testing Agent - Test Execution{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    
    # Run tests
    summary = agent.run_tests(test_pattern=pattern)
    
    # Generate reports
    if summary:
        agent.generate_reports(summary)
    
    click.echo(f"\n{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    click.echo(f"{Fore.GREEN}✓ Execution complete!{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}{'='*60}{Style.RESET_ALL}\n")


@cli.command()
@click.option('--resource', '-r', default='agents', help='API resource to test')
@click.option('--mock-agents', '-m', default=5, help='Number of mock agents')
@click.option('--output', '-o', type=click.Path(), help='Output directory')
def full(resource: str, mock_agents: int, output: Optional[str]):
    """Run complete workflow: generate + run + report."""
    agent = ACMSTestingAgent()
    
    if output:
        agent.config.output_dir = Path(output)
    
    click.echo(f"\n{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}ACMS API Testing Agent - Complete Workflow{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    
    # Check prerequisites
    if not agent.check_prerequisites():
        click.echo(f"\n{Fore.RED}Prerequisites not met. Exiting.{Style.RESET_ALL}")
        sys.exit(1)
    
    # Generate mock data
    mock_data = agent.generate_mock_data(num_agents=mock_agents)
    
    # Generate test code
    test_code = agent.generate_test_cases(resource=resource)
    
    if test_code:
        agent.save_test_code(test_code, resource)
    
    # Run tests
    summary = agent.run_tests()
    
    # Generate reports
    if summary:
        agent.generate_reports(summary)
    
    click.echo(f"\n{Fore.CYAN}{'='*60}{Style.RESET_ALL}")
    click.echo(f"{Fore.GREEN}✓ Complete workflow finished!{Style.RESET_ALL}")
    click.echo(f"{Fore.CYAN}{'='*60}{Style.RESET_ALL}\n")


if __name__ == "__main__":
    cli()
