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

import Helpers.ArgsReader;
import Helpers.Rule;
import Helpers.SizeCounter;
import Helpers.TrieNode;
import javafx.util.Pair;
import org.javatuples.Quartet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Program {
    public static void main(String[] args) {

        String search_string;
        int K;
        String dict_file_path;
        String rule_file_path;

        String build;
        int budget;

        try {
            if (args.length == 0)
                throw new Exception();

            ArgsReader reader = new ArgsReader(args);

            search_string = reader.ReadNext("");
            K = reader.ReadNext(10);
            dict_file_path = reader.ReadNext("dict.txt");
            rule_file_path = reader.ReadNext("rule.txt");

            build = reader.ReadNext("ET");
            budget = reader.ReadNext(100000);

        } catch (Exception e) {
            System.out.println("This is the Semantic Top-k Auto-Completion Tool");
            System.out.println("Developed by Pengfei Xu @ UDBMS Group @ University of Helsinki");
            System.out.println("For more Information, please visit http://udbms.cs.helsinki.fi/");
            System.out.println("");
            System.out.println("");
            System.out.println("Usage: topk SEARCH_STRING [K] [DICT_FILE] [SYNONYM_FILE] [TRIE] [(TRIE=HT) BUDGET]");
            System.out.println("");
            System.out.println("Perform top-[K] auto-completions for SEARCH_STRING with [TRIE] structure,");
            System.out.println("the results are from DICT_FILE in respect of synonyms in SYNONYM_FILE.");
            System.out.println("");
            System.out.println("  SEARCH_STRING The search string.");
            System.out.println("  K             Default=10. The maximum number of results returned.");
            System.out.println("  DICT_FILE     Default=\"dict.txt\". List of dictionary strings with scores.");
            System.out.println("  SYNONYM_FILE  Default=\"rule.txt\". List of synonym rules.");
            System.out.println("  TRIE          Default=\"ET\". The trie structure used in this search.");
            System.out.println("                Can be TT, ET or HT. If you choose HT, you may want to specify BUDGET.");
            System.out.println("  BUDGET        Default=100000. A number indicates the additional space (in bytes)");
            System.out.println("                budget can be occupied by HT.");

            return;
        }

        HashMap<String, Integer> dict = new HashMap<>();
        List<Rule> rules = new ArrayList<>();

        try {
            BufferedReader dict_file = new BufferedReader(new FileReader(dict_file_path));

            String line;

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
        } catch (Exception e) {
            System.out.println(e.getMessage());

            return;
        }

        System.out.println(String.format("Found %d strings with %d synonym rules. Build up %s...", dict.size(), rules.size(), build));

        Pair<TrieNode, TrieNode> trie = new Pair<>(null, null);

        long startTime = 0;
        long duration = 0;

        startTime = System.nanoTime();

        switch (build) {
            case "TT":
                trie = TTUtils.BuildTwoTries(dict, rules);
                break;
            case "ET":
                trie = new Pair<>(ETUtils.buildTrieMK2(dict, rules), null);
                break;
            case "HT":
                Quartet<TrieNode, TrieNode, Integer, Integer> ht = HTUtils.buildTrie(dict, rules, budget);
                trie = new Pair<>(ht.getValue0(), ht.getValue1());
                break;
        }

        duration = System.nanoTime() - startTime;

        System.out.println(String.format("%s is built, time cost %.3f ms.", build, (double) duration / 1000 / 1000));

        long size = SizeCounter.Count(trie.getKey(), build.equals("ET")) + SizeCounter.Count(trie.getValue(), true);

        System.out.println(String.format("Size of %s is %d bytes", build, size));

        System.out.println(String.format("Top-%d auto-completion results:", K));

        String[] resuls = {};

        startTime = System.nanoTime();

        // run 20 times
        for (int i = 0; i < 20; i++) {
            switch (build) {
                case "TT":
                    resuls = TopK_TT.GetTopK(search_string, trie.getKey(), trie.getValue(), K);
                    break;
                case "ET":
                    resuls = TopK_ET.GetTopK(search_string, trie.getKey(), K);
                    break;
                case "HT":
                    resuls = TopK_HT.GetTopK(search_string, trie.getKey(), trie.getValue(), K);
                    break;
            }
        }

        duration = System.nanoTime() - startTime;

        for (String r : resuls) {
            System.out.println(String.format("  %s", r));
        }

        System.out.println();
        System.out.println(String.format("Top-%d time cost %.3f ms.", K, (double) duration / 1000 / 1000 / 20));
    }

}

