package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MainPage extends BasePage {

	JPanel chart;
	Thread thread;

	double totalValue = toInt(getRows("select count(*) from purchase where date(date) <= '2022-08-31'").get(0).get(0));

	int max = toInt(getRows(
			"select count(*), month(date) from purchase where month(date) >= 1 and month(date) <= month(now()) group by month(date) order by count(*) desc")
					.get(0).get(0)),
			curHeight = 0, maxHeight = 250;

	public MainPage() {
		setLayout(new BorderLayout(5, 5));
		add(n = new JPanel(new BorderLayout(5, 5)), "North");
		add(c = new JPanel(new BorderLayout(5, 5)));
		add(s = new JPanel(new GridLayout(1, 0, 5, 5)), "South");

		var lst = getRows(
				"select count(*), month(date) from purchase where month(date) >= 1 and month(date) <= month(now()) group by month(date) order by month(date)");

		c.add(chart = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				var g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setFont(new Font("", Font.BOLD, 20));
				g2.setColor(Color.GRAY);
				g2.drawString("월간 접종자 추이", 5, 20);
				for (int i = 0; i < lst.size(); i++) {
					var myHeight = (int) (toInt(lst.get(i).get(0)) / (double) max * maxHeight);
					if (myHeight > curHeight)
						myHeight = curHeight;
					g2.setColor(Color.GRAY);
					g2.drawString(lst.get(i).get(0).toString(), 45 + i * 120, 320 - myHeight);
					g2.drawString(lst.get(i).get(1).toString() + "월", 40 + i * 120, 350);
					g2.setColor(Color.ORANGE);
					g2.fillRect(30 + i * 120, 330 - myHeight, 50, myHeight);
					g2.setColor(Color.BLACK);
					g2.drawRect(30 + i * 120, 330 - myHeight, 50, myHeight);
				}
			}
		});

		thread = new Thread(() -> {
			for (int i = 1; i <= maxHeight; i++) {
				curHeight = i;
				chart.repaint();
				chart.revalidate();
				try {
					thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		n.add(lbl("예방접종현황", JLabel.LEFT, 20), "North");
		n.add(nc = new JPanel(new GridLayout(1, 0, 50, 10)));
		n.add(ns = new JPanel(new GridLayout(1, 0, 50, 10)), "South");

		for (int i = 0; i < 4; i++) {
			nc.add(lbl("<html><font color='white'>" + (i + 1) + "차 접종", 0, 20));
			var temp = new JPanel(new BorderLayout(20, 10));
			var rs = getRows("select count(*) from purchase where shot = ? and date(date) <= now()", i + 1);
			temp.add(
					lbl("<html><font color = 'orange'>" + String.format("%.1f",
							(Double.parseDouble(rs.get(0).get(0).toString()) / totalValue) * 100) + "%", 0, 15),
					"West");

			temp.add(lbl("<html>누적 " + rs.get(0).get(0) + "명<br>신규 "
					+ getRows("select count(*) from purchase where date(date) = now() and shot = " + (i + 1)).get(0)
							.get(0)
					+ "명", JLabel.LEFT, 15));

			ns.add(temp);
		}

		for (var cap : "길찾기,프로필,로그아웃,종료".split(",")) {

			s.add(btn(cap, a -> {
				if (a.getActionCommand().equals("길찾기")) {
					try {
						mf.swapPage(new SearchPage());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (a.getActionCommand().equals("프로필")) {
					mf.swapPage(new MyPage());
				} else if (a.getActionCommand().equals("로그아웃")) {
					mf.swapPage(new LoginPage());
				} else {
					System.exit(0);
				}
			}));
		}

		nc.setBackground(Color.ORANGE);

		setBorder(new EmptyBorder(5, 5, 5, 5));
		thread.start();
	}

	public static void main(String[] args) {
		mf.swapPage(new MainPage());
	}
}
