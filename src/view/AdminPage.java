package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.geom.Arc2D;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AdminPage extends BasePage {
	public AdminPage() {
		setLayout(new BorderLayout());
		add(w = new JPanel(new FlowLayout(0)), "West");
		add(c = new JPanel(new BorderLayout()));

		try {
			c.add(new User());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (var str : "<html>&#128100 회원관리,<html>&#127968 건물관리,<html>&#128200 통계,<html>&#128275 로그아웃".split(",")) {
			var hyplbl = sz(hyplbl(str, 2, 20, (e) -> {
				c.removeAll();
				var me = (JLabel) e.getSource();

				for (var com : w.getComponents())
					((JComponent) com).setBorder(null);

				me.setBorder(
						new CompoundBorder(new MatteBorder(0, 3, 0, 0, Color.ORANGE), new EmptyBorder(0, 5, 0, 0)));

				c.removeAll();
				c.setLayout(new BorderLayout());
				if (str.contains("회원관리")) {
					try {
						c.add(new User());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else if (str.contains("건물관리"))
					c.add(new Building());
				else if (str.contains("통계"))
					c.add(new Chart());
				else
					mf.swapPage(new LoginPage());

				repaint();
				revalidate();

			}), 200, 30);
			hyplbl.setFont(new Font("맑은 고딕", 0, 20)); // font의 bold가 존나 먹어서 그런듯
			w.add(hyplbl);
		}

		((JComponent) w.getComponent(0))
				.setBorder(new CompoundBorder(new MatteBorder(0, 3, 0, 0, Color.ORANGE), new EmptyBorder(0, 5, 0, 0)));

		sz(w, 200, 240);
		w.setBackground(blue);
	}

	public static void main(String[] args) {
		mf.swapPage(new AdminPage());
	}

	class User extends JPanel {

		JComboBox<Item> combo;
		DefaultTableModel m = new DefaultTableModel(null, "번호,이름,아이디,비밀번호,전화번호,생일,거주지".split(",")) {
			public boolean isCellEditable(int row, int column) {
				return column != 0 && column != 2;
			};
		};

		JTable t = table(m);
		JTextField txt;

		JComboBox<Item> editCombo;

		public User() throws Exception {
			setLayout(new BorderLayout(5, 5));
			add(new JScrollPane(t));
			add(s = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5)), "South");

			data();

			s.add(btn("수정", a -> {
				for (int i = 0; i < t.getColumnCount(); i++) {
					setRows("update user set name= ?, pw = ?, phone = ?, building = ?, birth = ? where no = ?",
							t.getValueAt(i, 1), t.getValueAt(i, 3), t.getValueAt(i, 4), ((Item) t.getValueAt(i, 6)).no,
							t.getValueAt(i, 5), t.getValueAt(i, 0));

					data();
				}
				iMsg("수정이 완료되었습니다.");
			}));

			s.add(btn("삭제", a -> {
				if (t.getSelectedRow() == -1) {
					eMsg("삭제할 행을 선택해주세요.");
					return;
				}

				setRows("delete from user where no = ?", t.getValueAt(t.getSelectedRow(), 0));
				data();
			}));
			editCombo = new JComboBox<Item>(getRows("SELECT no, name FROM covid.building where type = 2;").stream()
					.map(a -> new Item(a.get(0) + "", a.get(1) + "")).toArray(Item[]::new));

			t.addMouseListener(new MouseAdapter() {
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (t.getSelectedRow() == -1)
						return;
					if (t.getSelectedColumn() == 6) {
						for (int i = 0; i < editCombo.getItemCount(); i++) {
							if (editCombo.getItemAt(i).no.equals(t.getValueAt(t.getSelectedRow(), 6)))
								editCombo.setSelectedIndex(i);
						}
					}

				};
			});

			t.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(editCombo));
			t.setRowHeight(30);
			setBorder(new EmptyBorder(10, 10, 10, 10));
		}

		void data() {
			var rs = getRows(
					"select u.no, u.name, id, pw, phone, birth, b.no,b.name  from building b , user u where b.no = u.building");
			for (var r : rs) {
				r.set(6, new Item(r.get(6) + "", r.get(7) + ""));
			}
			addRow(m, rs);
		}
	}

	class Building extends JPanel {

		JPanel s;

		DefaultTableModel m = new DefaultTableModel(null, "이름,종류,설명,시작시간,종료시간,사진,no".split(",")) {
			public boolean isCellEditable(int row, int column) {
				return column != 1;
			};
		};

		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				if (value instanceof JComponent) {
					return (JComponent) value;
				} else {
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			};
		};

		JTable t = table(m);

		public Building() {
			setLayout(new BorderLayout());
			add(new JScrollPane(t));
			add(s = new JPanel(new FlowLayout(4)), "South");

			s.add(btn("저장", a -> {
				for (int i = 0; i < t.getRowCount(); i++) {
					setRows("update building set name= ?, info= ?, open= ?, close= ?,  where no = ?",
							t.getValueAt(i, 0), t.getValueAt(i, 2), t.getValueAt(i, 3), t.getValueAt(i, 4),
							t.getValueAt(i, 6));
				}
				iMsg("수정이 완료되었습니다.");

			}));

			dtcr.setHorizontalAlignment(0);
			t.setSelectionMode(0);

			t.setRowHeight(80);
			t.setDefaultRenderer(JComponent.class, dtcr);

			t.getColumn("no").setMinWidth(0);
			t.getColumn("no").setMaxWidth(0);
			t.getColumn("사진").setMinWidth(120);
			t.getColumn("사진").setMaxWidth(120);

			data();

			setBorder(new EmptyBorder(5, 5, 5, 5));
		}

		void data() {
			for (var rs : getRows(
					"select name, type, info, time_format(open, '%H:%i'), time_format(close,'%H:%i'), img, no from building where type <> 3")) {
				rs.set(1, "진료소,병원,거주지".split(",")[toInt(rs.get(1))]);
				rs.set(5, new JLabel(toIcon(rs.get(5), 120, 80)));
				m.addRow(rs.toArray());
			}
		}
	}

	public class Chart extends JPanel {
		JPanel n, c;
		JPanel chart;
		JLabel title;
		JComboBox<String> com;
		Color col[] = { Color.red, Color.ORANGE, Color.yellow, Color.green, Color.BLUE };
		double arc = 90;

		public Chart() {
			super(new BorderLayout());
			add(n = new JPanel(new BorderLayout()), "North");
			c = new JPanel();
			setChart("select v.name, count(*) from purchase p, vaccine v where p.vaccine=v.no group by v.no",
					toInt(getRow("select count(*) from purchase p, vaccine v where p.vaccine=v.no").get(0)));

			n.add(title = new JLabel());
			n.add(com = new JComboBox<String>("상위 백신,상위 병원,상위 진료소".split(",")), "East");

			com.addActionListener(e -> {
				remove(c);

				if (com.getSelectedIndex() == 0) {
					setChart("select v.name, count(*) from purchase p, vaccine v where p.vaccine=v.no group by v.no",
							toInt(getRow("select count(*) from purchase p, vaccine v where p.vaccine=v.no").get(0)));
				} else if (com.getSelectedIndex() == 1) {
					int sum = 0;
					String sql = "select b.name, count(*) from building b, purchase p where p.building = b.no and type=1 group by b.no order by count(*) desc limit 5";
					for (var r : getRows(sql)) {
						sum += toInt(r.get(1));
					}
					setChart(sql, sum);
				} else if (com.getSelectedIndex() == 2) {
					int sum = 0;
					String sql = "select b.name, count(*) from building b, purchase p where p.building = b.no and type=0 group by b.no order by count(*) desc limit 5";
					for (var r : getRows(sql)) {
						sum += toInt(r.get(1));
					}
					setChart(sql, sum);
				}
			});

			setBorder(new EmptyBorder(10, 10, 10, 10));
		}

		public void setChart(String sql, double sum) {
			add(c = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					int height = 250;
					arc = 90;
					Graphics2D g2d = (Graphics2D) g;
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					var rs = getRows(sql);
					for (var r : rs) {
						var a = ((double) toInt(r.get(1)) / (double) sum * 360) * -1;

						g2d.setColor(col[rs.indexOf(r)]);
						var arc2d = new Arc2D.Float(Arc2D.PIE);
						arc2d.setFrame(150, 100, 300, 300);
						arc2d.setAngleStart(arc);
						arc2d.setAngleExtent(a);
						int midx = (int) (arc2d.getEndPoint().getX() + arc2d.getStartPoint().getX()) / 2;
						int midy = (int) (arc2d.getEndPoint().getY() + arc2d.getStartPoint().getY()) / 2;
						g2d.draw(arc2d);
						g2d.fill(arc2d);
						g2d.fillOval(600, height - 15, 20, 20);
						g2d.setColor(Color.BLACK);
						g2d.drawString(r.get(0).toString(), 625, height);
						g2d.drawString(String.format("%.1f", ((double) toInt(r.get(1)) / (double) sum) * 100) + "%",
								midx, midy);
						arc += a;
						height += 25;
					}
				}
			});

			repaint();
			revalidate();
		}
	}
}
