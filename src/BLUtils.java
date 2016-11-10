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

import Helpers.ExpandedString;
import Helpers.Rule;
import Helpers.TrieNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BLUtils {
    /**
     * Expand a string by rules.
     *
     * @param stringDictionary A string in dictionary
     * @param rules            A set of rules
     * @return An array of new strings and related rules
     */
    public static ExpandedString[] getExpandStrings(HashMap<String, Integer> stringDictionary, List<Rule> rules) {
        List<ExpandedString> result = new ArrayList<>();

        for (Map.Entry<String, Integer> dict : stringDictionary.entrySet()) {
            getExpendStringsRecursive(dict, result, getAvailableRules(dict, rules), new ArrayList<>(), dict.getKey(), 0, new ArrayList<>());
        }

        // we need sort results, for an one-pass procedure for building links
        result.sort((s1, s2) -> s1.activeRules.length - s2.activeRules.length);

        return result.toArray(new ExpandedString[result.size()]);
    }

    private static void getExpendStringsRecursive(Map.Entry<String, Integer> oldString, List<ExpandedString> result, List<Rule> availableRules,
                                                  List<Rule> usedRules, String newString, int step, List<ProtectedRegion> regions) {

        if (step >= availableRules.size()) {
            result.add(new ExpandedString(oldString, newString, usedRules.toArray(new Rule[usedRules.size()])));

            return;
        }

        Rule crtRule = availableRules.get(step);

        // 1) don't apply current rule, go next.
        getExpendStringsRecursive(oldString, result, availableRules, usedRules, newString, step + 1, regions);

        // 2) apply current rule
        int replaceStart = newString.indexOf(crtRule.lhs);

        // if this rule can be applied, do it.
        if (replaceStart >= 0)
            if (!IsInProtectedRegions(regions, replaceStart, crtRule.lhs.length())) {

                String old = newString;

                newString = newString.replaceFirst(Pattern.quote(crtRule.lhs), crtRule.rhs);

                regions.add(new ProtectedRegion(replaceStart, crtRule.rhs.length()));
                usedRules.add(crtRule);

                // ... then call next rule
                getExpendStringsRecursive(oldString, result, availableRules, usedRules, newString, step + 1, regions);

                // ... revert changes
                usedRules.remove(usedRules.size() - 1);
                regions.remove(regions.size() - 1);
                newString = old;
            }

    }

    private static List<Rule> getAvailableRules(Map.Entry<String, Integer> oldString, List<Rule> rules) {
        List<Rule> result = new ArrayList<>();

        for (Rule rule : rules) {
            if (oldString.getKey().contains(rule.lhs))
                result.add(rule);
        }

        /*

        int index = 0;

        while (index < stringDictionary.string.length()) {
            String searchString = stringDictionary.string.substring(index);

            boolean found = false;
            for (Rule rule : rules) {
                if (!searchString.startsWith(rule.lhs))
                    continue;

                result.add(new ActiveRule(rule));

                index += rule.lhs.length();

                found = true;
                break;
            }

            index += found ? 0 : 1;
        }
*/
        return result;
    }

    private static boolean IsInProtectedRegions(List<ProtectedRegion> regions, int start, int length) {
        for (ProtectedRegion region : regions) {
            if ((region.start + region.length) > start && (start + length) > region.start)
                return true;
        }
        return false;
    }

    public static TrieNode buildTrie(HashMap<String, Integer> dict, List<Rule> rules, ExpandedString[] newStrings) {

        TrieNode root = new TrieNode();

        for (ExpandedString str : newStrings) {
            TrieNode pointer = root;

            // build trie
            for (int i = 0; i < str.newString.length(); i++) {
                char ch = str.newString.charAt(i);

                pointer = pointer.AddChild(ch, str.oldString.getValue());
            }
        }

        for (ExpandedString str : newStrings) {
            // make links. As newStrings are SORTED, we can just search for existing nodes here.
            for (Rule rule : str.activeRules) {
                TrieNode from = root.FindByString(str.newString.substring(0, str.newString.indexOf(rule.rhs) + rule.rhs.length()));
                TrieNode to = root.FindByString(str.oldString.getKey().substring(0, str.oldString.getKey().indexOf(rule.lhs) + rule.lhs.length()));

                from.AddLink(to, to.GetDepth() - from.GetDepth());

                // label link nodes
                for (int i = 0; i < rule.rhs.length(); i++) {
                    if (from == root)
                        break;

                    from.score = 0;
                    from = from.parent;
                }
            }
        }

        // fix possible loss of ranks for nodes which is both link & dict
        for (Map.Entry<String, Integer> entry : dict.entrySet()) {
            for (TrieNode n : root.FindAllByString(entry.getKey())) {
                n.score = entry.getValue();
            }
        }

        // now we will fix all ranks
        // post-order traversal
        root.FixRank();

        return root;
    }

    private static class ProtectedRegion {
        public int start;
        public int length;

        public ProtectedRegion(int start, int length) {
            this.start = start;
            this.length = length;
        }
    }
}
