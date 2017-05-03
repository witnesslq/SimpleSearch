import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pingbu.common.Pinyin;
import pingbu.search.Search;

public class Example {

	private static class Video {
		public int id;
		public String name;
	}

	private List<Video> mVideos = new ArrayList<Video>();
	private Search mLexicon = new Search();

	static {
		try {
			Pinyin.init("data/common");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private Example() {
		try {
			FileInputStream f = new FileInputStream("data/search/Video.txt");
			InputStreamReader in = new InputStreamReader(f, "UTF-8");
			BufferedReader r = new BufferedReader(in);
			for (int id = 0;; ++id) {
				String l = r.readLine();
				if (l == null)
					break;
				Video video = new Video();
				video.id = id;
				video.name = l;
				mVideos.add(video);
			}
			r.close();
			in.close();
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		for (Video video : mVideos)
			mLexicon.addItem(video.name, video);
	}

	private void search() {
		Collection<Search.Result> rs = mLexicon.search("西游记");
		for (Search.Result r : rs) {
			Video video = (Video) mLexicon.getItemKey(r.item);
			System.out.printf("%f - [%d]%s\n", r.score, video.id, video.name);
		}
	}

	public static void main(String[] args) {
		new Example().search();
	}
}
