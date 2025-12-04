"""
User Story Parser Module

Parses user stories in Markdown format and extracts acceptance criteria.
"""

import logging
import re
from pathlib import Path
from typing import Dict, Any, List, Optional

from .utils.validators import validate_user_stories

logger = logging.getLogger(__name__)


class StoryParser:
    """
    Parser for user stories in Markdown format.
    
    Extracts user stories, acceptance criteria, and test scenarios.
    """
    
    def __init__(self, stories_file: Path):
        """
        Initialize the story parser.
        
        Args:
            stories_file: Path to Markdown file with user stories
        """
        self.stories_file = Path(stories_file)
        self.stories: List[Dict[str, Any]] = []
        
        self._load_stories()
    
    def _load_stories(self) -> None:
        """Load and parse user stories from Markdown file."""
        if not self.stories_file.exists():
            raise FileNotFoundError(f"Stories file not found: {self.stories_file}")
        
        logger.info(f"Loading user stories from {self.stories_file}")
        
        with open(self.stories_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Parse stories from markdown
        self._parse_markdown(content)
        
        # Validate stories
        if self.stories:
            validate_user_stories(self.stories)
        
        logger.info(f"Loaded {len(self.stories)} user stories")
    
    def _parse_markdown(self, content: str) -> None:
        """
        Parse user stories from Markdown content.
        
        Args:
            content: Markdown content
        """
        # Split by story headers (## User Story or ## Story)
        story_pattern = r'^##\s+(?:User\s+)?Story\s+(\d+):\s*(.+?)$'
        story_matches = list(re.finditer(story_pattern, content, re.MULTILINE))
        
        for i, match in enumerate(story_matches):
            story_num = match.group(1)
            story_title = match.group(2).strip()
            
            # Get content between this story and the next
            start = match.end()
            end = story_matches[i + 1].start() if i + 1 < len(story_matches) else len(content)
            story_content = content[start:end]
            
            # Extract acceptance criteria
            acceptance_criteria = self._extract_acceptance_criteria(story_content)
            
            # Extract description (content before first acceptance criterion)
            description = self._extract_description(story_content)
            
            story = {
                "id": f"US{story_num}",
                "title": story_title,
                "description": description,
                "acceptance_criteria": acceptance_criteria,
            }
            
            self.stories.append(story)
    
    def _extract_acceptance_criteria(self, content: str) -> List[Dict[str, str]]:
        """
        Extract acceptance criteria from story content.
        
        Args:
            content: Story content
            
        Returns:
            List of acceptance criteria
        """
        criteria_list = []
        
        # Look for numbered acceptance criteria with Given-When-Then format
        pattern = r'(\d+\.\s+\*\*Given\*\*\s+(.+?)\s*,\s*\*\*When\*\*\s+(.+?)\s*,\s*\*\*Then\*\*\s+(.+?))'
        
        matches = re.finditer(pattern, content, re.DOTALL | re.IGNORECASE)
        
        for i, match in enumerate(matches, 1):
            given = match.group(2).strip()
            when = match.group(3).strip()
            then = match.group(4).strip()
            
            # Clean up the text
            given = re.sub(r'\*\*|\n', ' ', given).strip()
            when = re.sub(r'\*\*|\n', ' ', when).strip()
            then = re.sub(r'\*\*|\n', ' ', then).strip()
            
            criterion = {
                "id": f"AC{i}",
                "given": given,
                "when": when,
                "then": then,
                "full_text": f"Given {given}, When {when}, Then {then}",
            }
            
            criteria_list.append(criterion)
        
        return criteria_list
    
    def _extract_description(self, content: str) -> str:
        """
        Extract story description (content before acceptance criteria).
        
        Args:
            content: Story content
            
        Returns:
            Description text
        """
        # Find the first acceptance criterion
        pattern = r'(?:Given|GIVEN)\s+'
        match = re.search(pattern, content, re.IGNORECASE)
        
        if match:
            description = content[:match.start()].strip()
        else:
            description = content.strip()
        
        # Remove markdown formatting
        description = re.sub(r'#+\s+', '', description)
        description = re.sub(r'\*\*(.+?)\*\*', r'\1', description)
        description = re.sub(r'__(.+?)__', r'\1', description)
        
        return description
    
    def get_stories(self) -> List[Dict[str, Any]]:
        """
        Get all extracted user stories.
        
        Returns:
            List of user stories
        """
        return self.stories
    
    def get_story_by_id(self, story_id: str) -> Optional[Dict[str, Any]]:
        """
        Get a specific story by ID.
        
        Args:
            story_id: Story ID (e.g., "US1")
            
        Returns:
            Story definition or None if not found
        """
        for story in self.stories:
            if story["id"] == story_id:
                return story
        return None
    
    def get_acceptance_criteria(self, story_id: str) -> List[Dict[str, str]]:
        """
        Get acceptance criteria for a story.
        
        Args:
            story_id: Story ID
            
        Returns:
            List of acceptance criteria
        """
        story = self.get_story_by_id(story_id)
        return story["acceptance_criteria"] if story else []
    
    def get_all_acceptance_criteria(self) -> List[Dict[str, Any]]:
        """
        Get all acceptance criteria from all stories.
        
        Returns:
            List of all acceptance criteria with story context
        """
        all_criteria = []
        
        for story in self.stories:
            for criterion in story["acceptance_criteria"]:
                criterion_with_story = {
                    **criterion,
                    "story_id": story["id"],
                    "story_title": story["title"],
                }
                all_criteria.append(criterion_with_story)
        
        return all_criteria
    
    def __repr__(self) -> str:
        """String representation of parser."""
        return f"StoryParser({self.stories_file}, stories={len(self.stories)})"
