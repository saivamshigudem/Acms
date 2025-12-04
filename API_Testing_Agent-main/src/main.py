"""
Main Entry Point for API Testing Agent

CLI interface for the API Testing Agent framework.
"""

import logging
import sys
from pathlib import Path
from typing import Optional
import click
from colorama import Fore, Style, init

from .config import Config
from .spec_parser import SpecParser
from .story_parser import StoryParser
from .test_generator import TestGenerator
from .code_generator import CodeGenerator

# Initialize colorama for colored output
init(autoreset=True)

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


@click.group()
@click.version_option(version="1.0.0", prog_name="API Testing Agent")
def cli():
    """
    API Testing Agent - Generic Specification-Driven Testing Framework
    
    Generate comprehensive test specifications and executable test code
    from OpenAPI 3.0 specifications and user stories.
    """
    pass


@cli.command()
@click.option(
    '--openapi',
    '-o',
    type=click.Path(exists=True),
    required=True,
    help='Path to OpenAPI 3.0 specification file (YAML or JSON)'
)
@click.option(
    '--stories',
    '-s',
    type=click.Path(exists=True),
    required=False,
    help='Path to user stories file (Markdown)'
)
@click.option(
    '--output',
    '-d',
    type=click.Path(),
    default='./generated_tests',
    help='Output directory for generated files (default: ./generated_tests)'
)
@click.option(
    '--config',
    '-c',
    type=click.Path(exists=True),
    required=False,
    help='Path to configuration file (YAML)'
)
@click.option(
    '--spec-only',
    is_flag=True,
    help='Generate only test specification, not code'
)
@click.option(
    '--code-only',
    is_flag=True,
    help='Generate only test code, not specification'
)
@click.option(
    '--verbose',
    '-v',
    is_flag=True,
    help='Enable verbose output'
)
def generate(
    openapi: str,
    stories: Optional[str],
    output: str,
    config: Optional[str],
    spec_only: bool,
    code_only: bool,
    verbose: bool
):
    """
    Generate test specifications and code from OpenAPI specification.
    
    Example:
        api-testing-agent generate --openapi spec.yaml --stories stories.md --output ./tests
    """
    try:
        # Setup logging
        if verbose:
            logging.getLogger().setLevel(logging.DEBUG)
        
        click.echo(f"{Fore.CYAN}API Testing Agent - Test Generation{Style.RESET_ALL}")
        click.echo("=" * 60)
        
        # Load configuration
        if config:
            cfg = Config.from_file(Path(config))
        else:
            cfg = Config()
        
        cfg.output_dir = Path(output)
        cfg.validate()
        cfg.ensure_directories()
        
        click.echo(f"{Fore.GREEN}✓ Configuration loaded{Style.RESET_ALL}")
        
        # Parse OpenAPI specification
        click.echo(f"\n{Fore.YELLOW}Parsing OpenAPI specification...{Style.RESET_ALL}")
        spec_parser = SpecParser(Path(openapi))
        click.echo(f"{Fore.GREEN}✓ Loaded {len(spec_parser.get_endpoints())} endpoints{Style.RESET_ALL}")
        
        # Parse user stories if provided
        story_parser = None
        if stories:
            click.echo(f"\n{Fore.YELLOW}Parsing user stories...{Style.RESET_ALL}")
            story_parser = StoryParser(Path(stories))
            click.echo(f"{Fore.GREEN}✓ Loaded {len(story_parser.get_stories())} user stories{Style.RESET_ALL}")
        
        # Generate test specifications
        click.echo(f"\n{Fore.YELLOW}Generating test specifications...{Style.RESET_ALL}")
        test_generator = TestGenerator(spec_parser, story_parser)
        test_cases = test_generator.generate_all_tests()
        click.echo(f"{Fore.GREEN}✓ Generated {len(test_cases)} test cases{Style.RESET_ALL}")
        
        # Generate test code if not spec-only
        if not spec_only:
            click.echo(f"\n{Fore.YELLOW}Generating test code...{Style.RESET_ALL}")
            code_generator = CodeGenerator(test_generator, cfg.output_dir, cfg.templates_dir)
            generated_files = code_generator.generate_test_code()
            click.echo(f"{Fore.GREEN}✓ Generated {len(generated_files)} test modules{Style.RESET_ALL}")
            
            for resource, file_path in generated_files.items():
                click.echo(f"  - {file_path.name}")
        
        # Generate test specification document if not code-only
        if not code_only:
            click.echo(f"\n{Fore.YELLOW}Generating test specification document...{Style.RESET_ALL}")
            code_generator = CodeGenerator(test_generator, cfg.output_dir, cfg.templates_dir)
            spec_file = cfg.output_dir / "test_specification.md"
            code_generator.generate_test_specification(spec_file)
            click.echo(f"{Fore.GREEN}✓ Generated test specification: {spec_file}{Style.RESET_ALL}")
        
        # Summary
        click.echo(f"\n{'=' * 60}")
        click.echo(f"{Fore.GREEN}✓ Generation complete!{Style.RESET_ALL}")
        click.echo(f"Output directory: {cfg.output_dir}")
        click.echo(f"Total test cases: {len(test_cases)}")
        
        # Test case summary
        by_type = {}
        for tc in test_cases:
            by_type[tc.scenario_type] = by_type.get(tc.scenario_type, 0) + 1
        
        click.echo(f"\nTest case breakdown:")
        for test_type, count in sorted(by_type.items()):
            click.echo(f"  - {test_type}: {count}")
        
    except FileNotFoundError as e:
        click.echo(f"{Fore.RED}✗ Error: {e}{Style.RESET_ALL}", err=True)
        sys.exit(1)
    except ValueError as e:
        click.echo(f"{Fore.RED}✗ Validation error: {e}{Style.RESET_ALL}", err=True)
        sys.exit(1)
    except Exception as e:
        click.echo(f"{Fore.RED}✗ Unexpected error: {e}{Style.RESET_ALL}", err=True)
        if verbose:
            import traceback
            traceback.print_exc()
        sys.exit(1)


@cli.command()
@click.option(
    '--config',
    '-c',
    type=click.Path(exists=True),
    required=False,
    help='Path to configuration file to validate'
)
def validate(config: Optional[str]):
    """
    Validate configuration and environment setup.
    
    Example:
        api-testing-agent validate --config config.yaml
    """
    try:
        click.echo(f"{Fore.CYAN}API Testing Agent - Configuration Validation{Style.RESET_ALL}")
        click.echo("=" * 60)
        
        # Load and validate configuration
        if config:
            cfg = Config.from_file(Path(config))
        else:
            cfg = Config()
        
        cfg.validate()
        
        click.echo(f"{Fore.GREEN}✓ Configuration is valid{Style.RESET_ALL}")
        click.echo(f"\nConfiguration details:")
        click.echo(f"  API Base URL: {cfg.api_base_url}")
        click.echo(f"  API Timeout: {cfg.api_timeout}s")
        click.echo(f"  Output Directory: {cfg.output_dir}")
        click.echo(f"  Test Framework: {cfg.test_framework}")
        click.echo(f"  Generate Happy Path: {cfg.generate_happy_path}")
        click.echo(f"  Generate Edge Cases: {cfg.generate_edge_cases}")
        click.echo(f"  Generate Error Scenarios: {cfg.generate_error_scenarios}")
        click.echo(f"  Generate Security Tests: {cfg.generate_security_tests}")
        
    except ValueError as e:
        click.echo(f"{Fore.RED}✗ Validation failed: {e}{Style.RESET_ALL}", err=True)
        sys.exit(1)
    except Exception as e:
        click.echo(f"{Fore.RED}✗ Error: {e}{Style.RESET_ALL}", err=True)
        sys.exit(1)


@cli.command()
@click.option(
    '--output',
    '-o',
    type=click.Path(),
    default='.env',
    help='Output file for environment template (default: .env)'
)
def init_env(output: str):
    """
    Initialize environment configuration file.
    
    Example:
        api-testing-agent init-env --output .env
    """
    try:
        click.echo(f"{Fore.CYAN}API Testing Agent - Environment Initialization{Style.RESET_ALL}")
        click.echo("=" * 60)
        
        # Create default configuration
        cfg = Config()
        
        # Save to file
        output_path = Path(output)
        cfg.save_to_file(output_path)
        
        click.echo(f"{Fore.GREEN}✓ Environment file created: {output_path}{Style.RESET_ALL}")
        click.echo(f"\nNext steps:")
        click.echo(f"  1. Edit {output_path} with your API configuration")
        click.echo(f"  2. Run: api-testing-agent generate --openapi spec.yaml")
        
    except Exception as e:
        click.echo(f"{Fore.RED}✗ Error: {e}{Style.RESET_ALL}", err=True)
        sys.exit(1)


@cli.command()
def version():
    """Show version information."""
    click.echo("API Testing Agent v1.0.0")
    click.echo("Generic Specification-Driven Testing Framework")
    click.echo("\nFor more information, visit: https://github.com/your-org/api-testing-agent")


if __name__ == '__main__':
    cli()
