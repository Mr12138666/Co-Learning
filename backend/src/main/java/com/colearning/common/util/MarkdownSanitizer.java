package com.colearning.common.util;

import jakarta.annotation.PostConstruct;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

/**
 * Renders Markdown to sanitized HTML.
 * Uses commonmark-java for parsing and OWASP HTML Sanitizer for XSS prevention.
 *
 * <p>Allowed elements: headings, bold, italic, code, links, lists, blockquotes,
 * images, tables, horizontal rules, line breaks. All dangerous elements (script,
 * iframe, style, event handlers) are stripped.
 */
@Component
public class MarkdownSanitizer {

    private Parser markdownParser;
    private HtmlRenderer htmlRenderer;
    private PolicyFactory sanitizerPolicy;

    @PostConstruct
    public void init() {
        this.markdownParser = Parser.builder().build();
        this.htmlRenderer = HtmlRenderer.builder().build();

        this.sanitizerPolicy = new HtmlPolicyBuilder()
                .allowElements(
                        // Text formatting
                        "p", "br", "hr", "strong", "b", "em", "i", "del", "s", "mark", "small", "sub", "sup",
                        // Headings
                        "h1", "h2", "h3", "h4", "h5", "h6",
                        // Lists
                        "ul", "ol", "li", "dl", "dt", "dd",
                        // Code
                        "code", "pre", "kbd", "samp",
                        // Blockquote
                        "blockquote",
                        // Links and images
                        "a", "img",
                        // Tables
                        "table", "thead", "tbody", "tr", "th", "td",
                        // Div and span (for commonmark extensions)
                        "div", "span"
                )
                .allowAttributes("href").onElements("a")
                .allowAttributes("src", "alt", "title").onElements("img")
                .allowAttributes("title").onElements("a")
                .allowAttributes("class").onElements("code", "pre", "div", "span")
                .allowAttributes("colspan", "rowspan").onElements("th", "td")
                .allowStandardUrlProtocols()
                .requireRelNofollowOnLinks()
                .requireRelsOnLinks("noopener", "noreferrer")
                .toFactory();
    }

    /**
     * Render Markdown source to sanitized HTML.
     *
     * @param markdown the Markdown source text
     * @return sanitized HTML string, or empty string if input is null/blank
     */
    public String renderToHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Node document = markdownParser.parse(markdown);
        String rawHtml = htmlRenderer.render(document);
        return sanitize(rawHtml);
    }

    /**
     * Sanitize an existing HTML string using the same policy.
     *
     * @param html the raw HTML to sanitize
     * @return sanitized HTML string
     */
    public String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return sanitizerPolicy.sanitize(html);
    }
}
