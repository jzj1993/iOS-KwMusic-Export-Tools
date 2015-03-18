import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Test {

	/**
	 * 存储播放列表的数据结构
	 * 
	 * @author jzj
	 */
	static class PlayList {
		int id;
		String name;

		public PlayList(String name, int id) {
			this.name = name;
			this.id = id;
		}
	}

	// 数据库完整路径
	static final String db_path = "G:\\IOS\\cloud.db";
	// 源文件夹
	static final String src_dir = "G:\\IOS\\Music\\";
	// 目标文件夹
	static final String dst_dir = "G:\\IOS\\Music1\\";

	public static void main(String[] args) throws Exception {

		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db_path);

		Statement stat1 = conn.createStatement();
		Statement stat2 = conn.createStatement();

		// 读取播放列表
		List<PlayList> lists = new ArrayList<Test.PlayList>();
		ResultSet rs_list = stat1.executeQuery("select * from playlistsInfo;");
		while (rs_list.next()) {
			final int id = rs_list.getInt("id");
			final String name = rs_list.getString("title");
			switch (name) {
			// 忽略这几个列表
			case "本地歌曲":
			case "默认列表":
			case "最近播放":
			case "我的电台":
				break;
			case "我喜欢听":
			default:
				lists.add(new PlayList(name, id));
			}
		}

		// 读取音乐信息
		ResultSet rs_res = stat1.executeQuery("select * from musicResource;");
		while (rs_res.next()) {

			// 源文件路径
			String fname = rs_res.getString("file");
			if (fname == null || fname.length() == 0) // 如果file字段为空则跳过
				continue;

			String src_path = src_dir + fname;

			File src = new File(src_path);
			if (!src.exists()) // 如果源文件不存在则跳过
				continue;

			// 获取音乐rid
			int rid = rs_res.getInt("rid");

			// 查找该音乐所在播放列表id, 如果没有找到则为-1
			ResultSet rs_pl = stat2.executeQuery(new StringBuilder(
					"select playlist_id from playlistMusics where rid=")
					.append(rid).append(';').toString());
			int playlist_id = -1;
			while (rs_pl.next()) { // 默认将一首歌放在编号最大的播放列表中(也就是最新创建的列表)
				int p_id = rs_pl.getInt("playlist_id");
				if (p_id > playlist_id)
					playlist_id = p_id;
			}
			rs_pl.close();

			// 目标文件夹路径
			StringBuilder b2 = new StringBuilder(dst_dir);
			if (playlist_id >= 0) {
				String playlist_name = getPlaylist(lists, playlist_id);
				if (playlist_name != null) {
					b2.append(playlist_name).append('\\');
				}
			}
			String dir = b2.toString();
			new File(dir).mkdirs();

			// 目标文件名: "艺术家 - 歌曲名.扩展名"
			StringBuilder b3 = new StringBuilder();
			b3.append(rs_res.getString("artist")).append(" - ")
					.append(rs_res.getString("title")).append('.')
					.append(rs_res.getString("format"));
			String dst_path = dir + b3.toString();

			// 移动和重命名
			File dst = new File(dst_path);
			src.renameTo(dst);

			// 输出信息
			System.out.println(new StringBuilder(src_path).append(" ---> ")
					.append(dst_path));
		}
		rs_res.close();
		conn.close();
	}

	static String getPlaylist(List<PlayList> lists, int playlist_id) {
		for (PlayList pl : lists) {
			if (pl.id == playlist_id)
				return pl.name;
		}
		return null;
	}
}
