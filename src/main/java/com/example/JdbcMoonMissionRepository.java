package com.example;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMoonMissionRepository implements MoonMissionRepository{


    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPass;


    public JdbcMoonMissionRepository(String jdbcUrl, String dbUser, String dbPass) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    @Override
    public List<String> findAllSpacecraftNames() {
        List<String> names = new ArrayList<>();

        String sql= "SELECT spacecraft FROM moon_mission ORDER BY launch_date";

        try( Connection connection= DriverManager.getConnection(jdbcUrl,dbUser,dbPass);
        Statement statement= connection.createStatement();
        ResultSet rs= statement.executeQuery(sql)) {

            while (rs.next()){
                names.add(rs.getString("spacecraft"));

            }
        } catch (SQLException e){
            System.err.println(" Failure to find names: " +e.getMessage());

        }

        return names;
    }

    @Override
    public Optional<MoonMission> findById(long missionId) {
        String sql = "SELECT mission_id, spacecraft, launch_date, carrier_rocket, operator, mission_type, outcome FROM moon_mission WHERE mission_id = ?";

        try( Connection connection= DriverManager.getConnection(jdbcUrl,dbUser,dbPass);
             PreparedStatement ps= connection.prepareStatement(sql)){

            ps.setLong(1, missionId);

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    //Map to MoonMission object:
                    MoonMission mission= new MoonMission(
                            rs.getLong("mission_id"),
                            rs.getString("spacecraft"),
                            rs.getObject("launch_date", LocalDate.class),
                            rs.getString("outcome"),
                            rs.getString("carrier_rocket"),
                            rs.getString("operator"),
                            rs.getString("mission_type")
                    );

                    return Optional.of(mission);
                }
            }
        }catch (SQLException e){
            System.err.println(" Failure in receiving mission per ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public int countByYear(int year) {
        String sql = "SELECT COUNT(*) FROM moon_mission WHERE YEAR(launch_date) = ?";

        try( Connection connection=DriverManager.getConnection(jdbcUrl,dbUser,dbPass);
        PreparedStatement ps=connection.prepareStatement(sql)){

            ps.setInt(1, year);

            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    //Result is COUNT(*) column 1:
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e ){
            System.err.println(" Failure in counting missions per year: " + e.getMessage());
        }
        //returnera 0 om inga resultat finns:
        return 0;
    }
}
