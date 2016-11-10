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

package Helpers;

import java.util.*;

public class FakeApply {

    /**
     * @return in bytes
     */
    public static int applyInSequence(TrieNode root, HashMap<String, Integer> dict, List<Rule> rules, List<Rule> real, boolean withLinks) {
        final int[] new_nodes = {0};

        //System.out.println(Helpers.SizeCounter.Count(root));

        List<TrieNode> sandbox = new LinkedList<>();

        dict.forEach((str, score) -> {

            rules.forEach(rule -> {
                if (real.contains(rule))
                    return;

                // maybe there is more than one lhs in str
                for (int str_start_pos : allIndexOf(str, rule.lhs)) {

                    TrieNode str_first = root.FindByString(str.substring(0, str_start_pos + 1));

                    // find synonym nodes
                    TrieNode pointer = str_first.parent;

                    for (int i = 0; i < rule.rhs.length(); i++) {
                        char ch = rule.rhs.charAt(i);

                        if (pointer.FindChild(ch) == null) { // if this child is not found, we add it while keep track of what we do

                            TrieNode new_c = pointer.AddChild(ch, 0);

                            sandbox.add(new_c);
                        }

                        pointer = pointer.FindChild(ch);
                    }
                }
            });
        });

        dict.forEach((str, score) -> {

            real.forEach(rule -> {
                // maybe there is more than one lhs in str
                for (int str_start_pos : allIndexOf(str, rule.lhs)) {

                    TrieNode str_first = root.FindByString(str.substring(0, str_start_pos + 1));

                    // find synonym nodes
                    TrieNode pointer = str_first.parent;

                    for (int i = 0; i < rule.rhs.length(); i++) {
                        char ch = rule.rhs.charAt(i);

                        if (pointer.FindChild(ch) == null) { // if this child is not found, we add it while keep track of what we do

                            TrieNode new_c = pointer.AddChild(ch, 0);

                            new_nodes[0]++;

                            sandbox.add(new_c);
                        }

                        pointer = pointer.FindChild(ch);
                    }
                }
            });
        });

        // undo changes
        sandbox.forEach(c -> {
            c.parent.children.remove(c.value);
            c.parent = null;
            c = null;
        });

        return new_nodes[0] * (1 + 4 + 4 + 4);
    }

    /**
     * @return in bytes
     */
    public static int applyInSequence(TrieNode root, HashMap<String, Integer> dict, List<Rule> rules, Rule real, boolean withLinks) {
        return applyInSequence(root, dict, rules, Arrays.asList(real), withLinks);
    }

    /**
     * @return in bytes
     */
    public static int apply(TrieNode root, HashMap<String, Integer> dict, List<Rule> rules, boolean withLinks) {
        return applyInSequence(root, dict, rules, rules, withLinks);
    }

    /**
     * @return in bytes
     */
    public static int apply(TrieNode root, HashMap<String, Integer> dict, Rule rule, boolean withLinks) {
        return applyInSequence(root, dict, Arrays.asList(rule), Arrays.asList(rule), withLinks);
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
