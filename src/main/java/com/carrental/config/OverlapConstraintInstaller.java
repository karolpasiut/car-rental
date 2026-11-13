package com.carrental.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies the overlap trigger from schema.sql on PostgreSQL. Docker init runs
 * that file only for a new data volume; Hibernate does not create triggers.
 */
@Component
public class OverlapConstraintInstaller implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OverlapConstraintInstaller.class);
    private static final String GUARD_MARKER = "-- OVERLAP GUARD";

    private final DataSource dataSource;

    public OverlapConstraintInstaller(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            if (product == null || !product.toLowerCase().contains("postgres")) {
                return;
            }
            String schema = new ClassPathResource("schema.sql")
                    .getContentAsString(StandardCharsets.UTF_8);
            int marker = schema.indexOf(GUARD_MARKER);
            if (marker < 0) {
                throw new IllegalStateException("schema.sql is missing the overlap guard");
            }
            List<String> statements = statements(schema.substring(marker));
            try (Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    statement.execute(sql);
                }
            }
            log.info("Ensured reservation overlap guard is installed");
        }
    }

    static List<String> statements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inDollarQuote = false;
        int index = 0;
        while (index < script.length()) {
            if (script.startsWith("$$", index)) {
                inDollarQuote = !inDollarQuote;
                current.append("$$");
                index += 2;
                continue;
            }
            char character = script.charAt(index);
            if (character == ';' && !inDollarQuote) {
                addStatement(statements, current);
                current.setLength(0);
            } else {
                current.append(character);
            }
            index++;
        }
        addStatement(statements, current);
        return statements;
    }

    private static void addStatement(List<String> statements, StringBuilder current) {
        String sql = current.toString().trim();
        String withoutComments = sql.replaceAll("(?m)^\\s*--.*(?:\\R|$)", "").trim();
        if (!withoutComments.isEmpty()) {
            statements.add(sql);
        }
    }
}
