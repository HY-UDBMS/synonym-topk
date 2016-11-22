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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RulePartitioner {

    public static List<RulePartition> partition(HashMap<String, Integer> dict, List<Rule> rules) {

        // HashMap<lhs, Pair<rules, applyCount>>
        HashMap<String, RulePartition> groups = new HashMap<>();

        rules.forEach(rule -> {
            // only check once
            if (groups.containsKey(rule.lhs)) {
                groups.get(rule.lhs).rules.add(rule);
                groups.get(rule.lhs).applyCountTotal *= 2;
                return;
            }

            groups.put(rule.lhs, new RulePartition());

            groups.get(rule.lhs).rules.add(rule);

            dict.forEach((str, score) -> {
                if (str.contains(rule.lhs)) {
                    groups.get(rule.lhs).applyCount += 1;
                    groups.get(rule.lhs).applyCountTotal += 1;
                }
            });
        });

        // calculate max saving
        groups.values().forEach(g -> {
            g.maxCommonPrefix = longestRhsCommonPrefix(g.rules);
        });

        return new ArrayList(groups.values());
    }

    public static int longestRhsCommonPrefix(List<Rule> strings) {
        if (strings.size() <= 2) {
            return 0;
        }

        int prefixLen = 0;
        for (prefixLen = 0; prefixLen < strings.get(0).rhs.length(); prefixLen++) {
            char c = strings.get(0).rhs.charAt(prefixLen);
            for (int i = 1; i < strings.size(); i++) {
                if ( prefixLen >= strings.get(i).rhs.length() ||
                        strings.get(i).rhs.charAt(prefixLen) != c ) {
                    // Mismatch found
                    return prefixLen;
                }
            }
        }
        return prefixLen;
    }

}
