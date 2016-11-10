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

import Helpers.DFUDSPairComparator;
import Helpers.DFUDSString;
import org.javatuples.Quartet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class TopK_DFUDS {

    public static List<String> GetTopK(String prefix, DFUDSString dfuds, int k) {
        List<String> result = new ArrayList<>(k);

        //HashMap<from, List<to>>
        HashMap<Integer, List<Integer>> used_links = new HashMap<>();

        // Pair<TrieNode, delta_length_changes_by_link>
        PriorityQueue<Quartet<DFUDSString, Integer, Integer, Integer>> queue = new PriorityQueue<>(new DFUDSPairComparator());

        int[] locuses = dfuds.FindMuchByString(prefix);

        if (locuses == null || locuses.length == 0)
            return result;

        // we only push the 1st element
        int locus = locuses[locuses.length - 1];
        if (dfuds.GetRank(locus) > 0)
            queue.add(new Quartet<>(dfuds, locus, 0, dfuds.GetFullString(locus).length()));
        dfuds.GetLinks(locus).forEach(l -> queue.add(new Quartet<>(dfuds, l.getKey(), l.getValue(), dfuds.GetFullString(l.getKey()).length())));

        search:
        {
            while (!queue.isEmpty()) {
                Quartet<DFUDSString, Integer, Integer, Integer> n_pair = queue.poll();
                int n = n_pair.getValue1();
                int link_delta = n_pair.getValue2();
                int depth = n_pair.getValue3();

                // found result?
                if (dfuds.GetAllChildren(n).length == 0 && dfuds.GetRank(n) > 0) {
                    String full = dfuds.GetFullString(n); // remove root indicator '$'
                    if (!result.contains(full))
                        result.add(full);
                }

                // enough candidates?
                if (result.size() == k)
                    break search;

                // if we reached the end of prefix
                if (depth - link_delta == prefix.length()) {
                    if (dfuds.GetLinks(n).size() > 0)
                        dfuds.GetLinks(n).forEach(l -> CheckAndPush(dfuds, used_links, queue, n, l.getKey(), link_delta + l.getValue()));
                }
                // in guess
                if (depth - link_delta >= prefix.length()) {
                    for (int child : dfuds.GetAllChildren(n)) {
                        if (dfuds.GetRank(child) != 0)
                            queue.add(new Quartet<>(dfuds, child, link_delta, depth + 1));
                    }
                }
                // if there still has next char, do not guess
                else {
                    char jb = prefix.charAt(depth - link_delta);
                    int nextChar = dfuds.FindChild(n, jb);
                    if (nextChar != -1) {
                        queue.add(new Quartet<>(dfuds, nextChar, link_delta, depth + 1));
                        dfuds.GetLinks(nextChar).forEach(l -> CheckAndPush(dfuds, used_links, queue, nextChar, l.getKey(), link_delta + l.getValue()));
                    }
                }
            }
        }

        return result;
    }

    private static void CheckAndPush(DFUDSString dfuds, HashMap<Integer, List<Integer>> used, PriorityQueue<Quartet<DFUDSString, Integer, Integer, Integer>> queue,
                                     int from, int to, int link_delta) {
        if (used.containsKey(from))
            if (used.get(from).contains(to))
                return;
            else
                used.get(from).add(to);
        else
            used.put(from, new ArrayList<>());

        queue.add(new Quartet<>(dfuds, to, link_delta, dfuds.GetFullString(to).length()));
    }
}
