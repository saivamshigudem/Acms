"""
API Testing Agent - Generic Specification-Driven Testing Framework

Setup configuration for package installation and distribution.
"""

from setuptools import setup, find_packages
from pathlib import Path

# Read README for long description
readme_file = Path(__file__).parent / "README.md"
long_description = readme_file.read_text(encoding="utf-8") if readme_file.exists() else ""

# Read requirements
requirements_file = Path(__file__).parent / "requirements.txt"
requirements = []
if requirements_file.exists():
    with open(requirements_file, "r", encoding="utf-8") as f:
        requirements = [
            line.strip() 
            for line in f 
            if line.strip() and not line.startswith("#")
        ]

setup(
    name="api-testing-agent",
    version="1.0.0",
    author="API Testing Agent Team",
    author_email="team@apitesting.com",
    description="Generic specification-driven functional testing framework for APIs",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/your-org/api-testing-agent",
    project_urls={
        "Bug Tracker": "https://github.com/your-org/api-testing-agent/issues",
        "Documentation": "https://github.com/your-org/api-testing-agent/wiki",
        "Source Code": "https://github.com/your-org/api-testing-agent",
    },
    packages=find_packages(include=["src", "src.*"]),
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Developers",
        "Intended Audience :: QA",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.11",
        "Programming Language :: Python :: 3.12",
        "Topic :: Software Development :: Testing",
        "Topic :: Software Development :: Code Generators",
    ],
    python_requires=">=3.11",
    install_requires=requirements,
    entry_points={
        "console_scripts": [
            "api-testing-agent=src.main:cli",
        ],
    },
    include_package_data=True,
    package_data={
        "src": [
            "templates/*.jinja2",
            "examples/*",
        ],
    },
    zip_safe=False,
)
