package com.example.jdbc;

import com.example.MoonMission;
import com.example.SimpleDriverManagerDataSource;
import com.example.repositorys.MoonMissionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMoonMissionRepository implements MoonMissionRepository {

    private final SimpleDriverManagerDataSource ds;

    public JdbcMoonMissionRepository(SimpleDriverManagerDataSource ds) {
        this.ds = ds;
    }

    @Override
    public List<String> listMissions() {
        String sql = "SELECT spacecraft FROM moon_mission";
        List<String> result = new ArrayList<>();

        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(rs.getString("spacecraft"));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Error listing missions", e);
        }
    }

    @Override
    public Optional<MoonMission> getMissionById(int missionId) {
        String sql = "SELECT * FROM moon_mission WHERE mission_id = ?";

        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, missionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MoonMission mission = new MoonMission();
                    mission.setMissionId(rs.getInt("mission_id"));
                    mission.setSpacecraft(rs.getString("spacecraft"));
                    java.sql.Date sqlDate = rs.getDate("launch_date");
                    mission.setLaunchDate(sqlDate != null ? sqlDate.toLocalDate() : null);
                    mission.setCarrierRocket(rs.getString("carrier_rocket"));
                    mission.setOutcome(rs.getString("outcome"));
                    mission.setMissionType(rs.getString("mission_type"));
                    mission.setOperator(rs.getString("operator"));
                    return Optional.of(mission);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching mission by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public int countMissionsByYear(int year) {
        String sql = "SELECT COUNT(*) AS mission_count FROM moon_mission WHERE YEAR(launch_date) = ?";

        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, year);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("mission_count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting missions by year", e);
        }
        return 0;
    }
}