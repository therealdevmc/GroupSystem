package de.therealdev.groupsystem.database.mysql;

import lombok.SneakyThrows;

import java.sql.*;

public class MySQL {

        private String host, user, password, database;
        private int port;

        private Connection conn;

        public MySQL(String host, int port, String user, String password, String database) throws Exception {
            this.host = host;
            this.port = port;
            this.user = user;
            this.password = password;
            this.database = database;

            this.openConnection();
        }

        public MySQL() {
            this.openH2Connection();
        }



        public void queryUpdate(PreparedStatement statement) {
            try {
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ResultSet query(PreparedStatement statement) {

            try {
                return statement.executeQuery();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public Connection getConnection() {
            return this.conn;
        }

        public void checkConnection() {
            try {
                if (this.conn == null || !this.conn.isValid(10) || this.conn.isClosed()) openConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    public void closeStatement(PreparedStatement statement) {
        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


        public Connection openConnection() throws Exception {
            Class.forName("com.mysql.jdbc.Driver");
            return this.conn = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database+"?autoReconnect=true", this.user, this.password);
        }

        @SneakyThrows
        public Connection openH2Connection() {
            return this.conn = DriverManager.getConnection("jdbc:h2:mem:default");
        }

        public void closeConnection() {
            try {
                this.conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.conn = null;
            }
        }

}
