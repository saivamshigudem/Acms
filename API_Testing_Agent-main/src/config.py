"""
Configuration Management Module

Handles configuration loading, validation, and environment variable management
for the API Testing Agent.
"""

import os
import logging
from pathlib import Path
from typing import Dict, Any, Optional
from dataclasses import dataclass, field, asdict
import yaml
from dotenv import load_dotenv

logger = logging.getLogger(__name__)


@dataclass
class Config:
    """
    Configuration class for API Testing Agent.
    
    Manages all configuration parameters including API settings, authentication,
    test generation options, and file paths.
    """
    
    # API Configuration
    api_base_url: str = "http://localhost:8080"
    api_timeout: int = 30
    api_version: str = "1.0.0"
    
    # Authentication Configuration
    auth_token: Optional[str] = None
    auth_header: str = "Authorization"
    auth_scheme: str = "Bearer"
    
    # Test Configuration
    test_data_cleanup: bool = True
    verbose_output: bool = True
    generate_specs: bool = True
    generate_code: bool = True
    
    # Performance Configuration
    performance_sla_ms: int = 200
    concurrent_requests: int = 10
    max_retries: int = 3
    
    # File Configuration
    output_dir: Path = field(default_factory=lambda: Path("./generated_tests"))
    templates_dir: Path = field(default_factory=lambda: Path("./templates"))
    examples_dir: Path = field(default_factory=lambda: Path("./examples"))
    
    # Generator Configuration
    generate_happy_path: bool = True
    generate_edge_cases: bool = True
    generate_error_scenarios: bool = True
    generate_security_tests: bool = True
    generate_integration_tests: bool = True
    generate_performance_tests: bool = True
    
    # Code Generation Configuration
    code_style: str = "black"
    test_framework: str = "pytest"
    language: str = "python"
    
    # Logging Configuration
    log_level: str = "INFO"
    log_format: str = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    
    # Internal state
    _config_file: Optional[Path] = field(default=None, init=False, repr=False)
    _env_file: Optional[Path] = field(default=None, init=False, repr=False)
    
    def __post_init__(self) -> None:
        """Post-initialization setup."""
        # Ensure paths are Path objects
        if isinstance(self.output_dir, str):
            self.output_dir = Path(self.output_dir)
        if isinstance(self.templates_dir, str):
            self.templates_dir = Path(self.templates_dir)
        if isinstance(self.examples_dir, str):
            self.examples_dir = Path(self.examples_dir)
        
        # Load environment variables
        self._load_environment()
    
    def _load_environment(self) -> None:
        """Load configuration from environment variables."""
        # Load from .env file if it exists
        env_file = Path(".env")
        if env_file.exists():
            load_dotenv(env_file)
            self._env_file = env_file
            logger.debug(f"Loaded environment from {env_file}")
        
        # Override with environment variables
        env_mappings = {
            "API_BASE_URL": ("api_base_url", str),
            "API_TIMEOUT": ("api_timeout", int),
            "API_VERSION": ("api_version", str),
            "AUTH_TOKEN": ("auth_token", str),
            "AUTH_HEADER": ("auth_header", str),
            "AUTH_SCHEME": ("auth_scheme", str),
            "TEST_DATA_CLEANUP": ("test_data_cleanup", lambda x: x.lower() == "true"),
            "VERBOSE_OUTPUT": ("verbose_output", lambda x: x.lower() == "true"),
            "GENERATE_SPECS": ("generate_specs", lambda x: x.lower() == "true"),
            "GENERATE_CODE": ("generate_code", lambda x: x.lower() == "true"),
            "PERFORMANCE_SLA_MS": ("performance_sla_ms", int),
            "CONCURRENT_REQUESTS": ("concurrent_requests", int),
            "MAX_RETRIES": ("max_retries", int),
            "OUTPUT_DIR": ("output_dir", Path),
            "TEMPLATES_DIR": ("templates_dir", Path),
            "EXAMPLES_DIR": ("examples_dir", Path),
            "GENERATE_HAPPY_PATH": ("generate_happy_path", lambda x: x.lower() == "true"),
            "GENERATE_EDGE_CASES": ("generate_edge_cases", lambda x: x.lower() == "true"),
            "GENERATE_ERROR_SCENARIOS": ("generate_error_scenarios", lambda x: x.lower() == "true"),
            "GENERATE_SECURITY_TESTS": ("generate_security_tests", lambda x: x.lower() == "true"),
            "GENERATE_INTEGRATION_TESTS": ("generate_integration_tests", lambda x: x.lower() == "true"),
            "GENERATE_PERFORMANCE_TESTS": ("generate_performance_tests", lambda x: x.lower() == "true"),
            "CODE_STYLE": ("code_style", str),
            "TEST_FRAMEWORK": ("test_framework", str),
            "LANGUAGE": ("language", str),
            "LOG_LEVEL": ("log_level", str),
            "LOG_FORMAT": ("log_format", str),
        }
        
        for env_var, (config_attr, converter) in env_mappings.items():
            value = os.getenv(env_var)
            if value is not None:
                try:
                    setattr(self, config_attr, converter(value))
                    logger.debug(f"Set {config_attr} from environment variable {env_var}")
                except (ValueError, TypeError) as e:
                    logger.warning(f"Failed to convert {env_var}={value}: {e}")
    
    @classmethod
    def from_file(cls, config_file: Path) -> "Config":
        """
        Load configuration from YAML file.
        
        Args:
            config_file: Path to YAML configuration file
            
        Returns:
            Config instance with values from file
            
        Raises:
            FileNotFoundError: If configuration file doesn't exist
            yaml.YAMLError: If YAML parsing fails
        """
        if not config_file.exists():
            raise FileNotFoundError(f"Configuration file not found: {config_file}")
        
        logger.info(f"Loading configuration from {config_file}")
        
        with open(config_file, "r", encoding="utf-8") as f:
            config_data = yaml.safe_load(f) or {}
        
        config = cls(**config_data)
        config._config_file = config_file
        
        return config
    
    def to_dict(self) -> Dict[str, Any]:
        """
        Convert configuration to dictionary.
        
        Returns:
            Dictionary representation of configuration
        """
        data = asdict(self)
        # Remove internal fields
        data.pop("_config_file", None)
        data.pop("_env_file", None)
        # Convert Path objects to strings
        data["output_dir"] = str(self.output_dir)
        data["templates_dir"] = str(self.templates_dir)
        data["examples_dir"] = str(self.examples_dir)
        return data
    
    def save_to_file(self, config_file: Path) -> None:
        """
        Save configuration to YAML file.
        
        Args:
            config_file: Path to save configuration to
        """
        logger.info(f"Saving configuration to {config_file}")
        
        config_data = self.to_dict()
        
        config_file.parent.mkdir(parents=True, exist_ok=True)
        
        with open(config_file, "w", encoding="utf-8") as f:
            yaml.dump(config_data, f, default_flow_style=False, indent=2)
        
        self._config_file = config_file
    
    def validate(self) -> bool:
        """
        Validate configuration values.
        
        Returns:
            True if valid
            
        Raises:
            ValueError: If configuration is invalid
        """
        errors = []
        
        # Validate API configuration
        if not self.api_base_url:
            errors.append("API base URL is required")
        
        if self.api_timeout <= 0:
            errors.append("API timeout must be positive")
        
        # Validate performance configuration
        if self.performance_sla_ms <= 0:
            errors.append("Performance SLA must be positive")
        
        if self.concurrent_requests <= 0:
            errors.append("Concurrent requests must be positive")
        
        # Validate test framework
        valid_frameworks = ["pytest", "unittest"]
        if self.test_framework not in valid_frameworks:
            errors.append(f"Test framework must be one of: {valid_frameworks}")
        
        # Validate language
        valid_languages = ["python"]
        if self.language not in valid_languages:
            errors.append(f"Language must be one of: {valid_languages}")
        
        if errors:
            error_msg = f"Configuration validation failed: {'; '.join(errors)}"
            logger.error(error_msg)
            raise ValueError(error_msg)
        
        logger.info("Configuration validation passed")
        return True
    
    def ensure_directories(self) -> None:
        """Ensure required directories exist."""
        directories = [
            self.output_dir,
            self.templates_dir,
            self.examples_dir,
            self.output_dir / "tests" / "python",
        ]
        
        for directory in directories:
            directory.mkdir(parents=True, exist_ok=True)
            logger.debug(f"Ensured directory exists: {directory}")
    
    def get_auth_headers(self) -> Dict[str, str]:
        """
        Get authentication headers for API requests.
        
        Returns:
            Dictionary of authentication headers
        """
        headers = {}
        
        if self.auth_token:
            headers[self.auth_header] = f"{self.auth_scheme} {self.auth_token}"
        
        return headers
    
    def __str__(self) -> str:
        """String representation of configuration."""
        return f"Config(api_base_url={self.api_base_url}, output_dir={self.output_dir})"
    
    def __repr__(self) -> str:
        """Detailed string representation of configuration."""
        return f"Config({self.to_dict()})"
