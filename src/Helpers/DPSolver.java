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

import org.javatuples.Octet;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DPSolver {

    /**
     * @return Quartet&lt;rules_selected, rules_rest, budget_acquired, best_benefit&gt;
     */
    public static Quartet<List<Rule>, List<Rule>, Integer, Integer> solve(TrieNode root, HashMap<String, Integer> dict, List<Rule> rules,
                                                                          int budget, double epsilon) {

        if (budget < 0)
            budget = 0;

        // part rules
        List<RulePartition> items = RulePartitioner.partition(dict, rules);

        // calculate maximum size (sizes) and profit (profits) for every item
        int[] sizes = new int[items.size()];
        int[] profits = new int[items.size()];

        int ITEMS_COUNT = items.size();
        int MAX_SIZE = 0;
        int MAX_PROFIT = 0;
        int SUM_PROFIT = 0;

        for (int i = 0; i < items.size(); i++) {
            RulePartition item = items.get(i);

            int item_size = FakeApply.apply(root, dict, item.rules, false);
            int item_profit = item.applyCountTotal;

            if (item_size > MAX_SIZE)
                MAX_SIZE = item_size;

            if (item_profit > MAX_PROFIT)
                MAX_PROFIT = item_profit;

            SUM_PROFIT += item_profit;

            sizes[i] = item_size;
            profits[i] = item_profit;
        }

        double theta = 1.0d;
        // scaling
        if (epsilon > 0) {
            theta = epsilon * MAX_PROFIT / ITEMS_COUNT;
            for (int i = 0; i < ITEMS_COUNT; i++)
                profits[i] = (int) Math.floor(profits[i] / theta);
            SUM_PROFIT = (int) Math.floor(SUM_PROFIT / theta);
        }

        int W = budget;

        int MAX = ITEMS_COUNT * MAX_SIZE + 1;

        int OPT[][] = new int[ITEMS_COUNT + 1][SUM_PROFIT + 1];
        boolean B[][] = new boolean[ITEMS_COUNT + 1][SUM_PROFIT + 1];

        int p_best = 0;
        int s_best = 0;

        // fill OPT with MAX
        for (int n = 0; n <= ITEMS_COUNT; n++) {
            for (int v = 0; v <= SUM_PROFIT; v++) {
                OPT[n][v] = n == 0 ? 0 : MAX;
            }
        }

        // set A[1, p1] = s1, others to MAX
        for (int i = 1; i <= SUM_PROFIT; i++)
            OPT[1][i] = (i == profits[0]) ? sizes[0] : MAX;
        B[1][profits[0]] = true;

        for (int n = 2; n <= ITEMS_COUNT; n++) {
            for (int v = 0; v <= SUM_PROFIT; v++) {
                int val1 = OPT[n - 1][v];

                int val2 = MAX;
                if (v - profits[n - 1] >= 0) {
                    val2 = OPT[n - 1][v - profits[n - 1]] + sizes[n - 1];
                }

                if (Math.min(val1, val2) < MAX) { // one of them is not MAX
                    if (val1 < val2) { // do not take ii
                        OPT[n][v] = val1;

                    } else { // take ii
                        OPT[n][v] = val2;
                        B[n][v] = true;
                    }
                } else {
                    OPT[n][v] = MAX;
                }
            }
        }

        // find best solution
        for (int vv = SUM_PROFIT; vv >= 0; vv--) {
            if (OPT[ITEMS_COUNT][vv] <= W) {
                p_best = vv;
                s_best = OPT[ITEMS_COUNT][vv];

                break;
            }
        }

        // collect selected rules
        Quartet<List<Rule>, List<Rule>, Integer, Integer> result = new Quartet<>(new ArrayList<Rule>(), new ArrayList<Rule>(), 0, 0);

        int p_left = p_best;

        for (int i = ITEMS_COUNT; i >= 1; i--) {
            if (B[i][p_left]) {
                result.getValue0().addAll(items.get(i - 1).rules);
                p_left -= profits[i - 1];
            } else {
                result.getValue1().addAll(items.get(i - 1).rules);
            }
        }

        result = result.setAt2(s_best);
        result = result.setAt3(p_best * (int) theta);

        return result;
    }
}
