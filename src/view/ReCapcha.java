package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Flow;
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

public class ReCapcha extends JDialog {
	LoginPage loginPage;
	HashMap<String, HashSet<File>> tagMap;
	HashSet<File> ansSet, selectSet;
	JComboBox<String> combo;

	JPanel n, c, s;

	public ReCapcha() {
		setModal(true);
		setSize(400, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setUndecorated(true);

		add(n = new JPanel(new FlowLayout(FlowLayout.LEFT)), "North");
		add(c = new JPanel(new GridLayout(0, 3, 5, 5)));
		add(s = new JPanel(new GridLayout(1, 0, 5, 5)), "South");

		tagMap = new HashMap<String, HashSet<File>>();

		loginPage = (LoginPage) BasePage.mf.getContentPane().getComponent(0);

		for (var f : new File("./datafiles/리캡차").listFiles()) {
			try {
				var data = Files.readAllBytes(f.toPath());
				var tmp = new String(data, "utf-8");
				String s = "<x:xmpmeta", e = "</x:xmpmeta>";
				var xml = tmp.substring(tmp.indexOf(s), tmp.indexOf(e) + e.length());
				var is = new ByteArrayInputStream(xml.getBytes("utf-8"));
				var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
				var lst = ((Element) doc.getElementsByTagName("dc:subject").item(0)).getElementsByTagName("rdf:li");

				for (int i = 0; i < lst.getLength(); i++) {
					var tag = lst.item(i).getTextContent();
					if (tagMap.containsKey(tag)) {
						tagMap.get(tag).add(f);
					} else {
						tagMap.put(tag, new HashSet<File>());
						tagMap.get(tag).add(f);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		n.setBackground(BasePage.blue);

		n.add(combo = new JComboBox<String>(tagMap.keySet().stream().toArray(String[]::new)));
		n.add(BasePage.lbl("<html><font color = 'white'>가 포함된 이미지를 선택해주세요.", 0));

		crtImgList();

		combo.addItemListener(i -> crtImgList());

		s.add(BasePage.btn("확인", a -> {
			if (selectSet.isEmpty()) {
				BasePage.emsg("선택을 하세요");
				return;
			}

			if (!(ansSet.containsAll(ansSet) && selectSet.containsAll(ansSet))) {
				BasePage.emsg("틀렸습니다.");
				crtImgList();
				return;
			}

			loginPage.flag = true;
			dispose();
		}));
		s.add(BasePage.btn("새로고침", a -> crtImgList()));

		((JPanel) getContentPane())
				.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
	}

	void crtImgList() {
		c.removeAll();
		selectSet = new HashSet<File>();
		ansSet = new HashSet<File>();
		var tag = combo.getSelectedItem().toString();

		var anslist = new ArrayList<File>(tagMap.get(tag));
		int rand = new Random().nextInt(9) + 1, sz = anslist.size();
		ansSet.addAll(anslist.subList(0, Math.min(sz, rand)));

		var lst = tagMap.entrySet().stream().map(a -> a.getValue()).flatMap(a -> a.stream()).distinct()
				.filter(a -> !anslist.contains(a)).collect(Collectors.toList());

		var imgList = new ArrayList<File>(ansSet);

		for (int i = 0; i < 9 - ansSet.size(); i++)
			imgList.add(lst.get(i));
		for (var img : imgList) {
			var lbl = BasePage.lbl("", 0, 0, a -> {
				var me = (JLabel) a.getSource();
				if (me.getBorder() == null) {
					me.setBorder(new LineBorder(Color.GREEN, 2));
					selectSet.add(img);
				} else {
					me.setBorder(null);
					selectSet.remove(img);
				}
			});
			lbl.setIcon(BasePage.getIcon(img.getPath(), 120, 120));
			c.add(lbl);
		}

		revalidate();
		repaint();
	}
}
