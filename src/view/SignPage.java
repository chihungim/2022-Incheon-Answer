package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class SignPage extends BasePage {

	JTextField txt[] = { new JTextField(20), new JTextField(20), new JTextField(20), new JTextField(20),
			new JTextField(20), new JTextField(20) };

	JComboBox<String> combo = new JComboBox<String>();

	JComponent[] jc = { txt[0], txt[1], txt[2], txt[3], txt[4], txt[5], combo };

	public SignPage() {
		setLayout(new GridBagLayout());

		add(c = new JPanel(new BorderLayout(5, 5)));

		c.add(lbl("회원가입", JLabel.CENTER, 20), "North");

		c.add(cc = new JPanel(new GridLayout(0, 1, 5, 5)));
		c.add(cs = new JPanel(new BorderLayout(5, 5)), "South");

		var temp = "이름,아이디,비밀번호,비밀번호 확인,전화번호,생년월일,거주지".split(",");

		for (int i = 0; i < temp.length; i++) {
			cc.add(lbl(temp[i], JLabel.LEFT, 15));
			cc.add(jc[i]);
		}

		cs.add(btn("회원가입", a -> {
			for (var t : txt) {
				if (t.getText().isEmpty()) {
					eMsg("빈칸이 있습니다.");
					return;
				}
			}

			if (!getRows("select id from user where id = ?", txt[1].getText()).isEmpty()) {
				eMsg("아이디가 중복되었습니다.");
				return;
			}

			if (!(txt[2].getText().matches(".*[a-zA-Z].*") && txt[2].getText().matches(".*[0-9].*")
					&& txt[2].getText().matches(".*[!@#$].*")) || txt[2].getText().length() < 4) {
				eMsg("비밀번호를 확인해주세요.");
				return;
			}

			if (!txt[2].getText().equals(txt[3].getTreeLock())) {
				eMsg("비밀번호가 일치하지 않습니다.");
				return;
			}

			if (!txt[4].getText().matches("^\\d{3}-\\d{4}-\\d{4}$")) {
				eMsg("전화번호를 확인해주세요.");
				return;
			}

			if (txt[5].getText().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
				BasePage.eMsg("날짜 형식이 잘못되었습니다.");
				return;
			}

			setRows("insert user values(0, ? , ? , ? , ? , ? , ?)", txt[0].getText(), txt[1].getText(),
					txt[2].getText(), txt[4].getText(), txt[5].getText(),
					getRows("select no from building where name = ?", combo.getSelectedItem()).get(0));

		}));

		cs.add(hyplbl("이미 계정이 있으십니까?", JLabel.LEFT, 13, (e) -> {
			mf.swapPage(new LoginPage());
		}), "South");

		for (var r : getRows("SELECT name FROM covid.building where type = 2;")) {
			combo.addItem(r.get(0) + "");
		}

		combo.setBackground(Color.WHITE);
		cc.setOpaque(false);
		cs.setOpaque(false);
		c.setOpaque(true);
		c.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
		c.setBackground(Color.WHITE);
	}
}
