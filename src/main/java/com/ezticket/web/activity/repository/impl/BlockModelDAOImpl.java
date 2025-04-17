package com.ezticket.web.activity.repository.impl;

import com.ezticket.web.activity.pojo.BlockModelVO;
import com.ezticket.web.activity.repository.BlockModelDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class BlockModelDAOImpl implements BlockModelDAO {
    String driver = "com.mysql.cj.jdbc.Driver";
    String url = System.getenv("SPRING_DATASOURCE_URL") + "?serverTimezone=Asia/Taipei";
    String userid = System.getenv("MYSQL_USER");
    String passwd = System.getenv("MYSQL_PASSWORD");

    private static final String INSERT_STMT =
            "INSERT INTO blockmodel (blockno, modelno, blockname, blocktype) VALUES (?, ?, ?, ?)";
    private static final String GET_ALL_STMT =
            "SELECT blockno, modelno, blockname, blocktype FROM blockmodel order by blockno";
    private static final String GET_ONE_STMT =
            "SELECT blockno, modelno, blockname, blocktype FROM blockmodel WHERE blockno = ?";
    private static final String DELETE =
            "DELETE FROM blockmodel WHERE blockno = ?";
    private static final String UPDATE =
            "UPDATE blockmodel SET modelno = ?, blockname = ?, blocktype = ? WHERE blockno = ?";

    @Override
    public void insert(BlockModelVO blockModelVO) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(INSERT_STMT);

            pstmt.setInt(1, blockModelVO.getBlockno());
            pstmt.setInt(2, blockModelVO.getModelno());
            pstmt.setString(3, blockModelVO.getBlockName());
            pstmt.setInt(4, blockModelVO.getBlockType());

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
    public void update(BlockModelVO blockModelVO) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(UPDATE);

            pstmt.setInt(1, blockModelVO.getModelno());
            pstmt.setString(2, blockModelVO.getBlockName());
            pstmt.setInt(3, blockModelVO.getBlockType());
            pstmt.setInt(4, blockModelVO.getBlockno());

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
    public void delete(Integer blockno) {

        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(DELETE);

            pstmt.setInt(1, blockno);

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
    public BlockModelVO findByPK(Integer blockno) {

        BlockModelVO blockModelVO = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_ONE_STMT);

            pstmt.setInt(1, blockno);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                blockModelVO = new BlockModelVO();
                blockModelVO.setBlockno(rs.getInt("blockno"));
                blockModelVO.setModelno(rs.getInt("modelno"));
                blockModelVO.setBlockName(rs.getString("blockName"));
                blockModelVO.setBlockType(rs.getInt("blockyType"));
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
        return blockModelVO;
    }

    @Override
    public List<BlockModelVO> getAll() {
        List<BlockModelVO> list = new ArrayList<BlockModelVO>();
        BlockModelVO blockModelVO = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_ALL_STMT);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                blockModelVO = new BlockModelVO();
                blockModelVO.setBlockno(rs.getInt("blockno"));
                blockModelVO.setModelno(rs.getInt("modelno"));
                blockModelVO.setBlockName(rs.getString("blockName"));
                blockModelVO.setBlockType(rs.getInt("blockType"));
                list.add(blockModelVO); // Store the row in the list
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
