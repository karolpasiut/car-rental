package com.carrental.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverlapConstraintInstallerTest {

    @Test
    void splitsFunctionBodyWithoutBreakingOnInternalSemicolons() {
        String script = """
                -- OVERLAP GUARD
                CREATE OR REPLACE FUNCTION reject_overlapping_active_assignment()
                RETURNS trigger
                LANGUAGE plpgsql
                AS $$
                BEGIN
                    IF NOT FOUND THEN
                        RAISE EXCEPTION 'missing';
                    END IF;
                    RETURN NEW;
                END;
                $$;

                DROP TRIGGER IF EXISTS reservation_cars_no_overlap ON reservation_cars;

                CREATE TRIGGER reservation_cars_no_overlap
                    BEFORE INSERT ON reservation_cars
                    FOR EACH ROW
                    EXECUTE FUNCTION reject_overlapping_active_assignment();
                """;

        List<String> statements = OverlapConstraintInstaller.statements(script);

        assertEquals(3, statements.size());
        assertTrue(statements.get(0).contains("RAISE EXCEPTION 'missing';"));
        assertTrue(statements.get(0).trim().endsWith("$$"));
        assertTrue(statements.get(1).startsWith("DROP TRIGGER"));
        assertTrue(statements.get(2).startsWith("CREATE TRIGGER"));
    }

    @Test
    void schemaGuardIsFourStatements() throws Exception {
        String schema = Files.readString(Path.of("src/main/resources/schema.sql"));
        int marker = schema.indexOf("-- OVERLAP GUARD");
        assertTrue(marker >= 0);

        List<String> statements = OverlapConstraintInstaller.statements(schema.substring(marker));

        assertEquals(4, statements.size());
        assertTrue(statements.get(0).contains("FOR UPDATE"));
        assertTrue(statements.get(0).contains("already booked"));
        assertTrue(statements.get(1).startsWith("DROP TRIGGER"));
        assertTrue(statements.get(2).startsWith("CREATE TRIGGER"));
        assertTrue(statements.get(3).startsWith("CREATE INDEX"));
    }
}
