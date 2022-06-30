package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JComboBox;
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

	DefaultTableModel m1 = model("병원 이름,내용,평점".split(","));
	DefaultTableModel m2 = model("구분,백신 종류,병원,가격".split(","));

	JTable t1 = table(m1);
	JTable t2 = table(m2);

	JTextField txt[] = { new JTextField(), new JTextField(), new JTextField(), new JTextField() };
	JComboBox<Item> combo = new JComboBox<Item>(getRows("select no, name from building where type =2").stream()
			.map(a -> new Item(a.get(0).toString(), a.get(1).toString())).toArray(Item[]::new));

	public MyPage() {
		setLayout(new BorderLayout(10, 10));

		var m = new JPanel(new GridLayout(1, 0, 10, 10));
		add(m);
		m.add(c = new JPanel(new BorderLayout(5, 5)));
		m.add(e = new JPanel(new BorderLayout(5, 5)));

		c.add(lbl("Profile", JLabel.CENTER, 20), "North");
		c.add(cc = new JPanel(new GridLayout(0, 1, 5, 5)));

		e.add(new JScrollPane(t1));
		e.add(sz(new JScrollPane(t2), 0, 80), "South");

		var str = "아이디,이름,전화번호,생년월일".split(",");

		for (int i = 0; i < str.length; i++) {
			cc.add(lbl(str[i], JLabel.LEFT));
			cc.add(txt[i]);
		}

		cc.add(lbl("거주지", JLabel.LEFT));
		cc.add(combo);
		cc.add(btn("수정", a -> {
		}));

		addRow(m1, getRows("select b.name, review, rate from building b ,rate r where b.no = r.building and r.user = ?",
				uno));
		addRow(m2, getRows(
				"select concat(shot, '차 접종'), v.name, b.name, concat(format(v.price, '#,##0'), '원') from building b, purchase p, vaccine v where b.no = p.building and p.vaccine = v.no and p.user = ?",
				uno));

		add(hyplbl("메인으로", JLabel.LEFT, 13, a -> mf.swapPage(new MainPage())), "South");

		c.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));

		setBorder(new EmptyBorder(20, 20, 20, 20));
	}

	public static void main(String[] args) {
		uno = "1";
		mf.swapPage(new MyPage());
	}
}
