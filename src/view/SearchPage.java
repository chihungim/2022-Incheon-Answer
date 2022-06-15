package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import com.mysql.cj.x.protobuf.MysqlxCrud.Collection;

public class SearchPage extends BasePage {
	BufferedImage map;
	double affTargetX = 0, affTargetY = 0, startAffX, startAffY, zoom = 0.5;
	AffineTransform aff = new AffineTransform();

	Point2D curAffPoint;
	JToggleButton toggle[] = new JToggleButton[2];
	JPanel srchP, pathP, pathRsP;
	CardLayout pages;
	ArrayList<Integer> path;
	ArrayList<Object[]> objList; // type, x, y
	ArrayList<ArrayList<Object[]>> adjList; // idx, cost, name
	String colorKey;
	Item selected;
	JTextField search, start, end;

	JPopupMenu menu = new JPopupMenu();
	{
		for (var i : "출발지,도착지".split(",")) {
			var it = new JMenuItem(i);
			menu.add(it);
			it.addActionListener(a -> {
				if (a.getActionCommand().equals("출발지"))
					setStart(selected);
				else
					setEnd(selected);
				dijkstra();
			});

		}
	}

	public SearchPage() {
		setLayout(new BorderLayout());
		dataInit();
		drawOnMap();
		add(c = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				var g2 = (Graphics2D) g;
				aff.translate(affTargetX, affTargetY);
				g2.drawImage(map, aff, null);
			}
		});

		var ma = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				try {
					curAffPoint = aff.inverseTransform(e.getPoint(), null);
				} catch (NoninvertibleTransformException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				startAffX = curAffPoint.getX();
				startAffY = curAffPoint.getY();

				int clickedX = (int) startAffX;
				int clickedY = (int) startAffY;

				var item = objList.stream().filter(obj -> clickedX >= toInt(obj[1]) && clickedX <= toInt(obj[1]) + 40
						&& clickedY >= toInt(obj[2]) && clickedY <= toInt(obj[2]) + 40).findFirst();

				if (item.isPresent()) {
					var building = (ArrayList<Object>) item.get()[0];
					if (e.getButton() == 1)
						new InfoDialog(building).setVisible(true);
					else if (e.getButton() == 3) {
						selected = new Item(building.get(0) + "", building.get(1) + "");
						menu.show(c, e.getX(), e.getY());
					}
				}

				repaint();
			}

			public void mouseDragged(MouseEvent e) {
				try {
					curAffPoint = aff.inverseTransform(e.getPoint(), null);
				} catch (NoninvertibleTransformException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				double affdiffX = curAffPoint.getX() - startAffX;
				double affdiffY = curAffPoint.getY() - startAffY;

				affTargetX += affdiffX;
				affTargetY += affdiffY;

				c.repaint();

				startAffX = curAffPoint.getX();
				startAffY = curAffPoint.getY();
			};

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				try {
					curAffPoint = aff.inverseTransform(e.getPoint(), null);
				} catch (NoninvertibleTransformException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				var flag = false;

				if (e.getPreciseWheelRotation() < 0) {
					if (zoom == 2)
						flag = false;
					else
						zoom = Math.min(2, zoom + 0.1);
				} else if (zoom == 0.1)
					flag = true;
				else
					zoom = Math.max(0.1, zoom - 0.1);

				if (!flag) {
					aff.setToIdentity();
					aff.translate(e.getX(), e.getY());
					aff.scale(zoom, zoom);
					aff.translate(-curAffPoint.getX(), -curAffPoint.getY());
					c.repaint();
				}

			}
		};

		c.addMouseListener(ma);
		c.addMouseMotionListener(ma);
		c.addMouseWheelListener(ma);

		c.setBackground(new Color(153, 217, 234));

		add(sz(w = new JPanel(new BorderLayout()), 280, 0), "West");

		c.add(cw = new JPanel(new GridBagLayout()), "West");
		var toglbl = new JLabel("") {
			String text = "◀";

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				var g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new JPanel().getBackground());
				g2.fillRoundRect(-10, 0, 30, 30, 5, 5);
				g2.setColor(blue);
				g2.drawString(text, 5, 20);
			}
		};

		toglbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (toglbl.text.equals("◀")) {
					sz(w, 0, 0);
					toglbl.text = "▶";
				} else {
					sz(w, 280, 0);
					toglbl.text = "◀";
				}

				revalidate();
				repaint();
			}
		});

		w.add(wn = new JPanel(new BorderLayout(5, 5)), "North");
		w.add(wc = new JPanel(pages = new CardLayout()));
		wn.setBackground(blue);
		{
			var temp1 = new JPanel(new BorderLayout(5, 5));
			var temp2 = new JPanel(new GridLayout(1, 0, 5, 5));
			wn.add(temp1, "North");
			wn.add(temp2);

			temp1.setOpaque(false);
			temp2.setOpaque(false);
			temp1.add(search = new JTextField());
			temp1.add(btn("검색", a -> search()), "East");

			var bcap = "검색,길찾기".split(",");

			var grp = new ButtonGroup();

			for (int i = 0; i < bcap.length; i++) {
				temp2.add(sz(toggle[i] = new JToggleButton(bcap[i]), 50, 50));
				grp.add(toggle[i]);
				toggle[i].setBackground(blue);
				toggle[i].setUI(new MetalToggleButtonUI() {
					@Override
					protected Color getSelectColor() {
						return blue.darker();
					}
				});
				final int k = i;

				toggle[i].setForeground(Color.white);
				toggle[i].addItemListener(j -> {
					if (j.getStateChange() == ItemEvent.SELECTED) {
						pages.show(wc, bcap[k]);
					}
				});
			}
		}

		wc.add(new JScrollPane(srchP = new JPanel(new BorderLayout())), "검색");
		wc.add(new JScrollPane(pathP = new JPanel(new BorderLayout())), "길찾기");
		pathP.add(pathRsP = new JPanel(new GridLayout(0, 1, 5, 5)));
		srchP.add(lbl("<html><center>텍스트필드에 텍스트를 입력하고<br>검색버튼을 누르세요.", JLabel.CENTER));
		toggle[0].setSelected(true);

		{
			var temp1 = new JPanel(new BorderLayout(5, 5));
			var temp2 = new JPanel(new GridLayout(0, 1, 5, 5));
			var temp3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

			temp1.add(temp2);
			pathP.add(temp1, "North");
			temp1.add(temp3, "South");

			temp2.add(start = new JTextField());
			temp2.add(end = new JTextField());
			temp1.add(btn("↑↓", a -> {
				var temp = new Item(start.getName(), start.getText());
				start.setText(end.getText());
				start.setName(end.getName());
				end.setText(temp.value);
				end.setName(temp.no);
				dijkstra();
			}), "East");
			temp3.add(btn("집을 출발지로 ", a -> {
				var rs = getRow(
						"select b.no, b.name from user u inner join building b on u.building = b.no where u.no = ?",
						uno);
				setStart(new Item(rs.get(0) + "", rs.get(1) + ""));
				dijkstra();
			}));
			temp1.setBorder(new EmptyBorder(5, 5, 5, 5));

		}

		wn.setBorder(new EmptyBorder(5, 5, 5, 5));
		w.add(hyplbl("메인으로", JLabel.LEFT, 13, (e) -> mf.swapPage(new MainPage())), "South");
		cw.setOpaque(false);
		cw.add(sz(toglbl, 20, 30));

	}

	void search() {
		srchP.removeAll();
		toggle[0].setSelected(true);
		srchP.setLayout(new GridBagLayout());
		if (search.getText().trim().isEmpty())
			srchP.add(lbl("<html><center><font color = rgb(0,123,255)>공백이 존재합니다,", 0));
		else {
			var rs = getRows(
					"select b.*, ifnull((select round(avg(r.rate),1) from rate r where r.building = b.no)  , 0) from building b where type <> 3 and b.name like '%"
							+ search.getText() + "%' or b.info like '%" + search.getText() + "%';");
			if (rs.isEmpty()) {
				srchP.add(lbl(
						"<html><center><font color = rgb(0,123,255)>" + search.getText() + "</font><br>의 결과가 없어요.", 0));
			} else {
				srchP.setLayout(new BorderLayout());
				srchP.add(lbl("<html>장소명 <Font color = rgb(0,123,255)>" + search.getText() + "</font> 의 검색 결과",
						JLabel.LEFT, 13), "North");

				var p = new JPanel(new GridLayout(0, 1));

				srchP.add(p);

				gotoCenter(toInt(rs.get(0).get(6)), toInt(rs.get(0).get(7)));

				for (var r : rs) {
					var temp1 = new JPanel(new BorderLayout());
					var temp2 = new JPanel(new BorderLayout());

					temp1.add(hyplbl("<html><font color = 'black'>" + (rs.indexOf(r) + 1) + ". " + r.get(1).toString(),
							JLabel.LEFT, 15, (e) -> new InfoDialog(r).setVisible(true)), "North");
					temp1.addMouseListener(new MouseAdapter() {
						public void mousePressed(MouseEvent e) {
							if (e.getButton() == 1 && e.getClickCount() == 2) {
								gotoCenter(toInt(r.get(6)), toInt(r.get(7)));
							}
							if (e.getButton() == 3) {
								selected = new Item(r.get(0) + "", r.get(1) + "");
								menu.show(temp1, e.getX(), e.getY());
							}
						};
					});

					temp1.add(temp2);

					temp2.add(new JLabel(toIcon(r.get(8), 80, 80)), "East");

					temp2.add(lbl(r.get(4) + "", JLabel.LEFT));
					temp1.add(lbl("평점: " + r.get(9), JLabel.LEFT), "South");
					p.add(temp1);
					sz(temp1, 180, 120);
					temp1.setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY),
							new EmptyBorder(5, 5, 5, 5)));
				}

				while (p.getComponentCount() < 3)
					p.add(new JLabel());

				p.setBorder(new EmptyBorder(5, 5, 5, 5));
			}
		}

		revalidate();
		repaint();
	}

	void gotoCenter(int x, int y) {
		zoom = 1;
		aff.setToIdentity();
		aff.scale(zoom, zoom);

		var point = new Point((c.getWidth() - map.getWidth()) / 2 - (x - map.getWidth() / 2),
				(c.getHeight() - map.getHeight()) / 2 - (y - map.getHeight() / 2));

		try {
			curAffPoint = aff.inverseTransform(point, null);
		} catch (NoninvertibleTransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		affTargetX = curAffPoint.getX();
		affTargetY = curAffPoint.getY();
		c.repaint();
	}

	void setStart(Item item) {
		if (end.getName() != null && end.getName().equals(item.no)) {
			System.out.println("ㅇㅇ");
			eMsg("출발지와 도착지는 같을 수 없습니다.");
			return;
		}

		start.setText(item.value);
		start.setName(item.no);
	}

	void setEnd(Item item) {
		if (start.getName() != null && start.getName().equals(item.no)) {
			eMsg("출발지와 도착지는 같을 수 없습니다.");
			return;
		}

		end.setText(item.value);
		end.setName(item.no);
	}

	private void drawOnMap() {
		try {
			map = ImageIO.read(new File("./datafiles/map.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		var g2 = (Graphics2D) map.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		if (path != null) {
			var temp = new ArrayList<String>();
			for (int i = 0; i < path.size() - 1; i++) {
				int n1 = path.get(i);
				int n2 = path.get(i + 1);
				g2.setColor(Color.YELLOW);

				var node = adjList.get(n1).stream().filter(a -> toInt(a[0]) == n2).findFirst().get();

				if (!temp.contains(temp.size() + ". " + node[2]))
					temp.add(temp.size() + 1 + ". " + node[2]);

				var pos1 = getRow("select x, y from building where no = ?", n1);
				var pos2 = getRow("select x, y from building where no = ?", n2);

				if (colorKey.equals(temp.get(temp.size() - 1)))
					g2.setColor(Color.MAGENTA);

				g2.drawLine(toInt(pos1.get(0)), toInt(pos1.get(1)), toInt(pos2.get(0)), toInt(pos2.get(1)));
			}
		}

		var d = "진료소,병원,주거지".split(",");

		for (var r : objList) {
			var building = (ArrayList<Object>) r[0];
			g2.setColor(Color.red);
			int x = toInt(r[1]), y = toInt(r[2]);
			BufferedImage img;
			try {
				img = ImageIO.read(new File("./datafiles/맵아이콘/" + d[toInt(building.get(5))] + ".png"));
				g2.drawString(building.get(1).toString(),
						(x + 20) - g2.getFontMetrics().stringWidth(building.get(1).toString()) / 2, y - 5);
				g2.drawImage(img, x, y, 40, 40, null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (c != null)
			c.repaint();

	}

	private void dataInit() {
		adjList = new ArrayList<ArrayList<Object[]>>();
		objList = new ArrayList<Object[]>();
		for (int i = 0; i < getRows("select * from building").size() + 1; i++)
			adjList.add(new ArrayList<Object[]>());

		for (var r : getRows(
				"select c.node1, c.node2, c.name ,b1.x, b1.y, b2.x, b2.y from connection c, building b1, building b2 where c.node1 = b1.no and c.node2 = b2.no")) {
			int u = toInt(r.get(0)), v = toInt(r.get(1)), x1 = toInt(r.get(3)), y1 = toInt(r.get(4)),
					x2 = toInt(r.get(5)), y2 = toInt(r.get(6));
			int cost = (int) Point2D.distance(x1, y1, x2, y2);
			adjList.get(u).add(new Object[] { v, cost, r.get(2) });
			adjList.get(v).add(new Object[] { u, cost, r.get(2) });
		}

		for (var r : getRows(
				"select b.*, ifnull((select round(avg(r.rate),1) from rate r where r.building = b.no)  , 0) from building b where type <>3;")) {
			objList.add(new Object[] { r, toInt(r.get(6)) - 20, toInt(r.get(7)) - 20 });
		}

	}

	void dijkstra() {
		colorKey = "";
		if (start.getText().isEmpty() || end.getText().isEmpty())
			return;

		int start = toInt(this.start.getName());
		int end = toInt(this.end.getName());

		int[][] dist = new int[2][adjList.size() + 1];

		for (int i = 0; i < dist[0].length; i++) {
			dist[0][i] = Integer.MAX_VALUE;
			dist[1][i] = -1;
		}

		path = new ArrayList<Integer>();

		var pq = new PriorityQueue<Object[]>((o1, o2) -> Integer.compare(toInt(o1[1]), toInt(o2[1])));

		pq.offer(new Object[] { start, 0 });
		dist[0][start] = 0;

		while (!pq.isEmpty()) {
			var cur = pq.poll();
			if (dist[0][toInt(cur[0])] < toInt(cur[1]))
				continue;
			for (int i = 0; i < adjList.get(toInt(cur[0])).size(); i++) {
				var next = adjList.get(toInt(cur[0])).get(i);
				if (dist[0][toInt(next[0])] > toInt(cur[1]) + toInt(next[1])) {
					dist[0][toInt(next[0])] = toInt(cur[1]) + toInt(next[1]);
					dist[1][toInt(next[0])] = toInt(cur[0]);
					pq.offer(new Object[] { toInt(next[0]), dist[0][toInt(next[0])] });
				}
			}
		}

		pathRsP.removeAll();

		int arv = start, dest = end;

		while (dest != arv) {
			path.add(dest);
			dest = dist[1][dest];
		}

		path.add(arv);
		Collections.reverse(path);

		var temp = new ArrayList<String[]>();

		for (int i = 1; i < path.size(); i++) {
			int n1 = path.get(i - 1);
			int n2 = path.get(i);
			var node = adjList.get(n1).stream().filter(a -> toInt(a[0]) == n2).findFirst().get();

			if (!temp.stream().filter(a -> a[0].equals(temp.size() + ". " + node[2].toString())).findFirst()
					.isPresent()) {
				temp.add(new String[] { temp.size() + 1 + ". " + node[2].toString(), node[1].toString() });
			} else {
				var str = temp.get(temp.size() - 1);
				int cost = toInt(str[1]) + toInt(node[1]);
				temp.set(temp.size() - 1, new String[] { str[0], cost + "" });
			}
		}

		int total = temp.stream().mapToInt(a -> toInt(a[1])).sum();

		pathRsP.add(lbl("총 거리:" + total + "m", JLabel.RIGHT));

		for (int i = 0; i < temp.size(); i++) {
			String text = "<html><font color = 'black'>";
			if (i == 0)
				text = "<html><font color = 'red'>출발 </font><font color = 'black'>";
			else if (i == temp.size() - 1)
				text = "<html><font color = 'blue'>도착 </font><font color = 'black'>";

			final int j = i;

			var hyplbl = hyplbl(text + "" + temp.get(i)[0] + " 총 " + temp.get(i)[1] + "m", JLabel.CENTER, 13, (e) -> {
				colorKey = temp.get(j)[0].toString();
				drawOnMap();
			});
			hyplbl.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
			pathRsP.add(hyplbl);
		}

		toggle[1].setSelected(true);

		drawOnMap();

		revalidate();
		repaint();

	}

	public static void main(String[] args) {
		uno = "1";
		mf.swapPage(new SearchPage());
	}
}
