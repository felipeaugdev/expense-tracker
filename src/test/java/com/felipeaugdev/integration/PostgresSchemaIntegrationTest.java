package com.felipeaugdev.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.datasource.driver-class-name=org.postgresql.Driver"
})
@Import(PostgresTestcontainersConfig.class)
class PostgresSchemaIntegrationTest implements SchemaConstraintTests {

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public JdbcTemplate jdbc() {
        return jdbc;
    }
}
