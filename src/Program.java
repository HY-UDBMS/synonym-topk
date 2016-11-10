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

import Helpers.*;
import javafx.util.Pair;
import org.javatuples.Quartet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Program {
    public static void main(String[] args) {

        String dataset = "conf";

        boolean skip_BL = false;
        boolean skip_dfuds = true;
        boolean skip_topk = true;

        String rule_file_path = String.format("data/%s_rules.txt", dataset);
        String dict_file_path = String.format("data/%s_dict.txt", dataset);
        String test_file_path = String.format("data/%s_test.txt", dataset);

        HashMap<String, Integer> dict = new HashMap<>();
        List<Rule> rules = new ArrayList<>();

        try {
            BufferedReader dict_file = new BufferedReader(new FileReader(dict_file_path));

            String line = null;

            while ((line = dict_file.readLine()) != null) {
                if (!line.contains("\t"))
                    continue;

                dict.put(line.split("\t")[0], Integer.parseInt(line.split("\t")[1]));
            }

            dict_file.close();

            BufferedReader rule_file = new BufferedReader(new FileReader(rule_file_path));

            while ((line = rule_file.readLine()) != null) {
                if (!line.contains("\t"))
                    continue;

                rules.add(new Rule(line.split("\t")[0], line.split("\t")[1]));
            }

            BufferedReader test_file = new BufferedReader(new FileReader(test_file_path));

            List<String> test_lines = new ArrayList<>();

            int max_len = 0;

            String l = test_file.readLine();
            while (l != null) {
                if (l.isEmpty())
                    continue;

                if (l.length() > max_len)
                    max_len = l.length();

                test_lines.add(l);
                l = test_file.readLine();
            }

            test_file.close();

            TestRecord[] et_avg = new TestRecord[max_len + 1];
            TestRecord[] dfuds_avg = new TestRecord[max_len + 1];
            TestRecord[] tt_avg = new TestRecord[max_len + 1];
            TestRecord[] set_avg = new TestRecord[max_len + 1];
            TestRecord[] pet_avg = new TestRecord[max_len + 1];

            for (int i = 0; i < max_len + 1; i++) {
                et_avg[i] = new TestRecord();
                dfuds_avg[i] = new TestRecord();
                tt_avg[i] = new TestRecord();
                set_avg[i] = new TestRecord();
                pet_avg[i] = new TestRecord();
            }

            //TrieNode root_dict = ETUtils.buildTrieMK2(dict, new Rule[0]);

            //BranchAndBoundClassifier c = new BranchAndBoundClassifier(root_temp, dict, rules, 600);
            //Quartet vvv = c.solve();

            // =======================================================================//
            // ============================== Size =================================//
            // =======================================================================//

            String sizes = "";
            // header
            sizes += "Struct\tDict\tSynonym1\tSynonym2\tTotal\n";


            final long[] startTime = {0};
            final long[] duration = {0};
            // ========================= Baseline =============================//

            TrieNode baseline = null;
            if (!skip_BL) {

                startTime[0] = System.nanoTime();

                baseline = BLUtils.buildTrie(dict, rules, BLUtils.getExpandStrings(dict, rules));

                duration[0] = System.nanoTime() - startTime[0];

                System.out.println(String.format("BL\t%d", duration[0] / 1000));

                // BL
                if (!skip_BL)
                    sizes += (String.format("BL\t%d\t%d\t%d\t%d\n",
                            SizeCounter.CountDict(baseline),
                            SizeCounter.CountSynonym(baseline, false),
                            0,
                            SizeCounter.Count(baseline, false)));


                baseline = null;

                System.gc();
            }
            // ========================= Expansion Trie =============================//

            startTime[0] = System.nanoTime();

            TrieNode et = ETUtils.buildTrieMK2(dict, rules);

            duration[0] = System.nanoTime() - startTime[0];

            System.out.println(String.format("ET\t%d", duration[0] / 1000));

            int size_et = SizeCounter.Count(et, false);
            // ET
            sizes += (String.format("ET\t%d\t%d\t%d\t%d\n",
                    SizeCounter.CountDict(et),
                    SizeCounter.CountSynonym(et, false),
                    0,
                    size_et));

            if (!skip_topk)
                for (String prefix : test_lines) {
                    startTime[0] = System.nanoTime();

                    String[] qaz = TopK_ET.GetTopK(prefix, et, 10);
                    //qaz.forEach(System.out::println);

                    duration[0] = System.nanoTime() - startTime[0];

                    et_avg[prefix.length()].Count += 1;
                    et_avg[prefix.length()].Value += duration[0];
                }

            et = null;
            System.gc();
            // ========================= Two Tries =============================//

            startTime[0] = System.nanoTime();

            Pair<TrieNode, TrieNode> tt = TTUtils.BuildTwoTries(dict, rules);

            duration[0] = System.nanoTime() - startTime[0];

            System.out.println(String.format("TT\t%d", duration[0] / 1000));

            int size_tt = SizeCounter.Count(tt.getKey(), false) + SizeCounter.Count(tt.getValue(), true);
            // TT
            sizes += (String.format("TT\t%d\t%d\t%d\t%d\n",
                    SizeCounter.Count(tt.getKey(), false),
                    0,
                    SizeCounter.Count(tt.getValue(), true),
                    size_tt));


            if (!skip_topk)
                for (String prefix : test_lines) {
                    startTime[0] = System.nanoTime();

                    String[] qaz3 = TopK_TT.GetTopK(prefix, tt.getKey(), tt.getValue(), 10);

                    duration[0] = System.nanoTime() - startTime[0];

                    tt_avg[prefix.length()].Count += 1;
                    tt_avg[prefix.length()].Value += duration[0];
                    //System.out.println(String.format("Lookup CT \t%d\t ns, length \t%d\t", duration[0], prefix.length()));
                }

            tt = null;
            System.gc();

            // ========================= Hybrid Trie =============================//

            int budget = size_et - size_tt;
            budget = budget <= 0 ? 0 : budget;

            startTime[0] = System.nanoTime();

            Quartet<TrieNode, TrieNode, Integer, Integer> set =
                    HTUtils.buildTrie(dict, rules, budget * 3 / 4);

            duration[0] = System.nanoTime() - startTime[0];

            System.out.println(String.format("HT\t%d", duration[0] / 1000));
            System.out.println(String.format("\tBudget used:\t%d / %d", set.getValue2(), set.getValue3()));

            // SET
            sizes += (String.format("HT\t%d\t%d\t%d\t%d\n",
                    SizeCounter.CountDict(set.getValue0()),
                    SizeCounter.CountSynonym(set.getValue0(), false),
                    SizeCounter.Count(set.getValue1(), true),
                    SizeCounter.Count(set.getValue0(), false) + SizeCounter.Count(set.getValue1(), true)));


            if (!skip_topk)
                for (String prefix : test_lines) {
                    startTime[0] = System.nanoTime();

                    String[] zaq = TopK_HT.GetTopK(prefix, set.getValue0(), set.getValue1(), 10);
                    //for (String s : qaz3)
                    //    System.out.println(s);

                    duration[0] = System.nanoTime() - startTime[0];

                    set_avg[prefix.length()].Count += 1;
                    set_avg[prefix.length()].Value += duration[0];
                    //System.out.println(String.format("Lookup CT \t%d\t ns, length \t%d\t", duration[0], prefix.length()));
                }

            set = null;
            System.gc();

            // ========================= DFUDS ET =============================//
            DFUDSString dfuds_et = null;
            if (!skip_dfuds) {

                startTime[0] = System.nanoTime();

                dfuds_et = TrieToDFUDS.Convert(et);

                duration[0] = System.nanoTime() - startTime[0];

                System.out.println(String.format("Build DFUDS on ET:\t%d", duration[0] / 1000));

                sizes += (String.format("DFUDS_ET\t%d\t%d\t%d\t%d\n",
                        SizeCounter.CountNode(dfuds_et),
                        SizeCounter.CountRelation(dfuds_et),
                        SizeCounter.CountLink(dfuds_et),
                        SizeCounter.Count(dfuds_et)));

                DFUDSString finalDfuds_et = dfuds_et;
                if (!skip_topk)
                    for (String prefix : test_lines) {
                        startTime[0] = System.nanoTime();

                        List<String> qaz2 = TopK_DFUDS.GetTopK(prefix, finalDfuds_et, 10);
                        //qaz2.forEach(System.out::println);

                        duration[0] = System.nanoTime() - startTime[0];

                        dfuds_avg[prefix.length()].Count += 1;
                        dfuds_avg[prefix.length()].Value += duration[0];
                        //System.out.println(String.format("Lookup DFUDS \t%d\t ns, length \t%d\t", duration[0], prefix.length()));
                    }
            }
            dfuds_et = null;
            System.gc();

            System.out.println();

            // =======================================================================//
            // ============================== Size =================================//
            // =======================================================================//

            System.out.println(sizes);
            System.out.println();

            // =======================================================================//
            // ============================== Time ===================================//
            // =======================================================================//

            // print header
            System.out.print("len\t");
            System.out.print("ET\t");
            System.out.print("TT\t");
            System.out.print("SET\t");
            if (!skip_dfuds)
                System.out.print("DFUDS_ET\t");

            System.out.println();

            // print values
            for (int i = 2; i < max_len + 1; i++) {
                // len
                System.out.print(String.format("%d\t", i));
                // ET
                System.out.print(String.format("%d\t", et_avg[i].Value / 1000 / (et_avg[i].Count == 0 ? 1 : et_avg[i].Count)));
                // TT
                System.out.print(String.format("%d\t", tt_avg[i].Value / 1000 / (tt_avg[i].Count == 0 ? 1 : tt_avg[i].Count)));
                // SET
                System.out.print(String.format("%d\t", set_avg[i].Value / 1000 / (set_avg[i].Count == 0 ? 1 : set_avg[i].Count)));
                // DFUDS_ET
                if (!skip_dfuds)
                    System.out.print(String.format("%d\t", dfuds_avg[i].Value / 1000 / (dfuds_avg[i].Count == 0 ? 1 : dfuds_avg[i].Count)));
                // count
                System.out.print(String.format("%d\t", et_avg[i].Count));

                System.out.println();
            }

            // fit model

            /*double[] et_model = RegressionTools.FitET(et_avg);
            double[] tt_model = RegressionTools.FitTT(tt_avg);

            //System.out.println(String.format("%f*ln(x)+%f=%f*x^2+%f*x+%f", et_model[1], et_model[0], tt_model[2], tt_model[1], tt_model[0]));
            System.out.println(String.format("%f*x^3+%f*x^2+%f*x+%f=%f*x^2+%f*x+%f", et_model[3], et_model[2], et_model[1], et_model[0], tt_model[2], tt_model[1], tt_model[0]));

            double[] thres = CubicFormula.solve(et_model[3], et_model[2] - tt_model[2], et_model[1] - tt_model[1], et_model[0] - tt_model[0]);

            System.out.println(String.format("Optimized Threshold = %f", thres[1]));*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

