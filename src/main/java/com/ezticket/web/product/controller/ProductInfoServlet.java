package com.ezticket.web.product.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ezticket.web.product.pojo.Pclass;
import com.ezticket.web.product.pojo.Pcomment;
import com.ezticket.web.product.pojo.Product;
import com.ezticket.web.product.service.PimgtService;
import com.ezticket.web.product.service.ProductService;
import com.ezticket.web.product.util.PageResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


@WebServlet("/ProductInfoServlet")
public class ProductInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductService productSvc;
    private PimgtService pimgtSvc;

    @Override
    public void init() throws ServletException {
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        productSvc = applicationContext.getBean(ProductService.class);
        pimgtSvc = applicationContext.getBean(PimgtService.class);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // CORS Header
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        //前台僅顯示上架商品(包含前台查詢)
        if ("availableProductList".equals(action)) {
            Map<String, String[]> map = new HashMap<String, String[]>(request.getParameterMap());
            String pstatus[] = {"0"};  //0是已上架，1是已下架
            map.put("pstatus", pstatus);
            // 建立 Timestamp 物件
            Date date = new java.util.Date();
            Timestamp today = new Timestamp(date.getTime());

            // 建立 SimpleDateFormat 物件
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            // 將 Timestamp 轉換為 String
            String todayString = sdf.format(today);
            String sale_date[] = {todayString};
            map.put("sale_date", sale_date);

            List<Product> allAvailableProductlist = productSvc.getAllByproductSearch(map);
            list2json(allAvailableProductlist, response);
            return;
        }

        //單一商品
        if ("singleProduct".equals(action)) {
            Integer productno = Integer.valueOf(request.getParameter("productno"));
            Product product = productSvc.getOneProduct(productno);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            String ujson = gson.toJson(product);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter pw = response.getWriter();
            pw.print(ujson);
            pw.flush();
            return;
        }

        //後台商品管理查詢所有商品
        if ("allProductList".equals(action)) {
            List<Product> allProductlist = productSvc.getAllProduct();
            list2json(allProductlist, response);
            return;
        }

        //商品複合查詢
        if ("ProductSearchForm".equals(action)) {
            Map<String, String[]> map = request.getParameterMap(); //將得到的資料轉成map
            List<Product> productList = productSvc.getAllByproductSearch(map); //轉交進行複合查詢
            list2json(productList, response);
        }

        //商品複合查詢搭配分頁(後台商品管理打這支)
        if ("ProductSearchPage".equals(action)) {
            Map<String, String[]> map = request.getParameterMap(); //將得到的資料轉成map
            Integer pageNumber = Integer.valueOf(request.getParameter("pageNumber"));
            Integer pageSize = Integer.valueOf(request.getParameter("pageSize"));
            PageResult<Product> productList = productSvc.getAllByproductSearch(map, pageNumber, pageSize); //轉交進行複合查詢
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            String json = gson.toJson(productList);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter pw = response.getWriter();
            pw.print(json);
            pw.flush();
            return;
        }
    }

    public void list2json(List<Product> productList, HttpServletResponse response) throws IOException {
        //轉換成json格式寫出
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String json = gson.toJson(productList);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter pw = response.getWriter();
        pw.print(json);
        pw.flush();
    }


}
