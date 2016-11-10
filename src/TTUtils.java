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
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TTUtils {

    /**
     * @return Pair&lt;root_dict, root_rules&gt;
     */
    public static Pair<TrieNode, TrieNode> BuildTwoTries(HashMap<String, Integer> stringDictionary, List<Rule> rules) {

        TrieNode root_d = new TrieNode();
        TrieNode root_r = new TrieNode();

        AppendTwoTries(root_d, root_r, stringDictionary, rules, false);

        return new Pair<>(root_d, root_r);
    }

    public static void AppendTwoTries(TrieNode root_d, TrieNode root_r, HashMap<String, Integer> dict, List<Rule> rules, boolean skip_dict) {

        // add dict string first
        if (!skip_dict) {
            dict.forEach((str, score) -> {
                TrieNode pointer = root_d;

                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);

                    pointer = pointer.AddChild(c, score);
                }
            });
        }

        dict.forEach((str, score) -> {
            // then link to all possible rules
            for (Rule rule : rules) {
                if (!str.contains(rule.lhs))
                    continue;

                // add the rule into rules trie
                TrieNode pointer_r = root_r;

                for (int i = 0; i < rule.rhs.length(); i++) {
                    char c = rule.rhs.charAt(i);

                    pointer_r = pointer_r.AddChild(c, -1);
                }

                // find the last node in rules trie
                TrieNode last_in_rules = pointer_r;

                // for all the lhs in dictionary string
                for (int str_start_pos : allIndexOf(str, rule.lhs)) {
                    // ... we point last_in_rules to it
                    TrieNode last_in_dict = root_d.FindByString(
                            str.substring(0, str_start_pos + rule.lhs.length()));

                    last_in_rules.AddLink(last_in_dict, rule.lhs.length() - rule.rhs.length());
                }
            }
        });

        root_d.FixRank();
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
