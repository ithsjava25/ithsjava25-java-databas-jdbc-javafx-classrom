package com.example;

import java.sql.Connection;
import java.sql.SQLException;

// define a new standard method for connection
public interface DataSource {

    Connection getConnection() throws SQLException;

}