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

import org.javatuples.Quartet;

import java.util.*;

public class BranchAndBoundClassifier {

    private HashMap<String, Integer> dict = null;
    private List<Rule> rules = null;
    private List<BABItem> items = new ArrayList<>();
    private int capacity;

    public BranchAndBoundClassifier(HashMap<String, Integer> dict, List<Rule> rules, int capacity) {
        this.dict = dict;
        this.rules = rules;
        this.capacity = capacity;
    }

    private static int longestPrefix(String a, String b) {
        int minLength = Math.min(a.length(), b.length());
        for (int i = 0; i < minLength; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return i;
            }
        }
        return minLength;
    }

    public void prepare() {

        // prepare BABItems
        List<RulePartition> parts = RulePartitioner.partition(dict, rules);

        parts.forEach(part -> {
            part.rules.forEach(rule -> {
                BABItem item = new BABItem();
                item.rule = rule;
                item.value = part.applyCount;

                //item.weight = FakeApply.apply(root, dict, rule, false);
                item.weight = getWeight(item);

                if (part.rules.size() > 1) {
                    //item.weightMin = FakeApply.applyInSequence(root, dict, part.rules, rule, false);
                    item.weightMin = getWeightMin(item, part.rules);
                } else {
                    item.weightMin = item.weight;
                }

                items.add(item);
            });
        });

        Collections.sort(items, BABItem.byRatioMin());
    }

    /**
     * @return Quartet(value, weight, items_taken, items_not_taken)
     */
    public Quartet solve() {

        Node best = new Node();
        Node root = new Node();
        root.computeLowerBound();
        root.computeUpperBound();

        PriorityQueue<Node> q = new PriorityQueue<Node>();
        q.offer(root);

        int visited = 0;

        while (!q.isEmpty()) {
            Node node = q.poll();

            if (node.ubound > best.value && node.h < items.size() - 1) {

                Node with = new Node(node);
                BABItem item = items.get(node.h);
                with.weight += getRealWeight(item, with.taken);

                if (with.weight <= capacity) {

                    with.taken.add(node.h);
                    with.value += item.value;
                    with.computeLowerBound();
                    with.computeUpperBound();

                    if (with.lbound > best.lbound) {
                        best = with;
                    }
                    if (with.ubound >= best.lbound) {
                        visited++;
                        q.offer(with);
                    }

                    Node without = new Node(node);
                    without.computeLowerBound();
                    without.computeUpperBound();

                    if (without.ubound >= best.lbound) {
                        visited++;
                        q.offer(without);
                    }
                }
            }
        }
        //System.out.println(visited);

        // if BnB terminates early than h levels, we know this best.lbound is the best solution
        best.takeLowerBoundNaive();

        // BABItem -> Rule
        List<Rule> takenn = new ArrayList<>();
        List<Rule> notTakenn = new ArrayList<>();
        best.taken.forEach(i -> takenn.add(items.get(i).rule));

        for (int i = 0; i < items.size(); i++) {
            if (!best.taken.contains(i))
                notTakenn.add(items.get(i).rule);
        }

        return new Quartet<>((int) best.value, (int) best.weight, takenn, notTakenn);
    }

    private int getRealWeight(BABItem item, List<Integer> taken) {
        List<Rule> takenn = new ArrayList<>();
        taken.forEach(i -> {
            BABItem t = items.get(i);
            //if (t.rule.lhs.equals(item.rule.lhs))
            takenn.add(t.rule);
        });
        takenn.add(item.rule);

        //return item.weight;
        //return FakeApply.applyInSequence(root, dict, takenn, item.rule, false);
        return getWeightMin(item, takenn);
    }

    private int getWeight(BABItem item) {
        Rule real = item.rule;

        // compare self lhs and rhs
        int max = longestPrefix(real.lhs, real.rhs);

        return (real.rhs.length() - max) * item.value * (1 + 4 + 4 + 4);
    }

    private int getWeightMin(BABItem item, List<Rule> rules) {
        Rule real = item.rule;
        int max = longestPrefix(real.lhs, real.rhs);

        for (Rule rule : rules) {
            if (rule == real)
                continue;

            int len = longestPrefix(rule.rhs, real.rhs);
            if (len > max)
                max = len;
        }

        return (real.rhs.length() - max) * item.value * (1 + 4 + 4 + 4);
    }

    private class Node implements Comparable<Node> {

        List<Integer> taken;
        private int h;
        private double lbound;
        private double ubound;
        private double value;
        private double weight;

        private Node() {
            taken = new ArrayList<>();
        }

        private Node(Node parent) {
            h = parent.h + 1;
            taken = new ArrayList<>(parent.taken);
            lbound = parent.lbound;
            ubound = parent.ubound;
            value = parent.value;
            weight = parent.weight;

        }

        public String toString() {
            return String.format("h=%d, taken=%d, lb=%f, ub=%f, v=%f, w=%f", h, taken.size(), lbound, ubound, value, weight);
        }

        // Sort by ubound
        public int compareTo(Node other) {
            return (int) (other.ubound - ubound);
        }

        private void computeUpperBound() {
            int i = h;
            double w = weight;
            ubound = value;
            BABItem item;
            do {
                item = items.get(i);
                if (w + item.weightMin > capacity) break;
                w += item.weightMin;
                ubound += item.value;
                i++;
            } while (i < items.size());

            if (i < items.size())
                ubound += (capacity - w) * (item.value / item.weightMin);
        }

        private void takeLowerBoundNaive() {
            int i = h;
            BABItem item;
            do {
                item = items.get(i);
                if (weight + item.weight > capacity)
                    break;
                weight += item.weight;
                value += item.value;
                taken.add(i);
                i++;
            } while (i < items.size());
        }

        private void computeLowerBound() {
            computeLowerBoundNaive();
            //computeLowerBoundDP();
            //System.out.println(String.format("%f\t\t%f", computeLowerBoundNaive(), computeLowerBoundDP()));
        }

        private double computeLowerBoundNaive() {
            int i = h;
            double w = weight;
            lbound = value;
            BABItem item;
            do {
                item = items.get(i);
                if (w + item.weight > capacity)
                    break;
                w += item.weight;
                lbound += item.value;
                i++;
            } while (i < items.size());

            return lbound;
        }

        private double computeLowerBoundDP() {
            int i = h;
            double w = weight;

            List<Rule> r = new ArrayList<>(items.size() - i);

            // prepare rules
            for (int n = i; n < items.size(); n++) {
                r.add(items.get(n).rule);
            }

            //int additional = DPSolver.solve(root, dict, r, capacity - (int) w, 0).getValue3();

            lbound = value;
            //lbound += additional;

            return lbound;
        }
    }

}
