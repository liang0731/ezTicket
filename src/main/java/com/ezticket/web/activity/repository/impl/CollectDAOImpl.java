package com.ezticket.web.activity.repository.impl;

import com.ezticket.web.activity.pojo.CollectVO;
import com.ezticket.web.activity.repository.CollectDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectDAOImpl implements CollectDAO {
    String driver = "com.mysql.cj.jdbc.Driver";
    String url = System.getenv("SPRING_DATASOURCE_URL") + "?serverTimezone=Asia/Taipei";
    String userid = System.getenv("MYSQL_USER");
    String passwd = System.getenv("MYSQL_PASSWORD");

    private static final String INSERT_STMT =
            "INSERT INTO collect (memberno, tdetailsno,qrcode) VALUES (?, ?, ?)";
    private static final String GET_ALL_STMT =
            "SELECT collectno, memberno, tdetailsno, tstatus, qrcode FROM collect ORDER BY collectno";
    private static final String GET_ONE_STMT =
            "SELECT collectno, memberno, tdetailsno, tstatus, qrcode FROM collect WHERE collectno = ?";
    private static final String DELETE =
            "DELETE FROM collect WHERE collectno = ?";
    private static final String UPDATE =
            "UPDATE collect SET memberno = ?, tdetailsno = ?, tstatus = ?, qrcode = ? WHERE collectno = ?";

    @Override
    public void insert(CollectVO collectVO) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(INSERT_STMT);

            pstmt.setInt(1, collectVO.getMemberno());
            pstmt.setInt(2, collectVO.gettDetailsno());
            pstmt.setBytes(3, collectVO.getQrcode());

            pstmt.executeUpdate();

            // Handle any driver errors
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't load database driver. "
                    + e.getMessage());
            // Handle any SQL errors
        } catch (SQLException se) {
            throw new RuntimeException("A database error occured. "
                    + se.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException se) {
                    se.printStackTrace(System.err);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }

    }

    @Override
    public void update(CollectVO collectVO) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(UPDATE);

            pstmt.setInt(1, collectVO.getMemberno());
            pstmt.setInt(2, collectVO.gettDetailsno());
            pstmt.setInt(3, collectVO.gettStatus());
            pstmt.setBytes(4, collectVO.getQrcode());
            pstmt.setInt(5, collectVO.getCollectno());

            pstmt.executeUpdate();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't load database driver. "
                    + e.getMessage());
        } catch (SQLException se) {
            throw new RuntimeException("A database error occured. "
                    + se.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException se) {
                    se.printStackTrace(System.err);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }

    }

    @Override
    public void delete(Integer collectno) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(DELETE);

            pstmt.setInt(1, collectno);

            pstmt.executeUpdate();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't load database driver. "
                    + e.getMessage());
        } catch (SQLException se) {
            throw new RuntimeException("A database error occured. "
                    + se.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException se) {
                    se.printStackTrace(System.err);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }

    }

    @Override
    public CollectVO findByPK(Integer collectno) {

        CollectVO collectVO = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_ONE_STMT);

            pstmt.setInt(1, collectno);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                collectVO = new CollectVO();
                collectVO.setCollectno(rs.getInt("collectno"));
                collectVO.setMemberno(rs.getInt("memberno"));
                collectVO.settDetailsno(rs.getInt("tdetailsno"));
                collectVO.settStatus(rs.getInt("tstatus"));
                collectVO.setQrcode(rs.getBytes("qrcode"));
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't load database driver. "
                    + e.getMessage());
        } catch (SQLException se) {
            throw new RuntimeException("A database error occured. "
                    + se.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException se) {
                    se.printStackTrace(System.err);
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException se) {
                    se.printStackTrace(System.err);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        return collectVO;
    }

    @Override
    public List<CollectVO> getAll() {
        List<CollectVO> list = new ArrayList<CollectVO>();
        CollectVO collectVO = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_ALL_STMT);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                collectVO = new CollectVO();
                collectVO.setCollectno(rs.getInt("collectno"));
                collectVO.setMemberno(rs.getInt("memberno"));
                collectVO.settDetailsno(rs.getInt("tdetailsno"));
                collectVO.settStatus(rs.getInt("tstatus"));
                collectVO.setQrcode(rs.getBytes("qrcode"));
                list.add(collectVO); // Store the row in the list
            }

            // Handle any driver errors
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't load database driver. "
                    + e.getMessage());
            // Handle any SQL errors
        } catch (SQLException se) {
            throw new RuntimeException("A database error occured. "
                    + se.getMessage());
            // Clean up JDBC resources
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException se) {
                    se.printStackTrace(System.err);
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException se) {
                    se.printStackTrace(System.err);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        return list;
    }

    public static void main(String[] args) {

        CollectDAOImpl dao = new CollectDAOImpl();

        // 新增
//        CollectVO collectVO1 = new CollectVO();
//        collectVO1.setMemberno(85346);
//        collectVO1.settDetailsno(10006);
//        dao.insert(collectVO1);

        // 修改
//        CollectVO collectVO2 = new CollectVO();
//        collectVO2.setCollectno(12);
//        collectVO2.setMemberno(85344);
//        collectVO2.settDetailsno(10006);
//        collectVO2.settStatus(1);
//        collectVO2.setQrcode(null);
//        dao.update(collectVO2);



        // 刪除
//        dao.delete(12);

        // 查詢
//        CollectVO collectVO3 = dao.findByPK(11);
//        System.out.print(collectVO3.getCollectno() + ",");
//        System.out.print(collectVO3.getMemberno() + ",");
//        System.out.print(collectVO3.gettDetailsno() + ",");
//        System.out.print(collectVO3.gettStatus() + ",");
//        System.out.print(collectVO3.getQrcode() + ",");
//        System.out.println("---------------------");


        // 查詢
        List<CollectVO> list = dao.getAll();
        for (CollectVO aCollect : list) {
            System.out.print(aCollect.getCollectno() + ",");
            System.out.print(aCollect.getMemberno() + ",");
            System.out.print(aCollect.gettDetailsno() + ",");
            System.out.print(aCollect.gettStatus() + ",");
            System.out.print(aCollect.getQrcode() + ",");
            System.out.println();
        }
    }
}
