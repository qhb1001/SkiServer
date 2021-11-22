package consumer.resortmicroservice.dao;

import consumer.db.DBCPDataSource;
import consumer.resortmicroservice.model.LiftRide;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
        String insertQueryStatement = "INSERT INTO LiftRides (resortId, skierId, liftId, day, time, vertical) " +
                "VALUES (?,?,?,?,?,?)";
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setInt(1, newLiftRide.getResortId());
            preparedStatement.setInt(2, newLiftRide.getSkierId());
            preparedStatement.setInt(3, newLiftRide.getLiftId());
            preparedStatement.setString(4, newLiftRide.getDay());
            preparedStatement.setInt(5, newLiftRide.getTime());
            preparedStatement.setInt(6, newLiftRide.getVertical());

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