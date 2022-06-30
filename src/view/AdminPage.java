package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

public class AdminPage extends BasePage {

	String cap[] = "&#128100회원관리,&#127968건물관리,&#128200통계,&#128275로그아웃".split(",");

	public AdminPage() {
		setLayout(new BorderLayout());
		add(sz(w = new JPanel(), 250, 0), "West");
		w.setLayout(new BoxLayout(w, BoxLayout.Y_AXIS));
		add(c = new JPanel(new BorderLayout()));

		c.add(new User());

		for (var c : cap) {
			var hyplbl = hyplbl("<html>" + c, JLabel.LEFT, 20, a -> {
				var me = (JLabel) a.getSource();

				for (var jc : w.getComponents())
					((JComponent) jc).setBorder(null);

				me.setBorder(
						new CompoundBorder(new MatteBorder(0, 3, 0, 0, Color.orange), new EmptyBorder(0, 5, 0, 0)));
				this.c.removeAll();
				this.c.setLayout(new BorderLayout());
				if (me.getText().equals("<html><u><html>" + cap[0])) {
					this.c.add(new User());
				} else if (me.getText().equals("<html><u><html>" + cap[1])) {
					this.c.add(new Building());
				} else if (me.getText().equals("<html><u><html>" + cap[2])) {
					this.c.add(new Chart());
				} else {
					mf.swapPage(new LoginPage());
				}

			});

			w.add(hyplbl);
			w.add(Box.createVerticalStrut(10));
			hyplbl.setFont(new Font("맑은 고딕", 0, 20));
		}

		((JComponent) w.getComponent(0))
				.setBorder(new CompoundBorder(new MatteBorder(0, 3, 0, 0, Color.orange), new EmptyBorder(0, 5, 0, 0)));
		w.setBorder(new EmptyBorder(5, 5, 5, 5));
		w.setBackground(blue);
	}

	class User extends JPanel {
		DefaultTableModel m = new DefaultTableModel(null, "번호,이름,아이디,비밀번호,전화번호,생일,거주지".split(",")) {
			public boolean isCellEditable(int row, int column) {
				return column != 0 && column != 2;
			};
		};
		JTable t = table(m);

		JComboBox<Item> editcombo = new JComboBox<Item>(getRows("select no, name from building where type =2").stream()
				.map(a -> new Item(a.get(0).toString(), a.get(1).toString())).toArray(Item[]::new));

		public User() {
			super(new BorderLayout(5, 5));
			var s = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
			add(new JScrollPane(t));
			add(s, "South");
			for (var bcap : "수정,삭제".split(",")) {
				s.add(btn(bcap, a -> {
					if (a.getActionCommand().equals("수정")) {
						for (int i = 0; i < t.getColumnCount(); i++) {
							setRows("update user set name = ? , pw = ?, phone = ?, birth = ?, building =? where no =?",
									t.getValueAt(i, 1), t.getValueAt(i, 3), t.getValueAt(i, 4), t.getValueAt(i, 5),
									((Item) t.getValueAt(i, 6)).getKey(), t.getValueAt(i, 0));
						}

						iMsg("수정이 완료되었습니다.");
					} else {
						if (t.getSelectedRow() == -1) {
							eMsg("삭제할 행을 선택해주세요.");
							return;
						}

						setRows("delete from user where no = ?", t.getValueAt(t.getSelectedRow(), 0));
						iMsg("삭제가 완료되었습니다.");
						data();
					}
				}));
			}
			t.setRowHeight(30);

			t.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(editcombo));

			t.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (t.getSelectedRow() == -1)
						return;

					if (t.getSelectedColumn() == 6) {
						for (int i = 0; i < editcombo.getItemCount(); i++)
							if (editcombo.getItemAt(i).getKey().equals(t.getValueAt(t.getSelectedRow(), 6)))
								editcombo.setSelectedIndex(i);
					}
				}
			});

			data();
			setBorder(new EmptyBorder(10, 10, 10, 10));
		}

		void data() {
			var rs = getRows(
					"select u.no, u.name, u.id, u.pw, u.phone,u.birth, b.no, b.name from user u, building b where u.building = b.no order by u.no");

			for (var r : rs) {
				r.set(6, new Item(r.get(6).toString(), r.get(7).toString()));
				r.remove(7);
			}

			addRow(m, rs);
		}

	}

	class Building extends JPanel {
		DefaultTableModel m = new DefaultTableModel(null, "이름,종류,설명,시작시간,종료시간,사진,번호".split(",")) {
			public boolean isCellEditable(int row, int column) {
				return column != 1 && column != 5;
			};
		};
		JTable t = table(m);

		public Building() {
			super(new BorderLayout(5, 5));
			var s = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
			add(new JScrollPane(t));
			add(s, "South");
			s.add(btn("저장", a -> {
				for (int i = 0; i < t.getColumnCount(); i++) {
					setRows("update building set name = ?, info = ?, open =?, close = ? where no = ?",
							t.getValueAt(i, 0), t.getValueAt(i, 2), t.getValueAt(i, 3), t.getValueAt(i, 4),
							t.getValueAt(i, 6));
				}

				iMsg("저장이 완료되었습니다.");
			}));
			t.setRowHeight(80);
			t.getColumn("번호").setMinWidth(0);
			t.getColumn("번호").setMaxWidth(0);
			data();
			setBorder(new EmptyBorder(10, 10, 10, 10));
		}

		void data() {
			var rs = getRows("select name, type, info, open, close, img,no from building where type <> 3");

			for (var r : rs) {
				r.set(1, "진료소,병원,거주지".split(",")[toInt(r.get(1))]);
				r.set(5, new JLabel(toIcon(r.get(5), 120, 80)));
			}

			addRow(m, rs);
		}
	}

	class Chart extends JPanel {
		String sql[] = {
				"select count(*), v.name from purchase p, vaccine v where p.vaccine = v.no and date <= now() group by vaccine",
				"select count(*), b.name from purchase p, building b where p.building = b.no and b.type = 1 and date <= now() group by p.building order by count(*) desc limit 5",
				"select count(*), b.name from purchase p, building b where p.building = b.no and b.type = 0 and date <= now() group by p.building order by count(*) desc limit 5" };

		JComboBox<String> combo = new JComboBox<String>("상위 백신 Top4,상위 병원 Top5,상위 진료소Top5".split(","));
		JPanel chart;

		Color[] col = { Color.red, Color.ORANGE, Color.YELLOW, Color.green, Color.blue };

		public Chart() {
			setLayout(new BorderLayout());
			var n = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			add(n, "North");
			n.add(combo);
			add(chart = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					var g2 = (Graphics2D) g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					var rs = getRows(sql[combo.getSelectedIndex()]);
					int sum = rs.stream().mapToInt(a -> toInt(a.get(0))).sum();
					double sarc = 90;
					int h = 250;
					for (int i = 0; i < rs.size(); i++) {
						var a2d = new Arc2D.Float(Arc2D.PIE);
						a2d.setFrame(150, 100, 300, 300);
						var arc = ((double) toInt(rs.get(i).get(0)) / sum) * 360 * -1;
						a2d.setAngleStart(sarc);
						a2d.setAngleExtent(arc);
						g2.setColor(col[i]);
						g2.draw(a2d);
						g2.fill(a2d);
						g2.fillOval(570, h, 20, 20);
						g2.setColor(Color.BLACK);
						g2.drawString(rs.get(i).get(1) + "", 600, h + 13);

						int midx = (int) (a2d.getEndPoint().getX() + a2d.getStartPoint().getX()) / 2;
						int midy = (int) (a2d.getEndPoint().getY() + a2d.getStartPoint().getY()) / 2;

						g2.drawString(String.format("%.1f", -(arc / 360) * 100) + "%", midx, midy);
						sarc += arc;
						h += 25;
					}

				}
			});

			combo.addItemListener(i -> chart.repaint());
		}

	}

	public static void main(String[] args) {
		mf.swapPage(new AdminPage());
	}
}
