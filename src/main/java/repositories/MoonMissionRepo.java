package repositories;

import com.example.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MoonMissionRepo implements MoonMissionInter{
    @Override
    public void displayColumn(String columnName) {
        String query = "select * from moon_mission";

        try (Connection con = DataSource.getConnection();
             PreparedStatement pS = con.prepareStatement(query)) {

            ResultSet result = pS.executeQuery();
            while (result.next()) {
                String name = result.getString(columnName);
                System.out.println(name);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getMissionFromID(int missionId) {
        String query = "select * from moon_mission where mission_id = ?";

        try (Connection con = DataSource.getConnection();
             PreparedStatement pS = con.prepareStatement(query)) {

            pS.setInt(1, missionId);
            ResultSet result = pS.executeQuery();

            if (result.next()) {
                ResultSetMetaData meta = result.getMetaData();
                int columns = meta.getColumnCount();

                System.out.println("\n-------- Mission Details --------\n");
                for (int i = 1; i <= columns; i++) {
                    System.out.println(meta.getColumnName(i) + ": " + result.getString(i));
                }

            } else {
                System.out.println("Could not find mission with id: " + missionId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void allMissionsConductedInYear(String year) {
        String query = "select count(*) from moon_mission where launch_date like ?";

        try (Connection con = DataSource.getConnection();
             PreparedStatement pS = con.prepareStatement(query)) {

            int numMissions = 0;
            pS.setString(1, year.trim() + "%");
            ResultSet result = pS.executeQuery();
            if (result.next()){
                numMissions = result.getInt(1);
            }

            System.out.println("There were " + numMissions + " moon missions registered during " + year + ".\n");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


}
