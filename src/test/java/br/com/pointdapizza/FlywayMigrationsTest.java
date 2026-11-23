package br.com.pointdapizza;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class FlywayMigrationsTest {

    @Inject
    Flyway flyway;

    @Test
    void deveExecutarAsTresMigrationsComSucesso() {
        MigrationInfo[] aplicadas = flyway.info().applied();
        List<String> versoes = Arrays.stream(aplicadas)
            .filter(info -> info.getState() == MigrationState.SUCCESS)
            .map(info -> info.getVersion().getVersion())
            .toList();

        assertEquals(List.of("1", "2", "3"), versoes);
    }
}
