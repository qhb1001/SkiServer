package consumer.skiermicroservice.dao;

import java.sql.*;

import consumer.db.DBCPDataSource;
import consumer.skiermicroservice.model.LiftRide;
import org.apache.commons.dbcp2.*;

public class LiftRideDao {
    private static BasicDataSource dataSource = null;

    public LiftRideDao(
            String HOST_NAME,
            String PORT,
            String DATABASE,
            String USERNAME,
            String PASSWORD
    ) {
        if (dataSource == null) {
            dataSource = new DBCPDataSource().getDataSource(HOST_NAME, PORT, DATABASE, USERNAME, PASSWORD);
        }
    }

    public void createLiftRide(LiftRide newLiftRide) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "INSERT INTO LiftRides (skierId, liftId, seasonId, day, vertical) " +
                "VALUES (?,?,?,?,?)";
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setInt(1, newLiftRide.getSkierId());
            preparedStatement.setInt(2, newLiftRide.getLiftId());
            preparedStatement.setInt(3, newLiftRide.getSeasonId());
            preparedStatement.setInt(4, newLiftRide.getDayId());
            preparedStatement.setInt(5, newLiftRide.getVertical());

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
}