package me.kubbidev.moonrise.common.dependencies;

import com.google.common.collect.ImmutableList;
import me.kubbidev.moonrise.common.dependencies.relocation.Relocation;
import me.kubbidev.moonrise.common.dependencies.relocation.RelocationHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * The dependencies used by MoonRise.
 */
public enum Dependency {

    ASM(
            "org.ow2.asm",
            "asm",
            "9.5",
            "ti6EtZgHKXUbBFjFNM8TZvcnVCu40VhiEzVoKkYPA1M="
    ),
    ASM_COMMONS(
            "org.ow2.asm",
            "asm-commons",
            "9.5",
            "cu7p+6+53o2UY/IN1YSkjO635RUq1MmHv74X3UgRya4="
    ),
    JAR_RELOCATOR(
            "me.lucko",
            "jar-relocator",
            "1.7",
            "b30RhOF6kHiHl+O5suNLh/+eAr1iOFEFLXhwkHHDu4I="
    ),
    ADVENTURE(
            "net{}kyori",
            "adventure-api",
            "4.14.0",
            "HUzIW6vEmRdGk2L9tLzSjvvIHIblK/Rz0Wful8DsXHY=",
            Relocation.of("adventure", "net{}kyori{}adventure")
    ),
    EVENT(
            "net{}kyori",
            "event-api",
            "3.0.0",
            "yjvdTdAyktl3iFEQFLHC3qYwwt7/DbCd7Zc8Q4SlIag=",
            Relocation.of("eventbus", "net{}kyori{}event")
    ),
    CAFFEINE(
            "com{}github{}ben-manes{}caffeine",
            "caffeine",
            "2.9.0",
            "VFMotEO3XLbTHfRKfL3m36GlN72E/dzRFH9B5BJiX2o=",
            Relocation.of("caffeine", "com{}github{}benmanes{}caffeine")
    ),
    OKIO(
            "com{}squareup{}" + RelocationHelper.OKIO_STRING,
            RelocationHelper.OKIO_STRING,
            "1.17.5",
            "Gaf/SNhtPPRJf38lD78pX0MME6Uo3Vt7ID+CGAK4hq0=",
            Relocation.of(RelocationHelper.OKIO_STRING, RelocationHelper.OKIO_STRING)
    ),
    OKHTTP(
            "com{}squareup{}" + RelocationHelper.OKHTTP3_STRING,
            "okhttp",
            "3.14.9",
            "JXD6tVUVy/iB16TO70n8UVSQvAJwV+Zmd2ooMkZa7KA=",
            Relocation.of(RelocationHelper.OKHTTP3_STRING, RelocationHelper.OKHTTP3_STRING),
            Relocation.of(RelocationHelper.OKIO_STRING, RelocationHelper.OKIO_STRING)
    ),
    MARIADB_DRIVER(
            "org{}mariadb{}jdbc",
            "mariadb-java-client",
            "3.1.3",
            "ESl+5lYkJsScgTh8hgFTy8ExxMPQQkktT20tl6s6HKU=",
            Relocation.of("mariadb", "org{}mariadb{}jdbc")
    ),
    MYSQL_DRIVER(
            "mysql",
            "mysql-connector-java",
            "8.0.23",
            "/31bQCr9OcEnh0cVBaM6MEEDsjjsG3pE6JNtMynadTU=",
            Relocation.of("mysql", "com{}mysql")
    ),
    POSTGRESQL_DRIVER(
            "org{}postgresql",
            "postgresql",
            "42.6.0",
            "uBfGekDJQkn9WdTmhuMyftDT0/rkJrINoPHnVlLPxGE=",
            Relocation.of("postgresql", "org{}postgresql")
    ),
    H2_DRIVER_LEGACY(
            "com.h2database",
            "h2",
            // seems to be a compat bug in 1.4.200 with older dbs
            // see: https://github.com/h2database/h2database/issues/2078
            "1.4.199",
            "MSWhZ0O8a0z7thq7p4MgPx+2gjCqD9yXiY95b5ml1C4="
            // we don't apply relocations to h2 - it gets loaded via
            // an isolated classloader
    ),
    H2_DRIVER(
            "com.h2database",
            "h2",
            "2.1.214",
            "1iPNwPYdIYz1SajQnxw5H/kQlhFrIuJHVHX85PvnK9A="
            // we don't apply relocations to h2 - it gets loaded via
            // an isolated classloader
    ),
    SQLITE_DRIVER(
            "org.xerial",
            "sqlite-jdbc",
            "3.28.0",
            "k3hOVtv1RiXgbJks+D9w6cG93Vxq0dPwEwjIex2WG2A="
            // we don't apply relocations to sqlite - it gets loaded via
            // an isolated classloader
    ),
    HIKARI(
            "com{}zaxxer",
            "HikariCP",
            "4.0.3",
            "fAJK7/HBBjV210RTUT+d5kR9jmJNF/jifzCi6XaIxsk=",
            Relocation.of("hikari", "com{}zaxxer{}hikari")
    ),
    SLF4J_SIMPLE(
            "org.slf4j",
            "slf4j-simple",
            "1.7.30",
            "i5J5y/9rn4hZTvrjzwIDm2mVAw7sAj7UOSh0jEFnD+4="
    ),
    SLF4J_API(
            "org.slf4j",
            "slf4j-api",
            "1.7.30",
            "zboHlk0btAoHYUhcax6ML4/Z6x0ZxTkorA1/lRAQXFc="
    ),
    JACKSON_ANNOTATIONS(
            "com{}fasterxml{}jackson{}core",
            "jackson-annotations",
            "2.17.2",
            "hzpgbiNQeWn5u76pOdXhknSoh3XqWhabp+LXlapRVuE=",
            Relocation.of("jackson", "com{}fasterxml{}jackson")
    ),
    JACKSON_CORE(
            "com{}fasterxml{}jackson{}core",
            "jackson-core",
            "2.17.2",
            "choYkkHasFJdnoWOXLYE0+zA7eCB4t531vNPpXeaW0Y=",
            Relocation.of("jackson", "com{}fasterxml{}jackson")
    ),
    JACKSON_DATABIND(
            "com{}fasterxml{}jackson{}core",
            "jackson-databind",
            "2.17.2",
            "wEmT8zwPhFNCZTeE8U84Nz0AUoDmNZ21+AhwHPrnPAw=",
            Relocation.of("jackson", "com{}fasterxml{}jackson")
    ),
    CONFIGURATE_CORE(
            "org{}spongepowered",
            "configurate-core",
            "3.7.2",
            "XF2LzWLkSV0wyQRDt33I+gDlf3t2WzxH1h8JCZZgPp4=",
            Relocation.of("configurate", "ninja{}leaping{}configurate")
    ),
    CONFIGURATE_YAML(
            "org{}spongepowered",
            "configurate-yaml",
            "3.7.2",
            "OBfYn4nSMGZfVf2DoZhZq+G9TF1mODX/C5OOz/mkPmc=",
            Relocation.of("configurate", "ninja{}leaping{}configurate")
    ),
    SNAKEYAML(
            "org.yaml",
            "snakeyaml",
            "1.28",
            "NURqFCFDXUXkxqwN47U3hSfVzCRGwHGD4kRHcwzh//o=",
            Relocation.of("yaml", "org{}yaml{}snakeyaml")
    ),
    TROVE4J(
            "net.sf.trove4j",
            "core",
            "3.1.0",
            "4f7U1xiobSfF67QngQVQ+lwDJg41DkGoXvLC226sCFY=",
            Relocation.of("trove4j", "gnu{}trove")
    ),
    NEOVISIONARIES(
            "com.neovisionaries",
            "nv-websocket-client",
            "2.14",
            "7tD7b1712xfQhwOfHoKc/oJzY7KGMmUlipbw7TIzE7c=",
            Relocation.of("neovisionaries", "com{}neovisionaries{}ws{}client")
    ),
    COLLECTIONS4(
            "org.apache.commons",
            "commons-collections4",
            "4.4",
            "Hfi5QwtcjtFD14FeQD4z71NxskAKrb6b2giDdi4IRtE=",
            Relocation.of("collections4", "org{}apache{}commons{}collections4")
    ),
    JDA(
            "net.dv8tion",
            "JDA",
            "5.2.2",
            "wF2S3v4+5DQZmRO39pWL1OV1HuLWH2Fqj7uJhuW6hdA=",
            Relocation.of("jda", "net{}dv8tion{}jda"),
            Relocation.of(RelocationHelper.OKHTTP3_STRING, RelocationHelper.OKHTTP3_STRING),
            Relocation.of("jackson", "com{}fasterxml{}jackson"),
            Relocation.of("trove4j", "gnu{}trove"),
            Relocation.of("neovisionaries", "com{}neovisionaries{}ws{}client"),
            Relocation.of("collections4", "org{}apache{}commons{}collections4")
    );

    private final String mavenRepoPath;
    private final String version;
    private final byte[] checksum;
    private final List<Relocation> relocations;

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    Dependency(String groupId, String artifactId, String version, String checksum) {
        this(groupId, artifactId, version, checksum, new Relocation[0]);
    }

    Dependency(String groupId, String artifactId, String version, String checksum, Relocation... relocations) {
        this.mavenRepoPath = String.format(MAVEN_FORMAT,
                rewriteEscaping(groupId).replace(".", "/"),
                rewriteEscaping(artifactId),
                version,
                rewriteEscaping(artifactId),
                version
        );
        this.version = version;
        this.checksum = Base64.getDecoder().decode(checksum);
        this.relocations = ImmutableList.copyOf(relocations);
    }

    private static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    public String getFileName(String classifier) {
        String name = name().toLowerCase(Locale.ROOT).replace('_', '-');
        String extra = classifier == null || classifier.isEmpty()
                ? ""
                : "-" + classifier;

        return name + "-" + this.version + extra + ".jar";
    }

    String getMavenRepoPath() {
        return this.mavenRepoPath;
    }

    public byte[] getChecksum() {
        return this.checksum;
    }

    public boolean checksumMatches(byte[] hash) {
        return Arrays.equals(this.checksum, hash);
    }

    public List<Relocation> getRelocations() {
        return this.relocations;
    }

    /**
     * Creates a {@link MessageDigest} suitable for computing the checksums
     * of dependencies.
     *
     * @return the digest
     */
    public static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}