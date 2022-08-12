package view;

import java.awt.BorderLayout;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;

import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.plaf.metal.MetalCheckBoxUI;
import javax.swing.plaf.metal.MetalToggleButtonUI;

public class LoginPage extends BasePage {

	JTextField txt[] = { new JTextField(), new JTextField() };

	JCheckBox box;
	boolean flag;

	public LoginPage() {
		setLayout(new GridBagLayout());
		add(sz(c = new JPanel(new BorderLayout()), 200, 250));

		c.add(lbl("COVID-19", JLabel.CENTER, 20), "North");
		c.add(cc = new JPanel(new GridLayout(0, 1)));
		c.add(cs = new JPanel(new BorderLayout()), "South");
		txt[0].setText("user01");
		txt[1].setText("user01!");
		cc.add(lbl("ID", JLabel.LEFT, 15));
		cc.add(txt[0]);
		cc.add(lbl("pw", JLabel.LEFT, 15));
		cc.add(txt[1]);

		cs.add(box = new JCheckBox("로봇이 아닙니다."), "North");
		cs.add(hyplbl("처음이십니까?", JLabel.LEFT, 15, e -> mf.swapPage(new SignPage())));
		cs.add(btn("로그인", a -> {
			if (txt[0].getText().isEmpty() || txt[1].getText().isEmpty()) {
				emsg("빈칸이 있습니다.");
				return;
			}

			if (!flag) {
				emsg("리캡챠를 확인해주세요.");
				return;
			}

			if (txt[0].getText().equals("admin") && txt[1].getText().equals("1234")) {
				mf.swapPage(new AdminPage());
				return;
			}

			if (getRow("select * from user where id = ? and pw = ?", txt[0].getText(), txt[1].getText()) == null) {
				emsg("아이디 또는 비밀번호가 잘못되었습니다.");
				return;
			}

			uno = getRow("select * from user where id = ? and pw = ?", txt[0].getText(), txt[1].getText()).get(0)
					.toString();

			mf.swapPage(new MainPage());
		}), "South");
		box.setEnabled(false);
		box.setModel(new DefaultButtonModel() {
			public boolean isSelected() {
				return flag;
			};
		});

		box.addMouseListener(new MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent e) {
				if (box.isSelected())
					return;
				new ReCapcha().setVisible(true);
			};
		});
		c.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
	}

	public static void main(String[] args) {
		
		mf.swapPage(new LoginPage());
	}
}
