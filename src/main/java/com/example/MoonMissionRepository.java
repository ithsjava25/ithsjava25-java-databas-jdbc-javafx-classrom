package com.example;

import javax.sql.DataSource;
import java.sql.*;

public class MoonMissionRepository {
    private final DataSource ds;

    public MoonMissionRepository(DataSource ds) {
        this.ds = ds;
    }

    public void listMissions() throws SQLException {
        String sql = "SELECT spacecraft FROM moon_mission ORDER BY mission_id";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.println(rs.getString("spacecraft"));
            }
        }
    }

    public void getMissionById(long id) throws SQLException {
        String sql = "SELECT mission_id, spacecraft, launch_date FROM moon_mission WHERE mission_id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Mission ID: " + rs.getLong("mission_id"));
                    System.out.println("Spacecraft: " + rs.getString("spacecraft"));
                    System.out.println("Launch Date: " + rs.getDate("launch_date"));
                }
            }
        }
    }

    public void countByYear(int year) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM moon_mission WHERE YEAR(launch_date)=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Number of missions in " + year + ": " + rs.getInt("count"));
                }
            }
        }
    }
}