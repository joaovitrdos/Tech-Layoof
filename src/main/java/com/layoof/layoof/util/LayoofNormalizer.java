package com.layoof.layoof.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class LayoofNormalizer {

    private static final int FINGERPRINT_TOKENS = 8;

    private static final Pattern SPACES = Pattern.compile("\\s+");

    private static final DateTimeFormatter PUBLISHED_AT = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .optionalStart()
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalEnd()
            .optionalStart()
            .appendOffsetId()
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    private static final Pattern LEGAL_SUFFIX = Pattern.compile(
            "(?i)[,\\s]+(inc|inc\\.|llc|l\\.l\\.c\\.|ltd|ltd\\.|ltda|ltda\\.|s\\.a\\.|sa|corp|corp\\.|"
                    + "corporation|co\\.|gmbh|plc|b\\.v\\.|nv|ag|pte|pty|group|holdings?)\\.?$");

    private static final Map<String, String> COUNTRIES = Map.ofEntries(
            Map.entry("usa", "Estados Unidos"),
            Map.entry("us", "Estados Unidos"),
            Map.entry("u.s.", "Estados Unidos"),
            Map.entry("u.s.a.", "Estados Unidos"),
            Map.entry("united states", "Estados Unidos"),
            Map.entry("united states of america", "Estados Unidos"),
            Map.entry("eua", "Estados Unidos"),
            Map.entry("brazil", "Brasil"),
            Map.entry("br", "Brasil"),
            Map.entry("india", "India"),
            Map.entry("uk", "Reino Unido"),
            Map.entry("united kingdom", "Reino Unido"),
            Map.entry("england", "Reino Unido"),
            Map.entry("germany", "Alemanha"),
            Map.entry("france", "Franca"),
            Map.entry("spain", "Espanha"),
            Map.entry("canada", "Canada"),
            Map.entry("mexico", "Mexico"),
            Map.entry("china", "China"),
            Map.entry("japan", "Japao"),
            Map.entry("israel", "Israel"),
            Map.entry("netherlands", "Holanda"),
            Map.entry("ireland", "Irlanda"),
            Map.entry("australia", "Australia"),
            Map.entry("singapore", "Singapura"),
            Map.entry("global", "Global"),
            Map.entry("worldwide", "Global"));

    private static final Set<String> TRACKING_PARAMETERS = Set.of(
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content", "utm_id",
            "fbclid", "gclid", "igshid", "mc_cid", "mc_eid", "ref", "ref_src", "amp", "cmpid");

    private static final Set<String> STOP_WORDS = Set.of(
            "que", "com", "por", "para", "nos", "nas", "dos", "das", "sua", "seu", "mais", "apos",
            "the", "for", "and", "its", "are", "will", "after", "amid", "with", "from", "says");

    private LayoofNormalizer() {
    }

    public static String company(String value) {
        String clean = text(value);
        if (clean == null) {
            return null;
        }
        String withoutSuffix = LEGAL_SUFFIX.matcher(clean).replaceAll("").strip();
        return withoutSuffix.isBlank() ? clean : withoutSuffix;
    }

    public static String country(String value) {
        String clean = text(value);
        if (clean == null) {
            return null;
        }
        return COUNTRIES.getOrDefault(clean.toLowerCase(Locale.ROOT), clean);
    }

    public static Integer cuts(Integer value) {
        if (value == null || value <= 0) {
            return null;
        }
        return value;
    }

    public static String text(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return SPACES.matcher(value.strip()).replaceAll(" ");
    }

    public static LocalDateTime publishedAt(String value) {
        String clean = text(value);
        if (clean == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(clean, PUBLISHED_AT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static String canonicalUrl(String value) {
        String clean = text(value);
        if (clean == null) {
            return null;
        }
        try {
            URI uri = URI.create(clean);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!"http".equals(scheme) && !"https".equals(scheme) || uri.getHost() == null) {
                return null;
            }
            String host = uri.getHost().toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
            String path = uri.getPath() == null ? "" : uri.getPath();
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            String query = queryWithoutTracking(uri.getQuery());
            return scheme + "://" + host + path + (query.isEmpty() ? "" : "?" + query);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static String fingerprint(String company, String title) {
        String titleSignature = Arrays.stream(withoutAccents(title).split("[^a-z0-9]+"))
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .distinct()
                .sorted()
                .limit(FINGERPRINT_TOKENS)
                .collect(Collectors.joining("-"));
        return sha256(withoutAccents(company(company)) + "|" + titleSignature);
    }

    private static String queryWithoutTracking(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        return Arrays.stream(query.split("&"))
                .filter(pair -> !pair.isBlank())
                .filter(pair -> !TRACKING_PARAMETERS.contains(pair.split("=", 2)[0].toLowerCase(Locale.ROOT)))
                .sorted()
                .collect(Collectors.joining("&"));
    }

    private static String withoutAccents(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .strip();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponivel na JVM", ex);
        }
    }
}
