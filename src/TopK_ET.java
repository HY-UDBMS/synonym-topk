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
import Helpers.TrieNodeTripletComparator;
import org.javatuples.Triplet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

public class TopK_ET {

    public static String[] GetTopK(String prefix, TrieNode root, int k) {
        LinkedHashSet<String> result = new LinkedHashSet<>(k);

        //HashMap<from, List<to>>
        HashMap<TrieNode, HashSet<TrieNode>> used_links = new HashMap<>();

        // Pair<TrieNode, delta_length_changes_by_link, current_level>
        PriorityQueue<Triplet<TrieNode, Integer, Integer>> queue = new PriorityQueue<>(new TrieNodeTripletComparator());

        TrieNode[] locuses = root.FindMuchByString(prefix);

        if (locuses == null || locuses.length == 0)
            root.children.values().forEach(c -> queue.add(new Triplet<>(c, 0, 1)));
        else {
            // we only push the 1st element
            TrieNode locus = locuses[locuses.length - 1];
            if (!locus.IsLinkNode())
                queue.add(new Triplet<>(locus, 0, locus.GetDepth()));

            locus.links.entrySet().forEach(l -> queue.add(new Triplet<>(l.getKey(), l.getValue(), l.getKey().GetDepth())));
        }

        while (!queue.isEmpty()) {
            Triplet<TrieNode, Integer, Integer> n_pair = queue.poll();
            TrieNode n = n_pair.getValue0();
            int link_delta = n_pair.getValue1();
            int n_len = n_pair.getValue2();

            // found result?
            if (n.IsLeaf() && !n.IsLinkNode()) {
                String full = n.GetFullString(false); // remove root indicator '$'
                result.add(full);

                // enough candidates?
                if (result.size() == k)
                    return result.toArray(new String[result.size()]);
            }

            // in prefix, then push all links
            if (n_len - link_delta <= prefix.length()) {
                n.links.entrySet().forEach(l -> CheckAndPush(used_links, queue, n, l.getKey(), link_delta + l.getValue()));
            }

            // push next char, or all children
            if (n_len - link_delta < prefix.length()) { // there is next char
                char nc = prefix.charAt(n_len - link_delta);
                TrieNode nn = n.FindChild(nc);
                if (nn != null) {
                    queue.add(new Triplet<>(nn, link_delta, n_len + 1));
                    //nn.links.forEach(l -> CheckAndPush(used_links, queue, nn, l.getKey(), link_delta + l.getValue()));
                }
            } else {
                for (TrieNode child : n.children.values()) {
                    if (!child.IsLinkNode())
                        queue.add(new Triplet<>(child, link_delta, n_len + 1));
                }
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private static void CheckAndPush(HashMap<TrieNode, HashSet<TrieNode>> used, PriorityQueue<Triplet<TrieNode, Integer, Integer>> queue,
                                     TrieNode from, TrieNode to, int link_delta) {
        if (used.containsKey(from))
            if (used.get(from).contains(to))
                return;
            else
                used.get(from).add(to);
        else
            used.put(from, new HashSet<>());

        queue.add(new Triplet<>(to, link_delta, to.GetDepth()));
    }
}
