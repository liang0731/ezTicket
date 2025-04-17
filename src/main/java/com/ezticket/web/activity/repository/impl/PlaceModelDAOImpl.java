package com.ezticket.web.activity.repository.impl;

import com.ezticket.web.activity.pojo.PlaceModelVO;
import com.ezticket.web.activity.repository.PlaceModelDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PlaceModelDAOImpl implements PlaceModelDAO {
    String driver = "com.mysql.cj.jdbc.Driver";
    String url = System.getenv("SPRING_DATASOURCE_URL") + "?serverTimezone=Asia/Taipei";
    String userid = System.getenv("MYSQL_USER");
    String passwd = System.getenv("MYSQL_PASSWORD");

    private static final String INSERT_STMT =
            "INSERT INTO placemodel (modelno, modelname, modelstatus) VALUES (?, ?, ?)";
    private static final String GET_ALL_STMT =
            "SELECT modelno, modelname, modelstatus FROM placemodel order by modelno";
    private static final String GET_ONE_STMT =
            "SELECT modelno, modelname, modelstatus FROM placemodel WHERE modelno = ?";
    private static final String DELETE =
            "DELETE FROM placemodel WHERE modelno = ?";
    private static final String UPDATE =
            "UPDATE placemodel SET modelname = ?, modelstatus = ? WHERE modelno = ?";

    @Override
    public void insert(PlaceModelVO placeModelVO) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(INSERT_STMT);

            pstmt.setInt(1, placeModelVO.getModelno());
            pstmt.setString(2, placeModelVO.getModelName());
            pstmt.setInt(3, placeModelVO.getModelStatus());

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
    public void update(PlaceModelVO placeModelVO) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(UPDATE);

            pstmt.setString(1, placeModelVO.getModelName());
            pstmt.setInt(2, placeModelVO.getModelStatus());
            pstmt.setInt(3, placeModelVO.getModelno());

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
    public void delete(Integer modelno) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(DELETE);

            pstmt.setInt(1, modelno);

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
    public PlaceModelVO findByPK(Integer modelno) {

        PlaceModelVO placeModelVO = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_ONE_STMT);

            pstmt.setInt(1, modelno);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                placeModelVO = new PlaceModelVO();
                placeModelVO.setModelno(rs.getInt("modelno"));
                placeModelVO.setModelName(rs.getString("modelName"));
                placeModelVO.setModelStatus(rs.getInt("modelStatus"));
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
        return placeModelVO;
    }

    @Override
    public List<PlaceModelVO> getAll() {
        List<PlaceModelVO> list = new ArrayList<PlaceModelVO>();
        PlaceModelVO placeModelVO = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_ALL_STMT);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                placeModelVO = new PlaceModelVO();
                placeModelVO.setModelno(rs.getInt("modelno"));
                placeModelVO.setModelName(rs.getString("modelName"));
                placeModelVO.setModelStatus(rs.getInt("modelStatus"));
                list.add(placeModelVO); // Store the row in the list
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

}
//	public static void main(String[] args) {
//
//		PlaceModelDAOImpl dao = new PlaceModelDAOImpl();

// 新增
//		PlaceModelVO placeModelVO1 = new PlaceModelVO();
//		placeModelVO1.setModelno(7);
//		placeModelVO1.setModelName("測試1");
//		placeModelVO1.setModelStatus(0);
//		dao.insert(placeModelVO1);

// 修改
//		PlaceModelVO placeModelVO2 = new PlaceModelVO();
//		placeModelVO2.setModelno(7);
//		placeModelVO2.setModelName("測試2");
//		placeModelVO2.setModelStatus(1);
//		dao.update(placeModelVO2);

// 刪除
//		dao.delete(7);
//}

