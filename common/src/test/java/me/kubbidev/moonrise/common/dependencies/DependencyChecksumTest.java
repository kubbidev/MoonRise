package me.kubbidev.moonrise.common.dependencies;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DependencyChecksumTest {

    @ParameterizedTest
    @EnumSource
    public void checksumMatches(Dependency dependency) throws DependencyDownloadException {
        for (DependencyRepository repo : DependencyRepository.values()) {
            byte[] hash = Dependency.createDigest().digest(repo.downloadRaw(dependency));
            assertTrue(dependency.checksumMatches(hash), "Dependency " + dependency.name() + " has hash " +  Base64.getEncoder().encodeToString(hash));
        }
    }
}
