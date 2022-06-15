package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.stream.Stream;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

public class DatePicker extends JPopupMenu {

	JPanel n, c;
	JPanel ne;
	JTextField txt;
	JLabel date, prev, next;
	JButton days[] = new JButton[42];

	LocalDate today = LocalDate.now();
	int year, month;

	boolean flag;

	public DatePicker(JTextField txt, boolean flag) {
		this.flag = flag;
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(txt.getWidth(), 200));

		add(n = new JPanel(new BorderLayout()), "North");
		add(c = new JPanel(new GridLayout(0, 7)));

		n.add(date = BasePage.lbl("yyyy년 mm월", 2, 13), "West");
		n.add(ne = new JPanel(new FlowLayout()), "East");

		ne.add(prev = BasePage.lbl("<", 0));
		ne.add(next = BasePage.lbl(">", 0));

		var m = "일,월,화,수,목,금,토".split(",");

		for (int i = 0; i < m.length; i++) {
			var lbl = BasePage.lbl(m[i], 0);
			c.add(lbl);
			if (i % 7 == 6) {
				lbl.setForeground(Color.BLUE);
			} else if (i % 7 == 0) {
				lbl.setForeground(Color.RED);
			}
		}

		for (int i = 0; i < days.length; i++) {
			c.add(days[i] = BasePage.btn(i + "", a -> {
				txt.setText(((JButton) a.getSource()).getName());
				setVisible(false);
			}));
			days[i].setBorder(null);
			days[i].setBackground(Color.WHITE);
			days[i].setForeground(Color.BLACK);
			if (i % 7 == 6)
				days[i].setForeground(Color.BLUE);
			else if (i % 7 == 0)
				days[i].setForeground(Color.RED);


		}

		Stream.of(prev, next).forEach(a -> {
			a.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getSource() == prev) {
						today = today.plusMonths(-1);
						setCal();
					} else if (e.getSource() == next) {
						today = today.plusMonths(1);
						setCal();
					}
				}

			});
		});

		setCal();
	}

	private void setCal() {
		year = today.getYear();
		month = today.getMonthValue();
		date.setText(year + "년 " + month + "월");
		var sdate = LocalDate.of(year, month, 1);
		int sday = sdate.getDayOfWeek().getValue() % 7;

		for (int i = 0; i < days.length; i++) {
			var tmp = sdate.plusDays(i - sday);
			if (flag)
				days[i].setEnabled(tmp.isAfter(LocalDate.now()));
			else
				days[i].setEnabled(tmp.isBefore(LocalDate.now()));
			days[i].setName(tmp + "");
			days[i].setVisible(tmp.getMonthValue() == today.getMonthValue());
			days[i].setText(tmp.getDayOfMonth() + "");
		}
	}
}
