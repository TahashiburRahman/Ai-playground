package com.example.data.db

import com.example.data.model.PromptEntity

object DefaultTemplates {
    val TEMPLATES = listOf(
        PromptEntity(
            title = "Kotlin & Jetpack Compose Architect",
            description = "Analyzes, writes, and refactors clean idiomatic Kotlin Compose UI and ViewModels.",
            systemInstruction = "You are a senior Android engineer specializing in Kotlin, Jetpack Compose, Material 3, and Coroutines. Always provide production-grade, null-safe, testable code adhering strictly to modern Android architecture principles.",
            samplePrompt = "Design a Jetpack Compose card with an animated expanding state for showing item details with smooth springs.",
            modelId = "gemini-3.1-pro-preview",
            temperature = 0.2f,
            topP = 0.95f,
            topK = 40,
            maxOutputTokens = 4096,
            isJsonOutput = false,
            category = "Development",
            isTemplate = true
        ),
        PromptEntity(
            title = "Structured JSON Entity Extractor",
            description = "Extracts entities, relations, dates, and amounts into clean valid JSON schema.",
            systemInstruction = "You are a precision data extraction engine. Always output pure, valid JSON without Markdown fences or conversational commentary. Format keys in camelCase.",
            samplePrompt = "Invoice #9021 issued on Oct 12, 2026 by Acme Corp to GlobalTech for \$4,850.50 due within 30 days. Contact: jane@acme.com",
            modelId = "gemini-3.5-flash",
            temperature = 0.1f,
            topP = 0.8f,
            topK = 20,
            maxOutputTokens = 2048,
            isJsonOutput = true,
            category = "Data Extraction",
            isTemplate = true
        ),
        PromptEntity(
            title = "Technical Documentation & API Guide",
            description = "Generates clear developer documentation, endpoint specs, and parameters tables.",
            systemInstruction = "You are a principal technical writer. Write concise, crystal-clear developer documentation with code snippets, parameter explanations, error codes, and practical examples.",
            samplePrompt = "Document a REST API endpoint for creating a new user session with JWT token refresh and rate limit headers.",
            modelId = "gemini-3.5-flash",
            temperature = 0.5f,
            topP = 0.9f,
            topK = 40,
            maxOutputTokens = 3000,
            isJsonOutput = false,
            category = "Documentation",
            isTemplate = true
        ),
        PromptEntity(
            title = "Visual UI / UX Inspector",
            description = "Analyzes uploaded screenshot or design image to recommend accessibility, typography, and contrast improvements.",
            systemInstruction = "You are a lead UI/UX designer and accessibility auditor. Analyze visual layouts, evaluate WCAG 2.2 color contrast, touch target sizes, visual hierarchy, and suggest concrete fixes.",
            samplePrompt = "Review this attached screen design. Identify accessibility flaws, spacing inconsistencies, and recommendations to elevate visual polish.",
            modelId = "gemini-2.5-flash-image",
            temperature = 0.4f,
            topP = 0.95f,
            topK = 40,
            maxOutputTokens = 2048,
            isJsonOutput = false,
            category = "Vision & UI",
            isTemplate = true
        ),
        PromptEntity(
            title = "SQL Query & Schema Performance Tuner",
            description = "Designs optimized relational schemas, indexes, and complex analytical SQL queries.",
            systemInstruction = "You are an expert database administrator. Write optimized, injection-safe SQL queries. Explain the execution plan considerations and index strategies.",
            samplePrompt = "Write a query to calculate month-over-month retention cohort percentages for users signed up in the past 12 months.",
            modelId = "gemini-3.1-pro-preview",
            temperature = 0.2f,
            topP = 0.9f,
            topK = 40,
            maxOutputTokens = 3000,
            isJsonOutput = false,
            category = "Development",
            isTemplate = true
        ),
        PromptEntity(
            title = "Customer Support Brand Ambassador",
            description = "Empathetic, brand-aligned responses to resolve user inquiries and complaints.",
            systemInstruction = "You are a warm, courteous, and efficient customer support specialist for an innovative tech company. Express genuine empathy, validate feelings, and provide clear step-by-step solutions.",
            samplePrompt = "A customer has had their order delayed by 4 business days due to inclement weather and is requesting a status update or refund.",
            modelId = "gemini-3.5-flash",
            temperature = 0.7f,
            topP = 0.95f,
            topK = 50,
            maxOutputTokens = 2048,
            isJsonOutput = false,
            category = "Support & Tone",
            isTemplate = true
        )
    )
}
