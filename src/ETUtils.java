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

import Helpers.Rule;
import Helpers.TrieNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ETUtils {
    public static TrieNode buildTrieMK2(HashMap<String, Integer> dict, List<Rule> rules) {
        TrieNode root = new TrieNode();

        appendTrieMK2(root, dict, rules);

        return root;
    }

    public static void appendTrieMK2(TrieNode root, HashMap<String, Integer> dict, List<Rule> rules) {

        dict.forEach((str, score) -> {

            // add str to trie
            TrieNode pointer = root;

            for (int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);

                pointer = pointer.AddChild(ch, score);
            }
        });


        dict.forEach((str, score) -> {

            // apply all possible rules
            for (Rule rule : rules) {
                if (!str.contains(rule.lhs))
                    continue;

                // maybe there is more than one lhs in str
                for (int str_start_pos : allIndexOf(str, rule.lhs)) {

                    TrieNode str_first = root.FindByString(str.substring(0, str_start_pos + 1));
                    TrieNode str_last = root.FindByString(str.substring(0, str_start_pos + rule.lhs.length()));

                    // add synonym nodes, attach to first.parent
                    TrieNode pointer = str_first.parent;

                    for (int i = 0; i < rule.rhs.length(); i++) {
                        char ch = rule.rhs.charAt(i);

                        pointer = pointer.AddChild(ch, 0);
                    }
                    // now pointer is the last node in synonym nodes

                    // create link from pointer to last char in dict string
                    pointer.AddLink(str_last, rule.lhs.length() - rule.rhs.length());
                    pointer.AddLink(str_last, rule.lhs.length() - rule.rhs.length());
                }
            }
        });

        root.FixRank();
    }

    private static List<Integer> allIndexOf(String str, String pattern) {
        List<Integer> result = new ArrayList<>();

        int index = str.indexOf(pattern);
        while (index >= 0) {
            result.add(index);
            index = str.indexOf(pattern, index + 1);
        }

        return result;
    }

}
