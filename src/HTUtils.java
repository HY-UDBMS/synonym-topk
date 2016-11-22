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

import Helpers.BranchAndBoundClassifier;
import Helpers.Rule;
import Helpers.TrieNode;
import org.javatuples.Quartet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HTUtils {

    /**
     * Build a HT
     *
     * @param dict   dict
     * @param rules  rules
     * @param budget overall space budget in bytes: additional space that can be acquired
     * @return Quartet&lt;root_dict, root_rules, budget_acquired, budget_total&gt;
     */
    public static Quartet<TrieNode, TrieNode, Integer, Integer> buildTrie(HashMap<String, Integer> dict, List<Rule> rules, int budget) {

        BranchAndBoundClassifier bnb = new BranchAndBoundClassifier(dict, rules, budget);
        bnb.prepare();
        Quartet<Integer, Integer, List<Rule>, List<Rule>> classified_rules = bnb.solve();

        List<Rule> rules_et = classified_rules.getValue2();
        List<Rule> rules_tt = classified_rules.getValue3();

        TrieNode root_et = ETUtils.buildTrieMK2(dict, rules_et);

        TrieNode root_rules = new TrieNode();
        TTUtils.AppendTwoTries(root_et, root_rules, dict, rules_tt, true);

        return new Quartet<>(root_et, root_rules, classified_rules.getValue1(), budget);
    }
}
