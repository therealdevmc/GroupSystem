package de.therealdev.groupsystem.database.mysql;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class AsyncMySQL {

    private MySQL sql;

    /**
     * Creates a new AsyncMySQL instance
     * @param host
     * @param port
     * @param user
     * @param password
     * @param database
     */
    public AsyncMySQL(String host, int port, String user, String password, String database) {
        try {
            sql = new MySQL(host, port, user, password, database);
            Logger.getLogger("").info("MySQL > Connected.");
        } catch (Exception e) {
            Logger.getLogger("").info("Error. Couldnt connect to your MySQL-DB.");
            Bukkit.shutdown();
        }
    }

    public AsyncMySQL() {
        sql = new MySQL();
    }


    /**
     * Updates the database asynchronously
     * @param statement
     */
    public void update(PreparedStatement statement) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            sql.queryUpdate(statement);
        });

        future.exceptionally(ex -> {
                    handleException(ex);
                    return null;
                }).thenRun(() -> sql.closeStatement(statement))
                .join();
    }

    /**
     * Queries the database asynchronously
     * @param statement
     * @return
     */
    public ResultSet query(PreparedStatement statement) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ResultSet resultSet = statement.executeQuery();
                return resultSet;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }).exceptionally(ex -> {
            handleException(ex);
            return null;
        }).join();
    }

    /**
     * Prepares a statement asynchronously
     * @param query
     * @return
     */

    public PreparedStatement prepare(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sql.getConnection().prepareStatement(query);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(ex -> {
            handleException(ex);
            return null;
        }).join();
    }

    /**
     * Gets the MySQL instance
     * @return
     */
    public MySQL getMySQL() {
        return sql;
    }

    /**
     * Handles an exception
     */
    private void handleException(Throwable ex) {
        ex.printStackTrace();
    }

}

