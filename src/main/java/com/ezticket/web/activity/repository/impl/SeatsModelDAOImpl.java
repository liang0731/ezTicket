package com.ezticket.web.activity.repository.impl;

import com.ezticket.web.activity.pojo.SeatsModelVO;
import com.ezticket.web.activity.repository.SeatsModelDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class SeatsModelDAOImpl implements SeatsModelDAO {
    String driver = "com.mysql.cj.jdbc.Driver";
    String url = System.getenv("SPRING_DATASOURCE_URL") + "?serverTimezone=Asia/Taipei";
    String userid = System.getenv("MYSQL_USER");
    String passwd = System.getenv("MYSQL_PASSWORD");

    private static final String INSERT_STMT =
            "INSERT INTO seatsmodel (seatmodelno, blockno, x, y, realx, realy, seatstatus) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String GET_ALL_STMT =
            "SELECT seatmodelno, blockno, x, y, realx, realy, seatstatus FROM seatsmodel order by seatmodelno";
    private static final String GET_ONE_STMT =
            "SELECT seatmodelno, blockno, x, y, realx, realy, seatstatus FROM seatsmodel FROM seatsmodel WHERE seatmodelno = ?";
    private static final String DELETE =
            "DELETE FROM seatsmodel WHERE seatmodelno = ?";
    private static final String UPDATE =
            "UPDATE seatsmodel SET blockno = ?, x = ?, y = ?, realx = ?, reay = ?, seatstatus = ? WHERE seatmodelno = ?";

    @Override
    public void insert(SeatsModelVO seatsModelVO) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(INSERT_STMT);

            pstmt.setInt(1, seatsModelVO.getSeatModelno());
            pstmt.setInt(2, seatsModelVO.getBlockno());
            pstmt.setInt(3, seatsModelVO.getX());
            pstmt.setInt(4, seatsModelVO.getY());
            pstmt.setString(5, seatsModelVO.getRealx());
            pstmt.setString(6, seatsModelVO.getRealy());
            pstmt.setInt(7, seatsModelVO.getSeatStatus());

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
    public void update(SeatsModelVO seatsModelVO) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(UPDATE);

            pstmt.setInt(1, seatsModelVO.getBlockno());
            pstmt.setInt(2, seatsModelVO.getX());
            pstmt.setInt(3, seatsModelVO.getY());
            pstmt.setString(4, seatsModelVO.getRealx());
            pstmt.setString(5, seatsModelVO.getRealy());
            pstmt.setInt(6, seatsModelVO.getSeatStatus());
            pstmt.setInt(7, seatsModelVO.getSeatModelno());

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
    public void delete(Integer seatModelno) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(DELETE);

            pstmt.setInt(1, seatModelno);

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
    public SeatsModelVO findByPK(Integer seatModelno) {

        SeatsModelVO seatsModelVO = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_ONE_STMT);

            pstmt.setInt(1, seatModelno);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                seatsModelVO = new SeatsModelVO();
                seatsModelVO.setSeatModelno(rs.getInt("seatModelno"));
                seatsModelVO.setBlockno(rs.getInt("blockno"));
                seatsModelVO.setX(rs.getInt("x"));
                seatsModelVO.setY(rs.getInt("y"));
                seatsModelVO.setRealx(rs.getString("realx"));
                seatsModelVO.setRealy(rs.getString("realy"));
                seatsModelVO.setSeatStatus(rs.getInt("seatStatus"));
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
        return seatsModelVO;
    }

    @Override
    public List<SeatsModelVO> getAll() {
        List<SeatsModelVO> list = new ArrayList<SeatsModelVO>();
        SeatsModelVO seatsModelVO = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_ALL_STMT);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                seatsModelVO = new SeatsModelVO();
                seatsModelVO.setSeatModelno(rs.getInt("seatModelno"));
                seatsModelVO.setBlockno(rs.getInt("blockno"));
                seatsModelVO.setX(rs.getInt("x"));
                seatsModelVO.setY(rs.getInt("y"));
                seatsModelVO.setRealx(rs.getString("realx"));
                seatsModelVO.setRealy(rs.getString("realy"));
                seatsModelVO.setSeatStatus(rs.getInt("seatStatus"));
                list.add(seatsModelVO); // Store the row in the list
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
        SeatsModelDAOImpl seatsModelDAOImpl = new SeatsModelDAOImpl();
        List<SeatsModelVO> list = seatsModelDAOImpl.getAll();
        for(SeatsModelVO SMVO: list){
            System.out.println(SMVO);
        }
    }
}

