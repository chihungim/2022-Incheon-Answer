package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;

public class AdminPage extends BasePage {

	public AdminPage() {
		JPanel w, c;
		setLayout(new BorderLayout(10, 10));
		add(sz(w = new JPanel(new FlowLayout(0)), 200, 240), "West");
		add(c = new JPanel(new BorderLayout()));

		try {
			c.add(new User());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (var cap : "<html>&#128100 회원관리,<html>&#127968 건물관리,<html>&#128200 통계,<html>&#128275 로그아웃".split(",")) {
			var lbl = sz(hyplbl(cap, 2, 20, (e) -> {
				c.removeAll();

				var myself = (JLabel) e.getSource();
				for (var comp : w.getComponents())
					((JComponent) comp).setBorder(null);

				myself.setBorder(
						new CompoundBorder(new MatteBorder(0, 3, 0, 0, Color.ORANGE), new EmptyBorder(0, 5, 0, 0)));
				if (cap.contains("회원관리")) {
					try {
						c.add(new User());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else if (cap.contains("건물관리")) {
					c.add(new Building());
				} else if (cap.contains("통계")) {
					c.add(new Chart());
				} else {
					mf.swapPage(new LoginPage());
				}

				repaint();
				revalidate();
			}), 200, 30);
			w.setBackground(new Color(0, 123, 255));
			lbl.setFont(new Font("맑은 고딕", 0, 20));
			w.add(lbl);
			((JComponent) w.getComponent(0)).setBorder(
					new CompoundBorder(new MatteBorder(0, 3, 0, 0, Color.ORANGE), new EmptyBorder(0, 5, 0, 0)));
		}

	}

	public static void main(String[] args) {
		mf.swapPage(new AdminPage());
	}

	class Building extends JPanel {

		JPanel s;

		DefaultTableModel m = new DefaultTableModel(null, "이름,종류,설명,시작시간,종료시간,사진,no".split(",")) {
			public java.lang.Class<?> getColumnClass(int columnIndex) {
				return JComponent.class;
			};

			public boolean isCellEditable(int row, int column) {
				return column != 1;
			};
		};
		DefaultTableCellRenderer d = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				if (value instanceof JComponent) {
					return (JComponent) value;
				} else {
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			};
		};
		JTable t = new JTable(m);

		class CustomJLabel extends JLabel {
			FileInputStream stream;
			public CustomJLabel(ImageIcon icon) {
				super(icon);
			}
		}

		public Building() {
			super(new BorderLayout());
			add(new JScrollPane(t));
			add(s = new JPanel(new FlowLayout(4)), "South");

			s.add(btn("저장", a -> {

				for (int i = 0; i < t.getRowCount(); i++) {
					var icon = ((JLabel) t.getValueAt(i, 5)).getIcon();
					var buff = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
					icon.paintIcon(null, buff.getGraphics(), 0, 0);
					var byteArr = new ByteArrayOutputStream();
					try {
						ImageIO.write(buff, "jpg", byteArr);
						setRows("update building set name= ?, info= ?, open= ?, close= ?, img = ? where no = ?",
								t.getValueAt(i, 0), t.getValueAt(i, 2), t.getValueAt(i, 3), t.getValueAt(i, 4),
								new ByteArrayInputStream(byteArr.toByteArray()), t.getValueAt(i, 6));
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}

			}));

			d.setHorizontalAlignment(0);
			t.setSelectionMode(0);

			t.setRowHeight(80);
			t.setDefaultRenderer(JComponent.class, d);

			var col = "no,사진".split(",");
			var width = new int[] { 0, 120 };
			for (int i = 0; i < width.length; i++) {
				t.getColumn(col[i]).setMinWidth(width[i]);
				t.getColumn(col[i]).setMaxWidth(width[i]);
			}

			col = "이름,설명".split(",");
			for (int j = 0; j < width.length; j++) {
				t.getColumn(col[j]).setCellEditor(new DefaultCellEditor(new JTextField()));
			}

			col = "시작시간,종료시간".split(",");
			for (int k = 0; k < width.length; k++) {
				t.getColumn(col[k]).setCellEditor(new Spin(k + 3));
			}

			for (var rs : getRows(
					"select name, type, info, time_format(open, '%H:%i'), time_format(close,'%H:%i'), img, no from building where type < 3")) {
				rs.set(1, "진료소,병원,거주지".split(",")[toInt(rs.get(1))]);
				rs.set(5, new JLabel(new ImageIcon(
						Toolkit.getDefaultToolkit().createImage((byte[]) rs.get(5)).getScaledInstance(120, 80, 4))));
				m.addRow(rs.toArray());
			}

			t.addMouseListener(new MouseAdapter() {	
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() != 1 || t.getSelectedRow() == -1 || t.getSelectedColumn() != 5) {
						return;
					}

					var jfc = new JFileChooser("./datafiles/건물사진");
					jfc.resetChoosableFileFilters();
					jfc.addChoosableFileFilter(new FileFilter() {

						@Override
						public String getDescription() {
							return "JPG files";
						}

						@Override
						public boolean accept(File f) {
							return f.getName().endsWith("jpg") || f.isDirectory();
						}
					});

					if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						var file = jfc.getSelectedFile();
						t.setValueAt(new JLabel(new ImageIcon(Toolkit.getDefaultToolkit()
								.getImage(file.getAbsolutePath()).getScaledInstance(120, 80, 4))), t.getSelectedRow(),
								5);
					}
				}
			});

			setBorder(new EmptyBorder(5, 5, 5, 5));
		}

		class Spin extends DefaultCellEditor {
			LocalTime date = LocalTime.of(4, 30);
			String[] open_times, close_times;
			JSpinner spinner;
			JSpinner.DefaultEditor editor;

			public Spin(int column) {
				super(new JTextField());
				open_times = Stream.generate(() -> {
					date = date.plusMinutes(30);
					return date + "";
				}).limit(10).toArray(String[]::new);
				date = LocalTime.of(18, 30);
				close_times = Stream.generate(() -> {
					date = date.plusMinutes(30);
					return date + "";
				}).limit(10).toArray(String[]::new);
				spinner = new JSpinner(new SpinnerListModel(column == 3 ? open_times : close_times));
			}

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
					int column) {
				if (table.getValueAt(row, 1).toString().equals("거주지")) {
					eMsg("거주지는 수정이 불가합니다.");
					return null;
				}

				spinner.setValue(t.getValueAt(row, column));
				return spinner;
			}

			@Override
			public Object getCellEditorValue() {
				return spinner.getValue();
			}
		}

	}

	class User extends JPanel {
		DefaultTableModel m = new DefaultTableModel(null, "번호,이름,아이디,비밀번호,전화번호,생일,거주지".split(",")) {
			public boolean isCellEditable(int row, int column) {
				return column != 0 && column != 2;
			};
		};
		JTable table = table(m);
		JTextField txt;

		class Item {
			String key;
			String value;

			public Item(String key, String value) {
				this.key = key;
				this.value = value;
			}

			@Override
			public String toString() {
				return value;
			}
		}

		JComboBox<Item> editCombo;

		MaskFormatter mask1 = new MaskFormatter("###-####-####");
		MaskFormatter mask2 = new MaskFormatter("####-##-##");

		{
			mask1.setPlaceholderCharacter('_');
			mask2.setPlaceholderCharacter('_');
		}
		JFormattedTextField editField = new JFormattedTextField(mask1);
		JFormattedTextField editField2 = new JFormattedTextField(mask2);

		public User() throws Exception {
			setLayout(new BorderLayout(5, 5));
			add(new JScrollPane(table));
			add(s = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5)), "South");
			addRow(m, getRows(
					"select u.no, u.name,u.id,u.pw, u.phone,u.birth,b.name from user u, building b where u.building = b.no order by u.no"));
			s.add(btn("수정", a -> {
				for (int i = 0; i < table.getColumnCount(); i++) {
					var name = table.getValueAt(i, 1).toString();
					var id = table.getValueAt(i, 2).toString();
					var pw = table.getValueAt(i, 3).toString();
					var phone = table.getValueAt(i, 4).toString();
					var birth = table.getValueAt(i, 5).toString();

					if (name.isEmpty() || id.isEmpty() || pw.isEmpty()
							|| phone.replace("-", "").replace("_", "").trim().isEmpty()
							|| birth.replace("-", "").replace("_", "").trim().isEmpty()) {
						eMsg("빈칸이 존재합니다.");
						return;
					}

					var simple = new SimpleDateFormat("yyyy-MM-dd");
					simple.setLenient(false);

					try {
						simple.parse(birth);
					} catch (ParseException e1) {
						eMsg("생년월일 포맷이 잘못되었습니다.");
						return;
					}
				}

				for (int i = 0; i < table.getColumnCount(); i++) {
					var name = table.getValueAt(i, 1).toString();
					var id = table.getValueAt(i, 2).toString();
					var pw = table.getValueAt(i, 3).toString();
					var phone = table.getValueAt(i, 4).toString();
					var building = getRow("select no from building where name = ?", table.getValueAt(i, 6).toString())
							.get(0);
					var birth = table.getValueAt(i, 5).toString();
					setRows("update user set name = ?, id = ?, pw = ?, phone = ?, building =?, birth = ? where no = ?",
							name, id, pw, phone, building, birth, table.getValueAt(i, 0) + "");
				}

				iMsg("수정이 완료되었습니다.");

				addRow(m, getRows(
						"select u.no, u.name,u.id,u.pw, u.phone,u.birth,b.name from user u, building b where u.building = b.no order by u.no"));
			}));

			s.add(btn("삭제", a -> {
				if (table.getSelectedRow() == -1) {
					eMsg("삭제할 행을 선택해주세요.");
					return;
				}
				setRows("delete from user where no = ?", table.getValueAt(table.getSelectedRow(), 0));
				addRow(m, getRows(
						"select u.no, u.name,u.id,u.pw, u.phone,u.birth,b.name from user u, building b where u.building = b.no order by u.no"));
			}));

			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (table.getSelectedRow() == -1)
						return;
					if (table.getSelectedColumn() == 4)
						editField.setText(table.getValueAt(table.getSelectedRow(), 4).toString());

					if (table.getSelectedColumn() == 5)
						editField2.setText(table.getValueAt(table.getSelectedRow(), 5).toString());

					if (table.getSelectedColumn() == 6) {
						for (int i = 0; i < editCombo.getItemCount(); i++) {
							if (editCombo.getItemAt(i).value.equals(table.getValueAt(table.getSelectedRow(), 6)))
								editCombo.setSelectedIndex(i);
						}
					}
				}
			});
			table.setRowHeight(30);
			editCombo = new JComboBox<Item>(getRows("select no, name from building where type = 2").stream()
					.map(a -> new Item(a.get(0) + "", a.get(1) + "")).toArray(Item[]::new));
			table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(editCombo));
			table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(editField));
			table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(editField2));

			setBorder(new EmptyBorder(10, 10, 10, 10));

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
