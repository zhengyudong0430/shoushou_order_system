package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Dish;
import model.Order;
import model.OrderDao;
import model.User;
import util.OrderSystemException;
import util.OrderSystemUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();

    static class Response {
        public int ok;
        public String reason;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response response = new Response();
        req.setCharacterEncoding("utf-8");
        try {
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("您尚未登录");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("您尚未登录");
            }
            if (user.getIsAdmin() == 1) {
                throw new OrderSystemException("您是管理员，不能添加订单");
            }
            String body = OrderSystemUtil.readBody(req);
            Integer[] dishIds = gson.fromJson(body, Integer[].class);
            Order order = new Order();
            order.setUserId(user.getUserId());
            List<Dish> dishes = new ArrayList<>();
            for (Integer dishId : dishIds) {
                Dish dish = new Dish();
                dish.setDishId(dishId);
                dishes.add(dish);
            }
            order.setDishes(dishes);
            OrderDao orderDao = new OrderDao();
            orderDao.add(order);
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setContentType("application/json; charset=utf-8");
        Response response = new Response();
        List<Order> orders = new ArrayList<>();
        try {
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("您尚未登录");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("您尚未登录");
            }
            OrderDao orderDao = new OrderDao();
            String orderIdStr = req.getParameter("orderId");
            if (orderIdStr == null) {
                if (user.getIsAdmin() == 0) {
                    orders = orderDao.selectByUserId(user.getUserId());
                } else {
                    orders = orderDao.selectAll();
                }
                String jsonString = gson.toJson(orders);
                resp.getWriter().write(jsonString);
            } else {
                int orderId = Integer.parseInt(orderIdStr);
                Order order = orderDao.selectById(orderId);
                if (user.getIsAdmin() == 0
                        && order.getUserId() != user.getUserId()) {
                    throw new OrderSystemException("当前您无权查看其他人的订单");
                }
                String jsonString = gson.toJson(order);
                resp.getWriter().write(jsonString);
            }
        } catch (OrderSystemException e) {
            String jsonString = gson.toJson(orders);
            resp.getWriter().write(jsonString);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("您尚未登录");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("您尚未登录");
            }
            if (user.getIsAdmin() == 0) {
                throw new OrderSystemException("您不是管理员");
            }
            String orderIdStr = req.getParameter("orderId");
            String isDoneStr = req.getParameter("isDone");
            if (orderIdStr == null || isDoneStr == null) {
                throw new OrderSystemException("参数有误");
            }
            OrderDao orderDao = new OrderDao();
            int orderId = Integer.parseInt(orderIdStr);
            int isDone = Integer.parseInt(isDoneStr);
            orderDao.changeState(orderId, isDone);
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }
}