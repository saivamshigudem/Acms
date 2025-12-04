"""
API Testing Agent - Generic Specification-Driven Testing Framework

A generic testing framework that accepts OpenAPI 3.0 specifications and user stories
as input and generates comprehensive functional test specifications and executable
Python Pytest code.

Version: 1.0.0
Author: API Testing Agent Team
License: MIT
"""

__version__ = "1.0.0"
__author__ = "API Testing Agent Team"
__email__ = "team@apitesting.com"
__license__ = "MIT"

# Core modules
from .config import Config
from .spec_parser import SpecParser
from .story_parser import StoryParser
from .test_generator import TestGenerator
from .code_generator import CodeGenerator

__all__ = [
    "Config",
    "SpecParser",
    "StoryParser",
    "TestGenerator",
    "CodeGenerator",
]
