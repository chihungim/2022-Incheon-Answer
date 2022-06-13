package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;

public class reCapcha extends JDialog {

	LoginPage loginPage;

	JPanel n, c, s;

	ArrayList<File> imgList;
	HashMap<String, HashSet<File>> keyMap;
	HashSet<File> selectSet = new HashSet<File>();
	HashSet<File> answerSet = new HashSet<File>();
	String mainKey;
	JComboBox<String> combo;

	public reCapcha() {
		setSize(350, 400);
		setModal(true);
		setDefaultCloseOperation(2);
		loginPage = (LoginPage) BasePage.mf.getContentPane().getComponent(0);
		setLocationRelativeTo(loginPage);
		setUndecorated(true);

		try {
			dataInit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setLayout(new BorderLayout(5, 5));

		add(n = new JPanel(new FlowLayout(JLabel.CENTER)), "North");
		add(c = new JPanel(new GridLayout(0, 3, 5, 5)));
		add(s = new JPanel(new GridLayout(1, 0, 5, 5)), "South");

		n.add(combo);
		n.add(BasePage.lbl("<html><font color = 'white'>가 포함된 이미지를 고르시오", JLabel.CENTER));

		s.add(BasePage.btn("확인", a -> {
			if (selectSet.isEmpty()) {
				BasePage.eMsg("선택을 하세요.");
				return;
			}

			if (!(selectSet.containsAll(answerSet) && answerSet.containsAll(selectSet))) {
				BasePage.eMsg("틀렸습니다.");
				crtImgList();
				return;
			}

			dispose();
		}));

		s.add(BasePage.btn("새로고침", a -> crtImgList()));
		combo.addItemListener(i -> {
			if (i.getStateChange() == ItemEvent.SELECTED) {
				crtImgList();
			}
		});

		((JPanel) getContentPane())
				.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));

		n.setBackground(new Color(0, 123, 255));

		addWindowListener(new WindowAdapter() {
			public void windowClosed(java.awt.event.WindowEvent e) {
				loginPage.chkpanel.flag1 = false;
				loginPage.chkpanel.repaint();
				loginPage.chkpanel.flag2 = true;
			};
		});

		crtImgList();
	}

	void dataInit() throws Exception {
		keyMap = new HashMap<String, HashSet<File>>();

		for (var f : new File("./datafiles/캡챠").listFiles()) {
			var data = Files.readAllBytes(f.toPath());
			String txt = new String(data, "utf-8");
			int s = txt.indexOf("<x:xmpmeta");
			int e = txt.indexOf("</x:xmpmeta>");
			String xml = txt.substring(s, e + 12).toString();
			var is = new ByteArrayInputStream(xml.getBytes("utf-8"));
			var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			var nodeList = ((Element) doc.getDocumentElement().getElementsByTagName("dc:subject").item(0))
					.getElementsByTagName("rdf:li");

			for (int j = 0; j < nodeList.getLength(); j++) {
				var key = nodeList.item(j).getTextContent();
				if (keyMap.containsKey(key)) {
					keyMap.get(key).add(f);
				} else {
					keyMap.put(key, new HashSet<File>());
					keyMap.get(key).add(f);
				}
			}
		}

		combo = new JComboBox<String>(new ArrayList<String>(keyMap.keySet()).toArray(String[]::new));
	}

	void crtImgList() {
		c.removeAll();
		answerSet.clear();
		selectSet.clear();

		mainKey = combo.getSelectedItem().toString();
		imgList = new ArrayList<File>();

		var lst = keyMap.entrySet().stream().filter(a -> !a.getKey().equals(mainKey)).map(a -> a.getValue())
				.flatMap(a -> a.stream()).distinct().collect(Collectors.toList());

		var anslst = new ArrayList<File>(keyMap.get(mainKey));

		Collections.shuffle(anslst);
		System.out.println(anslst.size());

		answerSet.addAll(anslst.subList(0, Math.min(new Random().nextInt(5) + 1, anslst.size())));

		System.out.println(answerSet);

		for (var ans : answerSet)
			imgList.add(ans);

		Collections.shuffle(lst);

		lst.stream().filter(a -> !answerSet.contains(a)).limit(9 - answerSet.size()).forEach(imgList::add);

//		System.out.println("답:" + answerSet);

		for (var img : imgList) {
			var lbl = new JLabel(BasePage.getIcon(img.getPath(), 150, 150)) {
				File f = img;
			};

			lbl.addMouseListener(new MouseAdapter() {
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (lbl.getBorder() == null) {
						lbl.setBorder(new LineBorder(Color.GREEN, 2));
						selectSet.add(lbl.f);
					} else {
						selectSet.remove(lbl.f);
						lbl.setBorder(null);
					}

				};
			});

			c.add(lbl);
		}

		revalidate();
		repaint();
	}
}
