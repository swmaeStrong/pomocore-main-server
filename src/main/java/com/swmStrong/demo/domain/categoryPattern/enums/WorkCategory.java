package com.swmStrong.demo.domain.categoryPattern.enums;

import java.util.Set;

public interface WorkCategory {
    Set<String> categories = Set.of(
            "Development",
            "LLM",
            "Documentation",
            "Design",
            "Video Editing",
            "Education",
            "Productivity",
            "Uncategorized",
            "Finance",
            "File Management",
            "Browsing",
            "Marketing",
            "System & Utilities"
    );
}
