package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class LoginPage extends BasePage {

	JTextField[] txt = { new JTextField(15), new JTextField(15) };
	CheckPanel chkpanel = new CheckPanel();

	public LoginPage() {
		setLayout(new GridBagLayout());
		add(sz(c = new JPanel(new BorderLayout()), 200, 250));

		c.add(lbl("COVID-19", 0, 20), "North");
		c.add(cc = new JPanel(new GridLayout(0, 1, 5, 5)));
		c.add(cs = new JPanel(new BorderLayout()), "South");

		for (int i = 0; i < txt.length; i++) {
			cc.add(lbl("ID,PW".split(",")[i], JLabel.LEFT, 15));
			cc.add(txt[i]);
		}

		cc.add(chkpanel);

		cs.add(hyplbl("처음이십니까?", JLabel.LEFT, 13, (e) -> {
			mf.swapPage(new SignPage());
		}), "North");

		cs.add(btn("로그인", a -> {
			if (!chkpanel.flag2) {
				eMsg("캡챠를 확인해주세요.");
				return;
			}

			if (txt[0].getText().equals("admin") && txt[1].getText().equals("1234")) {
				mf.swapPage(new AdminPage());
				return;
			}

			if (txt[0].getText().isEmpty() || txt[1].getText().isEmpty()) {
				eMsg("빈칸이 있습니다.");
				return;
			}

			if (getRows("select * from user where id = ? and pw = ?", txt[0].getText(), txt[1].getText()).isEmpty()) {
				eMsg("존재하는 회원이 없습니다.");
				return;
			}

			uno = getRow("select * from user where id = ? and pw = ?", txt[0].getText(), txt[1].getText()).get(0) + "";

			mf.swapPage(new MainPage());

		}));

		cc.setOpaque(false);
		cs.setOpaque(false);

		c.setBackground(Color.WHITE);
		c.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
	}

	public static void main(String[] args) {
		mf.swapPage(new LoginPage());
	}

	class CheckPanel extends JPanel {
		JPanel box;
		Thread thread;

		int arc = 0;
		boolean flag1, flag2; // flag1 click, flag2 after capture finished

		public CheckPanel() {
			setLayout(new BorderLayout(5, 5));
			add(sz(box = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					var g2 = (Graphics2D) g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setStroke(new BasicStroke(2));

					if (flag1) {
						g2.setColor(Color.LIGHT_GRAY);
						g2.drawOval(3, 3, 25, 25);
						g2.setColor(Color.GRAY);
						g2.drawArc(3, 3, 25, 25, arc, arc);
						if (thread.isInterrupted()) {
							new reCapcha().setVisible(true);
						}
					} else if (flag2) {
						g2.setColor(Color.green);
						g2.drawString("✔", g2.getFontMetrics().stringWidth("✔") / 2, 25);
					} else {
						g2.setColor(Color.BLACK);
						g2.drawRect(3, 3, 25, 25);
					}

				}
			}, 30, 30));

			box.add(lbl("로봇이 아닙니다.", 2, 15));
			box.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == 1 && !flag1)
						drawArc();
				}
			});

			box.setOpaque(false);
			setOpaque(false);
		}

		void drawArc() {
			if (!(flag1 && flag2)) {
				flag1 = true;
				thread = new Thread(() -> {
					while (arc <= 365) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						arc += 5;
						box.repaint();
					}
					thread.interrupt();
				});
				thread.start();
			} else {
				box.repaint();
				box.revalidate();
			}
		}

	}
}
