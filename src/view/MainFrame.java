package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

public class MainFrame extends JFrame {
	public MainFrame() {
		super("국삐");
		setSize(1000, 600);
		for (var f : UIManager.getDefaults().keySet()) {
			if (f.toString().contains("back"))
				UIManager.getDefaults().put(f, new ColorUIResource(Color.WHITE));
		}
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setIconImage(Toolkit.getDefaultToolkit().getImage("./datafiles/Covid.png"));
		setVisible(true);
	}

	public static void main(String[] args) {
		BasePage.uno = "1";
		var defaults = UIManager.getLookAndFeelDefaults();
		for (var key : defaults.keySet()) {
			if (key.toString().contains("back")) {
				UIManager.put(key, new ColorUIResource(Color.WHITE));
			}
		}

		BasePage.mf.swapPage(new SearchPage());

	}

	public void swapPage(BasePage page) {
		getContentPane().removeAll();
		getContentPane().setLayout(new BorderLayout());
		add(page);
		getContentPane().revalidate();
		getContentPane().repaint();
	}

}
