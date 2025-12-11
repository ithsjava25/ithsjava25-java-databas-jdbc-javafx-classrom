package com.example.repository;

import com.example.model.MoonMission;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of MoonMissionRepository using a DataSource.
 * Handles mapping of ResultSet to MoonMission objects and executing SQL queries.
 */
public class MoonMissionRepositoryJdbc extends BaseRepository<MoonMission> implements MoonMissionRepository {

    /**
     * Creates a new repository with the given DataSource and devMode flag.
     *
     * @param dataSource the DataSource to use for database connections
     * @param devMode if true, prints debug information
     */
    public MoonMissionRepositoryJdbc(DataSource dataSource, boolean devMode) {
        super(dataSource, devMode);
    }

    /**
     * Maps a ResultSet row to a MoonMission object.
     *
     * @param rs the ResultSet to map
     * @return a MoonMission object
     */
    @Override
    protected MoonMission map(java.sql.ResultSet rs) throws java.sql.SQLException {
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

    @Override
    public List<MoonMission> listMissions() {
        return queryList("SELECT * FROM moon_mission");
    }

    @Override
    public Optional<MoonMission> getMissionById(int missionId) {
        return querySingle("SELECT * FROM moon_mission WHERE mission_id=?", missionId);
    }

    @Override
    public int countMissionsByYear(int year) {
        return executeQuery(
                "SELECT COUNT(*) FROM moon_mission WHERE YEAR(launch_date)=?",
                rs -> { rs.next(); return rs.getInt(1); },
                year
        );
    }
}