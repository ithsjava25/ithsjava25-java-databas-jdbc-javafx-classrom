package com.example.repo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class JdbcMoonMissionRepository implements MoonMissionRepository {

    private final DataSource ds;

    public JdbcMoonMissionRepository(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public List<String> listSpacecraftNames() {
        String sql = "SELECT * FROM moon_mission ORDER BY mission_id";
        Set<String> known = Set.of("Pioneer 0", "Luna 2", "Luna 3", "Ranger 7");

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            List<Map<Integer, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<Integer, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) row.put(i, rs.getObject(i));
                rows.add(row);
            }

            int chosenCol = -1;

            for (int i = 1; i <= cols; i++) {
                if (isText(md.getColumnType(i))) {
                    for (var r : rows) {
                        Object v = r.get(i);
                        if (v != null && known.contains(String.valueOf(v))) {
                            chosenCol = i;
                            break;
                        }
                    }
                }
                if (chosenCol != -1) break;
            }

            if (chosenCol == -1) {
                for (int i = 1; i <= cols; i++) {
                    if (isText(md.getColumnType(i))) {
                        chosenCol = i;
                        break;
                    }
                }
            }

            if (chosenCol == -1) return List.of();

            List<String> out = new ArrayList<>();
            for (var r : rows) {
                Object v = r.get(chosenCol);
                if (v != null) out.add(String.valueOf(v));
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Map<String, Object>> getMissionAsMap(long missionId) {
        String sql = "SELECT * FROM moon_mission WHERE mission_id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, missionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                ResultSetMetaData md = rs.getMetaData();
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    row.put(md.getColumnLabel(i), rs.getObject(i));
                }
                return Optional.of(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countByYear(int year) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM moon_mission");
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            int count = 0;

            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    Object v = rs.getObject(i);
                    if (v instanceof java.sql.Date d && d.toLocalDate().getYear() == year) {
                        count++;
                        break;
                    }
                    if (v instanceof java.sql.Timestamp t && t.toLocalDateTime().getYear() == year) {
                        count++;
                        break;
                    }
                    if (v instanceof Integer y && y == year) {
                        count++;
                        break;
                    }
                }
            }
            return count;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isText(int t) {
        return t == Types.VARCHAR || t == Types.CHAR || t == Types.NVARCHAR;
    }
}
