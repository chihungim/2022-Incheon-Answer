package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import db.DB;

public class BasePage extends JPanel {

	static Connection con = DB.con;
	static Statement stmt = DB.stmt;

	JPanel n, c, e, w, s;
	JPanel nn, nc, ne, nw, ns;
	JPanel cn, cc, ce, cw, cs;
	JPanel wn, wc, we, ww, ws;
	JPanel en, ec, ee, ew, es;
	JPanel sn, sc, se, sw, ss;
	public static Color blue = new Color(0, 123, 255);
	public static MainFrame mf = new MainFrame();

	public static String uno;
	static String vaccine[] = { null, "아스트라제니카", "얀센", "화이자", "모더나" };

	static {
		try {
			stmt.execute("use covid");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ArrayList<ArrayList<Object>> getRows(String sql, Object... objs) {
		var col = new ArrayList<ArrayList<Object>>();

		try {
			var pst = con.prepareStatement(sql);
			if (objs != null) {
				for (int i = 0; i < objs.length; i++) {
					pst.setObject(i + 1, objs[i]);
				}
			}

			var rs = pst.executeQuery();
			var m = rs.getMetaData();
			while (rs.next()) {
				var rows = new ArrayList<Object>();
				for (int i = 0; i < m.getColumnCount(); i++) {
					rows.add(rs.getObject(i + 1));
				}
				col.add(rows);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return col;
	}

	public static ArrayList<Object> getRow(String sql, Object... objs) {
		if (getRows(sql, objs).isEmpty()) {
			return null;
		} else
			return getRows(sql, objs).get(0);
	}

	public static void execute(String sql, Object... objs) {
		var col = new ArrayList<ArrayList<Object>>();

		try {
			var pst = con.prepareStatement(sql);
			if (objs != null) {
				for (int i = 0; i < objs.length; i++) {
					pst.setObject(i + 1, objs[i]);
				}
			}

			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public BasePage() {
		super(new BorderLayout());
	}

	public static void emsg(String msg) {
		JOptionPane.showMessageDialog(null, msg, "경고", 0);
	}

	public static void imsg(String msg) {
		JOptionPane.showMessageDialog(null, msg, "정보", 1);
	}

	public static JLabel lbl(String t, int al) {
		JLabel l = new JLabel(t, al);
		l.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		return l;
	}

	public static JLabel lbl(String t, int al, int s) {
		JLabel l = new JLabel(t, al);
		l.setFont(new Font("맑은 고딕", Font.BOLD, s));
		return l;
	}

	public static JLabel lbl(String t, int al, int s, Consumer<MouseEvent> con) {
		JLabel l = new JLabel(t, al);
		l.setFont(new Font("맑은 고딕", Font.BOLD, s));
		l.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				con.accept(e);
			}
		});
		return l;
	}

	public static JLabel hyplbl(String text, int al, int s, Consumer<MouseEvent> con) {
		var l = new JLabel(text, al);
		l.setFont(new Font("맑은 고딕", Font.BOLD, s));
		l.setForeground(Color.ORANGE);
		l.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == 1)
					con.accept(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				l.setCursor(new Cursor(Cursor.HAND_CURSOR));
				l.setText("<html><u>" + text);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				l.setText(text);
			}

		});
		return l;
	}

	public static JButton btn(String t, ActionListener a) {
		var b = new JButton(t);
		b.addActionListener(a);
		b.setOpaque(true);
		b.setBackground(Color.ORANGE);
		b.setForeground(Color.WHITE);
		return b;
	}

	public static DefaultTableModel model(String[] col) {
		var m = new DefaultTableModel(null, col) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		return m;
	}

	public static ImageIcon toIcon(Object obj, int w, int h) {
		return new ImageIcon(
				Toolkit.getDefaultToolkit().createImage((byte[]) obj).getScaledInstance(w, h, Image.SCALE_SMOOTH));
	}

	public static ImageIcon getIcon(String path, int w, int h) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(path).getScaledInstance(w, h, Image.SCALE_SMOOTH));
	}

	public static ImageIcon getIcon(String path) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(path));
	}

	void addRow(DefaultTableModel m, ArrayList<ArrayList<Object>> rows) {
		m.setRowCount(0);
		for (var r : rows)
			m.addRow(r.toArray());

	}

	public JTable table(DefaultTableModel m) {
		var t = new JTable(m);
		var dtcr = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				if (value instanceof JComponent)
					return (JComponent) value;
				else
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		};
		t.getTableHeader().setReorderingAllowed(false);
		t.getTableHeader().setResizingAllowed(false);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		dtcr.setHorizontalAlignment(SwingConstants.CENTER);

		for (int i = 0; i < t.getColumnCount(); i++) {
			t.getColumnModel().getColumn(i).setCellRenderer(dtcr);
		}

		return t;
	}

	public static JComponent sz(JComponent jc, int w, int h) {
		jc.setPreferredSize(new Dimension(w, h));
		return jc;
	}

	public static int cint(Object objs) {
		return Integer.parseInt(objs.toString());
	}
}
