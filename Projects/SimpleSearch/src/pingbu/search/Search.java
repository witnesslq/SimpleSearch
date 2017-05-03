package pingbu.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pingbu.common.Pinyin;

public class Search {

	private static class MatchedItem {
		public int item;
		public char c1, c2;
	}

	private List<String> mItems = new ArrayList<String>();
	private List<Object> mItemKeys = new ArrayList<Object>();
	private Map<Short, List<MatchedItem>> mCharIndex = new HashMap<Short, List<MatchedItem>>();
	private Map<String, List<MatchedItem>> mIndex = new HashMap<String, List<MatchedItem>>();

	private static String normalize(char a, char b) {
		return Pinyin.normailizeChar(a) + "-" + Pinyin.normailizeChar(b);
	}

	public void addItem(String text, Object key) {
		mItems.add(text);
		mItemKeys.add(key);
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			short nc = Pinyin.normailizeChar(c);
			List<MatchedItem> index = mCharIndex.get(nc);
			if (index == null) {
				index = new ArrayList<MatchedItem>();
				mCharIndex.put(nc, index);
			}
			MatchedItem mi = new MatchedItem();
			mi.item = mItems.size() - 1;
			mi.c1 = c;
			mi.c2 = 0;
			index.add(mi);
		}
		for (int i = 1; i < text.length(); ++i) {
			String word = normalize(text.charAt(i - 1), text.charAt(i));
			List<MatchedItem> index = mIndex.get(word);
			if (index == null) {
				index = new ArrayList<MatchedItem>();
				mIndex.put(word, index);
			}
			MatchedItem mi = new MatchedItem();
			mi.item = mItems.size() - 1;
			mi.c1 = text.charAt(i - 1);
			mi.c2 = text.charAt(i);
			index.add(mi);
		}
	}

	public int getItemCount() {
		return mItems.size();
	}

	public Object getItemKey(int item) {
		return mItemKeys.get(item);
	}

	private static class SearchingIndex {
		public int pos;
		public List<MatchedItem> index;
	}

	public static class Result {
		public int item;
		public double score;
	}

	public Collection<Result> search(String text) {
		List<SearchingIndex> wordIndexes = new ArrayList<SearchingIndex>();
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			short nc = Pinyin.normailizeChar(c);
			SearchingIndex index = new SearchingIndex();
			index.pos = i;
			index.index = mCharIndex.get(nc);
			if (index != null)
				wordIndexes.add(index);
		}
		for (int i = 1; i < text.length(); ++i) {
			String word = normalize(text.charAt(i - 1), text.charAt(i));
			SearchingIndex index = new SearchingIndex();
			index.pos = i;
			index.index = mIndex.get(word);
			if (index != null)
				wordIndexes.add(index);
		}
		List<Result> results = new ArrayList<Result>();
		int[] wordIndexPos = new int[wordIndexes.size()];
		for (;;) {
			int item = Integer.MAX_VALUE;
			for (int i = 0; i < wordIndexes.size(); ++i) {
				List<MatchedItem> index = wordIndexes.get(i).index;
				if (index != null && wordIndexPos[i] < index.size()) {
					int t = index.get(wordIndexPos[i]).item;
					if (t < item)
						item = t;
				}
			}
			if (item >= Integer.MAX_VALUE)
				break;
			double score = 0;
			for (int i = 0; i < wordIndexes.size(); ++i) {
				SearchingIndex index = wordIndexes.get(i);
				while (index.index != null && wordIndexPos[i] < index.index.size()) {
					MatchedItem mi = index.index.get(wordIndexPos[i]);
					if (mi.item > item)
						break;
					if (mi.c2 == 0)
						score += Pinyin.compareChar(mi.c1, text.charAt(index.pos));
					else
						score += Pinyin.compareChar(mi.c1, text.charAt(index.pos - 1))
								* Pinyin.compareChar(mi.c2, text.charAt(index.pos)) * 4;
					++wordIndexPos[i];
				}
			}
			score /= text.length() * 5 - 4;
			if (score >= 0.6) {
				Result r = new Result();
				r.item = item;
				r.score = score;
				results.add(r);
			}
		}
		Collections.sort(results, new Comparator<Result>() {
			@Override
			public int compare(Result r0, Result r1) {
				return r0.score < r1.score ? 1 : r0.score == r1.score ? 0 : -1;
			}
		});
		return results;
	}
}
