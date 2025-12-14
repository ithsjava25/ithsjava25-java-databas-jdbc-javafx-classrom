package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MoonMissionRepositoryJdbc implements MoonMissionRepository {

    private final Connection connection;

    public MoonMissionRepositoryJdbc(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<MoonMission> listAllMissions() {
        List<MoonMission> missions = new ArrayList<>();
        String sql = "SELECT * FROM moon_mission";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                missions.add(new MoonMission(
                        rs.getInt("mission_id"),
                        rs.getString("spacecraft"),
                        rs.getDate("launch_date"),
                        rs.getString("carrier_rocket"),
                        rs.getString("operator"),
                        rs.getString("mission_type"),
                        rs.getString("outcome")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return missions;
    }

    @Override
    public MoonMission findMoonMissionById(int id) {
        String sql = "SELECT * FROM moon_mission WHERE mission_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MoonMission(
                            rs.getInt("mission_id"),
                            rs.getString("spacecraft"),
                            rs.getDate("launch_date"),
                            rs.getString("carrier_rocket"),
                            rs.getString("operator"),
                            rs.getString("mission_type"),
                            rs.getString("outcome")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int countMissionsByYear(int year) {
        String sql = "SELECT COUNT(*) FROM moon_mission WHERE YEAR(launch_date) = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}