package consumer.resortmicroservice.dao;

import consumer.db.DBCPDataSource;
import consumer.resortmicroservice.model.LiftRide;
import consumer.resortmicroservice.model.SeasonalVertical;
import consumer.resortmicroservice.model.SeasonalVerticals;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LiftRideDao {
    private static BasicDataSource dataSource;

    public LiftRideDao(
            String HOST_NAME,
            String PORT,
            String DATABASE,
            String USERNAME,
            String PASSWORD
    ) {
        dataSource = new DBCPDataSource().getDataSource(HOST_NAME, PORT, DATABASE, USERNAME, PASSWORD);
    }

    public void createLiftRide(LiftRide newLiftRide) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "INSERT INTO LiftRides (resortId, skierId, liftId, seasonId, dayId, time, vertical) " +
                "VALUES (?,?,?,?,?,?,?)";
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setInt(1, newLiftRide.getResortId());
            preparedStatement.setInt(2, newLiftRide.getSkierId());
            preparedStatement.setInt(3, newLiftRide.getLiftId());
            preparedStatement.setInt(4, newLiftRide.getSeasonId());
            preparedStatement.setInt(5, newLiftRide.getDayId());
            preparedStatement.setInt(6, newLiftRide.getTime());
            preparedStatement.setInt(7, newLiftRide.getVertical());

            // execute insert SQL statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public Integer getUniqueSkiersCount(Integer resortId, Integer seasonId, Integer dayId) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String queryStatement = "SELECT COUNT(skierId) AS numSkiers FROM (SELECT skierId FROM resortmicroservice.LiftRides WHERE resortId = ? AND seasonId = ? AND dayId = ? GROUP BY skierId) AS unique_skiers";

        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(queryStatement);
            preparedStatement.setInt(1, resortId);
            preparedStatement.setInt(2, seasonId);
            preparedStatement.setInt(3, dayId);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                return rs.getInt("numSkiers");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();

            }
        }
        return -1;
    }

    public Integer getSkierDayVerticalForSkier(Integer resortId, Integer seasonId, Integer dayId, Integer skierId) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "SELECT SUM(vertical) AS verticals FROM LiftRides WHERE resortId = ? AND seasonId = ? AND dayId = ? AND skierId = ?";

        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setInt(1, resortId);
            preparedStatement.setInt(2, seasonId);
            preparedStatement.setInt(3, dayId);
            preparedStatement.setInt(4, skierId);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                return rs.getInt("verticals");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();

            }
        }
        return -1;
    }

    public SeasonalVerticals getTotalVerticalForResort(Integer skierId, Integer resortId, Integer seasonId) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;


        try {
            conn = dataSource.getConnection();
            String queryStatement = "";
            if (seasonId == -1) {
                queryStatement = "SELECT seasonId, SUM(vertical) AS totalVertical FROM LiftRides WHERE skierId = ? AND resortId = ? GROUP BY seasonId";
                preparedStatement = conn.prepareStatement(queryStatement);
                preparedStatement.setInt(1, skierId);
                preparedStatement.setInt(2, resortId);
            } else {
                queryStatement = "SELECT ? AS seasonId, SUM(vertical) AS totalVertical FROM LiftRides WHERE skierId = ? AND resortId = ? AND seasonId = ?";
                preparedStatement = conn.prepareStatement(queryStatement);
                preparedStatement.setInt(1, seasonId);
                preparedStatement.setInt(2, skierId);
                preparedStatement.setInt(3, resortId);
                preparedStatement.setInt(4, seasonId);
            }


            ResultSet rs = preparedStatement.executeQuery();
            ArrayList<SeasonalVertical> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new SeasonalVertical(rs.getInt("seasonId"), rs.getInt("totalVertical")));
            }
            return new SeasonalVerticals(results);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();

            }
        }
        return new SeasonalVerticals(new ArrayList<>());
    }
}