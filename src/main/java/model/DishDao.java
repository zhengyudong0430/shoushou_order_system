package model;

import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DishDao {
    public void add(Dish dish) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "insert into dishes values(null, ?, ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, dish.getName());
            statement.setInt(2, dish.getPrice());
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("插入菜品失败");
            }
            System.out.println("插入菜品成功!");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入菜品失败");
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }

    public void delete(int dishId) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "delete from dishes where dishId = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, dishId);
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("删除菜品失败");
            }
            System.out.println("删除菜品成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("删除菜品失败");
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }

    public List<Dish> selectAll() throws OrderSystemException {
        List<Dish> dishes = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        String sql = "select * from dishes";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Dish dish = new Dish();
                dish.setDishId(resultSet.getInt("dishId"));
                dish.setName(resultSet.getString("name"));
                dish.setPrice(resultSet.getInt("price"));
                dishes.add(dish);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("查找所有菜品出错");
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return dishes;
    }

    public Dish selectById(int dishId) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "select * from dishes where dishId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, dishId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setDishId(resultSet.getInt("dishId"));
                dish.setName(resultSet.getString("name"));
                dish.setPrice(resultSet.getInt("price"));
                return dish;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("按照 id 查找菜品出错");
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }

    public static void main(String[] args) throws OrderSystemException {
        DishDao dishDao = new DishDao();
        dishDao.delete(2);
    }
}