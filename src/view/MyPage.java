package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class MyPage extends BasePage {

	DefaultTableModel m = model("병원 이름,내용,평점".split(","));
	JTable t = table(m);
	DefaultTableModel m1 = model("구분,백신 종류,병원,가격".split(","));
	JTable t1 = table(m1);

	JTextField txt[] = { new JTextField(), new JTextField(), new JTextField(), new JTextField() };
	JComboBox<item> combo = new JComboBox<item>() {
	};

	class item {
		String key;
		String value;

		public item(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	JComponent jc[] = { txt[0], txt[1], txt[2], txt[3], combo };

	public MyPage() {
		setLayout(new BorderLayout(10, 10));
		add(c = new JPanel(new GridLayout(0, 1, 5, 5)));
		add(e = new JPanel(new BorderLayout(5, 5)), "East");

		c.setBackground(Color.WHITE);
		c.add(lbl("Profile", 0, 25));

		var str = "아이디,이름,전화번호,생년월일,거주지".split(",");

		for (int i = 0; i < str.length; i++) {
			c.add(BasePage.lbl(str[i], JLabel.LEFT, 15));
			c.add(jc[i]);
		}

		combo.setBackground(Color.WHITE);

		e.add(new JScrollPane(t));
		e.add(sz(new JScrollPane(t1), 0, 120), "South");

		add(BasePage.hyplbl("메인으로 가기", JLabel.LEFT, 13, (e) -> {
			mf.swapPage(new MainPage());
		}), "South");

		c.add(BasePage.btn("수정하기", a -> {
			for (var t : txt) {
				if (t.getText().isEmpty()) {
					eMsg("빈칸이 있습니다.");
					return;
				}
			}

			if (getRow("select id from user where id = ? and no <> ?", txt[0].getText(), uno) != null) {
				eMsg("아이디가 중복되었습니다.");
				return;
			}

			if (!txt[2].getText().matches("^\\d{3}-\\d{4}-\\d{4}$")) {
				eMsg("전화번호를 확인해주세요.");
				return;
			}

			setRows("update user set id = ?, name = ?, phone = ?, building = ? where no = ?", txt[0].getText(),
					txt[1].getText(), txt[2].getText(), ((item) combo.getSelectedItem()).key, uno);
			iMsg("수정이 완료되었습니다.");
		}));

		for (var r : getRows("select no,name from building where type=2"))
			combo.addItem(new item(r.get(0) + "", r.get(1) + ""));

		for (int i = 0; i < combo.getItemCount(); i++)
			if (combo.getItemAt(i).key.equals(getRow("select building from user where no = ?", uno).get(0) + ""))
				combo.setSelectedIndex(i);

		addRow(m, getRows(
				"select b.name, r.review, r.rate from rate r, building b where b.`no` = r.building and r.user = ?",
				uno));

		addRow(m1, getRows(
				"select concat(p.shot, '차 접종') , v.name, b.name, concat(format(v.price, '#,##0'), '원') from purchase p, vaccine v, building b where p.vaccine = v.no and p.building = b.no and user = ? order by p.shot",
				uno));

		var rs = getRow("select id, name, phone, birth from user where no = ?", uno);

		for (int i = 0; i < rs.size(); i++)
			txt[i].setText(rs.get(i) + "");

		txt[3].setEditable(false);

		c.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));

		setBorder(new EmptyBorder(10, 20, 10, 20));

	}

	public static void main(String[] args) {
		uno = "1";
		mf.swapPage(new MyPage());
	}
}
