package com.ezticket.web.activity.repository.impl;

import com.ezticket.web.activity.pojo.TicketHolder;
import com.ezticket.web.activity.repository.TicketHolderDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketHolderDAOImpl implements TicketHolderDAO {

    String driver = "com.mysql.cj.jdbc.Driver";
    String url = System.getenv("SPRING_DATASOURCE_URL") + "?serverTimezone=Asia/Taipei";
    String userid = System.getenv("MYSQL_USER");
    String passwd = System.getenv("MYSQL_PASSWORD");
    private static final String GET_ONE_STMT =
            "SELECT collectno, memberno, mname, memail, tdetailsno, tstatus, activityno, aname, "
                    + "wetherseat, aimg, sessionstime, sessionetime, blockname, realx, realy, anote, aticketremind, aplace, aplaceaddress "
                    + "FROM ticketholder WHERE collectno = ?";
    private static final String GET_BY_MEM_STMT =
            "SELECT collectno, memberno, mname, memail, tdetailsno, tstatus, activityno, aname, "
                    + "wetherseat, aimg, sessionstime, sessionetime, blockname, realx, realy, anote, aticketremind, aplace, aplaceaddress "
                    + "FROM ticketholder WHERE memberno = ?  AND tstatus != -1 ORDER BY sessionstime";
    private static final String GET_ALL_STMT =
            "SELECT collectno, memberno, mname, memail, tdetailsno, tstatus, activityno, aname, "
                    + "wetherseat, aimg, sessionstime, sessionetime, blockname, realx, realy, anote, aticketremind, aplace, aplaceaddress "
                    + "FROM ticketholder ORDER BY sessionstime";

    @Override
    public TicketHolder findByCollectno(Integer collectno) {
        TicketHolder ticketHolder = null;

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
                ticketHolder = new TicketHolder();
                ticketHolder.setCollectno(rs.getInt("collectno"));
                ticketHolder.setMemberno(rs.getInt("memberno"));
                ticketHolder.setMname(rs.getString("mname"));
                ticketHolder.setMemail(rs.getString("memail"));
                ticketHolder.setTdetailsno(rs.getInt("tdetailsno"));
                ticketHolder.setTstatus(rs.getInt("tstatus"));
                ticketHolder.setActivityno(rs.getInt("activityno"));
                ticketHolder.setAname(rs.getString("aname"));
                ticketHolder.setWetherseat(rs.getInt("wetherseat"));
                ticketHolder.setAimg(rs.getBytes("aimg"));
                ticketHolder.setSessionstime(rs.getTimestamp("sessionstime"));
                ticketHolder.setSessionetime(rs.getTimestamp("sessionetime"));
                ticketHolder.setBlockname(rs.getString("blockname"));
                ticketHolder.setRealx(rs.getString("realx"));
                ticketHolder.setRealy(rs.getString("realy"));
                ticketHolder.setAnote(rs.getString("anote"));
                ticketHolder.setAticketremind(rs.getString("aticketremind"));
                ticketHolder.setAplace(rs.getString("aplace"));
                ticketHolder.setAplaceAddress(rs.getString("aplaceaddress"));
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
        return ticketHolder;
    }

//    取出該會員訂單沒有取消的票券
    @Override
    public List<TicketHolder> getByMemberno(Integer memberno) {
        List<TicketHolder> list = new ArrayList<>();
        TicketHolder ticketHolder = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_BY_MEM_STMT);

            pstmt.setInt(1, memberno);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                ticketHolder = new TicketHolder();
                ticketHolder.setCollectno(rs.getInt("collectno"));
                ticketHolder.setMemberno(rs.getInt("memberno"));
                ticketHolder.setMname(rs.getString("mname"));
                ticketHolder.setMemail(rs.getString("memail"));
                ticketHolder.setTdetailsno(rs.getInt("tdetailsno"));
                ticketHolder.setTstatus(rs.getInt("tstatus"));
                ticketHolder.setActivityno(rs.getInt("activityno"));
                ticketHolder.setAname(rs.getString("aname"));
                ticketHolder.setWetherseat(rs.getInt("wetherseat"));
                ticketHolder.setAimg(rs.getBytes("aimg"));
                ticketHolder.setSessionstime(rs.getTimestamp("sessionstime"));
                ticketHolder.setSessionetime(rs.getTimestamp("sessionetime"));
                ticketHolder.setBlockname(rs.getString("blockname"));
                ticketHolder.setRealx(rs.getString("realx"));
                ticketHolder.setRealy(rs.getString("realy"));
                ticketHolder.setAnote(rs.getString("anote"));
                ticketHolder.setAticketremind(rs.getString("aticketremind"));
                ticketHolder.setAplace(rs.getString("aplace"));
                ticketHolder.setAplaceAddress(rs.getString("aplaceaddress"));
                list.add(ticketHolder);
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
        return list;
    }

    @Override
    public List<TicketHolder> getAll() {
        List<TicketHolder> list = new ArrayList<>();
        TicketHolder ticketHolder = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            Class.forName(driver);
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(GET_ALL_STMT);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                ticketHolder = new TicketHolder();
                ticketHolder.setCollectno(rs.getInt("collectno"));
                ticketHolder.setMemberno(rs.getInt("memberno"));
                ticketHolder.setMname(rs.getString("mname"));
                ticketHolder.setMemail(rs.getString("memail"));
                ticketHolder.setTdetailsno(rs.getInt("tdetailsno"));
                ticketHolder.setTstatus(rs.getInt("tstatus"));
                ticketHolder.setActivityno(rs.getInt("activityno"));
                ticketHolder.setAname(rs.getString("aname"));
                ticketHolder.setWetherseat(rs.getInt("wetherseat"));
                ticketHolder.setAimg(rs.getBytes("aimg"));
                ticketHolder.setSessionstime(rs.getTimestamp("sessionstime"));
                ticketHolder.setSessionetime(rs.getTimestamp("sessionetime"));
                ticketHolder.setBlockname(rs.getString("blockname"));
                ticketHolder.setRealx(rs.getString("realx"));
                ticketHolder.setRealy(rs.getString("realy"));
                ticketHolder.setAnote(rs.getString("anote"));
                ticketHolder.setAticketremind(rs.getString("aticketremind"));
                ticketHolder.setAplace(rs.getString("aplace"));
                ticketHolder.setAplaceAddress(rs.getString("aplaceaddress"));
                list.add(ticketHolder);
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
        return list;
    }

    public static void main(String[] args) {

        TicketHolderDAOImpl dao = new TicketHolderDAOImpl();

        // 查詢單一票券
        TicketHolder vo1 = dao.findByCollectno(1);
        System.out.print(vo1.getCollectno() + ",");
        System.out.print(vo1.getMemberno() + ",");
        System.out.print(vo1.getMname() + ",");
        System.out.print(vo1.getMemail() + ",");
        System.out.print(vo1.getTdetailsno() + ",");
        System.out.print(vo1.getTstatus() + ",");
        System.out.print(vo1.getActivityno() + ",");
        System.out.print(vo1.getAname() + ",");
        System.out.print(vo1.getWetherseat() + ",");
        System.out.print(vo1.getAimg() + ",");
        System.out.print(vo1.getSessionstime() + ",");
        System.out.print(vo1.getSessionetime() + ",");
        System.out.print(vo1.getBlockname() + ",");
        System.out.print(vo1.getRealx() + ",");
        System.out.print(vo1.getRealy() + ",");
        System.out.print(vo1.getAnote() + ",");
        System.out.print(vo1.getAticketremind() + ",");
        System.out.print(vo1.getAplace() + ",");
        System.out.print(vo1.getAplaceAddress() + ",");
        System.out.println();
        System.out.println("---------------------");

        // 查詢單一會員
        List<TicketHolder> list1 = dao.getByMemberno(85341);
        for (TicketHolder aTicket : list1) {
            System.out.print(aTicket.getCollectno() + ",");
            System.out.print(aTicket.getMemberno() + ",");
            System.out.print(aTicket.getMname() + ",");
            System.out.print(aTicket.getMemail() + ",");
            System.out.print(aTicket.getTdetailsno() + ",");
            System.out.print(aTicket.getTstatus() + ",");
            System.out.print(aTicket.getActivityno() + ",");
            System.out.print(aTicket.getAname() + ",");
            System.out.print(aTicket.getWetherseat() + ",");
            System.out.print(aTicket.getAimg() + ",");
            System.out.print(aTicket.getSessionstime() + ",");
            System.out.print(aTicket.getSessionetime() + ",");
            System.out.print(aTicket.getBlockname() + ",");
            System.out.print(aTicket.getRealx() + ",");
            System.out.print(aTicket.getRealy() + ",");
            System.out.print(aTicket.getAnote() + ",");
            System.out.print(aTicket.getAticketremind() + ",");
            System.out.print(aTicket.getAplace() + ",");
            System.out.print(aTicket.getAplaceAddress() + ",");
            System.out.println();
        }
        System.out.println("---------------------");

        // 查詢所有票券
        List<TicketHolder> list2 = dao.getAll();
        for (TicketHolder aTicket : list2) {
            System.out.print(aTicket.getCollectno() + ",");
            System.out.print(aTicket.getMemberno() + ",");
            System.out.print(aTicket.getMname() + ",");
            System.out.print(aTicket.getMemail() + ",");
            System.out.print(aTicket.getTdetailsno() + ",");
            System.out.print(aTicket.getTstatus() + ",");
            System.out.print(aTicket.getActivityno() + ",");
            System.out.print(aTicket.getAname() + ",");
            System.out.print(aTicket.getWetherseat() + ",");
            System.out.print(aTicket.getAimg() + ",");
            System.out.print(aTicket.getSessionstime() + ",");
            System.out.print(aTicket.getSessionetime() + ",");
            System.out.print(aTicket.getBlockname() + ",");
            System.out.print(aTicket.getRealx() + ",");
            System.out.print(aTicket.getRealy() + ",");
            System.out.print(aTicket.getAnote() + ",");
            System.out.print(aTicket.getAticketremind() + ",");
            System.out.print(aTicket.getAplace() + ",");
            System.out.print(aTicket.getAplaceAddress() + ",");
            System.out.println();
        }
    }
}


