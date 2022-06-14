package db;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
	public static Connection con;
	public static Statement stmt;

	static SystemTray tray = SystemTray.getSystemTray();
	static Image img = Toolkit.getDefaultToolkit().getImage("./datafiles/covid.png");
	static TrayIcon icon = new TrayIcon(img);

	String cascade = "ON UPDATE CASCADE ON DELETE CASCADE";

	static {
		try {
			con = DriverManager.getConnection(
					"jdbc:mysql://localhost?serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&allowLoadLocalInfile=true",
					"root", "1234");
			stmt = con.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			icon.setImageAutoSize(true);
			try {
				tray.add(icon);
			} catch (AWTException e2) {
			}

			icon.displayMessage("DB 셋팅", "DB 셋팅 실패", MessageType.ERROR);
			System.exit(0);
		}
	}

	void execute(String sql) {
		try {
			stmt.execute(sql);
		} catch (SQLException e) {
			icon.setImageAutoSize(true);
			try {
				tray.add(icon);
			} catch (AWTException e2) {
			}
			System.out.println(sql);
			e.printStackTrace();
			icon.displayMessage("DB 셋팅", "DB 셋팅 실패", MessageType.ERROR);
			System.exit(0);
		}
	}

	void create(String t, String c) {
		execute("create table " + t + "(" + c + ")");
		execute("load data local infile './datafiles/" + t + ".txt' into table " + t + " ignore 1 lines");
	}

	public DB() {
		execute("drop database if exists covid");
		execute("create database covid default character set utf8");
		execute("drop user if exists user@localhost");
		execute("create user user@localhost identified by '1234'");
		execute("grant select, insert, update, delete on covid.* to user@localhost");
		execute("set global local_infile = 1");
		execute("use covid");

		create("building",
				"no int primary key not null auto_increment, name text, open time, close time, info text, type int, x int, y int, img longblob");
		create("connection",
				"node1 int, node2 int, name text, foreign key(node1) references building(no), foreign key(node2) references building(no)");
		create("user",
				"no int primary key not null auto_increment, name varchar(20), id varchar(20), pw varchar(20), phone varchar(30), birth date, building int, foreign key(building) references building(no)");
		create("vaccine", "no int primary key auto_increment, name varchar(20), price int");
		create("rate",
				"no int primary key auto_increment, building int, rate int, user int, review text, foreign key(building) references building(no), foreign key(user) references user(no) "
						+ cascade);
		create("purchase",
				"no int primary key not null auto_increment, user int, `date` datetime, building int, vaccine int, shot int, foreign key(user) references user(no) "
						+ cascade
						+ ", foreign key(building) references building(no), foreign key(vaccine) references vaccine(no)");

		try {
			var pst = con.prepareStatement("update building set img = ? where no = ?");
			var rs = stmt.executeQuery("select no from building where type <> 3");
			while (rs.next()) {
				pst.setObject(1, new FileInputStream(new File("./datafiles/건물사진/" + rs.getInt(1) + ".jpg")));
				pst.setObject(2, rs.getInt(1));
				pst.execute();
			}
		} catch (Exception e) {
			icon.setImageAutoSize(true);
			try {
				tray.add(icon);
			} catch (AWTException e2) {
			}
			e.printStackTrace();
			icon.displayMessage("DB 셋팅", "DB 셋팅 실패", MessageType.ERROR);
			System.exit(0);
		}

		icon.setImageAutoSize(true);
		try {
			tray.add(icon);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		icon.displayMessage("DB 셋팅", "DB 셋팅 성공", MessageType.INFO);
		System.exit(0);
	}

	public static void main(String[] args) {
		new DB();
	}
}
