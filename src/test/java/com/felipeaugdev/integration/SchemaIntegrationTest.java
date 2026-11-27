package com.felipeaugdev.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class SchemaIntegrationTest extends AbstractIntegrationTest implements SchemaConstraintTests {

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public JdbcTemplate jdbc() {
        return jdbc;
    }
}
