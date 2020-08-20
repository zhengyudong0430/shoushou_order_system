package model;


import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    public void add(Order order) throws OrderSystemException {

        addOrderUser(order);

        addOrderDish(order);
    }

    private void addOrderUser(Order order) throws OrderSystemException {

        Connection connection = DBUtil.getConnection();

        String sql = "insert into order_user values(null, ?, now(), 0)";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {

            statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1, order.getUserId());

            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("插入订单失败");
            }

            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {

                order.setOrderId(resultSet.getInt(1));
            }
            System.out.println("插入订单第一步成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入订单失败");
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
    }

    private void addOrderDish(Order order) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "insert into order_dish values(?, ?)";
        PreparedStatement statement = null;
        try {
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            List<Dish> dishes = order.getDishes();
            for (Dish dish : dishes)  {
                statement.setInt(1, order.getOrderId());
                statement.setInt(2, dish.getDishId());
                statement.addBatch(); // 给 sql 新增一个片段.
            }
            statement.executeBatch(); // 把刚才的 sql 进行执行.
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            deleteOrderUser(order.getOrderId());
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }

    private void deleteOrderUser(int orderId) throws OrderSystemException {

        Connection connection = DBUtil.getConnection();

        String sql = "delete from order_user where orderId = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, orderId);

            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("回滚失败");
            }
            System.out.println("回滚成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("回滚失败");
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }
    public List<Order> selectAll() {
        List<Order> orders = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_user";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            DBUtil.close(connection, statement, resultSet);
        }
        return orders;
    }

    public List<Order> selectByUserId(int userId) {
        List<Order> orders = new ArrayList<>();

        Connection connection = DBUtil.getConnection();

        String sql = "select * from order_user where userId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            DBUtil.close(connection, statement, resultSet);
        }
        return orders;
    }


    public Order selectById(int orderId) throws OrderSystemException {
        Order order = buildOrder(orderId);
        List<Integer> dishIds = selectDishIds(orderId);
        order = getDishDetail(order, dishIds);
        return order;
    }

    private Order buildOrder(int orderId) {

        Connection connection = DBUtil.getConnection();

        String sql = "select * from order_user where orderId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, orderId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }


    private List<Integer> selectDishIds(int orderId) {
        List<Integer> dishIds = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_dish where orderId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, orderId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                dishIds.add(resultSet.getInt("dishId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return dishIds;
    }

    private Order getDishDetail(Order order, List<Integer> dishIds) throws OrderSystemException {
        List<Dish> dishes = new ArrayList<>();
        DishDao dishDao = new DishDao();
        for (Integer dishId : dishIds) {
            Dish dish = dishDao.selectById(dishId);
            dishes.add(dish);
        }
        order.setDishes(dishes);
        return order;
    }

    public void changeState(int orderId, int isDone) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "update order_user set isDone = ? where orderId = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, isDone);
            statement.setInt(2, orderId);
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("修改订单状态失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("修改订单状态失败");
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }
}