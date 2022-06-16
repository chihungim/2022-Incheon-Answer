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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class LoginPage extends BasePage {

	JTextField[] txt = { new JTextField(15), new JTextField(15) };

	JCheckBox box = new JCheckBox("로봇이 아닙니다.");

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

		cc.add(box);

		cs.add(hyplbl("처음이십니까?", JLabel.LEFT, 13, (e) -> {
			mf.swapPage(new SignPage());
		}), "North");

		box.addActionListener(a -> {
			new reCapcha().setVisible(true);
			box.setSelected(false);
		});

		cs.add(btn("로그인", a -> {
			if (!box.isSelected()) {
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
}
