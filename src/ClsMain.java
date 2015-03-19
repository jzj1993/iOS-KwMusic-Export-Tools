import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ClsMain implements ActionListener {

	JFrame frame = new JFrame("iOS酷我音乐盒音乐导出工具 (by jzj1993)");
	Container container = new Container();

	Font font = new Font("微软雅黑", 0, 16);

	JLabel labDbfile = new JLabel("数据库文件");
	JButton btnDbfile = new JButton("...");
	JTextField txtDbfile = new JTextField();

	JLabel labDir = new JLabel("音乐文件夹");
	JButton btnDir = new JButton("...");
	JTextField txtDir = new JTextField();

	JButton btnStart = new JButton("开始处理");
	JTextArea txtInfo = new JTextArea();
	JScrollPane sp = new JScrollPane();

	JLabel labLink = new JLabel("<html><a href=''>访问软件主页</a></html>");

	JComponent coms[] = { labDbfile, labDir, btnDbfile, btnDir, btnStart,
			txtDbfile, txtDir, sp, labLink };

	public static void main(String[] args) throws Exception {
		new ClsMain();
	}

	public ClsMain() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		labDbfile.setBounds(10, 10, 80, 30);
		txtDbfile.setBounds(100, 10, 420, 30);
		btnDbfile.setBounds(530, 10, 40, 30);

		labDir.setBounds(10, 45, 80, 30);
		txtDir.setBounds(100, 45, 420, 30);
		btnDir.setBounds(530, 45, 40, 30);

		btnStart.setBounds(240, 90, 100, 30);

		txtInfo.setEditable(false);
		txtInfo.setLineWrap(true); // 激活自动换行功能
		txtInfo.setWrapStyleWord(true); // 激活断行不断字功能
		sp.setViewportView(txtInfo);
		sp.setBounds(10, 140, 560, 300);

		labLink.setBounds(470, 450, 100, 30);
		labLink.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				try {
					Runtime.getRuntime()
							.exec("cmd.exe /c start https://github.com/jzj1993/iOS-KwMusic-Export-Tools");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			public void mouseEntered(MouseEvent e) {// 鼠标进入
				labLink.setCursor(Cursor
						.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent e) {// 鼠标移除
				labLink.setCursor(Cursor.getDefaultCursor());
			}
		});

		btnDbfile.addActionListener(this); // 添加事件处理
		btnDir.addActionListener(this); // 添加事件处理
		btnStart.addActionListener(this); // 添加事件处理

		for (JComponent com : coms) {
			com.setFont(font);
			container.add(com);
		}

		frame.setSize(600, 530);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(container);
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src.equals(btnDbfile)) {
			selectDbFilePath(frame);
		} else if (src.equals(btnDir)) {
			selectFolderPath(frame);
		} else if (src.equals(btnStart)) {
			start();
		}
	}

	private void selectDbFilePath(Component parent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText("确定");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileFilter filter = new FileNameExtensionFilter("数据库文件(*.db)", "db");
		chooser.setFileFilter(filter);
		chooser.setSelectedFile(new File("cloud.db")); // 设置默认文件名
		int r = chooser.showOpenDialog(parent);
		if (r == JFileChooser.APPROVE_OPTION) {
			txtDbfile.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void selectFolderPath(Component parent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText("确定");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = chooser.showOpenDialog(parent);
		if (r == JFileChooser.APPROVE_OPTION) {
			txtDir.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void start() {
		final String db_path = getDbFilePath();
		final String src_dir = getSrcDirPath();
		final String dst_dir = getDstDirPath(src_dir);
		if (db_path == null) {
			JOptionPane.showMessageDialog(frame, "请选择数据库文件");
			return;
		} else if (src_dir == null) {
			JOptionPane.showMessageDialog(frame, "请选择音乐文件目录");
			return;
		} else if (dst_dir == null) {
			return;
		}
		final Data d = new Data() {
			@Override
			public void output(String s) {
				txtInfo.append(s + "\n");
				txtInfo.setCaretPosition(txtInfo.getText().length());
			}

			@Override
			public void finish() {
				try {
					Runtime.getRuntime().exec("cmd.exe /c start " + dst_dir);
				} catch (IOException e) {
					e.printStackTrace();
				}
				JOptionPane.showMessageDialog(frame, "转换完成，已输出到文件夹" + dst_dir);
			}
		};
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					d.run(db_path, src_dir, dst_dir);
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					txtInfo.setText(sw.toString());
					JOptionPane.showMessageDialog(frame, "发生错误");
				}
			}
		}).start();
	}

	private String getDbFilePath() {
		String path = txtDbfile.getText();
		if (path != null) {
			File f = new File(path);
			if (f.isFile() && f.exists()) {
				return path;
			}
		}
		return null;
	}

	private String getSrcDirPath() {
		String path = txtDir.getText();
		if (path != null) {
			File f = new File(path);
			if (f.isDirectory() && f.exists()) {
				return path;
			}
		}
		return null;
	}

	private String getDstDirPath(String src_dir) {
		String dst_dir = src_dir + "/iOS_Output";
		File f = new File(dst_dir);
		int n = 1;
		while (f.exists()) {
			dst_dir = src_dir + "/iOS_Output_" + n++;
			f = new File(dst_dir);
		}
		if (f.mkdirs()) {
			return f.getAbsolutePath();
		}
		JOptionPane.showMessageDialog(frame, "创建文件夹失败 " + dst_dir);
		return null;
	}
}
