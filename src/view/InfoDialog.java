package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

public class InfoDialog extends JDialog {

	ArrayList<Object> building;
	SearchPage page;
	CardLayout pages;
	JPanel info, reserv, c, s;
	JTextField txt[] = { new JTextField(15), new JTextField(15), new JTextField(15) };
	JComboBox combo[] = { new JComboBox<String>("아스트라제네카,얀센,화이자,모더나".split(",")), new JComboBox<String>() };
	JComponent jc[][] = { { BasePage.lbl("이름", JLabel.LEFT, 15) }, { txt[0] },
			{ BasePage.lbl("전화번호", JLabel.LEFT, 15) }, { txt[1] }, { BasePage.lbl("백신", JLabel.LEFT, 15) },
			{ combo[0] }, { BasePage.lbl("예약 날짜 및 시간", JLabel.LEFT, 15) }, { txt[2], combo[1] }, };
	JButton btn;
	boolean is시설 = false;

	public InfoDialog(ArrayList<Object> building) {
		this.building = building;
		setLayout(new BorderLayout(5, 5));
		setModal(true);
		setSize(400, 400);
		setLocationRelativeTo(BasePage.mf);
		setDefaultCloseOperation(2);
		setUndecorated(true);

		page = (SearchPage) BasePage.mf.getContentPane().getComponent(0);

		add(c = new JPanel(pages = new CardLayout()));
		add(s = new JPanel(new GridLayout(1, 0, 5, 5)), "South");

		c.add(info = new JPanel(new BorderLayout()), "인포");
		c.add(reserv = new JPanel(new GridLayout(0, 1, 5, 5)), "예약");
		s.add(btn = BasePage.btn("닫기", a -> {
			if (a.getActionCommand().equals("닫기")) {
				dispose();
			} else {
				pages.show(c, "인포");
				btn.setText("닫기");
			}
		}));

		is시설 = BasePage.toInt(building.get(5)) == 0 || BasePage.toInt(building.get(5)) == 1;

		if (is시설) {
			s.add(BasePage.btn("예약하기", a -> {
				if (!reserv.isVisible()) {
					pages.show(c, "예약");
					btn.setText("뒤로가기");
				} else {
					if (txt[2].getText().isEmpty()) {
						BasePage.eMsg("날짜를 선택해주세요.");
						return;
					}

					if (BasePage.toInt(BasePage.getRow("select count(*) from purchase where user = ?", BasePage.uno)
							.get(0)) == 4) {
						BasePage.eMsg("이미 모든 접종을 완료하셨습니다.");
						return;
					}

					BasePage.setRows("insert purchase values(0,?,?,?,?,?)", BasePage.uno,
							txt[2].getText() + " " + combo[1].getSelectedItem(), building.get(0),
							combo[0].getSelectedIndex() + 1, BasePage.toInt(BasePage
									.getRow("select count(*) from purchase where user = ?", BasePage.uno).get(0)));
					var lbl = BasePage.lbl("예약이 완료되었습니다.", JLabel.LEFT);
					var evtlbl = BasePage.hyplbl("<html><font color = rgb(0,123,255)>지도에서 보기", JLabel.LEFT, 13, (e) -> {
						JOptionPane.getRootFrame().dispose();
						page.gotoCenter(BasePage.toInt(building.get(6)), BasePage.toInt(building.get(7)));
					});

					JPanel temp = new JPanel(new GridLayout(0, 1));
					temp.add(lbl);
					temp.add(evtlbl);

					JOptionPane.showMessageDialog(this, temp, "확인", JOptionPane.INFORMATION_MESSAGE);
					dispose();
				}
			}));
		}

		for (var i : jc) {
			if (i.length > 1) {
				var temp1 = new JPanel(new BorderLayout(5, 5));
				temp1.add(i[0]);
				temp1.add(i[1], "East");
				reserv.add(temp1);
			} else
				for (var j : i)
					reserv.add(j);
		}

		txt[0].setText(BasePage.getRow("select name from user where no = ?", BasePage.uno).get(0) + "");
		txt[1].setText(BasePage.getRow("select phone from user where no = ?", BasePage.uno).get(0) + "");

		txt[2].addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == 3) {
				}
			}
		});

		txt[0].setRequestFocusEnabled(false);
		txt[1].setRequestFocusEnabled(false);
		txt[2].setRequestFocusEnabled(false);

		var start = LocalTime.parse(building.get(2).toString(), DateTimeFormatter.ofPattern("HH:mm:ss"));
		var end = LocalTime.parse(building.get(3).toString(), DateTimeFormatter.ofPattern("HH:mm:ss"));

		for (var t = start; t.isBefore(end); t = t.plusMinutes(30))
			combo[1].addItem(t.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

		info_ui();

		((JPanel) getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
	}

	void info_ui() {
		info.removeAll();
		var rate = BasePage.getRow("select * from rate where user = ? and building = ?", BasePage.uno, building.get(0));
		info.add(BasePage.lbl(building.get(1) + "", JLabel.CENTER, 20), "North");
		var c = new JPanel();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(BasePage.lbl("사진", JLabel.LEFT, 20));
		c.add(new JLabel(BasePage.toIcon(building.get(8), 350, 180)));
		c.add(BasePage.lbl("정보", JLabel.LEFT, 20));
		c.add(BasePage.sz(BasePage.lbl("<html>" + building.get(4), JLabel.LEFT), 350, 50));
		var stars = new JLabel[5];
		var ratelbl = BasePage.lbl((rate == null ? "0" : rate.get(2) + "") + "/5", JLabel.CENTER, 20);

		c.add(BasePage.lbl(rate == null ? "후기 작성" : "후기 수정", JLabel.LEFT, 20));
		var temp = new JPanel();
		temp.setLayout(new FlowLayout(FlowLayout.LEFT));
		temp.setAlignmentX(LEFT_ALIGNMENT);

		for (int i = 0; i < 5; i++) {
			final int j = i;
			temp.add(stars[i] = BasePage.lbl("★", JLabel.LEFT, 20, (e) -> {
				if (e.getButton() == 1) {
					for (var s : stars)
						s.setForeground(Color.LIGHT_GRAY);
					for (int k = 0; k <= j; k++)
						stars[k].setForeground(Color.RED);
					ratelbl.setText(j + 1 + "/5");
				}
			}));
			stars[i].setForeground(Color.LIGHT_GRAY);
		}
		temp.add(ratelbl);
		c.add(BasePage.sz(temp, 350, 50));

		var area = new JTextArea(2, 5);
		area.setBorder(new LineBorder(Color.BLACK));

		if (rate != null) {
			for (int i = 0; i < BasePage.toInt(rate.get(2)); i++)
				stars[i].setForeground(Color.RED);
			area.setText(rate.get(4) + "");
		}
		area.setLineWrap(true);
		area.setAlignmentX(LEFT_ALIGNMENT);
		c.add(BasePage.sz(area, 350, 60));

		var temp2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		temp2.setAlignmentX(LEFT_ALIGNMENT);

		temp2.add(BasePage.btn(rate == null ? "후기 작성하기" : "후기 수정하기", a -> {
			if (BasePage.toInt(ratelbl.getText().split("/")[0]) == 0) {
				BasePage.eMsg("평점을 선택해주세요.");
				return;
			}

			if (a.getActionCommand().equals("후기 작성하기")) {
				BasePage.setRows("insert rate values(0, ?, ?, ?, ?)", building.get(0), ratelbl.getText().split("/")[0],
						BasePage.uno, area.getText());
				BasePage.iMsg("작성이 완료되었습니다.");
			} else {
				BasePage.setRows("update rate set rate = ?, review = ?, where no = ?", ratelbl.getText().split("/")[0],
						area.getText(), building.get(0));
				BasePage.iMsg("수정이 완료되었습니다.");
			}
			if (!page.search.getText().isEmpty())
				page.search();

			info_ui();

		}));

		c.add(temp2);

		if (rate != null) {
			temp2.add(BasePage.btn("삭제", a -> {
				BasePage.setRows("delete from rate where no = ?", rate.get(0));

				if (!page.search.getText().isEmpty())
					page.search();
				info_ui();
			}));
		}

		var rs = BasePage.getRows("select r.*, u.name from rate r, user u where r.building = ? and r.user = u.no",
				building.get(0));
		c.add(BasePage.lbl("<html>전체<font color = '#007BFF'>" + rs.size(), JLabel.LEFT, 15));
		c.add(BasePage.lbl("평점 " + building.get(9), JLabel.LEFT, 20));

		for (var r : rs) {
			var temp3 = new JPanel(new BorderLayout());
			var tempn = new JPanel(new FlowLayout(FlowLayout.LEFT));
			var tempc = new JPanel(new FlowLayout(FlowLayout.LEFT));
			var temps = new JPanel(new FlowLayout(FlowLayout.LEFT));

			temp3.add(tempn, "North");
			temp3.add(tempc);
			temp3.add(temps, "South");

			for (int i = 0; i < 5; i++) {
				var lbl = new JLabel("★", JLabel.CENTER);
				lbl.setForeground(i < BasePage.toInt(r.get(2)) ? Color.RED : Color.lightGray);
				tempn.add(lbl);
			}

			temp3.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
			tempc.add(BasePage.lbl(r.get(4) + "", JLabel.LEFT));
			temps.add(BasePage.lbl(r.get(5) + "", JLabel.CENTER));
			temp3.setAlignmentX(LEFT_ALIGNMENT);
			c.add(temp3);
		}

		info.add(new JScrollPane(c));
		c.setBorder(new EmptyBorder(5, 10, 5, 5));
		revalidate();
		repaint();

	}
}
