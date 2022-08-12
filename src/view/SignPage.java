package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class SignPage extends BasePage {
	JTextField txt[] = { new JTextField(18), new JTextField(), new JTextField(), new JTextField(), new JTextField(),
			new JTextField() };

	JComboBox<Item> combo = new JComboBox<Item>(getRows("select no, name from building where type =2").stream()
			.map(a -> new Item(a.get(0).toString(), a.get(1).toString())).toArray(Item[]::new));

	public SignPage() {
		setLayout(new GridBagLayout());

		add(c = new JPanel(new BorderLayout()));
		c.add(lbl("회원가입", JLabel.CENTER, 20), "North");
		c.add(cc = new JPanel(new GridLayout(0, 1)));
		var str = "이름,아이디,비밀번호,비밀번호 확인,전화번호,생년월일".split(",");

		for (int i = 0; i < str.length; i++) {
			cc.add(lbl(str[i], JLabel.LEFT));
			cc.add(txt[i]);
		}

		cc.add(lbl("거주지", JLabel.LEFT));
		cc.add(combo);
		cc.add(hyplbl("이미 계정이 있으십니까?", JLabel.LEFT, 15, a -> mf.swapPage(new LoginPage())));
		cc.add(btn("회원가입", a -> {
			for (var t : txt) {
				if (t.getText().isEmpty()) {
					emsg("빈칸이 있습니다.");
					return;
				}
			}

			if (getRow("select * from user where id = ?", txt[1]) != null) {
				emsg("아이디가 중복되었습니다.");
				return;
			}

			if (!(txt[2].getText().matches(".*[a-zA-Z].*") && txt[2].getText().matches(".*[0-9].*")
					&& txt[2].getText().matches(".*[!@#$].*")) || txt[2].getText().length() < 4) {
				emsg("비밀번호 형식이 일치하지 않습니다.");
				return;
			}

			if (!txt[2].getText().equals(txt[3].getText())) {
				emsg("비밀번호가 일치하지 않습니다.");
				return;
			}

			if (!txt[4].getText().matches("^\\d{3}-\\d{4}-\\d{4}$")) {
				emsg("전화번호 형식이 잘못되었습니다.");
				return;
			}

			if (!txt[5].getText().matches("^\\d{4}-\\d{2}-\\d{2}")) {
				emsg("생년월일 형식이 잘못되었습니다.");
				return;
			}

			execute("insert user values(0, ?,?,?,?,?,?)", txt[0].getText(), txt[1].getText(), txt[2].getText(),
					txt[4].getText(), txt[5].getText(), ((Item) combo.getSelectedItem()).getKey());
			imsg("회원가입이 완료되었습니다.");
			mf.swapPage(new LoginPage());
		}));

		c.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));

	}

}
