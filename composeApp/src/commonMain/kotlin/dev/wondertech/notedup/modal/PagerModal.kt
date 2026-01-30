package dev.wondertech.notedup.modal

import org.jetbrains.compose.resources.DrawableResource

/**
 * Represents a single page in the onboarding pager.
 * Contains an image, motivational quote, and author attribution.
 *
 * @property image The drawable resource for the page's image
 * @property quote The motivational or informational quote to display
 * @property author The author of the quote
 */
data class PagerModel(val image: DrawableResource, val quote: String, val author: String)