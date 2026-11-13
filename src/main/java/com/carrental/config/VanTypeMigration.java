package com.carrental.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Existing databases were created with a HATCHBACK check. Rename those rows to
 * VAN before the application reads them.
 */
@Component
@Order(0)
public class VanTypeMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VanTypeMigration.class);

    private final DataSource dataSource;

    public VanTypeMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            if (product == null || !product.toLowerCase().contains("postgres")) {
                return;
            }
            try (Statement statement = connection.createStatement()) {
                List<String> hatchbackChecks = new ArrayList<>();
                try (ResultSet constraints = statement.executeQuery("""
                        SELECT conname
                        FROM pg_constraint
                        WHERE conrelid = 'cars'::regclass
                          AND contype = 'c'
                          AND pg_get_constraintdef(oid) ILIKE '%HATCHBACK%'
                        """)) {
                    while (constraints.next()) {
                        hatchbackChecks.add(constraints.getString(1));
                    }
                }
                for (String name : hatchbackChecks) {
                    statement.execute("ALTER TABLE cars DROP CONSTRAINT " + quoteIdentifier(name));
                }
                int updated = statement.executeUpdate("UPDATE cars SET type = 'VAN' WHERE type = 'HATCHBACK'");
                statement.execute("ALTER TABLE cars DROP CONSTRAINT IF EXISTS cars_type_check");
                statement.execute("""
                        ALTER TABLE cars
                        ADD CONSTRAINT cars_type_check
                        CHECK (type IN ('SUV', 'VAN', 'SEDAN'))
                        """);
                if (updated > 0) {
                    log.info("Renamed {} cars from HATCHBACK to VAN", updated);
                }
            }
        }
    }

    private static String quoteIdentifier(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }
}
