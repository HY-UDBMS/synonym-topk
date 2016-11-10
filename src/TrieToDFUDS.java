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

import Helpers.DFUDSString;
import Helpers.TrieNode;

import java.util.ArrayList;
import java.util.List;

public class TrieToDFUDS {
    public static DFUDSString Convert(TrieNode root) {
        StringBuilder parenthesis = new StringBuilder("");
        StringBuilder labels = new StringBuilder("");
        List<Integer> ranks = new ArrayList<>(labels.length());

        ConvertRecursive(root, parenthesis, labels, ranks);

        ArrayList<ArrayList<Integer>> links = new ArrayList<>();
        ArrayList<ArrayList<Integer>> deltas = new ArrayList<>();

        InitList(links, labels.length());
        InitList(deltas, labels.length());

        DFUDSString result = new DFUDSString(
                parenthesis.toString(),
                labels.toString(),
                ranks.stream().mapToInt(i -> i).toArray(),
                links,
                deltas
        );

        AddLinkRecursive(root, result);

        return result;
    }

    private static void ConvertRecursive(TrieNode root, StringBuilder parenthesis, StringBuilder labels, List<Integer> ranks) {
        // add (
        for (int i = 0; i < root.children.size(); i++) {
            parenthesis.append("(");
        }

        // add self
        parenthesis.append(")");
        labels.append(root.value);

        // add score
        ranks.add(root.score);

        // loop within children
        root.children.values().forEach(c -> ConvertRecursive(c, parenthesis, labels, ranks));
    }

    private static void AddLinkRecursive(TrieNode root, DFUDSString string) {
        if (root == null)
            return;

        root.links.entrySet().forEach(l -> {
            TrieNode link = l.getKey();
            int delta = l.getValue();

            int[] eles_from = string.FindByString(root.GetFullString(false));
            int[] eles_to = string.FindByString(link.GetFullString(false));

            string.AddLink(eles_from[eles_from.length - 1], eles_to[eles_to.length - 1], delta);
        });

        // loop within children
        root.children.values().forEach(c -> AddLinkRecursive(c, string));
    }

    private static void InitList(ArrayList<ArrayList<Integer>> list, int length) {
        for (int i = 0; i < length; i++) {
            list.add(new ArrayList<>());
        }
    }
}
