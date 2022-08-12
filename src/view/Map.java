package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class Map extends BasePage {

	BufferedImage map;

	AffineTransform aff = new AffineTransform();

	Point2D epoint = new Point2D.Float(), spoint = new Point2D.Float();
	Point2D from, to;
	double zoom = 1;
	ArrayList<Object[]> objList;
	Object adj[][][] = new Object[346][346][2];
	final int INF = Integer.MAX_VALUE;
	ValueRange current;
	JTextField txt, start, end;

	ArrayList<Integer> path;
	JPanel p1, p2, p2c;
	JPopupMenu menu = new JPopupMenu();
	JScrollPane pane;
	ArrayList<Object> selected;

	public Map() {
		dataInit();
		mapInit();

		for (var bcap : "출발지,도착지".split(",")) {
			var item = new JMenuItem(bcap);
			item.addActionListener(a -> {
				if (a.getActionCommand().equals("출발지"))
					setPath(0);
				else
					setPath(1);
				dijkstra();
			});
			menu.add(item);
		}

		add(c = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				var g2 = (Graphics2D) g;
				g2.drawImage(map, aff, null);
			}
		});

		add(sz(w = new JPanel(new BorderLayout()), 280, 0), "West");

		var ma = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				from = e.getPoint();
				to = null;

				try {
					epoint = aff.inverseTransform(e.getPoint(), null);
				} catch (NoninvertibleTransformException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				int cx = (int) epoint.getX();
				int cy = (int) epoint.getY();
				var item = objList.stream().filter(obj -> cx >= cint(obj[1]) && cx <= cint(obj[1]) + 40
						&& cy >= cint(obj[2]) && cy <= cint(obj[2]) + 40).findFirst();

				if (item.isPresent()) {
					var building = (ArrayList<Object>) item.get()[0];
					if (e.getButton() == 1) {
						if (building.get(5).toString().equals("2"))
							return;
						new InfoDialog(building).setVisible(true);
					} else {
						selected = building;
						menu.show(c, e.getX(), e.getY());
					}

				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				try {
					to = e.getPoint();
					spoint = aff.inverseTransform(from, null);
					epoint = aff.inverseTransform(to, null);
					var difx = spoint.getX() - epoint.getX();
					var dify = spoint.getY() - epoint.getY();
					aff.translate(-difx, -dify);
					from = to;
					to = null;
					c.repaint();
				} catch (NoninvertibleTransformException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				try {
					epoint = aff.inverseTransform(e.getPoint(), null);

					if (e.getPreciseWheelRotation() < 0)
						zoom = Math.min(2, zoom + 0.1);
					else
						zoom = Math.max(0.1, zoom - 0.1);

					aff.setToIdentity();
					aff.translate(e.getX(), e.getY());
					aff.scale(zoom, zoom);
					aff.translate(-epoint.getX(), -epoint.getY());
					c.repaint();
				} catch (NoninvertibleTransformException e1) {

					e1.printStackTrace();
				}
			}
		};

		var toglbl = new JLabel("", JLabel.CENTER) {

			String text = "◀";

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				var g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(-10, 0, 35, 30, 5, 5);
				g2.setColor(blue);
				g2.drawString(text, 5, 20);
			}
		};

		var wn = new JPanel(new BorderLayout(5, 5));
		var wnn = new JPanel(new BorderLayout(5, 5));
		var wnc = new JPanel(new GridLayout(1, 0, 5, 5));

		w.add(wn, "North");

		wn.add(wnn, "North");
		wn.add(wnc);

		wnn.add(txt = new JTextField());
		wnn.add(btn("검색", a -> search()), "East");

		w.add(pane = new JScrollPane(p1 = new JPanel(new BorderLayout())));

		for (var bcap : "검색,길찾기".split(",")) {
			var btn = btn(bcap, a -> {
				for (var w : wnc.getComponents())
					w.setBackground(blue);

				if (a.getActionCommand().equals("검색"))
					pane.setViewportView(p1);
				else
					pane.setViewportView(p2);

				((JButton) a.getSource()).setBackground(blue.darker());
			});
			wnc.add(sz(btn, 0, 60));
			btn.setBackground(blue);

			if (bcap.equals("검색"))
				btn.doClick();
		}

		w.add(hyplbl("메인으로", JLabel.LEFT, 13, a -> mf.swapPage(new MainPage())), "South");

		wn.setBackground(blue);
		wnc.setOpaque(false);
		wnn.setOpaque(false);
		wn.setBorder(new EmptyBorder(5, 5, 5, 5));

		var cw = new JPanel(new GridBagLayout());
		c.add(cw, "West");
		cw.add(sz(toglbl, 30, 30));
		cw.setOpaque(false);

		toglbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {

				if (toglbl.text.equals("◀")) {
					toglbl.text = "▶";
					sz(w, 0, 0);
				} else {
					sz(w, 280, 0);
					toglbl.text = "◀";
				}
				revalidate();
				repaint();
			}
		});

		p2 = new JPanel(new BorderLayout(5, 5));
		var p2n = new JPanel(new BorderLayout(5, 5));
		var p2nc = new JPanel(new GridLayout(0, 1, 5, 5));
		var p2ns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		p2.add(p2n, "North");
		p2n.add(p2nc);
		p2n.add(p2ns, "South");

		p2.add(p2c = new JPanel(new BorderLayout()));

		p2nc.add(start = new JTextField());
		p2nc.add(end = new JTextField());

		p2n.setBorder(new EmptyBorder(5, 5, 5, 5));

		p2n.add(btn("↑↓", a -> {
			var temp = start.getText();
			start.setText(end.getText());
			end.setText(temp);
			var temp2 = start.getName();
			start.setName(end.getName());
			end.setName(temp2);
			dijkstra();
		}), "East");
		p2ns.add(btn("집을 출발지로", a -> {
			var building = getRows("select * from building where no = ?",
					getRows("select building from user where no = ?", uno).get(0).get(0)).get(0);
			selected = building;
			setPath(0);
		}));

		c.setBackground(new Color(153, 217, 234));
		c.addMouseListener(ma);
		c.addMouseMotionListener(ma);
		c.addMouseWheelListener(ma);
	}

	void search() {
		p1.removeAll();
		if (txt.getText().isEmpty()) {
			emsg("검색 키워드를 입력하세요.");
			return;
		} else {
			var rs = objList.stream().map(a -> (ArrayList<Object>) a[0]).filter(
					a -> a.get(1).toString().contains(txt.getText()) || a.get(4).toString().contains(txt.getText()))
					.collect(Collectors.toList());

			if (rs.isEmpty()) {
				imsg("검색 결과가 없습니다.");
			} else {
				p1.add(lbl("<html>장소명 <font color = 'rgb(0,123,255)'>" + txt.getText() + "</font>의 검색 결과", JLabel.LEFT,
						13), "North");
				var p = new JPanel(new GridLayout(0, 1));
				p1.add(p);

				for (var r : rs) {
					var temp1 = new JPanel(new BorderLayout());
					var temp2 = new JPanel(new BorderLayout());
					temp1.add(hyplbl("<html><font color = 'black'>" + (rs.indexOf(r) + 1) + ":" + r.get(1), JLabel.LEFT,
							13, a -> new InfoDialog(r).setVisible(true)), "North");

					temp1.addMouseListener(new MouseAdapter() {
						public void mousePressed(MouseEvent e) {
							selected = r;
							if (e.getButton() == 1 && e.getClickCount() == 2) {
								center(cint(r.get(6)), cint(r.get(7)));
							} else if (e.getButton() == 3)
								menu.show(temp1, e.getX(), e.getY());
						};

					});

					temp1.add(temp2);
					temp2.add(new JLabel(toIcon(r.get(8), 80, 80)), "East");
					temp2.add(lbl(r.get(4) + "", JLabel.LEFT));
					temp2.add(lbl("평점:" + r.get(9), JLabel.LEFT), "South");
					sz(temp1, 180, 120);
					temp1.setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY),
							new EmptyBorder(5, 5, 5, 5)));
					p.add(temp1);

				}
				while (p.getComponentCount() < 5)
					p.add(new JLabel());
			}

			revalidate();
			repaint();
		}
	}

	void center(int x, int y) {
		try {
			zoom = 1;
			aff.setToIdentity();
			from = new Point(0, 0);
			to = new Point(c.getWidth() / 2 - x, c.getHeight() / 2 - y);

			spoint = aff.inverseTransform(from, null);
			epoint = aff.inverseTransform(to, null);
			var difx = epoint.getX() - spoint.getX();
			var dify = epoint.getY() - spoint.getY();
			aff.translate(difx, dify);
			c.repaint();
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		uno = "1";
		mf.swapPage(new Map());
	}

	void dijkstra() {
		current = null;
		if (start.getText().isEmpty() || end.getText().isEmpty())
			return;
		int start = cint(this.start.getName());
		int end = cint(this.end.getName());

		int[][] dist = new int[2][346];

		for (int i = 0; i < 346; i++) {
			dist[0][i] = INF;
			dist[1][i] = -1;
		}

		path = new ArrayList<Integer>();

		var pq = new PriorityQueue<Object[]>((o1, o2) -> Integer.compare(cint(o1[1]), cint(o2[1])));
		pq.offer(new Object[] { start, 0 });
		dist[0][start] = 0;

		while (!pq.isEmpty()) {
			var cur = pq.poll();
			int u = cint(cur[0]);
			int cost = cint(cur[1]);
			if (dist[0][u] < cost)
				continue;
			for (int v = 1; v < 346; v++) {
				if (cint(adj[u][v][0]) == INF)
					continue;
				var next = adj[u][v];
				int next_cost = cint(next[0]);
				if (dist[0][v] > cost + next_cost) {
					dist[0][v] = cost + next_cost;
					dist[1][v] = u;
					pq.offer(new Object[] { v, dist[0][v] });
				}
			}
		}

		p2c.removeAll();

		int arv = start, dest = end;
		for (int i = 1; i < 346; i++) {
			System.out.println(dist[1][i]);
		}
		while (dest != arv) {
			path.add(dest);
			dest = dist[1][dest];
		}

		path.add(arv);

		Collections.reverse(path);

		Stack<java.util.Map.Entry<String, Integer>> s = new Stack();

		ArrayList<ValueRange> validate = new ArrayList<>();
		for (int i = 1; i < path.size(); i++) {
			int n1 = path.get(i - 1);
			int n2 = path.get(i);
			var node = adj[n1][n2];
			if (s.empty() || !s.peek().getKey().equals(node[1] + "")) {
				s.add(java.util.Map.entry(node[1] + "", cint(node[0])));
				validate.add(ValueRange.of(i - 1, i - 1));
			} else {
				var value = s.peek().getValue() + cint(node[0]);
				s.pop();
				s.add(java.util.Map.entry(node[1] + "", value));
				validate.set(validate.size() - 1, ValueRange.of(validate.get(validate.size() - 1).getMinimum(), i));
			}
		}

		int total = s.stream().mapToInt(a -> a.getValue()).sum();
		p2c.add(lbl("총 거리:" + total + "m", JLabel.RIGHT), "North");
		var p2cc = new JPanel(new GridLayout(0, 1));
		var lst = s.stream().collect(Collectors.toList());
		Collections.reverse(lst);

		for (int i = 0; i < lst.size(); i++) {
			String text = "<html><font color ='black'>";
			if (i == 0)
				text = "<html><font color = 'red'>출발 </font>" + text;
			else if (i == lst.size() - 1)
				text = "<html><font color = 'blue'>도착 </font>" + text;
			final int j = i;
			var hyplbl = hyplbl(text + (i + 1) + ". " + lst.get(i).getKey() + " 총 " + lst.get(i).getValue() + "m", 13,
					JLabel.CENTER, e -> {
						current = validate.get(j);
						mapInit();
					});
			hyplbl.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
			p2cc.add(hyplbl);
		}
		p2c.add(p2cc);
		mapInit();
		revalidate();
		repaint();

	}

	void mapInit() {
		try {
			map = ImageIO.read(new File("./datafiles/map.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		var g2 = (Graphics2D) map.getGraphics();

		g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		if (path != null) {
			for (int i = 0; i < path.size() - 1; i++) {
				int n1 = path.get(i);
				int n2 = path.get(i + 1);
				g2.setColor(Color.YELLOW);
				var pos1 = getRows("select x, y from building where no = ?", n1).get(0);
				var pos2 = getRows("select x, y from building where no = ?", n2).get(0);
				if (current != null && current.isValidValue(i)) {
					g2.setColor(Color.magenta);
					System.out.println("ㅇㅇ");
				}
				g2.drawLine(cint(pos1.get(0)), cint(pos1.get(1)), cint(pos2.get(0)), cint(pos2.get(1)));
			}
		}

		var d = "진료소,병원,주거지".split(",");

		for (var r : objList) {
			var building = (ArrayList<Object>) r[0];
			g2.setColor(Color.RED);
			int x = cint(r[1]), y = cint(r[2]);
			BufferedImage img;
			try {
				img = ImageIO.read(new File("./datafiles/맵아이콘/" + d[cint(building.get(5))] + ".png"));
				g2.drawString(building.get(1) + "",
						(x + 20) - g2.getFontMetrics().stringWidth(building.get(1) + "") / 2, y - 5);
				g2.drawImage(img, x, y, 40, 40, null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		repaint();
		revalidate();

	}

	void setPath(int r) {
		System.out.println(selected);
		var arr = Arrays.asList(start, end);
		int comp = r == 0 ? 1 : 0;
		if (arr.get(comp).getName() != null && arr.get(comp).getName().equals(selected.get(0).toString())) {
			emsg("출발지와 도착지는 같을 수 없습니다.");
			return;
		}

		arr.get(r).setName(selected.get(0).toString());
		arr.get(r).setText(selected.get(1).toString());
	}

	void dataInit() {
		objList = new ArrayList<>();

		for (int i = 1; i < 346; i++) {
			for (int j = 1; j < 346; j++) {
				adj[i][j][0] = INF;
				if (i == j)
					adj[i][j][0] = 0;
			}
		}

		var rs = getRows(
				"select node1, node2, c.name, b1.x, b1.y, b2.x, b2.y from connection c, building b1, building b2 where c.node1 = b1.no and c.node2 = b2.no");

		for (var r : rs) {
			int cost = (int) Point.distance(cint(r.get(3)), cint(r.get(4)), cint(r.get(5)), cint(r.get(6)));
			int u = cint(r.get(0));
			int v = cint(r.get(1));
			adj[u][v][0] = adj[v][u][0] = cost;
			adj[u][v][1] = adj[v][u][1] = r.get(2).toString();
		}

		var rs2 = getRows(
				"select b.*,ifnull((select round(avg(r.rate),1) from rate r where r.building = b.no),0) from building b where type <>3");

		for (var r : rs2)
			objList.add(new Object[] { r, cint(r.get(6)) - 20, cint(r.get(7)) - 20 });

	}
}
