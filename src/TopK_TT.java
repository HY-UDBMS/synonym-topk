/* Copyright (c) 2016 UDBMS group, Department of Computer Science, University of Helsinki
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import Helpers.TrieNode;
import Helpers.TrieNodePairComparator;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class TopK_TT {
    public static String[] GetTopK(String prefix, TrieNode root_d, TrieNode root_r, int k) {
        List<String> result = new ArrayList<>(k);

        // Pair<TrieNode, remaining_index_in_prefix>
        PriorityQueue<Pair<TrieNode, Integer>> queue = new PriorityQueue<>(new TrieNodePairComparator());

        queue.add(new Pair<>(root_d, 0));

        while (!queue.isEmpty()) {
            Pair<TrieNode, Integer> p = queue.poll();
            TrieNode p1 = p.getKey(); // latest node found in dict trie
            int p2i = p.getValue();    // ... and index of remaining prefix string
            String p2 = prefix.substring(p2i > prefix.length() ? prefix.length() : p2i);    // ... and remaining prefix string

            for (int i = 0; i <= p2.length(); i++) {
                String p2_append = p2.substring(0, i);
                String p2_remaining = p2.substring(i, p2.length());

                int finalI = i;

                TrieNode last = p1.FindByString(p2_append);

                // if this new string is no longer find in dict trie, terminate
                if (last == null)
                    break;

                if (last.IsLeaf() && !last.IsLinkNode()) {
                    String full = last.GetFullString(false);

                    if (!result.contains(full))
                        result.add(full);

                    if (result.size() == k)
                        return result.toArray(new String[result.size()]);
                }

                // we are now at the end of prefix
                if (p2i + i == prefix.length()) {
                    last.links.entrySet().forEach(l -> queue.add(new Pair<>(l.getKey(), p2i + finalI)));
                }

                // we are in guess mode
                if (p2i + i >= prefix.length()) {
                    last.children.values().forEach(c -> {
                        if (!c.IsLinkNode())
                            queue.add(new Pair<>(c, p2i + finalI + 1));
                    });

                    break;
                }

                // find rest of p2 within rules trie
                TrieNode[] found_r = root_r.FindMuchByString(p2_remaining);
                for (TrieNode r : found_r) {
                    r.links.entrySet().forEach(l -> {
                        //String full = l.getKey().GetFullString(false);
                        // this is for checking whether link target have the same char before rhs with prefix
                        //if (full.substring(0, full.length() - r.GetFullString(false).length() - l.getValue())
                        //        .equals(p1.GetFullString(false) + p2_append))
                        TrieNode before = l.getKey().GoUp(r.GetDepth() + l.getValue());
                        if (last == before)
                            queue.add(new Pair<>(l.getKey(), p2i + finalI + r.GetDepth()));
                    });
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
