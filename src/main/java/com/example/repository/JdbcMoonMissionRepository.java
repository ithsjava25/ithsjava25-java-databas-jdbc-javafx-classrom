package com.example.repository;

import com.example.model.MoonMission;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import javax.sql.DataSource;
import javax.swing.text.html.Option;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMoonMissionRepository implements MoonMissionRepository {

    private final DataSource dataSource;

    public JdbcMoonMissionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<String> findAllSpaceCraftNames() {
        List<String> spacecrafts = new ArrayList<>();
        String query = "SELECT spacecraft FROM moon_mission";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while(rs.next()) {
                spacecrafts.add(rs.getString("spacecraft"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing missions", e);
        }
        return spacecrafts;
    }

    @Override
    public Optional<MoonMission> findById(int missionId) {
        String query = "SELECT mission_id, spacecraft, launch_date, carrier_rocket, " +
                "operator, mission_type, outcome " +
                "FROM moon_mission WHERE mission_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, missionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MoonMission mission = new MoonMission(
                            rs.getInt("mission_id"),
                            rs.getString("spacecraft"),
                            rs.getDate("launch_date").toLocalDate(),
                            rs.getString("carrier_rocket"),
                            rs.getString("operator"),
                            rs.getString("mission_type"),
                            rs.getString("outcome")
                    );
                    return Optional.of(mission);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error getting mission", e);
        }

        return Optional.empty();
    }

    @Override
    public int countByYear(int year) {
        String query = "SELECT COUNT(*) as count FROM moon_mission WHERE YEAR(launch_date) = ?";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting missions", e);
        }
        return 0;
    }
}
