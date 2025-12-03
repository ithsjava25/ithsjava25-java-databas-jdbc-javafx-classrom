package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MoonMissionRepositoryImpl implements MoonMissionRepository {
    Connection connection;
    PreparedStatement listMoonMissionsStmt,
            getMoonMissionByIdStmt,
            missionCountByYearStmt;

    public MoonMissionRepositoryImpl(String jdbc, String username, String password) {
        try {
            connection = DriverManager.getConnection(jdbc, username, password);
            listMoonMissionsStmt = connection.prepareStatement("select * from moon_mission");
            getMoonMissionByIdStmt = connection.prepareStatement("select * from moon_mission where mission_id = ?");
            missionCountByYearStmt = connection.prepareStatement("select count(*) as mission_count from moon_mission where year(launch_date) = ?");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<String> listMoonMissions() {
        List<String> moonMissions = new ArrayList<>();
        try {
            ResultSet rs = listMoonMissionsStmt.executeQuery();
            while (rs.next()) {
                moonMissions.add(rs.getString("spacecraft"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return moonMissions;
    }
    @Override
    public List<MoonMission> getMoonMissionById(String id) {
        List<MoonMission> moonMissions = new ArrayList<>();
        try {
            getMoonMissionByIdStmt.setString(1,id);
            ResultSet rs = getMoonMissionByIdStmt.executeQuery();
            while (rs.next()) {
               MoonMission m = new MoonMission(
                       rs.getInt("mission_id"),
                       rs.getString("spacecraft"),
                       rs.getDate("launch_date"),
                       rs.getString("carrier_rocket"),
                       rs.getString("operator"),
                       rs.getString("mission_type"),
                       rs.getString("outcome"));
               moonMissions.add(m);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return moonMissions;

    }

    @Override
    public int missionsCountByYear(int year) {
        int count= 0;
        try {
            missionCountByYearStmt.setInt(1, year);
            ResultSet rs = missionCountByYearStmt.executeQuery();
            while (rs.next()) {
                count = rs.getInt("mission_count");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }
}
