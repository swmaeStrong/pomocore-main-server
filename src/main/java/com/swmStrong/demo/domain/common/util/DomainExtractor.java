package com.swmStrong.demo.domain.common.util;

import com.google.common.net.InternetDomainName;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DomainExtractor {
    
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
        "^(?:https?://)?(?:www\\.)?([^:/\\s]+)(?:[:/]|$)"
    );
    
    public static String extractDomain(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        String trimmedUrl = url.trim();
        
        try {
            // Add protocol if missing
            if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
                trimmedUrl = "https://" + trimmedUrl;
            }
            
            URI uri = new URI(trimmedUrl);
            String host = uri.getHost();
            
            if (host == null) {
                return extractDomainFallback(url);
            }
            
            // Remove www. prefix if present
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            
            return host.toLowerCase();
            
        } catch (URISyntaxException e) {
            log.debug("Failed to parse URL with URI: {}, falling back to regex", url);
            return extractDomainFallback(url);
        }
    }
    
    private static String extractDomainFallback(String url) {
        Matcher matcher = DOMAIN_PATTERN.matcher(url);
        if (matcher.find()) {
            String domain = matcher.group(1);
            // Remove www. if present
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }
            return domain.toLowerCase();
        }
        return null;
    }
    
    /**
     * Extract domain without subdomains (e.g., mail.google.com -> google.com)
     * Uses Guava's InternetDomainName for accurate TLD handling.
     */
    public static String extractBaseDomain(String url) {
        String fullDomain = extractDomain(url);
        if (fullDomain == null) {
            return null;
        }
        
        try {
            InternetDomainName internetDomain = InternetDomainName.from(fullDomain);
            if (internetDomain.hasPublicSuffix()) {
                return internetDomain.topPrivateDomain().toString();
            }
            return fullDomain;
        } catch (IllegalArgumentException e) {
            log.debug("Failed to parse domain with InternetDomainName: {}", fullDomain);
            return fullDomain;
        }
    }
    
    /**
     * Extract domain with first path segment (e.g., google.com/search?q=test -> google.com/search)
     * Returns domain + first path segment, removing query parameters and fragments
     */
    public static String extractDomainWithPath(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        String trimmedUrl = url.trim();
        
        try {
            // Add protocol if missing
            if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
                trimmedUrl = "https://" + trimmedUrl;
            }
            
            URI uri = new URI(trimmedUrl);
            String host = uri.getHost();
            
            if (host == null) {
                return extractDomainWithPathFallback(url);
            }
            
            // Remove www. prefix if present
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            
            String path = uri.getPath();
            if (path == null || path.isEmpty() || path.equals("/")) {
                return host.toLowerCase();
            }
            
            // Extract first path segment
            String[] pathSegments = path.split("/");
            if (pathSegments.length > 1 && !pathSegments[1].isEmpty()) {
                return (host + "/" + pathSegments[1]).toLowerCase();
            }
            
            return host.toLowerCase();
            
        } catch (URISyntaxException e) {
            log.debug("Failed to parse URL with URI: {}, falling back to regex", url);
            return extractDomainWithPathFallback(url);
        }
    }
    
    private static String extractDomainWithPathFallback(String url) {
        Pattern domainWithPathPattern = Pattern.compile(
            "^(?:https?://)?(?:www\\.)?([^:/\\s?#]+)(?:/([^/?#\\s]*))?(?:[?#].*)?$"
        );
        
        Matcher matcher = domainWithPathPattern.matcher(url);
        if (matcher.find()) {
            String domain = matcher.group(1);
            String firstPath = matcher.group(2);

            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }
            
            if (firstPath != null && !firstPath.isEmpty()) {
                return (domain + "/" + firstPath).toLowerCase();
            }
            
            return domain.toLowerCase();
        }
        return null;
    }
}